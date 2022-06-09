package de.lemke.nakbuch.ui.fragments

import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager
import de.dlyt.yanndroid.oneui.sesl.utils.SeslRoundedCorner
import de.dlyt.yanndroid.oneui.view.RecyclerView
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.GetUserSettingsUseCase
import de.lemke.nakbuch.domain.MakeSectionOfTextBoldUseCase
import de.lemke.nakbuch.domain.hymnUseCases.GetSearchListUseCase
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.ui.TextviewActivity
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivitySearchFragment : Fragment() {
    private lateinit var searchList: MutableList<Hymn>
    private lateinit var rootView: View
    private lateinit var listView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var buchMode: BuchMode
    private lateinit var search: String

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var getSearchList: GetSearchListUseCase

    @Inject
    lateinit var makeSectionOfTextBold: MakeSectionOfTextBoldUseCase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_search, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = rootView.findViewById(R.id.searchList)
        lifecycleScope.launch {
            buchMode = getUserSettings().buchMode
            search = getUserSettings().search
            initList()
        }
    }

    private suspend fun initList() {
        searchList = getSearchList(buchMode, search).toMutableList()
        searchList.add(Hymn.hymnPlaceholder)
        imageAdapter = ImageAdapter()
        listView.adapter = imageAdapter
        val divider = TypedValue()
        context!!.theme.resolveAttribute(android.R.attr.listDivider, divider, true)
        listView.layoutManager = LinearLayoutManager(context)
        val decoration = ItemDecoration()
        listView.addItemDecoration(decoration)
        decoration.setDivider(AppCompatResources.getDrawable(context!!, divider.resourceId)!!)
        listView.itemAnimator = null
        listView.seslSetFastScrollerEnabled(true)
        listView.seslSetFillBottomEnabled(true)
        listView.seslSetGoToTopEnabled(true)
        listView.seslSetLastRoundedCorner(false)
    }

    //Adapter for the Icon RecyclerView
    inner class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
        override fun getItemCount(): Int = searchList.size

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getItemViewType(position: Int): Int = if (searchList[position] != Hymn.hymnPlaceholder) 0 else 1

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
                val hymn = searchList[position]
                val color = MaterialColors.getColor(
                    context!!, de.dlyt.yanndroid.oneui.R.attr.colorPrimary,
                    context!!.resources.getColor(R.color.primary_color, context!!.theme)
                )
                lifecycleScope.launch {
                    val alternativeSearchModeEnabled = getUserSettings().alternativeSearchModeEnabled
                    holder.textView.text = makeSectionOfTextBold(hymn.numberAndTitle, search, color, -1, alternativeSearchModeEnabled)
                    holder.textViewDescription.text =
                        makeSectionOfTextBold(hymn.text.replace("\n", "  "), search, color, 20, alternativeSearchModeEnabled)
                    holder.textViewCopyright.text = makeSectionOfTextBold(hymn.copyright, search, color, 5, alternativeSearchModeEnabled)
                }
                holder.parentView.setOnClickListener {
                    startActivity(
                        Intent(rootView.context, TextviewActivity::class.java)
                            .putExtra("hymnId", hymn.hymnId.toInt())
                            .putExtra("boldText", search)
                    )
                }
            }
        }

        inner class ViewHolder internal constructor(itemView: View, viewType: Int) :
            RecyclerView.ViewHolder(itemView) {
            var isItem: Boolean = viewType == 0
            lateinit var parentView: RelativeLayout
            lateinit var textView: TextView
            lateinit var textViewDescription: TextView
            lateinit var textViewCopyright: TextView

            init {
                if (isItem) {
                    parentView = itemView as RelativeLayout
                    textView = parentView.findViewById(R.id.search_tab_item_text)
                    textViewDescription = parentView.findViewById(R.id.search_tab_item_description)
                    textViewCopyright = parentView.findViewById(R.id.search_tab_item_copyright)
                }
            }
        }
    }

    inner class ItemDecoration : RecyclerView.ItemDecoration() {
        private val mSeslRoundedCornerTop: SeslRoundedCorner = SeslRoundedCorner(context, true)
        private val mSeslRoundedCornerBottom: SeslRoundedCorner
        private var mDivider: Drawable? = null
        private var mDividerHeight = 0
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
            mSeslRoundedCornerBottom = SeslRoundedCorner(context, true)
            mSeslRoundedCornerBottom.roundedCorners = 12
        }
    }
}