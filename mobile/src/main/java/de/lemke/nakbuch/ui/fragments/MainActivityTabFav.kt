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
import de.dlyt.yanndroid.oneui.layout.DrawerLayout
import de.dlyt.yanndroid.oneui.menu.MenuItem
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager
import de.dlyt.yanndroid.oneui.sesl.utils.SeslRoundedCorner
import de.dlyt.yanndroid.oneui.view.RecyclerView
import de.dlyt.yanndroid.oneui.widget.SwipeRefreshLayout
import de.dlyt.yanndroid.oneui.widget.TabLayout
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.GetUserSettingsUseCase
import de.lemke.nakbuch.domain.hymndataUseCases.GetFavoriteHymnsUseCase
import de.lemke.nakbuch.domain.hymndataUseCases.SetFavoritesFromPersonalHymnListUseCase
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.PersonalHymn
import de.lemke.nakbuch.ui.TextviewActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class MainActivityTabFav : Fragment() {
    private val coroutineContext: CoroutineContext = Dispatchers.Main
    private val coroutineScope: CoroutineScope = CoroutineScope(coroutineContext)
    private lateinit var buchMode: BuchMode
    private lateinit var favHymns: MutableList<PersonalHymn>
    private lateinit var mRootView: View
    private lateinit var mContext: Context
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var listView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var mainTabs: TabLayout
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private lateinit var selected: HashMap<Int, Boolean>
    private var mSelecting = false
    private var checkAllListening = true
    private val mHandler = Handler(Looper.getMainLooper())
    private val mShowBottomBarRunnable = Runnable { drawerLayout.showSelectModeBottomBar(true) }

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase
    @Inject
    lateinit var getFavoriteHymns: GetFavoriteHymnsUseCase
    @Inject
    lateinit var setFavoritesFromList: SetFavoritesFromPersonalHymnListUseCase


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mRootView = inflater.inflate(R.layout.fragment_tab_fav, container, false)
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        drawerLayout = requireActivity().findViewById(R.id.drawer_view)
        swipeRefreshLayout = mRootView.findViewById(R.id.tabFavSwipeRefresh)
        swipeRefreshLayout.setOnRefreshListener {
            initList()
        }
        swipeRefreshLayout.isRefreshing = true
        listView = mRootView.findViewById(R.id.favHymnList)
        mainTabs = requireActivity().findViewById(R.id.main_tabs)
        onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                setSelecting(false)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    override fun onResume() {
        super.onResume()
        coroutineScope.launch {
            swipeRefreshLayout.isRefreshing = true
            buchMode = getUserSettings().buchMode
            favHymns = getFavoriteHymns(buchMode).toMutableList()
            favHymns.add(PersonalHymn.personalHymnPlaceholder)
            initList()
        }
    }

    private fun initList() {
        swipeRefreshLayout.isRefreshing = true
        selected = HashMap()
        for (i in favHymns.indices) selected[i] = false
        imageAdapter = ImageAdapter()
        listView.adapter = imageAdapter
        listView.layoutManager = LinearLayoutManager(mContext)
        listView.itemAnimator = null
        listView.seslSetFastScrollerEnabled(true)
        listView.seslSetFillBottomEnabled(true)
        listView.seslSetGoToTopEnabled(true)
        listView.seslSetLastRoundedCorner(false)
        listView.seslSetLongPressMultiSelectionListener(object :
            RecyclerView.SeslLongPressMultiSelectionListener {
            override fun onItemSelected(view: RecyclerView, child: View, position: Int, id: Long) {
                if (imageAdapter.getItemViewType(position) == 0) {
                    toggleItemSelected(position)
                }
            }

            override fun onLongPressMultiSelectionStarted(x: Int, y: Int) {
                drawerLayout.showSelectModeBottomBar(false)
            }

            override fun onLongPressMultiSelectionEnded(x: Int, y: Int) {
                mHandler.postDelayed(mShowBottomBarRunnable, 300)
            }
        })
        val divider = TypedValue()
        mContext.theme.resolveAttribute(android.R.attr.listDivider, divider, true)
        val decoration = ItemDecoration()
        listView.addItemDecoration(decoration)
        decoration.setDivider(AppCompatResources.getDrawable(mContext, divider.resourceId)!!)
        swipeRefreshLayout.isRefreshing = false
    }

    fun setSelecting(enabled: Boolean) {
        if (enabled) {
            mSelecting = true
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.itemCount - 1)
            drawerLayout.setSelectModeBottomMenu(R.menu.remove_menu) { item: MenuItem ->
                if (item.itemId == R.id.menuButtonRemove) {
                    coroutineScope.launch {
                        swipeRefreshLayout.isRefreshing = true
                        setFavoritesFromList(favHymns.filterIndexed {
                                index, personalHymn -> !selected[index]!! && personalHymn != PersonalHymn.personalHymnPlaceholder
                        }.map { it.copy(favorite = false) })
                        favHymns = favHymns.filterIndexed {
                                index, personalHymn -> selected[index]!! && personalHymn != PersonalHymn.personalHymnPlaceholder
                        }.toMutableList()
                        favHymns.add(PersonalHymn.personalHymnPlaceholder)
                        initList()
                    }
                    setSelecting(false)
                } else {
                    item.badge = item.badge + 1
                    Toast.makeText(mContext, item.title, Toast.LENGTH_SHORT).show()
                }
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
            mainTabs.isEnabled = false
            onBackPressedCallback.isEnabled = true
        } else {
            mSelecting = false
            for (i in 0 until imageAdapter.itemCount - 1) selected[i] = false
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.itemCount - 1)
            drawerLayout.setSelectModeCount(0)
            drawerLayout.dismissSelectMode()
            mainTabs.isEnabled = true
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
    inner class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
        override fun getItemCount(): Int {
            return favHymns.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemViewType(position: Int): Int {
            return if (favHymns[position] != PersonalHymn.personalHymnPlaceholder) 0
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

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder.isItem) {
                holder.checkBox.visibility = if (mSelecting) View.VISIBLE else View.GONE
                holder.checkBox.isChecked = selected[position]!!
                //holder.imageView.setImageResource(R.drawable.ic_samsung_audio);
                holder.textView.text = favHymns[position].hymn.numberAndTitle
                holder.parentView.setOnClickListener {
                    if (mSelecting) toggleItemSelected(position) else {
                        startActivity(
                            Intent(mRootView.context, TextviewActivity::class.java).putExtra(
                                "hymnId",
                                favHymns[position].hymn.hymnId.toInt()
                            )
                        )
                    }
                }
                holder.parentView.setOnLongClickListener {
                    if (!mSelecting) setSelecting(true)
                    toggleItemSelected(position)
                    listView.seslStartLongPressMultiSelection()
                    true
                }
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