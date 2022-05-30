package de.lemke.nakbuch.ui.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import de.dlyt.yanndroid.oneui.dialog.ProgressDialog
import de.dlyt.yanndroid.oneui.layout.DrawerLayout
import de.dlyt.yanndroid.oneui.menu.MenuItem
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager
import de.dlyt.yanndroid.oneui.sesl.utils.SeslRoundedCorner
import de.dlyt.yanndroid.oneui.view.RecyclerView
import de.dlyt.yanndroid.oneui.view.ViewPager2
import de.dlyt.yanndroid.oneui.widget.TabLayout
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.GetUserSettingsUseCase
import de.lemke.nakbuch.domain.hymnUseCases.GetAllHymnsUseCase
import de.lemke.nakbuch.domain.hymndataUseCases.SetFavoritesFromHymnListUseCase
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.ui.TextviewActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class TabListSubtabNumeric : Fragment() {
    private val coroutineContext: CoroutineContext = Dispatchers.Main
    private val coroutineScope: CoroutineScope = CoroutineScope(coroutineContext)
    private lateinit var rootView: View
    private lateinit var mContext: Context //cause getContext() is nullable
    private lateinit var buchMode: BuchMode
    private lateinit var hymns: MutableList<Hymn>
    private lateinit var listView: RecyclerView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var subTabs: TabLayout
    private lateinit var mainTabs: TabLayout
    private lateinit var viewPager2List: ViewPager2
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private var selected = HashMap<Int, Boolean>()
    private var selecting = false
    private var checkAllListening = true
    private val handler = Handler(Looper.getMainLooper())
    private val showBottomBarRunnable = Runnable { drawerLayout.showSelectModeBottomBar(true) }

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var getAllHymns: GetAllHymnsUseCase

    @Inject
    lateinit var setFavoritesFromHymnList: SetFavoritesFromHymnListUseCase

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_tab_list_subtab_numeric, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        listView = rootView.findViewById(R.id.hymnList)
        drawerLayout = activity.findViewById(R.id.drawer_view)
        subTabs = activity.findViewById(R.id.sub_tabs)
        mainTabs = activity.findViewById(R.id.main_tabs)
        viewPager2List = activity.findViewById(R.id.viewPager2Lists)
        coroutineScope.launch {
            buchMode = getUserSettings().buchMode
            hymns = getAllHymns(buchMode).toMutableList()
            hymns.add(Hymn.hymnPlaceholder)
            initList()
            onBackPressedCallback = object : OnBackPressedCallback(false) {
                override fun handleOnBackPressed() {
                    if (selecting) setSelecting(false)
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
        }
    }

    private fun initList() {
        selected = HashMap()
        for (i in hymns.indices) selected[i] = false
        listView.layoutManager = LinearLayoutManager(mContext)
        imageAdapter = ImageAdapter()
        listView.adapter = imageAdapter
        listView.itemAnimator = null
        listView.seslSetFastScrollerEnabled(true)
        listView.seslSetIndexTipEnabled(true)
        listView.seslSetFillBottomEnabled(true)
        listView.seslSetGoToTopEnabled(true)
        listView.seslSetLastRoundedCorner(false)
        listView.seslSetLongPressMultiSelectionListener(object :
            RecyclerView.SeslLongPressMultiSelectionListener {
            override fun onItemSelected(view: RecyclerView, child: View, position: Int, id: Long) {
                if (imageAdapter.getItemViewType(position) == 0) toggleItemSelected(position)
            }

            override fun onLongPressMultiSelectionStarted(x: Int, y: Int) {
                drawerLayout.showSelectModeBottomBar(false)
            }

            override fun onLongPressMultiSelectionEnded(x: Int, y: Int) {
                handler.postDelayed(showBottomBarRunnable, 300)
            }
        })
        val divider = TypedValue()
        val decoration = ItemDecoration()
        mContext.theme.resolveAttribute(android.R.attr.listDivider, divider, true)
        listView.addItemDecoration(decoration)
        decoration.setDivider(AppCompatResources.getDrawable(mContext, divider.resourceId)!!)
    }

    fun setSelecting(enabled: Boolean) {
        if (enabled) {
            selecting = true
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.itemCount - 1)
            drawerLayout.setSelectModeBottomMenu(R.menu.fav_menu) { item: MenuItem ->
                val onlySelected = HashMap(selected.filter { it.value })
                when (item.itemId) {
                    R.id.addToFav -> {
                        val dialog = ProgressDialog(mContext)
                        dialog.setProgressStyle(ProgressDialog.STYLE_CIRCLE)
                        dialog.setCancelable(false)
                        dialog.show()
                        coroutineScope.launch {
                            setFavoritesFromHymnList(hymns, onlySelected, true)
                        }.invokeOnCompletion {
                            dialog.dismiss()
                        }
                    }
                    R.id.removeFromFav -> {
                        val dialog = ProgressDialog(mContext)
                        dialog.setProgressStyle(ProgressDialog.STYLE_CIRCLE)
                        dialog.setCancelable(false)
                        dialog.show()
                        coroutineScope.launch {
                            setFavoritesFromHymnList(hymns, onlySelected, false)
                        }.invokeOnCompletion {
                            dialog.dismiss()
                        }
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
            drawerLayout.setSelectModeAllCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
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
            drawerLayout.showSelectModeBottomBar(false)
            subTabs.isEnabled = false
            mainTabs.isEnabled = false
            viewPager2List.isUserInputEnabled = false
            onBackPressedCallback.isEnabled = true
        } else {
            selecting = false
            handler.removeCallbacks(showBottomBarRunnable)
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
    inner class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>(), SectionIndexer {
        private var sections: MutableList<String> = mutableListOf()
        private var positionForSection: MutableList<Int> = mutableListOf()
        private var sectionForPosition: MutableList<Int> = mutableListOf()
        override fun getSections(): Array<Any> = sections.toTypedArray()
        override fun getPositionForSection(i: Int): Int = positionForSection[i]
        override fun getSectionForPosition(i: Int): Int = sectionForPosition[i]
        override fun getItemCount(): Int = hymns.size
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getItemViewType(position: Int): Int = if (hymns[position] != Hymn.hymnPlaceholder) 0 else 1
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            var resId = 0
            when (viewType) {
                0 -> resId = R.layout.listview_item
                1 -> resId = R.layout.listview_bottom_spacing
            }
            return ViewHolder(LayoutInflater.from(parent.context).inflate(resId, parent, false), viewType)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder.isItem) {
                holder.checkBox.visibility = if (selecting) View.VISIBLE else View.GONE
                holder.checkBox.isChecked = selected[position]!!
                holder.textView.text = hymns[position].numberAndTitle
                holder.parentView.setOnClickListener {
                    if (selecting) toggleItemSelected(position)
                    else {
                        startActivity(
                            Intent(rootView.context, TextviewActivity::class.java).putExtra("hymnId", hymns[position].hymnId.toInt())
                        )
                    }
                }
                holder.parentView.setOnLongClickListener {
                    if (!selecting) setSelecting(true)
                    toggleItemSelected(position)
                    listView.seslStartLongPressMultiSelection()
                    true
                }
            }
        }

        inner class ViewHolder internal constructor(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
            var isItem: Boolean = viewType == 0
            lateinit var parentView: RelativeLayout
            lateinit var textView: TextView
            lateinit var checkBox: CheckBox

            init {
                if (isItem) {
                    parentView = itemView as RelativeLayout
                    textView = parentView.findViewById(R.id.icon_tab_item_text)
                    checkBox = parentView.findViewById(R.id.checkbox)
                }
            }
        }

        init {
            for (i in hymns.indices) {
                if (i != hymns.size - 1) {
                    sections.add(hymns[i].hymnId.number.toString())
                    positionForSection.add(i)
                }
                sectionForPosition.add(sections.size - 1)
            }

        }
    }

    inner class ItemDecoration : RecyclerView.ItemDecoration() {
        private val seslRoundedCornerTop: SeslRoundedCorner = SeslRoundedCorner(requireContext(), true)
        private val seslRoundedCornerBottom: SeslRoundedCorner
        private var divider: Drawable? = null
        private var dividerHeight = 0
        override fun seslOnDispatchDraw(canvas: Canvas, recyclerView: RecyclerView, state: RecyclerView.State) {
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
            seslRoundedCornerBottom = SeslRoundedCorner(requireContext(), true)
            seslRoundedCornerBottom.roundedCorners = 12
        }
    }
}