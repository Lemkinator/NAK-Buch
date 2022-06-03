package de.lemke.nakbuch.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.SectionIndexer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.allViews
import dagger.hilt.android.AndroidEntryPoint
import de.dlyt.yanndroid.oneui.layout.SwitchBarLayout
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager
import de.dlyt.yanndroid.oneui.sesl.utils.SeslRoundedCorner
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.dlyt.yanndroid.oneui.view.RecyclerView
import de.dlyt.yanndroid.oneui.widget.Switch
import de.dlyt.yanndroid.oneui.widget.SwitchBar
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.GetUserSettingsUseCase
import de.lemke.nakbuch.domain.UpdateUserSettingsUseCase
import de.lemke.nakbuch.domain.hymndataUseCases.GetHistoryListUseCase
import de.lemke.nakbuch.domain.hymndataUseCases.ResetHistoryUseCase
import de.lemke.nakbuch.domain.model.Hymn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class HistorySwitchBarActivity : AppCompatActivity(), SwitchBar.OnSwitchChangeListener {
    private val coroutineContext: CoroutineContext = Dispatchers.Main
    private val coroutineScope: CoroutineScope = CoroutineScope(coroutineContext)
    private lateinit var history: MutableList<Pair<Hymn, LocalDate>>
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
        ThemeUtil(this, resources.getString(R.color.primary_color))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        listView = findViewById(R.id.historyList)
        switchBarLayout = findViewById(R.id.switchbarlayout_history)
        switchBarLayout.switchBar.addOnSwitchChangeListener(this)
        switchBarLayout.setNavigationButtonTooltip(getString(de.dlyt.yanndroid.oneui.R.string.sesl_navigate_up))
        switchBarLayout.setNavigationButtonOnClickListener { onBackPressed() }
        switchBarLayout.inflateToolbarMenu(R.menu.switchpreferencescreen_menu)
        switchBarLayout.setOnToolbarMenuItemClickListener {
            coroutineScope.launch {
                resetHistory()
                initList()
            }
            true
        }
    }

    override fun onSwitchChanged(switchCompat: Switch, z: Boolean) {
        coroutineScope.launch {
            enabled = updateUserSettings{ it.copy(historyEnabled = z) }.historyEnabled
            initList()
        }
    }

    public override fun onResume() {
        super.onResume()
        coroutineScope.launch {
            enabled = getUserSettings().historyEnabled
            switchBarLayout.switchBar.isChecked = enabled
            initList()
        }
    }

    private suspend fun initList() {
        history = getHistoryList().toMutableList()
        history.add(Pair(Hymn.hymnPlaceholder, LocalDate.MIN))
        listView.adapter = ImageAdapter()
        val divider = TypedValue()
        theme.resolveAttribute(android.R.attr.listDivider, divider, true)
        listView.layoutManager = LinearLayoutManager(this)
        val decoration = ItemDecoration()
        listView.addItemDecoration(decoration)
        decoration.setDivider(AppCompatResources.getDrawable(this, divider.resourceId)!!)
        listView.itemAnimator = null
        listView.seslSetIndexTipEnabled(true)
        listView.seslSetFastScrollerEnabled(true)
        listView.seslSetFillBottomEnabled(true)
        listView.seslSetGoToTopEnabled(true)
        listView.seslSetLastRoundedCorner(false)
    }

    inner class ImageAdapter internal constructor() :
        RecyclerView.Adapter<ImageAdapter.ViewHolder>(), SectionIndexer {
        private var sections: MutableList<String> = mutableListOf()
        private var positionForSection: MutableList<Int> = mutableListOf()
        private var sectionForPosition: MutableList<Int> = mutableListOf()

        init {
            if (history.size > 1) {
                history.forEachIndexed { index, pair ->
                    val date: String =
                        if (index != history.size - 1) pair.second.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                        else sections[sections.size - 1]
                    if (index == 0 || sections[sections.size - 1] != date) {
                        sections.add(date)
                        positionForSection.add(index)
                    }
                    sectionForPosition.add(sections.size - 1)
                }
            }
        }

        override fun getSections(): Array<Any> {
            return sections.toTypedArray()
        }

        override fun getPositionForSection(i: Int): Int {
            return if (positionForSection.size > 0) positionForSection[i] else 0
        }

        override fun getSectionForPosition(i: Int): Int {
            return if (sectionForPosition.size > 0) sectionForPosition[i] else 0
        }

        override fun getItemCount(): Int {
            return history.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemViewType(position: Int): Int {
            return if (history[position].first != Hymn.hymnPlaceholder) 0
            else 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            var resId = 0
            when (viewType) {
                0 -> resId = R.layout.listview_item
                1 -> resId = R.layout.listview_bottom_spacing
            }
            val view = LayoutInflater.from(parent.context).inflate(resId, parent, false)
            return ViewHolder(view, viewType)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val hymnPair = history[position]
            if (holder.isItem) {
                //holder.imageView.setImageResource(R.drawable.ic_samsung_audio);
                holder.textView.text = hymnPair.second.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) +
                        ": (" + hymnPair.first.hymnId.buchMode.toCompactString() + ") " + hymnPair.first.numberAndTitle
                holder.parentView.setOnClickListener {
                    coroutineScope.launch {
                        //updateUserSettings{ it.copy(buchMode = hymnPair.first.hymnId.buchMode) } //change Mode here? no
                        startActivity(Intent(this@HistorySwitchBarActivity, TextviewActivity::class.java).putExtra("hymnId", hymnPair.first.hymnId.toInt()))
                    }
                }
                holder.parentView.allViews.forEach { view -> view.isEnabled = enabled }
            }
        }

        inner class ViewHolder internal constructor(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
            var isItem: Boolean = viewType == 0
            lateinit var parentView: RelativeLayout

            //ImageView imageView;
            lateinit var textView: TextView

            init {
                if (isItem) {
                    parentView = itemView as RelativeLayout
                    //imageView = parentView.findViewById(R.id.icon_tab_item_image);
                    textView = parentView.findViewById(R.id.icon_tab_item_text)
                }
            }
        }
    }

    inner class ItemDecoration : RecyclerView.ItemDecoration() {
        private val seslRoundedCornerTop: SeslRoundedCorner = SeslRoundedCorner(this@HistorySwitchBarActivity, true)
        private val seslRoundedCornerBottom: SeslRoundedCorner
        private var divider: Drawable? = null
        private var dividerHeight = 0
        override fun seslOnDispatchDraw(canvas: Canvas, recyclerView: RecyclerView, state: RecyclerView.State) {
            super.seslOnDispatchDraw(canvas, recyclerView, state)
            val childCount = recyclerView.childCount
            val width = recyclerView.width

            for (i in 0 until childCount) {
                val childAt = recyclerView.getChildAt(i)
                val viewHolder = recyclerView.getChildViewHolder(childAt) as ImageAdapter.ViewHolder
                val y = childAt.y.toInt() + childAt.height
                val shallDrawDivider: Boolean =
                    if (recyclerView.getChildAt(i + 1) != null) (recyclerView.getChildViewHolder(
                        recyclerView.getChildAt(i + 1)
                    ) as ImageAdapter.ViewHolder).isItem else false
                if (divider != null && viewHolder.isItem && shallDrawDivider) {
                    divider!!.setBounds(0, y, width, dividerHeight + y)
                    divider!!.draw(canvas)
                }
                if (!viewHolder.isItem) {
                    if (recyclerView.getChildAt(i + 1) != null) seslRoundedCornerTop.drawRoundedCorner(
                        recyclerView.getChildAt(i + 1),
                        canvas
                    )
                    if (recyclerView.getChildAt(i - 1) != null) seslRoundedCornerBottom.drawRoundedCorner(
                        recyclerView.getChildAt(i - 1),
                        canvas
                    )
                }
            }
            seslRoundedCornerTop.drawRoundedCorner(canvas)
        }

        fun setDivider(d: Drawable) {
            divider = d
            dividerHeight = d.intrinsicHeight
            listView.invalidateItemDecorations()
        }

        init {
            seslRoundedCornerTop.roundedCorners = 3
            seslRoundedCornerBottom = SeslRoundedCorner(this@HistorySwitchBarActivity, true)
            seslRoundedCornerBottom.roundedCorners = 12
        }
    }
}