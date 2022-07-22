package de.lemke.nakbuch.ui.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.indexscroll.widget.SeslArrayIndexer
import androidx.indexscroll.widget.SeslIndexScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.GetUserSettingsUseCase
import de.lemke.nakbuch.domain.hymnUseCases.GetAllHymnsSortedAlphabeticalUseCase
import de.lemke.nakbuch.domain.hymndataUseCases.SetFavoritesFromHymnListUseCase
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.ui.TextviewActivity
import dev.oneuiproject.oneui.dialog.ProgressDialog
import dev.oneuiproject.oneui.layout.DrawerLayout
import dev.oneuiproject.oneui.widget.MarginsTabLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class TabListSubtabAlphabetic : Fragment() {
    private lateinit var rootView: View
    private lateinit var buchMode: BuchMode
    private lateinit var hymnsAlphsort: MutableList<Hymn>
    private lateinit var listView: RecyclerView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var subTabs: MarginsTabLayout
    private lateinit var mainTabs: MarginsTabLayout
    private lateinit var viewPager2List: ViewPager2
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private lateinit var showBottomBarJob: Job
    private var currentSectionIndex = 0
    private var selected = HashMap<Int, Boolean>()
    private var selecting = false
    private var checkAllListening = true


    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var getAllHymnsSortedAlphabetical: GetAllHymnsSortedAlphabeticalUseCase

    @Inject
    lateinit var setFavoritesFromHymnList: SetFavoritesFromHymnListUseCase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_tab_list_subtab_alphabetic, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        listView = rootView.findViewById(R.id.hymnListAlphabetical)
        drawerLayout = activity.findViewById(R.id.drawer_view)
        subTabs = activity.findViewById(R.id.sub_tabs)
        mainTabs = activity.findViewById(R.id.main_tabs)
        viewPager2List = activity.findViewById(R.id.viewPager2Lists)
        lifecycleScope.launch {
            buchMode = getUserSettings().buchMode
            hymnsAlphsort = getAllHymnsSortedAlphabetical(buchMode).toMutableList()
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
        hymnsAlphsort.indices.forEach { i -> selected[i] = false }
        listView.layoutManager = LinearLayoutManager(context)
        imageAdapter = ImageAdapter()
        listView.adapter = imageAdapter
        listView.itemAnimator = null
        listView.seslSetIndexTipEnabled(true)
        listView.seslSetFillBottomEnabled(true)
        listView.seslSetGoToTopEnabled(true)
        listView.seslSetLastRoundedCorner(true)
        listView.seslSetLongPressMultiSelectionListener(object :
            RecyclerView.SeslLongPressMultiSelectionListener {
            override fun onItemSelected(view: RecyclerView, child: View, position: Int, id: Long) {
                if (imageAdapter.getItemViewType(position) == 0) toggleItemSelected(position)
            }

            override fun onLongPressMultiSelectionStarted(x: Int, y: Int) {
                //drawerLayout.showSelectModeBottomBar(false)
            }

            override fun onLongPressMultiSelectionEnded(x: Int, y: Int) {
                showBottomBarJob = lifecycleScope.launch {
                    //delay(300)
                    //drawerLayout.showSelectModeBottomBar(true)
                }
            }
        })
        listView.addItemDecoration(ItemDecoration(requireContext()))
        val indexScrollView: SeslIndexScrollView = rootView.findViewById(R.id.indexScrollViewAlphabetical)
        val list: List<String> = hymnsAlphsort.map { it.title }
        val isRtl = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
        indexScrollView.setIndexBarGravity(if (isRtl) SeslIndexScrollView.GRAVITY_INDEX_BAR_LEFT else SeslIndexScrollView.GRAVITY_INDEX_BAR_RIGHT)
        val indexer = SeslArrayIndexer(list, "ABCDEFGHIJKLMNOPQRSTUVWXYZÜ")
        indexScrollView.setIndexer(indexer)
        indexScrollView.setIndexBarTextMode(true)
        indexScrollView.setOnIndexBarEventListener(
            object : SeslIndexScrollView.OnIndexBarEventListener {
                override fun onIndexChanged(sectionIndex: Int) {
                    if (currentSectionIndex != sectionIndex) {
                        currentSectionIndex = sectionIndex
                        if (listView.scrollState != RecyclerView.SCROLL_STATE_IDLE) listView.stopScroll()
                        (listView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(sectionIndex, 0)
                    }
                }

                override fun onPressed(v: Float) {
                    subTabs.isEnabled = false
                    viewPager2List.isUserInputEnabled = false
                }
                override fun onReleased(v: Float) {
                    subTabs.isEnabled = true
                    viewPager2List.isUserInputEnabled = true
                }
            })
        indexScrollView.attachToRecyclerView(listView)
    }

    fun setSelecting(enabled: Boolean) {
        if (enabled) {
            selecting = true
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.itemCount)
            drawerLayout.setActionModeBottomMenu(R.menu.fav_menu)
            drawerLayout.setActionModeBottomMenuListener { item: MenuItem ->
                val onlySelected = HashMap(selected.filter { it.value })
                when (item.itemId) {
                    R.id.addToFav -> {
                        val dialog = ProgressDialog(context)
                        dialog.setProgressStyle(ProgressDialog.STYLE_CIRCLE)
                        dialog.setCancelable(false)
                        dialog.show()
                        lifecycleScope.launch {
                            setFavoritesFromHymnList(hymnsAlphsort, onlySelected, true)
                        }.invokeOnCompletion { dialog.dismiss() }
                    }
                    R.id.removeFromFav -> {
                        val dialog = ProgressDialog(context)
                        dialog.setProgressStyle(ProgressDialog.STYLE_CIRCLE)
                        dialog.setCancelable(false)
                        dialog.show()
                        lifecycleScope.launch {
                            setFavoritesFromHymnList(hymnsAlphsort, onlySelected, false)
                        }.invokeOnCompletion { dialog.dismiss() }
                    }
                    else -> {
                        Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                    }
                }
                setSelecting(false)
                true
            }
            drawerLayout.showActionMode()
            drawerLayout.setActionModeCheckboxListener { _, isChecked ->
                if (checkAllListening) {
                    selected.replaceAll { _, _ -> isChecked }
                    selected.forEach { (index, _) -> imageAdapter.notifyItemChanged(index) }
                }
                drawerLayout.setActionModeCount(selected.values.count { it }, imageAdapter.itemCount)
            }
            //drawerLayout.showSelectModeBottomBar(false)
            subTabs.isEnabled = false
            mainTabs.isEnabled = false
            viewPager2List.isUserInputEnabled = false
            onBackPressedCallback.isEnabled = true
        } else {
            selecting = false
            showBottomBarJob.cancel()
            selected.replaceAll { _, _ -> false }
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.itemCount)
            drawerLayout.setActionModeCount(0, imageAdapter.itemCount)
            drawerLayout.dismissActionMode()
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
        drawerLayout.setActionModeCount(selected.values.count { it }, imageAdapter.itemCount)
        checkAllListening = true
    }

    //Adapter for the Icon RecyclerView
    inner class ImageAdapter internal constructor() :
        RecyclerView.Adapter<ImageAdapter.ViewHolder>(), SectionIndexer {
        private var sections: MutableList<String> = mutableListOf()
        private var positionForSection: MutableList<Int> = mutableListOf()
        private var sectionForPosition: MutableList<Int> = mutableListOf()
        override fun getSections(): Array<Any> = sections.toTypedArray()
        override fun getPositionForSection(i: Int): Int = positionForSection[i]
        override fun getSectionForPosition(i: Int): Int = sectionForPosition[i]
        override fun getItemCount(): Int = hymnsAlphsort.size
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getItemViewType(position: Int): Int = 0
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            var resId = 0
            when (viewType) {
                0 -> resId = R.layout.listview_item
                //1 -> resId = R.layout.listview_bottom_spacing
            }
            return ViewHolder(LayoutInflater.from(parent.context).inflate(resId, parent, false), viewType)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder.isItem) {
                holder.checkBox.visibility = if (selecting) View.VISIBLE else View.GONE
                holder.checkBox.isChecked = selected[position]!!
                holder.textView.text = hymnsAlphsort[position].numberAndTitle
                holder.parentView.setOnClickListener {
                    if (selecting) toggleItemSelected(position) else {
                        startActivity(
                            Intent(rootView.context, TextviewActivity::class.java).putExtra(
                                "hymnId",
                                hymnsAlphsort[position].hymnId.toInt()
                            )
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
            for (i in hymnsAlphsort.indices) {
                val letter: String = hymnsAlphsort[i].title.substring(0, 1).uppercase(Locale.getDefault())
                if (i == 0 || sections[sections.size - 1] != letter) {
                    sections.add(letter)
                    positionForSection.add(i)
                }
                sectionForPosition.add(sections.size - 1)
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