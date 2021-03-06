package de.lemke.nakbuch.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SeslSwitchBar
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.allViews
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.*
import dev.oneuiproject.oneui.layout.SwitchBarLayout
import dev.oneuiproject.oneui.utils.DialogUtils
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.xml.KonfettiView
import javax.inject.Inject

@AndroidEntryPoint
class EasterEggSwitchBarActivity : AppCompatActivity(), SeslSwitchBar.OnSwitchChangeListener {
    private lateinit var discoveredEasterEggs: MutableList<String>
    private lateinit var easterEggCommentButton: AppCompatButton
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
        switchBarLayout.setNavigationButtonTooltip(getString(R.string.sesl_navigate_up))
        switchBarLayout.setNavigationButtonOnClickListener { onBackPressed() }
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
            .setCancelable(false)
            .create()
        val easterEggTippDialog1 = AlertDialog.Builder(this)
            .setTitle(getString(R.string.tips))
            .setMessage(getString(R.string.confirmTips))
            .setNegativeButton(R.string.showTips) { _: DialogInterface?, _: Int ->
                easterEggTippDialog2.show()
                DialogUtils.setDialogButtonTextColor(
                    easterEggTippDialog2,
                    DialogInterface.BUTTON_NEGATIVE,
                    resources.getColor(dev.oneuiproject.oneui.R.color.oui_functional_red_color, theme)
                )
            }
            .setPositiveButton(R.string.sesl_cancel) { _: DialogInterface?, _: Int ->
                Toast.makeText(this@EasterEggSwitchBarActivity, "Besser isses auch...", Toast.LENGTH_SHORT).show()
            }
            .create()
        val easterEggTippDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.easterEggs))
            .setMessage(getString(R.string.easterEggsHelp))
            .setNeutralButton(R.string.tips) { _: DialogInterface?, _: Int ->
                easterEggTippDialog1.show()
                DialogUtils.setDialogButtonTextColor(
                    easterEggTippDialog1,
                    DialogInterface.BUTTON_NEGATIVE,
                    resources.getColor(dev.oneuiproject.oneui.R.color.oui_functional_red_color, theme)
                )
                DialogUtils.setDialogButtonTextColor(
                    easterEggTippDialog1,
                    DialogInterface.BUTTON_POSITIVE,
                    resources.getColor(dev.oneuiproject.oneui.R.color.oui_functional_green_color, theme)
                )
            }
            .setPositiveButton(R.string.ok, null)
            .create()
        floatingActionButton = findViewById(R.id.easterEgg_fab)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.switchpreferencescreen_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_reset) {
            lifecycleScope.launch {
                resetEasterEggs()
                initList()
            }
            return true
        }
        return false
    }

    override fun onSwitchChanged(switchCompat: SwitchCompat, z: Boolean) {
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
        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = ImageAdapter()
        listView.itemAnimator = null
        listView.seslSetFastScrollerEnabled(true)
        listView.seslSetFillBottomEnabled(true)
        listView.seslSetGoToTopEnabled(true)
        listView.seslSetLastRoundedCorner(true)
        listView.addItemDecoration(ItemDecoration(this))
        refreshEnableDisableEasterEggView()
    }

    private fun refreshEnableDisableEasterEggView() {
        easterEggsHeader.isEnabled = enabled
        easterEggsHeader.setTextColor(resources.getColor(dev.oneuiproject.oneui.R.color.oui_primary_text_color, this.theme))
        easterEggCommentButton.isEnabled = enabled
        easterEggCommentButton.setTextColor(
            MaterialColors.getColor(
                this,
                androidx.appcompat.R.attr.colorPrimary,
                resources.getColor(R.color.primary_color, this.theme)
            )
        )
        floatingActionButton.isEnabled = enabled
    }

    inner class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
        override fun getItemCount(): Int = discoveredEasterEggs.size

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getItemViewType(position: Int): Int = 0 //if (discoveredEasterEggs[position] == "") 1 else 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            var resId = 0
            when (viewType) {
                0 -> resId = R.layout.listview_item
                //1 -> resId = R.layout.listview_bottom_spacing_small
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