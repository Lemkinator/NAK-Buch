package de.lemke.nakbuch.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.SectionIndexer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SeslSwitchBar
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.allViews
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.GetHistoryListUseCase
import de.lemke.nakbuch.domain.GetUserSettingsUseCase
import de.lemke.nakbuch.domain.ResetHistoryUseCase
import de.lemke.nakbuch.domain.UpdateUserSettingsUseCase
import de.lemke.nakbuch.domain.model.Hymn
import dev.oneuiproject.oneui.layout.SwitchBarLayout
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@AndroidEntryPoint
class HistorySwitchBarActivity : AppCompatActivity(), SeslSwitchBar.OnSwitchChangeListener {
    private lateinit var history: MutableList<Pair<Hymn, LocalDateTime>>
    private lateinit var listView: RecyclerView
    private lateinit var switchBarLayout: SwitchBarLayout
    private var enabled: Boolean = true

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    @Inject
    lateinit var resetHistory: ResetHistoryUseCase

    @Inject
    lateinit var getHistoryList: GetHistoryListUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        listView = findViewById(R.id.historyList)
        switchBarLayout = findViewById(R.id.switchbarlayout_history)
        switchBarLayout.switchBar.addOnSwitchChangeListener(this)
        switchBarLayout.setNavigationButtonTooltip(getString(R.string.sesl_navigate_up))
        switchBarLayout.setNavigationButtonOnClickListener { onBackPressed() }
        switchBarLayout.toolbar.inflateMenu(R.menu.switchpreferencescreen_menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        lifecycleScope.launch {
            resetHistory()
            initList()
        }
        return true
    }

    override fun onSwitchChanged(switchCompat: SwitchCompat, z: Boolean) {
        lifecycleScope.launch {
            enabled = updateUserSettings { it.copy(historyEnabled = z) }.historyEnabled
            initList()
        }
    }

    public override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            enabled = getUserSettings().historyEnabled
            switchBarLayout.switchBar.isChecked = enabled
            initList()
        }
    }

    private suspend fun initList() {
        history = getHistoryList().toMutableList()
        listView.adapter = ImageAdapter()
        listView.layoutManager = LinearLayoutManager(this)
        listView.addItemDecoration(ItemDecoration(this))
        listView.itemAnimator = null
        listView.seslSetIndexTipEnabled(true)
        listView.seslSetFastScrollerEnabled(true)
        listView.seslSetFillBottomEnabled(true)
        listView.seslSetGoToTopEnabled(true)
        listView.seslSetLastRoundedCorner(true)
    }

    inner class ImageAdapter internal constructor() :
        RecyclerView.Adapter<ImageAdapter.ViewHolder>(), SectionIndexer {
        private var sections: MutableList<String> = mutableListOf()
        private var positionForSection: MutableList<Int> = mutableListOf()
        private var sectionForPosition: MutableList<Int> = mutableListOf()

        init {
            if (history.size > 1) {
                history.forEachIndexed { index, pair ->
                    val date: String = pair.second.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                    if (index == 0 || sections[sections.size - 1] != date) {
                        sections.add(date)
                        positionForSection.add(index)
                    }
                    sectionForPosition.add(sections.size - 1)
                }
            }
        }

        override fun getSections(): Array<Any> = sections.toTypedArray()

        override fun getPositionForSection(i: Int): Int = if (positionForSection.size > 0) positionForSection[i] else 0

        override fun getSectionForPosition(i: Int): Int = if (sectionForPosition.size > 0) sectionForPosition[i] else 0

        override fun getItemCount(): Int = history.size

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getItemViewType(position: Int): Int = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            var resId = 0
            when (viewType) {
                0 -> resId = R.layout.listview_item
                //1 -> resId = R.layout.listview_bottom_spacing
            }
            val view = LayoutInflater.from(parent.context).inflate(resId, parent, false)
            return ViewHolder(view, viewType)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val hymnPair = history[position]
            if (holder.isItem) {
                holder.textView.text = hymnPair.second.toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) +
                        ": (" + hymnPair.first.hymnId.buchMode.toCompactString() + ") " + hymnPair.first.numberAndTitle
                holder.parentView.setOnClickListener {
                    lifecycleScope.launch {
                        startActivity(
                            Intent(this@HistorySwitchBarActivity, TextviewActivity::class.java).putExtra(
                                "hymnId",
                                hymnPair.first.hymnId.toInt()
                            )
                        )
                    }
                }
                holder.parentView.allViews.forEach { view -> view.isEnabled = enabled }
            }
        }

        inner class ViewHolder internal constructor(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
            var isItem: Boolean = viewType == 0
            lateinit var parentView: RelativeLayout
            lateinit var textView: TextView

            init {
                if (isItem) {
                    parentView = itemView as RelativeLayout
                    textView = parentView.findViewById(R.id.icon_tab_item_text)
                }
            }
        }
    }

    private class ItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
        private val divider: Drawable
        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDraw(c, parent, state)
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                val top = (child.bottom + (child.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin)
                val bottom = divider.intrinsicHeight + top
                divider.setBounds(parent.left, top, parent.right, bottom)
                divider.draw(c)
            }
        }

        init {
            val outValue = TypedValue()
            context.theme.resolveAttribute(androidx.appcompat.R.attr.isLightTheme, outValue, true)
            divider = context.getDrawable(
                if (outValue.data == 0) androidx.appcompat.R.drawable.sesl_list_divider_dark
                else androidx.appcompat.R.drawable.sesl_list_divider_light
            )!!
        }
    }
}