package de.lemke.nakbuch.fragments

import de.lemke.nakbuch.utils.HymnPrefsHelper.writeFavsToList
import de.lemke.nakbuch.utils.HymnPrefsHelper.getFavList
import android.view.View
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import de.lemke.nakbuch.R
import java.lang.Runnable
import android.util.TypedValue
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import android.graphics.Canvas
import androidx.activity.OnBackPressedCallback
import android.content.Intent
import android.os.Handler
import android.widget.*
import androidx.fragment.app.Fragment
import de.dlyt.yanndroid.oneui.layout.DrawerLayout
import de.dlyt.yanndroid.oneui.menu.MenuItem
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager
import de.dlyt.yanndroid.oneui.sesl.utils.SeslRoundedCorner
import de.dlyt.yanndroid.oneui.view.RecyclerView
import de.dlyt.yanndroid.oneui.widget.TabLayout
import java.util.*

class MainActivityTabFav : Fragment() {
    //private ArrayList<HashMap<String, String>> hymns;
    private var gesangbuchSelected = false
    private lateinit var favHymns: ArrayList<HashMap<String, String>>
    private lateinit var mRootView: View
    private lateinit var mContext: Context
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var listView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var mainTabs: TabLayout
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private lateinit var selected: HashMap<Int, Boolean>
    private var mSelecting = false
    private var checkAllListening = true
    private val mHandler = Handler()
    private val mShowBottomBarRunnable = Runnable { drawerLayout.showSelectModeBottomBar(true) }
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
        mRootView = inflater.inflate(R.layout.fragment_tab_fav, container, false)
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
        drawerLayout = requireActivity().findViewById(R.id.drawer_view)
        listView = mRootView.findViewById(R.id.favHymnList)
        mainTabs = requireActivity().findViewById(R.id.main_tabs)
        gesangbuchSelected = sp.getBoolean("gesangbuchSelected", true)

        //initAssets();
        //setFavLists();
        initList()
        onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                setSelecting(false)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    override fun onResume() {
        super.onResume()
        //setFavLists();
        initList()
    }

    private fun initList() {
        favHymns = getFavList(mContext, gesangbuchSelected, sp, spHymns)
        favHymns.add(HashMap()) //Placeholder
        selected = HashMap()
        for (i in favHymns.indices) selected[i] = false
        listView.layoutManager = LinearLayoutManager(mContext)
        imageAdapter = ImageAdapter()
        listView.adapter = imageAdapter
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

        //divider
        val divider = TypedValue()
        mContext.theme.resolveAttribute(android.R.attr.listDivider, divider, true)
        val decoration = ItemDecoration()
        listView.addItemDecoration(decoration)
        AppCompatResources.getDrawable(mContext, divider.resourceId)
            ?.let { decoration.setDivider(it) }

        //select mode dismiss on back
        onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                setSelecting(false)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    fun setSelecting(enabled: Boolean) {
        if (enabled) {
            mSelecting = true
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.itemCount - 1)
            drawerLayout.setSelectModeBottomMenu(R.menu.remove_menu) { item: MenuItem ->
                if (item.itemId == R.id.menuButtonRemove) {
                    writeFavsToList(gesangbuchSelected, spHymns, selected, favHymns, "")
                    setSelecting(false)
                    initList()
                } else {
                    item.badge = item.badge + 1
                    Toast.makeText(mContext, item.title, Toast.LENGTH_SHORT).show()
                }
                true
            }
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

    /*private void initAssets() {
        hymns = getHymnArrayList(mContext, gesangbuchSelected ? getString(R.string.filename_hymnsGesangbuch) : getString(R.string.filename_hymnsChorbuch), sp);
    }

    private void setFavLists() {
        favHymns = new ArrayList<>();
        for (int i = 0; i < hymns.size(); i++) {
            if (getFromList(gesangbuchSelected, spHymns, i + 1, "fav").equals("1")) {
                favHymns.add(hymns.get(i));
            }
        }
        favHymns.add(new HashMap<>()); //Placeholder
    }*/
    //Adapter for the Icon RecyclerView
    inner class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
        override fun getItemCount(): Int {
            return favHymns.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemViewType(position: Int): Int {
            return if (favHymns[position].containsKey("hymnNr")) {
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
                holder.textView.text = favHymns[position]["hymnNrAndTitle"]
                holder.parentView.setOnClickListener {
                    if (mSelecting) toggleItemSelected(position) else {
                        /* TODO startActivity(
                            Intent(mRootView.context, TextviewActivity::class.java)
                                .putExtra(
                                    "nr",
                                    favHymns[position]["hymnNr"]?.toInt() ?: -1
                                )
                        )*/
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