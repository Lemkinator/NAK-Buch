package de.lemke.nakbuch.fragments

import de.lemke.nakbuch.utils.AssetsHelper.getHymnArrayList
import de.lemke.nakbuch.utils.AssetsHelper.getRubricListItemArrayList
import de.lemke.nakbuch.utils.AssetsHelper.getRubricTitlesArrayList
import android.view.View
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import de.lemke.nakbuch.R
import android.util.TypedValue
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import android.widget.CompoundButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.graphics.Canvas
import androidx.activity.OnBackPressedCallback
import android.widget.CheckBox
import android.widget.SectionIndexer
import android.widget.ImageView
import androidx.fragment.app.Fragment
import de.dlyt.yanndroid.oneui.layout.DrawerLayout
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager
import de.dlyt.yanndroid.oneui.sesl.utils.SeslRoundedCorner
import de.dlyt.yanndroid.oneui.view.RecyclerView
import de.dlyt.yanndroid.oneui.view.ViewPager2
import de.dlyt.yanndroid.oneui.widget.TabLayout
import java.util.*

class TabList_SubtabRubric : Fragment() {
    private lateinit var listView: RecyclerView
    private lateinit var mRootView: View
    private lateinit var mContext: Context
    private lateinit var hymns: ArrayList<HashMap<String, String>>
    private lateinit var rubricListItemViewTypes: ArrayList<Int>
    private lateinit var rubricTitles: ArrayList<String>
    private lateinit var rubList: ArrayList<HashMap<String, String>>
    private var rubrikIndex = 0
    private var rubrikIndexName = ""
    private var gesangbuchSelected = false
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var subTabs: TabLayout
    private lateinit var mainTabs: TabLayout
    private lateinit var viewPager2List: ViewPager2
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private var selected = HashMap<Int, Boolean>()
    private var mSelecting = false
    private var checkAllListening = true
    private lateinit var sp: SharedPreferences
    private lateinit var spHymns: SharedPreferences
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mRootView = inflater.inflate(R.layout.fragment_tab_list_subtab_rubric, container, false)
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sp = mContext.getSharedPreferences(
            getString(R.string.preference_file_default),
            Context.MODE_PRIVATE
        )
        spHymns = mContext.getSharedPreferences(
            getString(R.string.preference_file_hymns),
            Context.MODE_PRIVATE
        )
        gesangbuchSelected = sp.getBoolean("gesangbuchSelected", true)
        drawerLayout = requireActivity().findViewById(R.id.drawer_view)
        listView = mRootView.findViewById(R.id.hymnListRubric)
        subTabs = requireActivity().findViewById(R.id.sub_tabs)
        mainTabs = requireActivity().findViewById(R.id.main_tabs)
        viewPager2List = requireActivity().findViewById(R.id.viewPager2Lists)
        onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (mSelecting) setSelecting(false) else {
                    rubrikIndex = 0
                    initList()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
        initAssets()
        initList()
    }

    @Synchronized
    fun setRubList() {
        rubList = ArrayList()
        if (rubrikIndex == 0) {
            onBackPressedCallback.isEnabled = false
            for ((currentIndex, i) in rubricTitles.indices.withIndex()) {
                val hm = HashMap<String, String>()
                hm["hymnNrAndTitle"] = rubricTitles[i]
                if (rubricListItemViewTypes[i] == 0) {
                    hm["mainRub"] = ""
                } else {
                    hm["rubIndex"] = currentIndex.toString()
                }
                rubList.add(hm)
            }
        } else {
            onBackPressedCallback.isEnabled = true
            val hmBack = HashMap<String, String>()
            hmBack["backHeader"] = ""
            rubList.add(hmBack)
            for (hm in hymns) {
                if (hm.containsKey("hymnRubricIndex")) {
                    if (hm["hymnRubricIndex"]!!.toInt() == rubrikIndex) {
                        rubList.add(hm)
                    }
                }
            }
        }
        rubList.add(HashMap())
    }

    private fun initList() {
        setRubList()
        selected = HashMap()
        for (i in rubList.indices) selected[i] = false
        val divider = TypedValue()
        mContext.theme.resolveAttribute(android.R.attr.listDivider, divider, true)
        listView.layoutManager = LinearLayoutManager(mContext)
        imageAdapter = ImageAdapter()
        listView.adapter = imageAdapter
        val decoration = ItemDecoration()
        listView.addItemDecoration(decoration)
        AppCompatResources.getDrawable(mContext, divider.resourceId)
            ?.let { decoration.setDivider(it) }
        listView.itemAnimator = null
        listView.seslSetIndexTipEnabled(rubrikIndex == 0)
        listView.seslSetFastScrollerEnabled(true)
        listView.seslSetFillBottomEnabled(true)
        listView.seslSetGoToTopEnabled(true)
        listView.seslSetLastRoundedCorner(false)
    }

    fun setSelecting(enabled: Boolean) {
        if (enabled) {
            mSelecting = true
            drawerLayout.showSelectMode()
            drawerLayout.setSelectModeAllCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                if (checkAllListening) {
                    for (i in 0 until imageAdapter.itemCount - 1) {
                        selected[i] = isChecked
                        imageAdapter.notifyItemChanged(i)
                    }
                }
                var count = 0
                for (b in selected.values) if (b) count++
                drawerLayout.setSelectModeCount(count)
            }
            subTabs.isEnabled = false
            mainTabs.isEnabled = false
            viewPager2List.isUserInputEnabled = false
            onBackPressedCallback.isEnabled = true
        } else {
            mSelecting = false
            for (i in 0 until imageAdapter.itemCount - 1) selected[i] = false
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.itemCount - 1)
            drawerLayout.setSelectModeCount(0)
            drawerLayout.dismissSelectMode()
            subTabs.isEnabled = true
            mainTabs.isEnabled = true
            viewPager2List.isUserInputEnabled = true
            onBackPressedCallback.isEnabled = false
        }
    }

    fun toggleItemSelected(position: Int) {
        selected[position] = !selected[position]!!
        imageAdapter.notifyItemChanged(position)
        checkAllListening = false
        var count = 0
        for (b in selected.values) if (b) count++
        drawerLayout.setSelectModeAllChecked(count == imageAdapter.itemCount - 1)
        drawerLayout.setSelectModeCount(count)
        checkAllListening = true
    }

    private fun initAssets() {
        hymns = getHymnArrayList(mContext, sp, gesangbuchSelected)
        rubricListItemViewTypes = getRubricListItemArrayList(gesangbuchSelected)
        rubricTitles = getRubricTitlesArrayList(gesangbuchSelected)
        hymns.add(HashMap()) //Placeholder
    }

    //Adapter for the Icon RecyclerView
    inner class ImageAdapter internal constructor() :
        RecyclerView.Adapter<ImageAdapter.ViewHolder>(), SectionIndexer {
        private var mSections: MutableList<String> = ArrayList()
        private var mPositionForSection: MutableList<Int> = ArrayList()
        private var mSectionForPosition: MutableList<Int> = ArrayList()
        override fun getItemCount(): Int {
            return rubList.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemViewType(position: Int): Int {
            return when {
                rubList[position].containsKey("mainRub") -> {
                    2
                }
                rubList[position].containsKey("backHeader") -> {
                    3
                }
                rubList[position].containsKey("hymnNrAndTitle") -> {
                    0
                }
                else -> 1
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            var resId = 0
            when (viewType) {
                0 -> resId = R.layout.listview_item
                1 -> resId = R.layout.listview_bottom_spacing
                2 -> resId = R.layout.listview_header
                3 -> resId = R.layout.listview_back_header
            }
            val view = LayoutInflater.from(parent.context).inflate(resId, parent, false)
            return ViewHolder(view, viewType)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder.isBackHeader) {
                holder.textView.text = rubrikIndexName
                holder.parentView.setOnClickListener {
                    if (rubList[position].containsKey("backHeader")) {
                        rubrikIndex = 0
                        initList()
                    }
                }
            }
            if (holder.isItem) {
                holder.checkBox.visibility = if (mSelecting) View.VISIBLE else View.GONE
                holder.checkBox.isChecked = selected[position]!!
                //holder.imageView.setImageResource(R.drawable.ic_samsung_audio);
                holder.textView.text = rubList[position]["hymnNrAndTitle"]
                holder.parentView.setOnClickListener {
                    if (mSelecting) toggleItemSelected(position) else {
                        if (rubrikIndex == 0) {
                            if (rubList[position].containsKey("rubIndex")) {
                                rubrikIndex = rubList[position]["rubIndex"]!!.toInt()
                                rubrikIndexName = rubList[position]["hymnNrAndTitle"].toString()
                                initList()
                            }
                        } else if (rubList[position].containsKey("hymnNr")) {
                            /* TODO startActivity(
                                Intent(mRootView.context, TextviewActivity::class)
                                    .putExtra(
                                        "nr",
                                        rubList[position]["hymnNr"]?.toInt() ?: -1
                                    )
                            )*/
                        }
                    }
                }
                if (false) { //Disable selecting, maybe enable in future...
                    holder.parentView.setOnLongClickListener {
                        if (!mSelecting) setSelecting(true)
                        toggleItemSelected(position)
                        listView.seslStartLongPressMultiSelection()
                        listView.seslSetLongPressMultiSelectionListener(object :
                            RecyclerView.SeslLongPressMultiSelectionListener {
                            override fun onItemSelected(
                                var1: RecyclerView,
                                var2: View,
                                var3: Int,
                                var4: Long
                            ) {
                                if (getItemViewType(var3) == 0) toggleItemSelected(var3)
                            }

                            override fun onLongPressMultiSelectionEnded(var1: Int, var2: Int) {}
                            override fun onLongPressMultiSelectionStarted(var1: Int, var2: Int) {}
                        })
                        true
                    }
                }
            }
            if (holder.isHeader) {
                holder.textView.text = rubList[position]["hymnNrAndTitle"]
            }
        }

        override fun getSections(): Array<Any> {
            return mSections.toTypedArray()
        }

        override fun getPositionForSection(i: Int): Int {
            return mPositionForSection[i]
        }

        override fun getSectionForPosition(i: Int): Int {
            return mSectionForPosition[i]
        }

        inner class ViewHolder internal constructor(itemView: View, viewType: Int) :
            RecyclerView.ViewHolder(
                itemView
            ) {
            var isItem: Boolean = viewType == 0
            var isHeader: Boolean = viewType == 2
            var isBackHeader: Boolean = viewType == 3
            lateinit var parentView: RelativeLayout
            lateinit var imageView: ImageView
            lateinit var textView: TextView
            lateinit var checkBox: CheckBox

            init {
                if (isItem) {
                    parentView = itemView as RelativeLayout
                    //imageView = parentView.findViewById(R.id.icon_tab_item_image);
                    textView = parentView.findViewById(R.id.icon_tab_item_text)
                    checkBox = parentView.findViewById(R.id.checkbox)
                }
                if (isHeader || isBackHeader) {
                    parentView = itemView as RelativeLayout
                    imageView = parentView.findViewById(R.id.icon_tab_item_image)
                    textView = parentView.findViewById(R.id.icon_tab_item_text)
                    //checkBox = parentView.findViewById(R.id.checkbox);
                }
            }
        }

        init {
            if (rubrikIndex == 0) {
                for (i in rubList.indices) {
                    val rubName: String =
                        if (i != rubList.size - 1 && rubList[i].containsKey("mainRub")) ({
                            rubList[i]["hymnNrAndTitle"]
                        }).toString() else {
                            mSections[mSections.size - 1]
                        }
                    if (i == 0 || mSections[mSections.size - 1] != rubName) {
                        mSections.add(rubName)
                        mPositionForSection.add(i)
                    }
                    mSectionForPosition.add(mSections.size - 1)
                }
            }
        }
    }

    inner class ItemDecoration : RecyclerView.ItemDecoration() {
        private val mSeslRoundedCornerTop: SeslRoundedCorner =
            SeslRoundedCorner(requireContext(), true)
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

            // draw divider for each item
            for (i in 0 until childCount) {
                val childAt = recyclerView.getChildAt(i)
                val viewHolder = recyclerView.getChildViewHolder(childAt) as ImageAdapter.ViewHolder
                val y = childAt.y.toInt() + childAt.height
                val shallDrawDivider: Boolean =
                    if (recyclerView.getChildAt(i + 1) != null) (recyclerView.getChildViewHolder(
                        recyclerView.getChildAt(i + 1)
                    ) as ImageAdapter.ViewHolder).isItem else false
                if (mDivider != null && viewHolder.isItem && shallDrawDivider) {
                    //int moveRTL = isRTL() ? 130 : 0;
                    //mDivider.setBounds(130 - moveRTL, y, width - moveRTL, mDividerHeight + y);
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
            mSeslRoundedCornerBottom = SeslRoundedCorner(requireContext(), true)
            mSeslRoundedCornerBottom.roundedCorners = 12
        }
    }
}