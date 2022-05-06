package de.lemke.nakbuch.ui

import android.annotation.SuppressLint
import android.content.Context
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
import de.dlyt.yanndroid.oneui.layout.SwitchBarLayout
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager
import de.dlyt.yanndroid.oneui.sesl.utils.SeslRoundedCorner
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.dlyt.yanndroid.oneui.view.RecyclerView
import de.dlyt.yanndroid.oneui.widget.Switch
import de.dlyt.yanndroid.oneui.widget.SwitchBar
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.hymndata.GetHistoryListUseCase
import de.lemke.nakbuch.domain.hymndata.ResetHistoryUseCase
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.hymnPlaceholder
import de.lemke.nakbuch.domain.settings.IsHistroyEnabledUseCase
import de.lemke.nakbuch.domain.settings.SetBuchModeUseCase
import de.lemke.nakbuch.domain.settings.SetHistoryEnabledUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class HistorySwitchBarActivity : AppCompatActivity(), SwitchBar.OnSwitchChangeListener {
    private lateinit var historyList: ArrayList<Pair<Hymn, LocalDate>>
    private lateinit var listView: RecyclerView
    private lateinit var mContext: Context
    private var mEnabled: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtil(this)
        super.onCreate(savedInstanceState)
        mContext = this
        setContentView(R.layout.activity_history)
        listView = findViewById(R.id.historyList)
        val switchBarLayout = findViewById<SwitchBarLayout>(R.id.switchbarlayout_history)
        mEnabled = IsHistroyEnabledUseCase()()
        switchBarLayout.switchBar.isChecked = mEnabled
        switchBarLayout.switchBar.addOnSwitchChangeListener(this)
        switchBarLayout.setNavigationButtonTooltip(getString(de.dlyt.yanndroid.oneui.R.string.sesl_navigate_up))
        switchBarLayout.setNavigationButtonOnClickListener { onBackPressed() }
        switchBarLayout.inflateToolbarMenu(R.menu.switchpreferencescreen_menu)
        switchBarLayout.setOnToolbarMenuItemClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                ResetHistoryUseCase()()
                withContext(Dispatchers.Main) { initList() }
            }
            true
        }
    }

    override fun onSwitchChanged(switchCompat: Switch, z: Boolean) {
        SetHistoryEnabledUseCase()(z)
        mEnabled = z
        initList()
    }

    public override fun onResume() {
        super.onResume()
        initList()
    }

    private fun initList() {
        CoroutineScope(Dispatchers.IO).launch {
            historyList = GetHistoryListUseCase()()
            historyList.add(Pair(hymnPlaceholder, LocalDate.MIN))
            withContext(Dispatchers.Main) {
                listView.adapter = ImageAdapter()
                val divider = TypedValue()
                theme.resolveAttribute(android.R.attr.listDivider, divider, true)
                listView.layoutManager = LinearLayoutManager(mContext)
                val decoration = ItemDecoration()
                listView.addItemDecoration(decoration)
                decoration.setDivider(AppCompatResources.getDrawable(mContext, divider.resourceId)!!)
                listView.itemAnimator = null
                listView.seslSetIndexTipEnabled(true)
                listView.seslSetFastScrollerEnabled(true)
                listView.seslSetFillBottomEnabled(true)
                listView.seslSetGoToTopEnabled(true)
                listView.seslSetLastRoundedCorner(false)
            }
        }


    }

    inner class ImageAdapter internal constructor() :
        RecyclerView.Adapter<ImageAdapter.ViewHolder>(), SectionIndexer {
        private var mSections: MutableList<String> = ArrayList()
        private var mPositionForSection: MutableList<Int> = ArrayList()
        private var mSectionForPosition: MutableList<Int> = ArrayList()

        init {
            if (historyList.size > 1) {
                historyList.forEachIndexed { index, pair ->
                    val date: String =
                        if (index != historyList.size - 1) pair.second.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                        else mSections[mSections.size - 1]
                    if (index == 0 || mSections[mSections.size - 1] != date) {
                        mSections.add(date)
                        mPositionForSection.add(index)
                    }
                    mSectionForPosition.add(mSections.size - 1)
                }
            }
        }

        override fun getSections(): Array<Any> {
            return mSections.toTypedArray()
        }

        override fun getPositionForSection(i: Int): Int {
            return if (mPositionForSection.size > 0) mPositionForSection[i] else 0
        }

        override fun getSectionForPosition(i: Int): Int {
            return if (mSectionForPosition.size > 0) mSectionForPosition[i] else 0
        }

        override fun getItemCount(): Int {
            return historyList.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemViewType(position: Int): Int {
            return if (historyList[position].first != hymnPlaceholder) 0
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
            val hymnPair = historyList[position]
            if (holder.isItem) {
                //holder.imageView.setImageResource(R.drawable.ic_samsung_audio);
                holder.textView.text = hymnPair.second.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) +
                        ": (" + hymnPair.first.buchMode + ") " + hymnPair.first.numberAndTitle
                holder.parentView.setOnClickListener {
                    SetBuchModeUseCase()(hymnPair.first.buchMode)
                    startActivity(
                        Intent(mContext, TextviewActivity::class.java)
                            .putExtra("nr", hymnPair.first.number)
                    )
                }
                holder.parentView.allViews.forEach { view -> view.isEnabled = mEnabled }
            }
        }

        inner class ViewHolder internal constructor(itemView: View, viewType: Int) :
            RecyclerView.ViewHolder(
                itemView
            ) {
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
        private val mSeslRoundedCornerTop: SeslRoundedCorner = SeslRoundedCorner(mContext, true)
        private val mSeslRoundedCornerBottom: SeslRoundedCorner
        private var mDivider: Drawable? = null
        private var mDividerHeight = 0
        override fun seslOnDispatchDraw(
            canvas: Canvas,
            recyclerView: RecyclerView,
            state: RecyclerView.State
        ) {
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
                if (mDivider != null && viewHolder.isItem && shallDrawDivider) {
                    mDivider!!.setBounds(0, y, width, mDividerHeight + y)
                    mDivider!!.draw(canvas)
                }
                if (!viewHolder.isItem) {
                    if (recyclerView.getChildAt(i + 1) != null) mSeslRoundedCornerTop.drawRoundedCorner(
                        recyclerView.getChildAt(i + 1),
                        canvas
                    )
                    if (recyclerView.getChildAt(i - 1) != null) mSeslRoundedCornerBottom.drawRoundedCorner(
                        recyclerView.getChildAt(i - 1),
                        canvas
                    )
                }
            }
            mSeslRoundedCornerTop.drawRoundedCorner(canvas)
        }

        fun setDivider(d: Drawable) {
            mDivider = d
            mDividerHeight = d.intrinsicHeight
            listView.invalidateItemDecorations()
        }

        init {
            mSeslRoundedCornerTop.roundedCorners = 3
            mSeslRoundedCornerBottom = SeslRoundedCorner(mContext, true)
            mSeslRoundedCornerBottom.roundedCorners = 12
        }
    }
}