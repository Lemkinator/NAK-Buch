package de.lemke.nakbuch.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.*
import de.lemke.nakbuch.domain.model.BuchMode
import dev.oneuiproject.oneui.dialog.ProgressDialog
import dev.oneuiproject.oneui.layout.DrawerLayout
import dev.oneuiproject.oneui.layout.ToolbarLayout
import dev.oneuiproject.oneui.widget.MarginsTabLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.xml.KonfettiView
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    companion object {
        var refreshView = false
    }

    private var currentTab = 0
    private var previousTab = 0
    private var currentFragment: Fragment? = null
    private var time: Long = 0
    private lateinit var fragmentManager: FragmentManager
    private var searchJob: Job? = null
    private lateinit var buchMode: BuchMode
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var konfettiView: KonfettiView
    private lateinit var tabLayout: MarginsTabLayout
    private lateinit var searchHelpFAB: FloatingActionButton
    /*
    private lateinit var tipPopupDrawer: TipPopup
    private lateinit var tipPopupSearch: TipPopup
    private lateinit var tipPopupMenuButton: TipPopup
    private lateinit var tipPopupOkButton: TipPopup
    */
    private lateinit var gesangbuchOption: LinearLayout
    private lateinit var chorbuchOption: LinearLayout
    private lateinit var jugendliederbuchOption : LinearLayout
    private lateinit var jbErgaenzungsheftOption: LinearLayout
    private lateinit var helpOption: LinearLayout
    private lateinit var aboutAppOption: LinearLayout
    private lateinit var aboutMeOption: LinearLayout
    private lateinit var settingsOption: LinearLayout
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    @Inject
    lateinit var discoverEasterEgg: DiscoverEasterEggUseCase

    @Inject
    lateinit var openBischoffApp: OpenBischoffAppUseCase

    @Inject
    lateinit var getHints: GetHintsUseCase

    @Inject
    lateinit var setHints: SetHintsUseCase

    @Inject
    lateinit var checkAppStart: CheckAppStartUseCase

    @Inject
    lateinit var mute: MuteUseCase

    @Inject
    lateinit var doNotDisturb: DoNotDisturbUseCase

    @Inject
    lateinit var initDatabase: InitDatabaseUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        time = System.currentTimeMillis()
        drawerLayout = findViewById(R.id.drawer_view)
        tabLayout = findViewById(R.id.main_tabs)
        searchHelpFAB = findViewById(R.id.searchHelpFAB)
        konfettiView = findViewById(R.id.konfettiViewMain)
        gesangbuchOption = findViewById(R.id.draweritem_gesangbuch)
        chorbuchOption = findViewById(R.id.draweritem_chorbuch)
        jugendliederbuchOption = findViewById(R.id.draweritem_jugendliederbuch)
        jbErgaenzungsheftOption = findViewById(R.id.draweritem_jbergaenzungsheft)
        helpOption = findViewById(R.id.draweritem_help)
        aboutAppOption = findViewById(R.id.draweritem_about_app)
        aboutMeOption = findViewById(R.id.draweritem_about_me)
        settingsOption = findViewById(R.id.draweritem_settings)
        fragmentManager = supportFragmentManager

        lifecycleScope.launch {
            buchMode = getUserSettings().buchMode
            setBuchMode(buchMode)
            when (checkAppStart()) {
                AppStart.FIRST_TIME -> setCurrentItem()
                AppStart.NORMAL -> setCurrentItem()
                AppStart.OLD_HYMNTEXTS -> oldHymnTextsDialog()
                AppStart.FIRST_TIME_VERSION -> setCurrentItem()
            }
        }

        // TabLayout
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.number)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.list)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.favourites)))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                previousTab = currentTab
                currentTab = tab.position
                setCurrentItem()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        //DrawerLayout
        drawerLayout.setDrawerButtonIcon(getDrawable(R.drawable.ic_baseline_oui_info_outline_24))
        drawerLayout.toolbar.inflateMenu(R.menu.main)
        drawerLayout.setDrawerButtonOnClickListener { startActivity(Intent().setClass(this@MainActivity, AboutActivity::class.java)) }
        drawerLayout.setDrawerButtonTooltip(getText(R.string.aboutApp))
        drawerLayout.setSearchModeListener(object : ToolbarLayout.SearchModeListener {
            //TODO override fun onVoiceInputClick(intent: Intent) { activityResultLauncher.launch(intent) }

            override fun onQueryTextSubmit(query: String?): Boolean {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    updateUserSettings { it.copy(search = query ?: "") }
                    setFragment(3)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                var searchText = newText ?: ""
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    if (getUserSettings().easterEggsEnabled) {
                        if (searchText.replace(" ", "").equals("easteregg", ignoreCase = true)) {
                            discoverEasterEgg(konfettiView, R.string.easterEggEntrySearch)
                            drawerLayout.searchView.setQuery("", true) //TODO s.clear()??
                            searchText = ""
                            val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            inputManager.hideSoftInputFromWindow(currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                        }
                    }
                    updateUserSettings { it.copy(search = searchText) }
                    setFragment(3)
                }
                return true
            }

            override fun onSearchModeToggle(searchView: SearchView?, visible: Boolean) {
                if (visible) {
                    searchJob = lifecycleScope.launch {
                        searchView?.setQuery(getUserSettings().search, true)
                        //TODO search_edittext.setSelection(0, search_edittext.text.length)
                    }
                    searchHelpFAB.visibility = View.VISIBLE
                } else {
                    setCurrentItem()
                    searchHelpFAB.visibility = View.GONE
                }
            }
        })
        gesangbuchOption.setOnClickListener {
            setBuchMode(BuchMode.Gesangbuch)
        }
        chorbuchOption.setOnClickListener {
            setBuchMode(BuchMode.Chorbuch)
        }
        jugendliederbuchOption.setOnClickListener {
            setBuchMode(BuchMode.Jugendliederbuch)
        }
        jbErgaenzungsheftOption.setOnClickListener {
            setBuchMode(BuchMode.JBErgaenzungsheft)
        }
        helpOption.setOnClickListener {
            startActivity(Intent(this@MainActivity, HelpActivity::class.java))
        }
        aboutAppOption.setOnClickListener {
            startActivity(Intent(this@MainActivity, AboutActivity::class.java))
        }
        aboutMeOption.setOnClickListener {
            startActivity(Intent(this@MainActivity, AboutMeActivity::class.java))
        }
        settingsOption.setOnClickListener {
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
        }
        /* TODO
        searchHelpFAB.backgroundTintList =
            ResourcesCompat.getColorStateList(resources, de.dlyt.yanndroid.oneui.R.color.sesl_swipe_refresh_background, theme)
        searchHelpFAB.supportImageTintList =
            ResourcesCompat.getColorStateList(resources, de.dlyt.yanndroid.oneui.R.color.sesl_tablayout_selected_indicator_color, theme)

         */
        //Tooltip.setTooltipText(searchHelpFAB, getString(R.string.help))
        searchHelpFAB.setOnClickListener { searchDialog() }
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                lifecycleScope.launch {
                    if (drawerLayout.isSearchMode) drawerLayout.dismissSearchMode()
                    else if (currentTab != 0) {
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
        })
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
            //TODO drawerLayout.onSearchModeVoiceInputResult(result)
        }
        AppUpdateManagerFactory.create(this).appUpdateInfo.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE)
                drawerLayout.setButtonBadges(ToolbarLayout.N_BADGE, DrawerLayout.N_BADGE)
        }
    }

    override fun onResume() {
        super.onResume()
        if (refreshView) {
            refreshView = false
            recreate()
        }
        lifecycleScope.launch {
            val hints = getHints().toMutableSet()
            if (hints.remove("appIntroduction")) {
                setHints(hints)
                updateUserSettings { it.copy(showMainTips = true, showTextViewTips = true, showImageViewTips = true) }
            }
            if (hints.contains("appHint")) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle(getString(R.string.hint) + ":")
                    .setMessage(getString(R.string.appHintTextShort))
                    .setNeutralButton(getString(R.string.moreInformation)) { _: DialogInterface?, _: Int ->
                        startActivity(Intent(this@MainActivity, HelpActivity::class.java))
                    }
                    .setPositiveButton(getString(R.string.ok)) { _: DialogInterface?, _: Int ->
                        hints.remove("appHint")
                        lifecycleScope.launch { setHints(hints) }
                        lifecycleScope.launch { easterEggDialog() }
                    }
                    .setCancelable(false)
                    .create()
                    .show()

            } else {
                easterEggDialog()
            }
        }
    }

    private fun setBuchMode(newBuchMode: BuchMode) {
        gesangbuchOption.isSelected = false
        chorbuchOption.isSelected = false
        jugendliederbuchOption.isSelected = false
        jbErgaenzungsheftOption.isSelected = false
        when (newBuchMode) {
            BuchMode.Gesangbuch -> gesangbuchOption.isSelected = true
            BuchMode.Chorbuch -> chorbuchOption.isSelected = true
            BuchMode.Jugendliederbuch -> jugendliederbuchOption.isSelected = true
            BuchMode.JBErgaenzungsheft -> jbErgaenzungsheftOption.isSelected = true
        }
        lifecycleScope.launch {
            buchMode = updateUserSettings { it.copy(buchMode = newBuchMode) }.buchMode
            setCurrentItem()
        }
        drawerLayout.setTitle(newBuchMode.toString())
        drawerLayout.setDrawerOpen(false, true)
    }

    fun setCurrentItem() {
        if (tabLayout.isEnabled) {
            val tab = tabLayout.getTabAt(currentTab)
            if (tab != null) {
                tab.select()
                setFragment(currentTab)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> drawerLayout.showSearchMode()
            R.id.mute -> if (!mute())
                Toast.makeText(this@MainActivity, this@MainActivity.getString(R.string.failedToMuteStreams), Toast.LENGTH_SHORT).show()
            R.id.dnd -> doNotDisturb()
            R.id.openOfficialApp -> lifecycleScope.launch {
                if (buchMode == BuchMode.Gesangbuch || buchMode == BuchMode.Chorbuch) openBischoffApp(buchMode, true)
                else discoverEasterEgg(konfettiView, R.string.easterEggWhichOfficialApp)
            }
        }
        return true
    }

    private fun setFragment(tabPosition: Int) {
        if (fragmentManager.isDestroyed) return
        val mTabsTagName: Array<String> = resources.getStringArray(R.array.mainactivity_tab_tag)
        val mTabsClassName: Array<String> = resources.getStringArray(R.array.mainactivity_tab_class)
        val tabName = mTabsTagName[tabPosition]
        val fragment = fragmentManager.findFragmentByTag(tabName)
        try {
            currentFragment?.let { fragmentManager.beginTransaction().detach(it).commit() }
        } catch (e: IllegalStateException) {
            //Fatal Exception: java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
            currentFragment?.let { fragmentManager.beginTransaction().detach(it).commitAllowingStateLoss() }
        }
        if (fragment != null) {
            currentFragment = fragment
            try {
                fragmentManager.beginTransaction().attach(fragment).commit()
            } catch (e: IllegalStateException) {
                //Fatal Exception: java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
                fragmentManager.beginTransaction().attach(fragment).commitAllowingStateLoss()
            }
        } else {
            Log.d("MainActivity setFragment", "New instance for " + mTabsClassName[tabPosition])
            try {
                currentFragment = Class.forName(mTabsClassName[tabPosition]).newInstance() as Fragment
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
            fragmentManager.beginTransaction().add(R.id.fragment_container, currentFragment!!, tabName).commit()
        }
    }

    private fun searchDialog() {
        AlertDialog.Builder(this@MainActivity)
            .setTitle(R.string.help)
            .setMessage(R.string.searchHelp)
            .setNeutralButton(R.string.ok, null)
            .setNegativeButton(R.string.standardSearchMode) { _: DialogInterface, _: Int ->
                lifecycleScope.launch {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle(getString(R.string.setStandardSearchMode))
                        .setNeutralButton(R.string.ok, null)
                        .setSingleChoiceItems(
                            arrayOf<CharSequence>(getString(R.string.onlyExactSearchText), getString(R.string.searchForAllPartialWords)),
                            if (getUserSettings().alternativeSearchModeEnabled) 1 else 0
                        )
                        { _: DialogInterface, i: Int ->
                            lifecycleScope.launch { updateUserSettings { it.copy(alternativeSearchModeEnabled = (i == 1)) } }
                        }
                        .show()
                }
            }
            .create()
            .show()
    }

    private suspend fun oldHymnTextsDialog() {
        if (!getUserSettings().usingPrivateTexts) {
            val dialog = ProgressDialog(this@MainActivity)
            dialog.setProgressStyle(ProgressDialog.STYLE_CIRCLE)
            dialog.setCancelable(false)
            dialog.show()
            initDatabase(forceInit = true).invokeOnCompletion {
                lifecycleScope.launch { setCurrentItem() }
                dialog.dismiss()
            }
        } else {
            AlertDialog.Builder(this@MainActivity)
                .setTitle(getString(R.string.newTextsTitle))
                .setMessage(getString(R.string.newTexts))
                .setNegativeButton("Downgrade") { dialogInterface: DialogInterface, _: Int ->
                    initDatabase(forceInit = true).invokeOnCompletion {
                        dialogInterface.dismiss()
                        lifecycleScope.launch { recreate() }
                    }
                }
                //.setNegativeButtonColor(resources.getColor(R.color.red, this@MainActivity.theme))
                //.setNegativeButtonProgress(true)
                .setPositiveButton(getString(R.string.ok), null)
                .setOnDismissListener { setCurrentItem() }
                .create()
                .show()
        }
    }

    private suspend fun easterEggDialog() {
        if (getUserSettings().showEasterEggHint) {
            AlertDialog.Builder(this@MainActivity)
                .setTitle(getString(R.string.easterEggs))
                .setMessage(getString(R.string.easterEggsText))
                .setNegativeButton(getString(R.string.deactivate)) { dialogInterface: DialogInterface, _: Int ->
                    lifecycleScope.launch {
                        updateUserSettings { it.copy(easterEggsEnabled = false, showEasterEggHint = false) }
                        delay(700)
                        dialogInterface.dismiss()
                    }
                }
                .setPositiveButton(getString(R.string.ok)) { _: DialogInterface, _: Int ->
                    lifecycleScope.launch {
                        updateUserSettings { it.copy(showEasterEggHint = false) }
                    }
                }
                //.setNegativeButtonColor(resources.getColor(R.color.red, this@MainActivity.theme))
                //.setNegativeButtonProgress(true)
                .setOnDismissListener { lifecycleScope.launch { showTipPopup() } }
                .create()
                .show()
        } else {
            showTipPopup()
        }
    }

    private fun initTipPopup() {
        /*
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
        tipPopupMenuButton.setMessage(getString(R.string.mute) + ", " + getString(R.string.dndDescription) + " " + getString(R.string.or) + " " + getString(R.string.openOfficialApp))
        tipPopupOkButton.setMessage(getString(R.string.okButtonTip))
         */
    }

    private suspend fun showTipPopup() {
        if (getUserSettings().showMainTips) {
            /*
            updateUserSettings { it.copy(showMainTips = false) }
            try {
                initTipPopup()
                val drawerButtonView = drawerLayout.findViewById<View>(de.dlyt.yanndroid.oneui.R.id.toolbar_layout_navigationButton)
                val outLocation = IntArray(2)
                drawerButtonView.getLocationOnScreen(outLocation)
                tipPopupDrawer.setTargetPosition(
                    outLocation[0] + drawerButtonView.width / 2,
                    outLocation[1] + drawerButtonView.height / 2 + resources.getDimensionPixelSize(de.dlyt.yanndroid.oneui.R.dimen.sesl_action_button_icon_size)
                )
                tipPopupDrawer.show(TipPopup.DIRECTION_BOTTOM_RIGHT)
            } catch (e: Exception) {
                // android.view.WindowManager$BadTokenException: Unable to add window -- token null is not valid; is your activity running?
                e.printStackTrace()
            }
             */
        }
    }
}