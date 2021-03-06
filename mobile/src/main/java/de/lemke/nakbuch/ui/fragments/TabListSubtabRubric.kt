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
import android.view.ViewGroup.MarginLayoutParams
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.util.SeslRoundedCorner
import androidx.appcompat.util.SeslSubheaderRoundedCorner
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.GetUserSettingsUseCase
import de.lemke.nakbuch.domain.hymnUseCases.GetAllRubricsUseCase
import de.lemke.nakbuch.domain.hymnUseCases.GetHymnsWithRubricUseCase
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.Rubric
import de.lemke.nakbuch.ui.TextviewActivity
import dev.oneuiproject.oneui.layout.DrawerLayout
import dev.oneuiproject.oneui.widget.MarginsTabLayout
import dev.oneuiproject.oneui.widget.Separator
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class TabListSubtabRubric : Fragment() {
    private var currentRubric: Rubric? = null
    private lateinit var rootView: View
    private lateinit var hymns: MutableList<Hymn?>
    private lateinit var rubrics: MutableList<Rubric>
    private lateinit var buchMode: BuchMode
    private lateinit var listView: RecyclerView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var subTabs: MarginsTabLayout
    private lateinit var mainTabs: MarginsTabLayout
    private lateinit var viewPager2List: ViewPager2
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var getAllRubrics: GetAllRubricsUseCase

    @Inject
    lateinit var getHymnsWithRubric: GetHymnsWithRubricUseCase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_tab_list_subtab_rubric, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        listView = rootView.findViewById(R.id.hymnListRubric)
        drawerLayout = activity.findViewById(R.id.drawer_view)
        subTabs = activity.findViewById(R.id.sub_tabs)
        mainTabs = activity.findViewById(R.id.main_tabs)
        viewPager2List = activity.findViewById(R.id.viewPager2Lists)
        lifecycleScope.launch {
            buchMode = getUserSettings().buchMode
            onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    currentRubric = null
                    lifecycleScope.launch { initList() }
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
            initList()
        }
    }

    private suspend fun initList() {
        rubrics = getAllRubrics(buchMode).toMutableList()
        hymns = mutableListOf()
        onBackPressedCallback.isEnabled = false
        if (currentRubric != null) {
            hymns = getHymnsWithRubric(currentRubric!!).toMutableList()
            hymns.add(0, null)
            onBackPressedCallback.isEnabled = true
        }
        imageAdapter = ImageAdapter()
        listView.adapter = imageAdapter
        listView.layoutManager = LinearLayoutManager(context)
        listView.addItemDecoration(ItemDecoration(requireContext()))
        listView.itemAnimator = null
        //listView.seslSetIndexTipEnabled(rubrikIndex == 0) //see ImageAdapter why
        listView.seslSetFastScrollerEnabled(true)
        listView.seslSetFillBottomEnabled(true)
        listView.seslSetGoToTopEnabled(true)
        listView.seslSetLastRoundedCorner(true)
    }

    //Adapter for the Icon RecyclerView
    inner class ImageAdapter :
        RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

        override fun getItemCount(): Int = if (currentRubric == null) rubrics.size else hymns.size

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getItemViewType(position: Int): Int {
            return if (currentRubric == null) {
                when {
                    rubrics[position].isMain -> 1
                    else -> 0
                }
            } else {
                when (position) {
                    0 -> 2
                    else -> 0
                }
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return when (viewType) {
                0 -> ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.listview_item, parent, false), viewType)
                1 -> ViewHolder(Separator(requireContext()), viewType)
                2 -> ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.listview_back_header, parent, false), viewType)
                else -> ViewHolder(Separator(requireContext()), viewType)
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder.isItem) {
                if (currentRubric != null) {
                    holder.textView.text = hymns[position]?.numberAndTitle
                    holder.parentView.setOnClickListener {
                        startActivity(
                            Intent(rootView.context, TextviewActivity::class.java).putExtra("hymnId", hymns[position]!!.hymnId.toInt())
                        )
                    }
                } else {
                    holder.textView.text = rubrics[position].name
                    holder.parentView.setOnClickListener {
                        currentRubric = rubrics[position]
                        lifecycleScope.launch { initList() }
                    }
                }
            }
            if (holder.isSeparator) {
                holder.textView.layoutParams =
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                holder.textView.text = rubrics[position].name
            }
            if (holder.isBackHeader) {
                holder.textView.text = currentRubric!!.name
                holder.parentView.setOnClickListener {
                    currentRubric = null
                    lifecycleScope.launch { initList() }
                }
            }
        }

        inner class ViewHolder internal constructor(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
            var isItem: Boolean = viewType == 0
            var isSeparator: Boolean = viewType == 1
            var isBackHeader: Boolean = viewType == 2
            lateinit var parentView: RelativeLayout
            lateinit var textView: TextView

            init {
                when {
                    isItem -> {
                        parentView = itemView as RelativeLayout
                        textView = parentView.findViewById(R.id.icon_tab_item_text)
                    }
                    isSeparator -> {
                        textView = itemView as TextView
                    }
                    isBackHeader -> {
                        parentView = itemView as RelativeLayout
                        textView = parentView.findViewById(R.id.icon_tab_item_text)
                    }
                }
            }
        }
    }

    inner class ItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
        private val divider: Drawable?
        private val roundedCorner: SeslSubheaderRoundedCorner
        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDraw(c, parent, state)
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                val holder: ImageAdapter.ViewHolder = listView.getChildViewHolder(child) as ImageAdapter.ViewHolder
                if (holder.isItem) {
                    val top = (child.bottom + (child.layoutParams as MarginLayoutParams).bottomMargin)
                    val bottom = divider!!.intrinsicHeight + top
                    divider.setBounds(parent.left, top, parent.right, bottom)
                    divider.draw(c)
                }
            }
        }

        override fun seslOnDispatchDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                val holder: ImageAdapter.ViewHolder = listView.getChildViewHolder(child) as ImageAdapter.ViewHolder
                if (!holder.isItem) roundedCorner.drawRoundedCorner(child, c)
            }
        }

        init {
            val outValue = TypedValue()
            context.theme.resolveAttribute(androidx.appcompat.R.attr.isLightTheme, outValue, true)
            divider = context.getDrawable(
                if (outValue.data == 0) androidx.appcompat.R.drawable.sesl_list_divider_dark
                else androidx.appcompat.R.drawable.sesl_list_divider_light
            )!!
            roundedCorner = SeslSubheaderRoundedCorner(context)
            roundedCorner.roundedCorners = SeslRoundedCorner.ROUNDED_CORNER_ALL
        }
    }
}