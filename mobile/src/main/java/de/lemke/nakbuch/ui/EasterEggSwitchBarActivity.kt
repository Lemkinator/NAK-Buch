package de.lemke.nakbuch.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.allViews
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import de.dlyt.yanndroid.oneui.dialog.AlertDialog
import de.dlyt.yanndroid.oneui.layout.SwitchBarLayout
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager
import de.dlyt.yanndroid.oneui.sesl.utils.SeslRoundedCorner
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.dlyt.yanndroid.oneui.view.RecyclerView
import de.dlyt.yanndroid.oneui.widget.Switch
import de.dlyt.yanndroid.oneui.widget.SwitchBar
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.*
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.xml.KonfettiView
import javax.inject.Inject

@AndroidEntryPoint
class EasterEggSwitchBarActivity : AppCompatActivity(), SwitchBar.OnSwitchChangeListener {
    private lateinit var discoveredEasterEggs: MutableList<String>
    private lateinit var easterEggCommentButton: MaterialButton
    private lateinit var easterEggsHeader: TextView
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var listView: RecyclerView
    private lateinit var konfettiView: KonfettiView
    private lateinit var easterEggComments: Array<String>
    private var time: Long = 0
    private var clickCounter = 0
    private var enabled: Boolean = true

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    @Inject
    lateinit var discoverEasterEgg: DiscoverEasterEggUseCase

    @Inject
    lateinit var getDiscoveredEasterEggs: GetDiscoveredEasterEggsUseCase

    @Inject
    lateinit var resetEasterEggs: ResetEasterEggsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil(this, resources.getString(R.color.primary_color))
        setContentView(R.layout.activity_easteregg)
        easterEggComments = resources.getStringArray(R.array.easterEggComments)
        konfettiView = findViewById(R.id.konfettiViewEasterEgg)
        listView = findViewById(R.id.easterEggList)
        easterEggsHeader = findViewById(R.id.discoveredEasterEggsText)
        easterEggCommentButton = findViewById(R.id.easterEggCommentButton)
        easterEggCommentButton.setOnClickListener {
            if (System.currentTimeMillis() - time < 400) {
                if (clickCounter++ > 5) {
                    clickCounter = 0
                    lifecycleScope.launch {
                        discoverEasterEgg(konfettiView, R.string.easterEggEntryComment)
                        initList()
                    }
                }
            } else {
                clickCounter = 0
            }
            time = System.currentTimeMillis()
        }
        val switchBarLayout = findViewById<SwitchBarLayout>(R.id.switchbarlayout_easteregg)
        switchBarLayout.switchBar.addOnSwitchChangeListener(this)
        switchBarLayout.setNavigationButtonTooltip(getString(de.dlyt.yanndroid.oneui.R.string.sesl_navigate_up))
        switchBarLayout.setNavigationButtonOnClickListener { onBackPressed() }
        switchBarLayout.inflateToolbarMenu(R.menu.switchpreferencescreen_menu)
        switchBarLayout.setOnToolbarMenuItemClickListener { //reset button
            lifecycleScope.launch {
                resetEasterEggs()
                initList()
            }
            true
        }
        floatingActionButton = findViewById(R.id.easterEgg_fab)
        floatingActionButton.rippleColor = resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl4_ripple_color, theme)
        floatingActionButton.backgroundTintList = ColorStateList.valueOf(
            resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_swipe_refresh_background, theme)
        )
        floatingActionButton.supportImageTintList = ResourcesCompat.getColorStateList(
            resources, de.dlyt.yanndroid.oneui.R.color.sesl_tablayout_selected_indicator_color, theme
        )
        val easterEggTippDialog2 = AlertDialog.Builder(this)
            .setTitle(getString(R.string.tips))
            .setMessage(getString(R.string.easterEggTips))
            .setNegativeButton(R.string.easterEggTipsShame) { _: DialogInterface?, _: Int ->
                lifecycleScope.launch {
                    updateUserSettings { it.copy(easterEggTipsUsed = true) }
                    initList()
                }
                Toast.makeText(this@EasterEggSwitchBarActivity, "Zu Recht...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButtonColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red, theme))
            .setCancelable(false)
            .create()
        val easterEggTippDialog1 = AlertDialog.Builder(this)
            .setTitle(getString(R.string.tips))
            .setMessage(getString(R.string.confirmTips))
            .setNegativeButton(R.string.showTips) { _: DialogInterface?, _: Int -> easterEggTippDialog2.show() }
            .setPositiveButton(de.dlyt.yanndroid.oneui.R.string.sesl_cancel) { _: DialogInterface?, _: Int ->
                Toast.makeText(this@EasterEggSwitchBarActivity, "Besser isses auch...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButtonColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red, theme))
            .setPositiveButtonColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_green, theme))
            .create()
        val easterEggTippDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.easterEggs))
            .setMessage(getString(R.string.easterEggsHelp))
            .setNeutralButton(R.string.tips) { _: DialogInterface?, _: Int -> easterEggTippDialog1.show() }
            .setPositiveButton(R.string.ok, null)
            .create()
        floatingActionButton.setOnClickListener { easterEggTippDialog.show() }
        floatingActionButton.setOnLongClickListener {
            lifecycleScope.launch {
                discoverEasterEgg(konfettiView, R.string.easterEggEntryHelp)
                initList()
            }
            true
        }
        lifecycleScope.launch {
            enabled = getUserSettings().easterEggsEnabled
            switchBarLayout.switchBar.isChecked = enabled
            initList()
        }
    }

    override fun onSwitchChanged(switchCompat: Switch, z: Boolean) {
        lifecycleScope.launch {
            enabled = updateUserSettings { it.copy(easterEggsEnabled = z) }.easterEggsEnabled
            initList()
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun initList() {
        discoveredEasterEggs = getDiscoveredEasterEggs().toMutableList()
        easterEggsHeader.text = getString(R.string.alreadyDiscoveredEasterEggs) +
                (if (getUserSettings().easterEggTipsUsed) " (mit Tipps)" else "") +
                " (${discoveredEasterEggs.size}/${easterEggComments.size - 1}):"
        easterEggCommentButton.text = easterEggComments[discoveredEasterEggs.size]
        discoveredEasterEggs.add("") //placeholder
        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = ImageAdapter()
        listView.itemAnimator = null
        listView.seslSetFastScrollerEnabled(true)
        listView.seslSetFillBottomEnabled(true)
        listView.seslSetGoToTopEnabled(true)
        listView.seslSetLastRoundedCorner(false)
        val divider = TypedValue()
        theme.resolveAttribute(android.R.attr.listDivider, divider, true)
        val decoration = ItemDecoration()
        listView.addItemDecoration(decoration)
        decoration.setDivider(AppCompatResources.getDrawable(this, divider.resourceId)!!)

        refreshEnableDisableEasterEggView()
    }

    private fun refreshEnableDisableEasterEggView() {
        easterEggsHeader.isEnabled = enabled
        easterEggsHeader.setTextColor(
            resources.getColor(
                if (enabled) de.dlyt.yanndroid.oneui.R.color.sesl_dialog_body_text_color
                else de.dlyt.yanndroid.oneui.R.color.abc_secondary_text_material_dark,
                this.theme
            )
        )
        easterEggCommentButton.isEnabled = enabled
        easterEggCommentButton.setTextColor(
            if (enabled) MaterialColors.getColor(
                this,
                de.dlyt.yanndroid.oneui.R.attr.colorPrimary,
                resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_dialog_body_text_color, this.theme)
            ) else resources.getColor(de.dlyt.yanndroid.oneui.R.color.abc_secondary_text_material_dark, this.theme)
        )
        floatingActionButton.isEnabled = enabled
    }

    inner class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
        override fun getItemCount(): Int = discoveredEasterEggs.size

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getItemViewType(position: Int): Int = if (discoveredEasterEggs[position] == "") 1 else 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            var resId = 0
            when (viewType) {
                0 -> resId = R.layout.listview_item
                1 -> resId = R.layout.listview_bottom_spacing_small
            }
            val view = LayoutInflater.from(parent.context).inflate(resId, parent, false)
            return ViewHolder(view, viewType)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder.isItem) {
                holder.textView!!.text = discoveredEasterEggs[position]
                holder.parentView!!.setOnClickListener { }
            }
            holder.parentView?.allViews?.forEach { view -> view.isEnabled = enabled }
        }

        inner class ViewHolder internal constructor(itemView: View?, viewType: Int) : RecyclerView.ViewHolder(itemView!!) {
            var isItem: Boolean = viewType == 0
            var parentView: RelativeLayout? = null
            var textView: TextView? = null

            init {
                if (isItem) {
                    parentView = itemView as RelativeLayout?
                    textView = parentView!!.findViewById(R.id.icon_tab_item_text)
                }
            }
        }
    }

    inner class ItemDecoration : RecyclerView.ItemDecoration() {
        private val seslRoundedCornerTop: SeslRoundedCorner = SeslRoundedCorner(this@EasterEggSwitchBarActivity, true)
        private val seslRoundedCornerBottom: SeslRoundedCorner
        private var divider: Drawable? = null
        private var dividerHeight = 0
        override fun seslOnDispatchDraw(canvas: Canvas, recyclerView: RecyclerView, state: RecyclerView.State) {
            super.seslOnDispatchDraw(canvas, recyclerView, state)
            val childCount = recyclerView.childCount
            val width = recyclerView.width

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
            seslRoundedCornerBottom = SeslRoundedCorner(this@EasterEggSwitchBarActivity, true)
            seslRoundedCornerBottom.roundedCorners = 12
        }
    }
}