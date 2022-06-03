package de.lemke.nakbuch.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import de.dlyt.yanndroid.oneui.dialog.AlertDialog
import de.dlyt.yanndroid.oneui.dialog.ClassicColorPickerDialog
import de.dlyt.yanndroid.oneui.dialog.DetailedColorPickerDialog
import de.dlyt.yanndroid.oneui.dialog.ProgressDialog
import de.dlyt.yanndroid.oneui.layout.DrawerLayout
import de.dlyt.yanndroid.oneui.layout.ToolbarLayout
import de.dlyt.yanndroid.oneui.menu.MenuItem
import de.dlyt.yanndroid.oneui.sesl.support.ViewSupport
import de.dlyt.yanndroid.oneui.sesl.tabs.SamsungTabLayout
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.dlyt.yanndroid.oneui.view.OptionGroup
import de.dlyt.yanndroid.oneui.view.Snackbar
import de.dlyt.yanndroid.oneui.view.TipPopup
import de.dlyt.yanndroid.oneui.view.Tooltip
import de.dlyt.yanndroid.oneui.widget.OptionButton
import de.dlyt.yanndroid.oneui.widget.TabLayout
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.*
import de.lemke.nakbuch.domain.model.BuchMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.xml.KonfettiView
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    companion object {
        var colorSettingChanged = false
    }

    private val coroutineContext: CoroutineContext = Dispatchers.Main
    private val coroutineScope: CoroutineScope = CoroutineScope(coroutineContext)
    private var currentTab = 0
    private var previousTab = 0
    private var currentFragment: Fragment? = null
    private var time: Long = 0
    private lateinit var fragmentManager: FragmentManager
    private lateinit var searchJob: Job
    private lateinit var buchMode: BuchMode
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var konfettiView: KonfettiView
    private lateinit var tabLayout: TabLayout
    private lateinit var optionGroup: OptionGroup
    private lateinit var searchHelpFAB: FloatingActionButton
    private lateinit var tipPopupDrawer: TipPopup
    private lateinit var tipPopupSearch: TipPopup
    private lateinit var tipPopupMenuButton: TipPopup
    private lateinit var tipPopupOkButton: TipPopup
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    @Inject
    lateinit var discoverEasterEgg: DiscoverEasterEggUseCase

    @Inject
    lateinit var getHints: GetHintsUseCase

    @Inject
    lateinit var setHints: SetHintsUseCase

    @Inject
    lateinit var checkAppStart: CheckAppStartUseCase

    @Inject
    lateinit var mute: MuteUseCase

    @Inject lateinit var doNotDisturb: DoNotDisturbUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ThemeUtil(this, resources.getString(R.color.primary_color))
        time = System.currentTimeMillis()
        drawerLayout = findViewById(R.id.drawer_view)
        tabLayout = findViewById(R.id.main_tabs)
        optionGroup = findViewById(R.id.optiongroup)
        searchHelpFAB = findViewById(R.id.searchHelpFAB)
        konfettiView = findViewById(R.id.konfettiViewMain)
        fragmentManager = supportFragmentManager
        ViewSupport.semSetRoundedCorners(window.decorView, 0)

        coroutineScope.launch {
            when (checkAppStart()) {
                AppStart.FIRST_TIME -> {}
                AppStart.FIRST_TIME_VERSION -> {}
                AppStart.NORMAL -> {}
                AppStart.OLD_ARCHITECTURE -> {}
            }
            buchMode = getUserSettings().buchMode
            setCurrentItem()
        }

        // TabLayout
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.number)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.list)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.favourites)))
        tabLayout.addOnTabSelectedListener(object : SamsungTabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: SamsungTabLayout.Tab) {
                previousTab = currentTab
                currentTab = tab.position
                setCurrentItem()
            }

            override fun onTabUnselected(tab: SamsungTabLayout.Tab) {}
            override fun onTabReselected(tab: SamsungTabLayout.Tab) {}
        })
        //DrawerLayout
        drawerLayout.inflateToolbarMenu(R.menu.main)
        drawerLayout.setDrawerButtonOnClickListener { startActivity(Intent().setClass(this@MainActivity, AboutActivity::class.java)) }
        drawerLayout.setDrawerButtonTooltip(getText(R.string.aboutApp))
        drawerLayout.setOnToolbarMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.search -> drawerLayout.showSearchMode()
                R.id.mute -> if (!mute())
                    Toast.makeText(this@MainActivity, this@MainActivity.getString(R.string.failedToMuteStreams), Toast.LENGTH_SHORT).show()
                R.id.dnd -> doNotDisturb()
                //R.id.info -> startActivity(Intent().setClass(this@MainActivity, AboutActivity::class.java))
                //R.id.settings -> startActivity(Intent().setClass(this@MainActivity,SettingsActivity::class.java))
            }
            true
        }
        drawerLayout.setSearchModeListener(object : ToolbarLayout.SearchModeListener() {
            override fun onSearchOpened(search_edittext: EditText) {
                searchJob = coroutineScope.launch {
                    search_edittext.setText(getUserSettings().search)
                    search_edittext.setSelection(0, search_edittext.text.length)
                }
                searchHelpFAB.visibility = View.VISIBLE
            }

            override fun onSearchDismissed(search_edittext: EditText) {
                setCurrentItem()
                searchHelpFAB.visibility = View.GONE
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                var searchText = s.toString()
                searchJob.cancel()
                searchJob = coroutineScope.launch {
                    if (getUserSettings().easterEggsEnabled) {
                        if (searchText.replace(" ", "").equals("easteregg", ignoreCase = true)) {
                            discoverEasterEgg(konfettiView, R.string.easterEggEntrySearch)
                            s.clear()
                            searchText = ""
                            val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            inputManager.hideSoftInputFromWindow(currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                        }
                    }
                    updateUserSettings { it.copy(search = searchText) }
                    setFragment(3)
                }
            }

            override fun onKeyboardSearchClick(s: CharSequence) {
                searchJob.cancel()
                searchJob = coroutineScope.launch {
                    updateUserSettings { it.copy(search = s.toString()) }
                    setFragment(3)
                }
            }

            override fun onVoiceInputClick(intent: Intent) {
                activityResultLauncher.launch(intent)
            }
        })
        optionGroup.setOnOptionButtonClickListener { _: OptionButton, checkedId: Int, _: Int ->
            when (checkedId) {
                R.id.ob_gesangbuch -> {
                    coroutineScope.launch {
                        buchMode = updateUserSettings { it.copy(buchMode = BuchMode.Gesangbuch) }.buchMode
                        setCurrentItem()
                    }
                }
                R.id.ob_chorbuch -> {
                    coroutineScope.launch {
                        buchMode = updateUserSettings { it.copy(buchMode = BuchMode.Chorbuch) }.buchMode
                        setCurrentItem()
                    }
                }
                R.id.ob_jugendliederbuch -> {
                    coroutineScope.launch {
                        buchMode = updateUserSettings { it.copy(buchMode = BuchMode.Jugendliederbuch) }.buchMode
                        setCurrentItem()
                    }
                }
                R.id.ob_settings -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                R.id.ob_about -> startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                R.id.ob_help -> startActivity(Intent(this@MainActivity, HelpActivity::class.java))
                R.id.ob_about_me -> startActivity(Intent(this@MainActivity, AboutMeActivity::class.java))
            }
            drawerLayout.setDrawerOpen(false, true)
            updateOptionbuttons()
        }
        searchHelpFAB.backgroundTintList =
            ResourcesCompat.getColorStateList(resources, de.dlyt.yanndroid.oneui.R.color.sesl_swipe_refresh_background, theme)
        searchHelpFAB.supportImageTintList =
            ResourcesCompat.getColorStateList(resources, de.dlyt.yanndroid.oneui.R.color.sesl_tablayout_selected_indicator_color, theme)
        Tooltip.setTooltipText(searchHelpFAB, getString(R.string.help))
        searchHelpFAB.setOnClickListener {
            val searchModes = arrayOf<CharSequence>(getString(R.string.onlyExactSearchText), getString(R.string.searchForAllPartialWords))
            AlertDialog.Builder(this@MainActivity)
                .setTitle(R.string.help)
                .setMessage(R.string.searchHelp)
                .setNeutralButton(R.string.ok, null)
                .setNegativeButton(R.string.standardSearchMode) { _: DialogInterface, _: Int ->
                    coroutineScope.launch {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle(getString(R.string.setStandardSearchMode))
                            .setNeutralButton(R.string.ok, null)
                            .setSingleChoiceItems(searchModes, if (getUserSettings().alternativeSearchModeEnabled) 1 else 0)
                            { _: DialogInterface, i: Int ->
                                coroutineScope.launch { updateUserSettings { it.copy(alternativeSearchModeEnabled = (i == 1)) } }
                            }
                            .show()
                    }
                }
                .create()
                .show()
        }
        val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                coroutineScope.launch {
                    if (currentTab != 0) {
                        currentTab = 0
                        setCurrentItem()
                    } else {
                        if (getUserSettings().confirmExit) {
                            if (System.currentTimeMillis() - time < 3000) finishAffinity()
                            else {
                                Toast.makeText(this@MainActivity, resources.getString(R.string.pressAgainToExit), Toast.LENGTH_SHORT).show()
                                time = System.currentTimeMillis()
                            }
                        } else finishAffinity()
                    }

                }
            }
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
            drawerLayout.onSearchModeVoiceInputResult(result)
        }
        AppUpdateManagerFactory.create(this).appUpdateInfo.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE)
                drawerLayout.setButtonBadges(ToolbarLayout.N_BADGE, DrawerLayout.N_BADGE)
        }
    }

    public override fun attachBaseContext(context: Context) {
        // pre-OneUI
        if (Build.VERSION.SDK_INT <= 28) super.attachBaseContext(ThemeUtil.createDarkModeContextWrapper(context))
        else super.attachBaseContext(context)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // pre-OneUI
        if (Build.VERSION.SDK_INT <= 28) resources.configuration.setTo(ThemeUtil.createDarkModeConfig(this, newConfig))
    }

    override fun onResume() {
        super.onResume()
        if (colorSettingChanged) {
            colorSettingChanged = false
            recreate()
        }
        coroutineScope.launch {
            /* TODO does mode change somewhere else?
            val newBuchMode = getUserSettings().buchMode
            if (!::buchMode.isInitialized) {
                buchMode = newBuchMode
                setCurrentItem()
            } else if (newBuchMode != buchMode) {
                buchMode = newBuchMode
                setCurrentItem()
            }*/
            val hints = getHints().toMutableSet()
            if (hints.remove("appIntroduction")) {
                setHints(hints)
                updateUserSettings { it.copy(showMainTips = true, showTextViewTips = true, showImageViewTips = true) }
            }
            if (hints.contains("appHint")) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle(getString(R.string.hint) + ":")
                    .setMessage(getString(R.string.appHintText))
                    .setNegativeButton(getString(R.string.dontShowAgain)) { _: DialogInterface?, _: Int ->
                        hints.remove("appHint")
                        coroutineScope.launch { setHints(hints) }
                    }
                    .setPositiveButton(getString(R.string.ok), null)
                    .setOnDismissListener { coroutineScope.launch { easterEggDialog() } }
                    .create()
                    .show()

            } else {
                easterEggDialog()
            }
        }
    }

    fun setCurrentItem() {
        if (tabLayout.isEnabled) {
            val tab = tabLayout.getTabAt(currentTab)
            if (tab != null) {
                tab.select()
                setFragment(currentTab)
            }
        }
        updateOptionbuttons()
    }

    private fun updateOptionbuttons() {
        drawerLayout.setTitle(buchMode.toString())
        optionGroup.selectedOptionButton = when (buchMode) {
            BuchMode.Gesangbuch -> findViewById(R.id.ob_gesangbuch)
            BuchMode.Chorbuch -> findViewById(R.id.ob_chorbuch)
            BuchMode.Jugendliederbuch -> findViewById(R.id.ob_jugendliederbuch)
        }
    }

    private fun setFragment(tabPosition: Int) {
        if (fragmentManager.isDestroyed) return
        val mTabsTagName: Array<String> = resources.getStringArray(R.array.mainactivity_tab_tag)
        val mTabsClassName: Array<String> = resources.getStringArray(R.array.mainactivity_tab_class)
        val tabName = mTabsTagName[tabPosition]
        val fragment = fragmentManager.findFragmentByTag(tabName)
        currentFragment?.let { fragmentManager.beginTransaction().detach(it).commit() }
        if (fragment != null) {
            currentFragment = fragment
            try {
                fragmentManager.beginTransaction().attach(fragment).commit()
            } catch (e: IllegalStateException) {
                //Fatal Exception: java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
                fragmentManager.beginTransaction().attach(fragment).commitAllowingStateLoss()
            }
        } else {
            //Toast.makeText(applicationContext, "New instance for " + mTabsClassName[tabPosition], Toast.LENGTH_SHORT).show()
            try {
                currentFragment = Class.forName(mTabsClassName[tabPosition]).newInstance() as Fragment
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
            fragmentManager.beginTransaction().add(R.id.fragment_container, currentFragment!!, tabName).commit()
        }
    }

    private suspend fun easterEggDialog() {
        if (getUserSettings().showEasterEggHint) {
            AlertDialog.Builder(this@MainActivity)
                .setTitle(getString(R.string.easterEggs))
                .setMessage(getString(R.string.easterEggsText))
                .setNegativeButton(getString(R.string.deactivate)) { dialogInterface: DialogInterface, _: Int ->
                    coroutineScope.launch { updateUserSettings { it.copy(easterEggsEnabled = false) } }
                    Handler(Looper.getMainLooper()).postDelayed({ dialogInterface.dismiss() }, 700)
                }
                .setPositiveButton(getString(R.string.ok), null)
                .setNegativeButtonColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red, this@MainActivity.theme))
                .setNegativeButtonProgress(true)
                .setOnDismissListener { coroutineScope.launch { showTipPopup() } }
                .create()
                .show()
            updateUserSettings { it.copy(showEasterEggHint = false) }
        } else {
            showTipPopup()
        }
    }

    private fun initTipPopup() {
        val toolbarMenuItemContainer =
            drawerLayout.findViewById<ViewGroup>(de.dlyt.yanndroid.oneui.R.id.toolbar_layout_action_menu_item_container)
        val drawerButtonView = drawerLayout.findViewById<View>(de.dlyt.yanndroid.oneui.R.id.toolbar_layout_navigationButton)
        val searchItemView = toolbarMenuItemContainer.getChildAt(0)
        val menuItemView = toolbarMenuItemContainer.getChildAt(1)
        val okButtonView = findViewById<View>(R.id.b_ok)
        tipPopupDrawer = TipPopup(drawerButtonView) //,TipPopup.MODE_TRANSLUCENT);
        tipPopupSearch = TipPopup(searchItemView) //,TipPopup.MODE_TRANSLUCENT);
        tipPopupMenuButton = TipPopup(menuItemView) //,TipPopup.MODE_TRANSLUCENT);
        tipPopupOkButton = TipPopup(okButtonView) //,TipPopup.MODE_TRANSLUCENT);
        tipPopupDrawer.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, theme))
        tipPopupSearch.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, theme))
        tipPopupMenuButton.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, theme))
        tipPopupOkButton.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, theme))
        tipPopupDrawer.setExpanded(true)
        tipPopupSearch.setExpanded(true)
        tipPopupMenuButton.setExpanded(true)
        tipPopupOkButton.setExpanded(true)
        tipPopupDrawer.setOnDismissListener { tipPopupSearch.show(TipPopup.DIRECTION_BOTTOM_LEFT) }
        tipPopupSearch.setOnDismissListener { tipPopupMenuButton.show(TipPopup.DIRECTION_BOTTOM_LEFT) }
        tipPopupMenuButton.setOnDismissListener { tipPopupOkButton.show(TipPopup.DIRECTION_TOP_LEFT) }
        tipPopupDrawer.setMessage(getString(R.string.menuGeneralTip))
        tipPopupSearch.setMessage(getString(R.string.searchTip))
        tipPopupMenuButton.setMessage(getString(R.string.mute) + " " + getString(R.string.or) + " " + getString(R.string.dndDescription))
        tipPopupOkButton.setMessage(getString(R.string.okButtonTip))
    }

    private suspend fun showTipPopup() {
        if (getUserSettings().showMainTips) {
            updateUserSettings { it.copy(showMainTips = false) }
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    initTipPopup()
                    val drawerButtonView =
                        drawerLayout.findViewById<View>(de.dlyt.yanndroid.oneui.R.id.toolbar_layout_navigationButton)
                    val outLocation = IntArray(2)
                    drawerButtonView.getLocationOnScreen(outLocation)
                    tipPopupDrawer.setTargetPosition(
                        outLocation[0] + drawerButtonView.width / 2,
                        outLocation[1] + drawerButtonView.height / 2 + resources.getDimensionPixelSize(de.dlyt.yanndroid.oneui.R.dimen.sesl_action_button_icon_size)
                    )
                    tipPopupDrawer.show(TipPopup.DIRECTION_BOTTOM_RIGHT)
                } catch (e: Exception) { // still crashing? : android.view.WindowManager$BadTokenException: Unable to add window -- token null is not valid; is your activity running?
                    e.printStackTrace()
                }
            }, 50)
        }
    }


    //Dialog samples:
    @Suppress("UNUSED_PARAMETER", "unused")
    fun classicColorPickerDialog(view: View?) {
        val mClassicColorPickerDialog: ClassicColorPickerDialog
        val sharedPreferences = getSharedPreferences("ThemeColor", MODE_PRIVATE)
        val stringColor = sharedPreferences.getString("color", "0381fe")
        val currentColor = Color.parseColor("#$stringColor")
        try {
            mClassicColorPickerDialog = ClassicColorPickerDialog(
                this,
                { i -> if (currentColor != i) ThemeUtil.setColor(this@MainActivity, Color.red(i), Color.green(i), Color.blue(i)) },
                currentColor
            )
            mClassicColorPickerDialog.show()
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    fun detailedColorPickerDialog(view: View?) {
        val mDetailedColorPickerDialog: DetailedColorPickerDialog
        val sharedPreferences = getSharedPreferences("ThemeColor", MODE_PRIVATE)
        val stringColor = sharedPreferences.getString("color", "0381fe")
        val currentColor = Color.parseColor("#$stringColor")
        try {
            mDetailedColorPickerDialog = DetailedColorPickerDialog(
                this,
                { i -> if (currentColor != i) ThemeUtil.setColor(this@MainActivity, Color.red(i), Color.green(i), Color.blue(i)) },
                currentColor
            )
            mDetailedColorPickerDialog.show()
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    fun standardDialog(view: View?) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Title")
            .setMessage("Message")
            .setNeutralButton("Maybe", null)
            .setNegativeButton("No") { dialogInterface: DialogInterface, _: Int ->
                Handler(Looper.getMainLooper()).postDelayed({ dialogInterface.dismiss() }, 700)
            }
            .setPositiveButton("Yes") { dialogInterface: DialogInterface, _: Int ->
                Handler(Looper.getMainLooper()).postDelayed({ dialogInterface.dismiss() }, 700)
            }
            .setNegativeButtonColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red, this@MainActivity.theme))
            .setPositiveButtonColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_green, this@MainActivity.theme))
            .setPositiveButtonProgress(true)
            .setNegativeButtonProgress(true)
            .create()
        dialog.show()
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    fun singleChoiceDialog(view: View?) {
        val charSequences = arrayOf<CharSequence>("Choice1", "Choice2", "Choice3")
        AlertDialog.Builder(this)
            .setTitle("Title")
            .setNeutralButton("Maybe", null)
            .setNegativeButton("No", null)
            .setPositiveButton("Yes", null)
            .setSingleChoiceItems(charSequences, 0, null)
            .show()
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    fun multiChoiceDialog(view: View?) {
        val charSequences = arrayOf<CharSequence>("Choice1", "Choice2", "Choice3")
        val booleans = booleanArrayOf(true, false, true)
        AlertDialog.Builder(this)
            .setTitle("Title")
            .setNeutralButton("Maybe", null)
            .setNegativeButton("No", null)
            .setPositiveButton("Yes", null)
            .setMultiChoiceItems(charSequences, booleans, null)
            .show()
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    fun progressDialog(view: View) {
        val dialog = ProgressDialog(this)
        dialog.isIndeterminate = true
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setTitle("Title")
        dialog.setMessage("ProgressDialog")
        dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", null as DialogInterface.OnClickListener?)
        dialog.setOnCancelListener { progressDialogCircleOnly(view) }
        dialog.show()
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    private fun progressDialogCircleOnly(view: View) {
        val dialog = ProgressDialog(this)
        dialog.setProgressStyle(ProgressDialog.STYLE_CIRCLE)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setOnCancelListener {
            Snackbar.make(view, "Text label", Snackbar.LENGTH_SHORT).setAction("Action") { }.show()
        }
        dialog.show()
    }
}