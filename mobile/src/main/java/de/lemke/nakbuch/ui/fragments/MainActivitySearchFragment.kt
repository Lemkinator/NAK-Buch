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
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
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
        imageAdapter = ImageAdapter()
        listView.adapter = imageAdapter
        listView.layoutManager = LinearLayoutManager(context)
        listView.addItemDecoration(ItemDecoration(requireContext()))
        listView.itemAnimator = null
        listView.seslSetFastScrollerEnabled(true)
        listView.seslSetFillBottomEnabled(true)
        listView.seslSetGoToTopEnabled(true)
        listView.seslSetLastRoundedCorner(true)
    }

    //Adapter for the Icon RecyclerView
    inner class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
        override fun getItemCount(): Int = searchList.size

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getItemViewType(position: Int): Int = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            var resId = 0
            when (viewType) {
                0 -> resId = R.layout.search_tab_listview_item
                //1 -> resId = R.layout.listview_bottom_spacing
            }
            val view = LayoutInflater.from(parent.context).inflate(resId, parent, false)
            return ViewHolder(view, viewType)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder.isItem) {
                val hymn = searchList[position]
                val color = MaterialColors.getColor(
                    requireContext(), androidx.appcompat.R.attr.colorPrimary,
                    requireContext().resources.getColor(R.color.primary_color, context?.theme)
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

    private class ItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
        private val divider: Drawable
        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDraw(c, parent, state)
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                val top = (child.bottom + (child.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin)
                val bottom = divider.intrinsicHeight + top
                divider.setBounds(parent.left, top, parent.right, bottom)
                divider.draw(c)
            }
        }

        init {
            val outValue = TypedValue()
            context.theme.resolveAttribute(androidx.appcompat.R.attr.isLightTheme, outValue, true)
            divider = context.getDrawable(
                if (outValue.data == 0) androidx.appcompat.R.drawable.sesl_list_divider_dark
                else androidx.appcompat.R.drawable.sesl_list_divider_light
            )!!
        }
    }
}