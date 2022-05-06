package de.lemke.nakbuch.ui.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import de.dlyt.yanndroid.oneui.layout.DrawerLayout
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager
import de.dlyt.yanndroid.oneui.sesl.utils.SeslRoundedCorner
import de.dlyt.yanndroid.oneui.view.RecyclerView
import de.dlyt.yanndroid.oneui.view.ViewPager2
import de.dlyt.yanndroid.oneui.widget.TabLayout
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.hymns.GetAllRubricsUseCase
import de.lemke.nakbuch.domain.hymns.GetHymnsWithRubricUseCase
import de.lemke.nakbuch.domain.model.*
import de.lemke.nakbuch.domain.settings.GetBuchModeUseCase
import de.lemke.nakbuch.ui.TextviewActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TabListSubtabRubric : Fragment() {
    private lateinit var mRootView: View
    private lateinit var mContext: Context
    private lateinit var hymns: ArrayList<Hymn>
    private lateinit var rubrics: ArrayList<Rubric>
    private var currentRubric: Rubric? = null
    private lateinit var buchMode: BuchMode
    private lateinit var listView: RecyclerView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var subTabs: TabLayout
    private lateinit var mainTabs: TabLayout
    private lateinit var viewPager2List: ViewPager2
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mRootView = inflater.inflate(R.layout.fragment_tab_list_subtab_rubric, container, false)
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buchMode = GetBuchModeUseCase()()
        drawerLayout = requireActivity().findViewById(R.id.drawer_view)
        listView = mRootView.findViewById(R.id.hymnListRubric)
        subTabs = requireActivity().findViewById(R.id.sub_tabs)
        mainTabs = requireActivity().findViewById(R.id.main_tabs)
        viewPager2List = requireActivity().findViewById(R.id.viewPager2Lists)
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                currentRubric = null
                initList()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
        initList()
    }

    private fun initList() {
        CoroutineScope(Dispatchers.IO).launch {
            rubrics = GetAllRubricsUseCase()(buchMode)
            rubrics.add(rubricPlaceholder)
            onBackPressedCallback.isEnabled = false
            if (currentRubric != null) {
                hymns = GetHymnsWithRubricUseCase()(currentRubric!!)
                hymns.add(0, hymnPlaceholder)
                hymns.add(hymnPlaceholder)
                onBackPressedCallback.isEnabled = true
            }
            withContext(Dispatchers.Main) {
                imageAdapter = ImageAdapter()
                listView.adapter = imageAdapter
                val divider = TypedValue()
                mContext.theme.resolveAttribute(android.R.attr.listDivider, divider, true)
                listView.layoutManager = LinearLayoutManager(mContext)
                val decoration = ItemDecoration()
                listView.addItemDecoration(decoration)
                decoration.setDivider(AppCompatResources.getDrawable(mContext, divider.resourceId)!!)
                listView.itemAnimator = null
                //listView.seslSetIndexTipEnabled(rubrikIndex == 0) //see ImageAdapter why
                listView.seslSetFastScrollerEnabled(true)
                listView.seslSetFillBottomEnabled(true)
                listView.seslSetGoToTopEnabled(true)
                listView.seslSetLastRoundedCorner(false)
            }
        }

    }

    //Adapter for the Icon RecyclerView
    inner class ImageAdapter :
        RecyclerView.Adapter<ImageAdapter.ViewHolder>() {// !!!Sections in inner rubrics not working!!!       , SectionIndexer {
        /*private var mSections: MutableList<String> = ArrayList()
        private var mPositionForSection: MutableList<Int> = ArrayList()
        private var mSectionForPosition: MutableList<Int> = ArrayList()

        init {
            if (rubrikIndex == 0) {
                for (i in rubList.indices) {
                    val rubName: String =
                        if (i != rubList.size - 1 && rubList[i].containsKey("mainRub")) { rubList[i]["hymnNrAndTitle"]!! }
                        else { mSections[mSections.size - 1] }
                    if (i == 0 || mSections[mSections.size - 1] != rubName) {
                        mSections.add(rubName)
                        mPositionForSection.add(i)
                    }
                    mSectionForPosition.add(mSections.size - 1)
                }
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

        */

        override fun getItemCount(): Int {
            return if (currentRubric == null) rubrics.size
            else hymns.size

        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemViewType(position: Int): Int {
            return if (currentRubric == null) {
                when {
                    rubrics[position] == rubricPlaceholder -> 1
                    rubrics[position].isMain -> 2
                    else -> 0
                }
            } else {
                when {
                    position == 0 -> 3
                    hymns[position] == hymnPlaceholder -> 1
                    else -> 0
                }
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
            /*if (holder.isBackHeader) {
                holder.textView.text = rubrikIndexName
                holder.parentView.setOnClickListener {
                    if (rubrics[position].containsKey("backHeader")) {
                        rubrikIndex = 0
                        initList()
                    }
                }
            }*/
            if (holder.isItem) {
                //holder.imageView.setImageResource(R.drawable.ic_samsung_audio);
                if (currentRubric != null) {
                    holder.textView.text = hymns[position].numberAndTitle
                    holder.parentView.setOnClickListener {
                        startActivity(Intent(mRootView.context, TextviewActivity::class.java).putExtra("nr", hymns[position].number))
                    }
                } else {
                    holder.textView.text = rubrics[position].name
                    holder.parentView.setOnClickListener {
                        currentRubric = rubrics[position]
                        initList()
                    }
                }
            }
            if (holder.isHeader) {
                holder.textView.text = rubrics[position].name
            }
            if (holder.isBackHeader) {
                holder.textView.text = currentRubric!!.name
                holder.parentView.setOnClickListener {
                    currentRubric = null
                    initList()
                }
            }
        }

        inner class ViewHolder internal constructor(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
            var isItem: Boolean = viewType == 0
            var isHeader: Boolean = viewType == 2
            var isBackHeader: Boolean = viewType == 3

            lateinit var parentView: RelativeLayout
            lateinit var imageView: ImageView
            lateinit var textView: TextView

            init {
                if (isItem) {
                    parentView = itemView as RelativeLayout
                    textView = parentView.findViewById(R.id.icon_tab_item_text)
                }
                if (isHeader || isBackHeader) {
                    parentView = itemView as RelativeLayout
                    imageView = parentView.findViewById(R.id.icon_tab_item_image)
                    textView = parentView.findViewById(R.id.icon_tab_item_text)
                }
            }
        }
    }

    inner class ItemDecoration : RecyclerView.ItemDecoration() {
        private val mSeslRoundedCornerTop: SeslRoundedCorner = SeslRoundedCorner(requireContext(), true)
        private val mSeslRoundedCornerBottom: SeslRoundedCorner
        private var mDivider: Drawable? = null
        private var mDividerHeight = 0
        override fun seslOnDispatchDraw(canvas: Canvas, recyclerView: RecyclerView, state: RecyclerView.State) {
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