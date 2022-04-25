package de.lemke.nakbuch.ui

import android.annotation.SuppressLint
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
import android.widget.RelativeLayout
import android.widget.SectionIndexer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.dlyt.yanndroid.oneui.layout.SwitchBarLayout
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager
import de.dlyt.yanndroid.oneui.sesl.utils.SeslRoundedCorner
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.dlyt.yanndroid.oneui.view.RecyclerView
import de.dlyt.yanndroid.oneui.widget.Switch
import de.dlyt.yanndroid.oneui.widget.SwitchBar
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.utils.AssetsHelper.validHymnr
import de.lemke.nakbuch.domain.utils.Constants

class HistorySwitchBarActivity : AppCompatActivity(), SwitchBar.OnSwitchChangeListener {
    private var historyList: ArrayList<HashMap<String, String>>? = null
    private lateinit var listView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var sp: SharedPreferences
    private lateinit var mContext: Context

    @SuppressLint("ApplySharedPref")
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtil(this)
        super.onCreate(savedInstanceState)
        mContext = this
        sp = getSharedPreferences(getString(R.string.preference_file_default), MODE_PRIVATE)
        setContentView(R.layout.activity_history)
        listView = findViewById(R.id.historyList)
        val switchBarLayout = findViewById<SwitchBarLayout>(R.id.switchbarlayout_history)
        switchBarLayout.switchBar.isChecked = switchBarDefaultStatus
        switchBarLayout.switchBar.addOnSwitchChangeListener(this)
        switchBarLayout.setNavigationButtonTooltip(getString(de.dlyt.yanndroid.oneui.R.string.sesl_navigate_up))
        switchBarLayout.setNavigationButtonOnClickListener { onBackPressed() }
        switchBarLayout.inflateToolbarMenu(R.menu.switchpreferencescreen_menu)
        switchBarLayout.setOnToolbarMenuItemClickListener {
            sp.edit().putString("historyList", null).commit()
            initList()
            true
        }
        if (sp.getBoolean("historyEnabled", true)) {
            listView.visibility = View.VISIBLE
        } else {
            listView.visibility = View.GONE
        }
        initList()
    }

    private val switchBarDefaultStatus: Boolean
        get() = sp.getBoolean("historyEnabled", true)

    override fun onSwitchChanged(switchCompat: Switch, z: Boolean) {
        sp.edit().putBoolean("historyEnabled", z).apply()
        listView.visibility = if (z) View.VISIBLE else View.GONE
    }

    public override fun onResume() {
        super.onResume()
        initList()
    }

    @SuppressLint("SetTextI18n")
    private fun initList() {
        historyList = Gson().fromJson(
            sp.getString("historyList", null),
            object : TypeToken<ArrayList<HashMap<String, String>>>() {}.type
        )
        if (historyList == null) historyList = ArrayList()
        historyList!!.add(HashMap())
        val divider = TypedValue()
        theme.resolveAttribute(android.R.attr.listDivider, divider, true)
        listView.layoutManager = LinearLayoutManager(this)
        imageAdapter = ImageAdapter()
        listView.adapter = imageAdapter
        val decoration = ItemDecoration()
        listView.addItemDecoration(decoration)
        AppCompatResources.getDrawable(this, divider.resourceId)?.let { decoration.setDivider(it) }
        listView.itemAnimator = null
        listView.seslSetFastScrollerEnabled(true)
        listView.seslSetFillBottomEnabled(true)
        listView.seslSetGoToTopEnabled(true)
        listView.seslSetLastRoundedCorner(false)
    }

    inner class ImageAdapter internal constructor() :
        RecyclerView.Adapter<ImageAdapter.ViewHolder>(), SectionIndexer {
        private var mSections: MutableList<String> = ArrayList()
        private var mPositionForSection: MutableList<Int> = ArrayList()
        private var mSectionForPosition: MutableList<Int> = ArrayList()
        override fun getSections(): Array<Any> {
            return mSections.toTypedArray()
        }

        override fun getPositionForSection(i: Int): Int {
            return mPositionForSection[i]
        }

        override fun getSectionForPosition(i: Int): Int {
            return mSectionForPosition[i]
        }

        override fun getItemCount(): Int {
            return historyList!!.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemViewType(position: Int): Int {
            return if (historyList!![position].containsKey("nrAndTitle")) {
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

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder.isItem) {
                //holder.imageView.setImageResource(R.drawable.ic_samsung_audio);
                holder.textView.text =
                    historyList!![position]["date"].toString() + ": (" + historyList!![position]["buchMode"] + ") " + historyList!![position]["nrAndTitle"]
                holder.parentView.setOnClickListener {
                    /*if (mSelecting) toggleItemSelected(position);
                    else {

                    }*/
                    val myIntent = Intent(mContext, TextviewActivity::class.java)
                    val hymnNr: Int = if (historyList!![position]["buchMode"] == "CB") {
                        myIntent.putExtra("buchMode", Constants.CHORBUCHMODE)
                        validHymnr(Constants.CHORBUCHMODE, historyList!![position]["nr"]!!)
                    } else {
                        myIntent.putExtra("buchMode", Constants.GESANGBUCHMODE)
                        validHymnr(Constants.GESANGBUCHMODE, historyList!![position]["nr"]!!)
                    }
                    if (hymnNr > 0) {
                        myIntent.putExtra("nr", hymnNr)
                        startActivity(myIntent)
                    }
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

            init {
                if (isItem) {
                    parentView = itemView as RelativeLayout
                    //imageView = parentView.findViewById(R.id.icon_tab_item_image);
                    textView = parentView.findViewById(R.id.icon_tab_item_text)
                }
            }
        }

        init {
            if (historyList!!.size > 1) {
                for (i in historyList!!.indices) {
                    val letter: String = if (i != historyList!!.size - 1) ({
                        historyList!![i]["date"]
                    }).toString() else {
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