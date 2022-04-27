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
import android.widget.CompoundButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import de.dlyt.yanndroid.oneui.layout.DrawerLayout
import de.dlyt.yanndroid.oneui.menu.MenuItem
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager
import de.dlyt.yanndroid.oneui.sesl.utils.SeslRoundedCorner
import de.dlyt.yanndroid.oneui.view.RecyclerView
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.utils.AssetsHelper.getHymnArrayList
import de.lemke.nakbuch.domain.utils.HymnPrefsHelper.writeFavsToList
import de.lemke.nakbuch.domain.utils.PartyUtils.Companion.discoverEasterEgg
import de.lemke.nakbuch.domain.utils.TextHelper
import de.lemke.nakbuch.ui.TextviewActivity
import nl.dionsegijn.konfetti.xml.KonfettiView

class MainActivitySearchFragment : Fragment() {
    private lateinit var hymns: ArrayList<HashMap<String, String>>
    var searchList: ArrayList<HashMap<String, String>> = ArrayList()
    private lateinit var mRootView: View
    private lateinit var mContext: Context
    private lateinit var mActivity: AppCompatActivity
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var listView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var konfettiView: KonfettiView
    private var selected = HashMap<Int, Boolean>()
    private var mSelecting = false
    private var checkAllListening = true
    private lateinit var buchMode: BuchMode
    private lateinit var sp: SharedPreferences
    private lateinit var spHymns: SharedPreferences
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = activity as AppCompatActivity
        sp = mContext.getSharedPreferences(
            getString(R.string.preferenceFileDefault),
            Context.MODE_PRIVATE
        )
        spHymns = mContext.getSharedPreferences(
            getString(R.string.preferenceFileHymns),
            Context.MODE_PRIVATE
        )
        buchMode = if (sp.getBoolean("gesangbuchSelected", true)) BuchMode.Gesangbuch else BuchMode.Chorbuch
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mRootView = inflater.inflate(R.layout.fragment_search, container, false)
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = mRootView.findViewById(R.id.searchList)
        drawerLayout = requireActivity().findViewById(R.id.drawer_view)
        konfettiView = mActivity.findViewById(R.id.konfettiViewTabSearch)
        initAssets()
        initList()
    }

    private fun setSearchList(search: String) {
        if (sp.getBoolean("easterEggs", true)) {
            if (search.replace(" ", "").equals("easteregg", ignoreCase = true)) {
                discoverEasterEgg(mContext, konfettiView, R.string.easterEggEntrySearch)
            }
        }
        searchList = ArrayList()
        if (search.isNotEmpty()) {
            if (search.startsWith("\"") && search.endsWith("\"")) {
                if (search.length > 2) {
                    val s = search.substring(1, search.length - 1)
                    if (sp.getBoolean("searchAlternativeMode", false)) {
                        addToSearchWithKeywords(HashSet(listOf(s)))
                    } else {
                        addToSearchWithKeywords(HashSet(s.trim().split(" ")))
                    }
                }
            } else {
                if (sp.getBoolean("searchAlternativeMode", false)) {
                    addToSearchWithKeywords(HashSet(search.trim().split(" ")))
                } else {
                    addToSearchWithKeywords(HashSet(listOf(search)))
                }
            }
        }
        searchList.add(HashMap()) //Placeholder
    }

    private fun addToSearchWithKeywords(searchs: HashSet<String>) {
        for (s in searchs) {
            if (s.isNotBlank()) {
                for (hm in hymns) {
                    if (hm["hymnText"]!!.contains(s, ignoreCase = true) ||
                        hm["hymnNrAndTitle"]!!.contains(s, ignoreCase = true) ||
                        hm["hymnCopyright"]!!.contains(s, ignoreCase = true)
                    ) {
                        searchList.add(hm)
                    }
                }
            }
        }
    }

    private fun initList() {
        val search = sp.getString("search", "")!!
        setSearchList(search)
        selected = HashMap()
        for (i in searchList.indices) selected[i] = false
        val divider = TypedValue()
        mContext.theme.resolveAttribute(android.R.attr.listDivider, divider, true)
        listView.layoutManager = LinearLayoutManager(mContext)
        imageAdapter = ImageAdapter()
        listView.adapter = imageAdapter
        val decoration = ItemDecoration()
        listView.addItemDecoration(decoration)
        decoration.setDivider(AppCompatResources.getDrawable(mContext, divider.resourceId)!!)
        listView.itemAnimator = null
        listView.seslSetFastScrollerEnabled(true)
        listView.seslSetFillBottomEnabled(true)
        listView.seslSetGoToTopEnabled(true)
        listView.seslSetLastRoundedCorner(false)
    }

    private fun setSelecting(enabled: Boolean) { //TODO selecting?
        if (enabled) {
            mSelecting = true
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.itemCount - 1)
            drawerLayout.setSelectModeBottomMenu(R.menu.fav_menu) { item: MenuItem ->
                when (item.itemId) {
                    R.id.addToFav -> {
                        writeFavsToList(buchMode == BuchMode.Gesangbuch, spHymns, selected, searchList, "1")
                    }
                    R.id.removeFromFav -> {
                        writeFavsToList(buchMode == BuchMode.Gesangbuch, spHymns, selected, searchList, "")
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
        } else {
            mSelecting = false
            for (i in 0 until imageAdapter.itemCount - 1) selected[i] = false
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.itemCount - 1)
            drawerLayout.setSelectModeCount(0)
            drawerLayout.dismissSelectMode()
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
        hymns = getHymnArrayList(mContext, sp, buchMode == BuchMode.Gesangbuch)
    }

    //Adapter for the Icon RecyclerView
    inner class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
        override fun getItemCount(): Int {
            return searchList.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemViewType(position: Int): Int {
            return if (searchList[position].containsKey("hymnNr")) {
                0
            } else 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            var resId = 0
            when (viewType) {
                0 -> resId = R.layout.search_tab_listview_item
                1 -> resId = R.layout.listview_bottom_spacing
            }
            val view = LayoutInflater.from(parent.context).inflate(resId, parent, false)
            return ViewHolder(view, viewType)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder.isItem) {
                //holder.checkBox.setVisibility(mSelecting ? View.VISIBLE : View.GONE);
                //holder.checkBox.setChecked(selected.get(position));

                //holder.imageView.setImageResource(R.drawable.ic_samsung_audio);
                val search = sp.getString("search", "")!!
                val hymn = searchList[position]
                holder.textView.text = TextHelper.makeSectionOfTextBold(mContext, sp, hymn["hymnNrAndTitle"]!!, search, -1)
                holder.textViewDescription.text = TextHelper.makeSectionOfTextBold(mContext, sp, hymn["hymnText"]?.replace("</p><p>", " ")?.replace("<br>", "") ?: "", search, 20)
                holder.textViewCopyright.text = TextHelper.makeSectionOfTextBold(mContext, sp, hymn["hymnCopyright"]?.replace("<br>", "") ?: "", search, 5)

                holder.parentView.setOnClickListener {
                    if (mSelecting) toggleItemSelected(position) else {
                        if (hymn.containsKey("hymnNr")) {
                            startActivity(
                                Intent(mRootView.context, TextviewActivity::class.java)
                                    .putExtra("nr", hymn["hymnNr"]?.toInt() ?: -1)
                                    .putExtra("boldText", search)
                            )
                        }
                    }
                }
                /*holder.parentView.setOnLongClickListener(v -> {
                    if (!mSelecting) setSelecting(true);
                    toggleItemSelected(position);

                    listView.seslStartLongPressMultiSelection();
                    listView.seslSetLongPressMultiSelectionListener(new RecyclerView.SeslLongPressMultiSelectionListener() {
                        @Override
                        public void onItemSelected(RecyclerView var1, View var2, int var3, long var4) {
                            if (getItemViewType(var3) == 0) toggleItemSelected(var3);
                        }

                        @Override
                        public void onLongPressMultiSelectionEnded(int var1, int var2) {

                        }

                        @Override
                        public void onLongPressMultiSelectionStarted(int var1, int var2) {

                        }
                    });
                    return true;
                });*/
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
            lateinit var textViewDescription: TextView
            lateinit var textViewCopyright: TextView

            //CheckBox checkBox;
            init {
                if (isItem) {
                    parentView = itemView as RelativeLayout
                    //imageView = parentView.findViewById(R.id.icon_tab_item_image);
                    textView = parentView.findViewById(R.id.search_tab_item_text)
                    textViewDescription =
                        parentView.findViewById(R.id.search_tab_item_description)
                    textViewCopyright = parentView.findViewById(R.id.search_tab_item_copyright)
                    //checkBox = parentView.findViewById(R.id.checkbox);
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
            mSeslRoundedCornerBottom = SeslRoundedCorner(mContext, true)
            mSeslRoundedCornerBottom.roundedCorners = 12
        }
    }
}