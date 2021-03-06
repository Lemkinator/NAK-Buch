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
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.GetUserSettingsUseCase
import de.lemke.nakbuch.domain.hymndataUseCases.GetFavoriteHymnsUseCase
import de.lemke.nakbuch.domain.hymndataUseCases.SetFavoritesFromPersonalHymnListUseCase
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.PersonalHymn
import de.lemke.nakbuch.ui.TextviewActivity
import dev.oneuiproject.oneui.layout.DrawerLayout
import dev.oneuiproject.oneui.widget.MarginsTabLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivityTabFav : Fragment() {
    private lateinit var buchMode: BuchMode
    private lateinit var favHymns: MutableList<PersonalHymn>
    private lateinit var rootView: View
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var listView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var mainTabs: MarginsTabLayout
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private lateinit var selected: HashMap<Int, Boolean>
    private lateinit var showBottomBarJob: Job
    private var selecting = false
    private var checkAllListening = true

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var getFavoriteHymns: GetFavoriteHymnsUseCase

    @Inject
    lateinit var setFavoritesFromList: SetFavoritesFromPersonalHymnListUseCase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_tab_fav, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        drawerLayout = activity.findViewById(R.id.drawer_view)
        mainTabs = activity.findViewById(R.id.main_tabs)
        swipeRefreshLayout = rootView.findViewById(R.id.tabFavSwipeRefresh)
        listView = rootView.findViewById(R.id.favHymnList)
        lifecycleScope.launch {
            swipeRefreshLayout.isRefreshing = true
            swipeRefreshLayout.setOnRefreshListener { lifecycleScope.launch { initList() } }
            onBackPressedCallback = object : OnBackPressedCallback(false) {
                override fun handleOnBackPressed() {
                    setSelecting(false)
                }
            }
            activity.onBackPressedDispatcher.addCallback(onBackPressedCallback)
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { initList() }
    }

    private suspend fun initList() {
        swipeRefreshLayout.isRefreshing = true
        buchMode = getUserSettings().buchMode
        favHymns = getFavoriteHymns(buchMode).toMutableList()
        swipeRefreshLayout.isRefreshing = true
        selected = HashMap()
        favHymns.indices.forEach { i -> selected[i] = false }
        imageAdapter = ImageAdapter()
        listView.adapter = imageAdapter
        listView.layoutManager = LinearLayoutManager(context)
        listView.itemAnimator = null
        listView.seslSetFastScrollerEnabled(true)
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
        swipeRefreshLayout.isRefreshing = false
    }

    fun setSelecting(enabled: Boolean) {
        if (enabled) {
            selecting = true
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.itemCount)
            drawerLayout.setActionModeBottomMenu(R.menu.remove_menu)
            drawerLayout.setActionModeBottomMenuListener { item: MenuItem ->
                if (item.itemId == R.id.menuButtonRemove) {
                    lifecycleScope.launch {
                        swipeRefreshLayout.isRefreshing = true
                        setFavoritesFromList(favHymns.filterIndexed { index, _ -> selected[index]!! }.map { it.copy(favorite = false) })
                        initList()
                    }
                    setSelecting(false)
                } else {
                    Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                }
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
            mainTabs.isEnabled = false
            onBackPressedCallback.isEnabled = true
        } else {
            selecting = false
            showBottomBarJob.cancel()
            selected.replaceAll { _, _ -> false }
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.itemCount)
            drawerLayout.setActionModeCount(0, imageAdapter.itemCount)
            drawerLayout.dismissActionMode()
            mainTabs.isEnabled = true
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
    inner class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
        override fun getItemCount(): Int = favHymns.size

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
                holder.textView.text = favHymns[position].hymn.numberAndTitle
                holder.parentView.setOnClickListener {
                    if (selecting) toggleItemSelected(position) else {
                        startActivity(
                            Intent(rootView.context, TextviewActivity::class.java).putExtra(
                                "hymnId",
                                favHymns[position].hymn.hymnId.toInt()
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