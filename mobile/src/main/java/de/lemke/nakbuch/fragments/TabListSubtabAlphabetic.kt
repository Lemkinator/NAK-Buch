package de.lemke.nakbuch.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import de.dlyt.yanndroid.oneui.layout.DrawerLayout
import de.dlyt.yanndroid.oneui.menu.MenuItem
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager
import de.dlyt.yanndroid.oneui.sesl.utils.SeslRoundedCorner
import de.dlyt.yanndroid.oneui.view.IndexScrollView
import de.dlyt.yanndroid.oneui.view.RecyclerView
import de.dlyt.yanndroid.oneui.view.ViewPager2
import de.dlyt.yanndroid.oneui.widget.TabLayout
import de.lemke.nakbuch.R
import de.lemke.nakbuch.ui.TextviewActivity
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.utils.AssetsHelper
import de.lemke.nakbuch.domain.utils.HymnPrefsHelper.writeFavsToList
import java.util.*

class TabListSubtabAlphabetic : Fragment() {
    private lateinit var listView: RecyclerView
    private lateinit var mRootView: View
    private lateinit var mContext: Context
    private lateinit var hymnsAlphsort: ArrayList<HashMap<String, String>>
    private lateinit var buchMode: BuchMode
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
        mRootView = inflater.inflate(R.layout.fragment_tab_list_subtab_alphabetic, container, false)
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
        buchMode = if (sp.getBoolean("gesangbuchSelected", true)) BuchMode.Gesangbuch else BuchMode.Chorbuch
        drawerLayout = requireActivity().findViewById(R.id.drawer_view)
        listView = mRootView.findViewById(R.id.hymnListAlphabetical)
        subTabs = requireActivity().findViewById(R.id.sub_tabs)
        mainTabs = requireActivity().findViewById(R.id.main_tabs)
        viewPager2List = requireActivity().findViewById(R.id.viewPager2Lists)
        onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (mSelecting) setSelecting(false)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
        hymnsAlphsort = AssetsHelper.getHymnArrayList(mContext, sp, buchMode == BuchMode.Gesangbuch)
        hymnsAlphsort.sortWith(Comparator.comparing { hm: HashMap<String, String> -> hm["hymnTitle"]!! })
        hymnsAlphsort.add(HashMap()) //Placeholder
        initList()
    }

    private fun initList() {
        selected = HashMap()
        for (i in hymnsAlphsort.indices) selected[i] = false
        listView.layoutManager = LinearLayoutManager(mContext)
        imageAdapter = ImageAdapter()
        listView.adapter = imageAdapter
        listView.itemAnimator = null
        listView.seslSetIndexTipEnabled(true)
        listView.seslSetFillBottomEnabled(true)
        listView.seslSetGoToTopEnabled(true)
        listView.seslSetLastRoundedCorner(false)
        val divider = TypedValue()
        mContext.theme.resolveAttribute(android.R.attr.listDivider, divider, true)
        val decoration = ItemDecoration()
        listView.addItemDecoration(decoration)
        decoration.setDivider(AppCompatResources.getDrawable(mContext, divider.resourceId)!!)
        val indexScrollView: IndexScrollView =
            mRootView.findViewById(R.id.indexScrollViewAlphabetical)
        val list: MutableList<String?> = ArrayList()
        for (i in 0 until hymnsAlphsort.size - 1) list.add(hymnsAlphsort[i]["hymnTitle"])
        indexScrollView.syncWithRecyclerView(listView, list, true)
        indexScrollView.setIndexBarGravity(1)
    }

    fun setSelecting(enabled: Boolean) {
        if (enabled) {
            mSelecting = true
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.itemCount - 1)
            drawerLayout.setSelectModeBottomMenu(R.menu.fav_menu) { item: MenuItem ->
                when (item.itemId) {
                    R.id.addToFav -> {
                        writeFavsToList(buchMode == BuchMode.Gesangbuch, spHymns, selected, hymnsAlphsort, "1")
                    }
                    R.id.removeFromFav -> {
                        writeFavsToList(buchMode == BuchMode.Gesangbuch, spHymns, selected, hymnsAlphsort, "")
                    }
                    else -> {
                        item.badge = item.badge + 1
                        Toast.makeText(mContext, item.title, Toast.LENGTH_SHORT).show()
                    }
                }
                setSelecting(false)
                true
            }
            drawerLayout.showSelectMode()
            drawerLayout.setSelectModeAllCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
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

    //Adapter for the Icon RecyclerView
    inner class ImageAdapter internal constructor() :
        RecyclerView.Adapter<ImageAdapter.ViewHolder>(), SectionIndexer {
        private var mSections: MutableList<String> = ArrayList()
        private var mPositionForSection: MutableList<Int> = ArrayList()
        private var mSectionForPosition: MutableList<Int> = ArrayList()
        override fun getItemCount(): Int {
            return hymnsAlphsort.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemViewType(position: Int): Int {
            return if (hymnsAlphsort[position].containsKey("hymnNr")) {
                0
            } else 1
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

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder.isItem) {
                holder.checkBox.visibility = if (mSelecting) View.VISIBLE else View.GONE
                holder.checkBox.isChecked = selected[position]!!
                //holder.imageView.setImageResource(R.drawable.ic_samsung_audio);
                holder.textView.text = hymnsAlphsort[position]["hymnNrAndTitle"]
                holder.parentView.setOnClickListener {
                    if (mSelecting) toggleItemSelected(position) else {
                        startActivity(
                            Intent(mRootView.context, TextviewActivity::class.java)
                                .putExtra(
                                    "nr",
                                    hymnsAlphsort[position]["hymnNr"]?.toInt() ?: -1
                                )
                        )
                    }
                }
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
            }
        }

        init {
            for (i in hymnsAlphsort.indices) {
                val letter: String = if (i != hymnsAlphsort.size - 1) {
                    hymnsAlphsort[i]["hymnTitle"]!!.substring(0, 1).uppercase(Locale.getDefault())
                } else {
                    mSections[mSections.size - 1]
                }
                if (i == 0 || mSections[mSections.size - 1] != letter) {
                    mSections.add(letter)
                    mPositionForSection.add(i)
                }
                mSectionForPosition.add(mSections.size - 1)
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
                val shallDrawDivider: Boolean = if (recyclerView.getChildAt(i + 1) != null) (recyclerView.getChildViewHolder(
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