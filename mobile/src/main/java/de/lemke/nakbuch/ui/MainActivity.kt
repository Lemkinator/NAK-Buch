package de.lemke.nakbuch.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
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
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
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
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.settings.*
import de.lemke.nakbuch.domain.utils.TabsManager

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    companion object {
        var colorSettingChanged = false
    }
    private var mContext: Context = this
    private var mFragment: Fragment? = null
    private lateinit var mFragmentManager: FragmentManager
    private lateinit var mTabsManager: TabsManager
    private lateinit var buchMode: BuchMode
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tabLayout: TabLayout
    private lateinit var og1: OptionGroup
    private lateinit var searchHelpFAB: FloatingActionButton
    private lateinit var tipPopupDrawer: TipPopup
    private lateinit var tipPopupSearch: TipPopup
    private lateinit var tipPopupSwitchBuchMode: TipPopup
    private lateinit var tipPopupMenuButton: TipPopup
    private lateinit var tipPopupOkButton: TipPopup
    private var activityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult? ->
            drawerLayout.onSearchModeVoiceInputResult(result)
        }
    private var time: Long = 0
    private val mHandler = Handler(Looper.getMainLooper())
    private val showSearchRunnable = Runnable { setFragment(3) }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtil(this, "4099ff")
        //val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        time = System.currentTimeMillis()
        mContext = this
        when (CheckAppStartUseCase()()) {
            CheckAppStartUseCase.AppStart.FIRST_TIME -> {}
            CheckAppStartUseCase.AppStart.FIRST_TIME_VERSION -> {}
            CheckAppStartUseCase.AppStart.NORMAL -> {}
            CheckAppStartUseCase.AppStart.OLD_ARCHITECTURE -> {}
        }
        init()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val hints: HashSet<String> = GetHintsUseCase()()
        if (hints.contains("appIntroduction")) {
            SetBooleanSettingUseCase()("mainTips", true)
            SetBooleanSettingUseCase()("textviewTips", true)
            SetBooleanSettingUseCase()("imageviewTips", true)
            hints.remove("appIntroduction")
            SetHintsUseCase()(hints)
        }
        if (hints.contains("appHint")) {
            AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.hint) + ":")
                .setMessage(getString(R.string.appHintText))
                .setNegativeButton(getString(R.string.dontShowAgain)) { _: DialogInterface?, _: Int ->
                    hints.remove("appHint")
                    SetHintsUseCase()(hints)
                }
                .setPositiveButton(getString(R.string.ok), null)
                .setOnDismissListener { easterEggDialog() }
                .create()
                .show()

        } else {
            easterEggDialog()
        }
    }

    public override fun attachBaseContext(context: Context) {
        // pre-OneUI
        if (Build.VERSION.SDK_INT <= 28) {
            super.attachBaseContext(ThemeUtil.createDarkModeContextWrapper(context))
        } else super.attachBaseContext(context)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // pre-OneUI
        if (Build.VERSION.SDK_INT <= 28) {
            val res = resources
            res.configuration.setTo(ThemeUtil.createDarkModeConfig(mContext, newConfig))
        }
    }

    override fun onResume() {
        super.onResume()
        val newBuchMode = GetBuchModeUseCase()()
        if (newBuchMode != buchMode) {
            buchMode = newBuchMode
        }
        setCurrentItem()
        if (colorSettingChanged) {
            colorSettingChanged = false
            recreate()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun init() {
        ViewSupport.semSetRoundedCorners(window.decorView, 0)
        drawerLayout = findViewById(R.id.drawer_view)
        tabLayout = findViewById(R.id.main_tabs)
        buchMode = GetBuchModeUseCase()()
        buchMode =
            if (intent.getBooleanExtra("gesangbuchSelected", buchMode == BuchMode.Gesangbuch)) BuchMode.Gesangbuch
            else BuchMode.Chorbuch
        SetBuchModeUseCase()(buchMode)

        mTabsManager = TabsManager(mContext, getString(R.string.preferenceFileDefault))
        mTabsManager.setTabPosition(0)
        mFragmentManager = supportFragmentManager

        //DrawerLayout
        drawerLayout.setDrawerButtonOnClickListener { startActivity(Intent().setClass(mContext, AboutActivity::class.java)) }
        drawerLayout.setDrawerButtonTooltip(getText(R.string.aboutApp))
        drawerLayout.inflateToolbarMenu(R.menu.main)
        drawerLayout.setOnToolbarMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.search -> {
                    drawerLayout.setSearchModeListener(object : ToolbarLayout.SearchModeListener() {
                        override fun onSearchOpened(search_edittext: EditText) {
                            search_edittext.setText(GetSearchUseCase()())
                            search_edittext.setSelection(0, search_edittext.text.length)
                            //setFragment(3)
                            searchHelpFAB.visibility = View.VISIBLE
                        }

                        override fun onSearchDismissed(search_edittext: EditText) {
                            setCurrentItem()
                            searchHelpFAB.visibility = View.GONE
                        }

                        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

                        override fun afterTextChanged(s: Editable) {
                            //if (s.toString().isNotEmpty())
                                SetSearchUseCase()(s.toString())
                            mHandler.removeCallbacks(showSearchRunnable)
                            mHandler.post(showSearchRunnable)
                            if (s.toString().replace(" ", "").equals("easteregg", ignoreCase = true)) {
                                setFragment(3)
                                Handler(Looper.getMainLooper()).postDelayed({ s.clear() }, 1500)
                                val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                                inputManager.hideSoftInputFromWindow(
                                    currentFocus!!.windowToken,
                                    InputMethodManager.HIDE_NOT_ALWAYS
                                )
                            }
                        }

                        override fun onKeyboardSearchClick(s: CharSequence) {
                            SetSearchUseCase()(s.toString())
                            setFragment(3)
                        }

                        override fun onVoiceInputClick(intent: Intent) {
                            activityResultLauncher.launch(intent)
                        }
                    })
                    drawerLayout.showSearchMode()
                }
                R.id.switchBuchMode -> {
                    buchMode = if (buchMode == BuchMode.Gesangbuch) BuchMode.Chorbuch else BuchMode.Gesangbuch
                    SetBuchModeUseCase()(buchMode)
                    setCurrentItem()
                }
                R.id.mute -> {
                    if(!MuteUseCase()()) Toast.makeText(mContext, mContext.getString(R.string.failedToMuteStreams), Toast.LENGTH_SHORT)
                        .show()
                }
                R.id.dnd -> {
                    DoNotDisturbUseCase()(mContext)
                }
                //R.id.info -> { startActivity(Intent().setClass(mContext, AboutActivity::class.java)) }
                //R.id.settings -> { startActivity(Intent().setClass(mContext,SettingsActivity::class.java)) }
            }
            true
        }
        searchHelpFAB = findViewById(R.id.searchHelpFAB)
        searchHelpFAB.backgroundTintList =
            ResourcesCompat.getColorStateList(resources, de.dlyt.yanndroid.oneui.R.color.sesl_swipe_refresh_background, theme)
        searchHelpFAB.supportImageTintList =
            ResourcesCompat.getColorStateList(resources, de.dlyt.yanndroid.oneui.R.color.sesl_tablayout_selected_indicator_color, theme)
        Tooltip.setTooltipText(searchHelpFAB, getString(R.string.help))
        searchHelpFAB.setOnClickListener {
            val searchModes = arrayOf<CharSequence>(getString(R.string.onlyExactSearchText), getString(R.string.searchForAllPartialWords))
            AlertDialog.Builder(mContext)
                .setTitle(R.string.help)
                .setMessage(R.string.searchHelp)
                .setNeutralButton(R.string.ok, null)
                .setNegativeButton(R.string.changeSearchMode) { _: DialogInterface, _: Int ->
                    AlertDialog.Builder(mContext)
                        .setTitle(getString(R.string.setStandardSearchMode))
                        .setNeutralButton(R.string.ok, null)
                        .setSingleChoiceItems(searchModes, if (IsSearchModeAlternativeUseCase()()) 1 else 0)
                        { _: DialogInterface, i: Int -> SetSearchModeAlternativeUseCase()(i == 1) }
                        .show()
                }
                .create()
                .show()
        }

        og1 = findViewById(R.id.optiongroup)
        og1.setOnOptionButtonClickListener { _: OptionButton, checkedId: Int, _: Int ->
            when (checkedId) {
                R.id.ob_gesangbuch -> {
                    buchMode = BuchMode.Gesangbuch
                    SetBuchModeUseCase()(buchMode)
                    setCurrentItem()
                }
                R.id.ob_chorbuch -> {
                    buchMode = BuchMode.Chorbuch
                    SetBuchModeUseCase()(buchMode)
                    setCurrentItem()
                }
                R.id.ob_settings -> {
                    startActivity(Intent(mContext, SettingsActivity::class.java))
                }
                R.id.ob_about -> {
                    startActivity(Intent(mContext, AboutActivity::class.java))
                }
                R.id.ob_help -> {
                    startActivity(Intent(mContext, HelpActivity::class.java))
                }
                R.id.ob_about_me -> {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website))))
                }
                R.id.ob_support_me -> {
                    startActivity(Intent(mContext, SupportMeActivity::class.java))
                }
            }
            drawerLayout.setDrawerOpen(false, true)
            updateOptionbuttons()
        }

        // TabLayout
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.number)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.list)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.favourites)))

        tabLayout.addOnTabSelectedListener(object : SamsungTabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: SamsungTabLayout.Tab) {
                mTabsManager.setTabPosition(tab.position)
                setCurrentItem()
            }

            override fun onTabUnselected(tab: SamsungTabLayout.Tab) {}
            override fun onTabReselected(tab: SamsungTabLayout.Tab) {}
        })
        setCurrentItem()

        val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mTabsManager.currentTab != 0) {
                    mTabsManager.setTabPosition(0)
                    setCurrentItem()
                } else {
                    if (GetBooleanSettingUseCase()("confirmExit", true)) {
                        if (System.currentTimeMillis() - time < 3000) {
                            finishAffinity()
                        } else {
                            Toast.makeText(mContext, resources.getString(R.string.pressAgainToExit), Toast.LENGTH_SHORT).show()
                            time = System.currentTimeMillis()
                        }
                    } else {
                        finishAffinity()
                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
        AppUpdateManagerFactory.create(mContext).appUpdateInfo.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                drawerLayout.setButtonBadges(ToolbarLayout.N_BADGE, DrawerLayout.N_BADGE)
            }
        }
    }

    fun setCurrentItem() {
        if (tabLayout.isEnabled) {
            val tabPosition = mTabsManager.currentTab
            val tab = tabLayout.getTabAt(tabPosition)
            if (tab != null) {
                tab.select()
                setFragment(tabPosition)
            }
        }
        updateOptionbuttons()
    }

    private fun updateOptionbuttons() {
        if (buchMode == BuchMode.Gesangbuch) {
            drawerLayout.setTitle(getString(R.string.titleGesangbuch))
            og1.selectedOptionButton = findViewById(R.id.ob_gesangbuch)
        } else {
            drawerLayout.setTitle(getString(R.string.titleChorbuch))
            og1.selectedOptionButton = findViewById(R.id.ob_chorbuch)
        }
    }

    private fun setFragment(tabPosition: Int) {
        val mTabsTagName: Array<String> = resources.getStringArray(R.array.mainactivity_tab_tag)
        val mTabsClassName: Array<String> = resources.getStringArray(R.array.mainactivity_tab_class)
        val tabName = mTabsTagName[tabPosition]
        val fragment = mFragmentManager.findFragmentByTag(tabName)
        mFragment?.let { mFragmentManager.beginTransaction().detach(it).commit() }
        if (fragment != null) {
            mFragment = fragment
            mFragmentManager.beginTransaction().attach(fragment).commit()
        } else {
            //Toast.makeText(applicationContext, "New instance for " + mTabsClassName[tabPosition], Toast.LENGTH_SHORT).show()
            try {
                mFragment = Class.forName(mTabsClassName[tabPosition]).newInstance() as Fragment
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
            mFragmentManager.beginTransaction().add(R.id.fragment_container, mFragment!!, tabName).commit()
        }
    }

    private fun easterEggDialog() {
        if (GetBooleanSettingUseCase()("easterEggHint", true)) {
            AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.easterEggs))
                .setMessage(getString(R.string.easterEggsText))
                .setNegativeButton(getString(R.string.deactivate)) { dialogInterface: DialogInterface, _: Int ->
                    SetEasterEggsEnabledUseCase()(false)

                    Handler(Looper.getMainLooper()).postDelayed({ dialogInterface.dismiss() }, 700)
                }
                .setPositiveButton(getString(R.string.ok), null)
                .setNegativeButtonColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red, mContext.theme))
                .setNegativeButtonProgress(true)
                .setOnDismissListener {
                    showTipPopup()
                }
                .create()
                .show()
            SetBooleanSettingUseCase()("easterEggHint", false)
        } else {
            showTipPopup()
        }
    }

    private fun initTipPopup() {
        val toolbarMenuItemContainer =
            drawerLayout.findViewById<ViewGroup>(de.dlyt.yanndroid.oneui.R.id.toolbar_layout_action_menu_item_container)
        val drawerButtonView = drawerLayout.findViewById<View>(de.dlyt.yanndroid.oneui.R.id.toolbar_layout_navigationButton)
        val searchItemView = toolbarMenuItemContainer.getChildAt(0)
        val switchBuchModeItemView = toolbarMenuItemContainer.getChildAt(1)
        val menuItemView = toolbarMenuItemContainer.getChildAt(2)
        val okButtonView = drawerLayout.findViewById<View>(R.id.b_ok)
        tipPopupDrawer = TipPopup(drawerButtonView) //,TipPopup.MODE_TRANSLUCENT);
        tipPopupSearch = TipPopup(searchItemView) //,TipPopup.MODE_TRANSLUCENT);
        tipPopupSwitchBuchMode = TipPopup(switchBuchModeItemView) //,TipPopup.MODE_TRANSLUCENT);
        tipPopupMenuButton = TipPopup(menuItemView) //,TipPopup.MODE_TRANSLUCENT);
        tipPopupOkButton = TipPopup(okButtonView) //,TipPopup.MODE_TRANSLUCENT);
        tipPopupDrawer.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, theme))
        tipPopupSearch.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, theme))
        tipPopupSwitchBuchMode.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, theme))
        tipPopupMenuButton.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, theme))
        tipPopupOkButton.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, theme))
        tipPopupDrawer.setExpanded(true)
        tipPopupSearch.setExpanded(true)
        tipPopupSwitchBuchMode.setExpanded(true)
        tipPopupMenuButton.setExpanded(true)
        tipPopupOkButton.setExpanded(true)
        tipPopupDrawer.setOnDismissListener { tipPopupSearch.show(TipPopup.DIRECTION_BOTTOM_LEFT) }
        tipPopupSearch.setOnDismissListener { tipPopupSwitchBuchMode.show(TipPopup.DIRECTION_BOTTOM_LEFT) }
        tipPopupSwitchBuchMode.setOnDismissListener { tipPopupMenuButton.show(TipPopup.DIRECTION_BOTTOM_LEFT) }
        tipPopupMenuButton.setOnDismissListener { tipPopupOkButton.show(TipPopup.DIRECTION_TOP_LEFT) }
        tipPopupDrawer.setMessage(getString(R.string.menuGeneralTip))
        tipPopupSearch.setMessage(getString(R.string.searchTip))
        tipPopupSwitchBuchMode.setMessage(getString(R.string.switchModeDescription))
        tipPopupMenuButton.setMessage(getString(R.string.mute) + " " + getString(R.string.or) + " " + getString(R.string.dndMode))
        tipPopupOkButton.setMessage(getString(R.string.okButtonTip))

        //tipPopup2 = new TipPopup(Objects.requireNonNull(tabLayout.getTabAt(0)).seslGetTextView());
        //tipPopup2.setExpanded(true);
        //tipPopup2.setMessage("This is the Number tab");
    }

    private fun showTipPopup() {
        if (GetBooleanSettingUseCase()("mainTips", true)) {
            SetBooleanSettingUseCase()("mainTips", false)
            Handler(Looper.getMainLooper()).postDelayed({
                initTipPopup()
                try {
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
                { i ->
                    if (currentColor != i) ThemeUtil.setColor(
                        this@MainActivity,
                        Color.red(i),
                        Color.green(i),
                        Color.blue(i)
                    )
                },
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
                { i ->
                    if (currentColor != i) ThemeUtil.setColor(
                        this@MainActivity,
                        Color.red(i),
                        Color.green(i),
                        Color.blue(i)
                    )
                },
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
                Handler(Looper.getMainLooper()).postDelayed(
                    { dialogInterface.dismiss() },
                    700
                )
            }
            .setPositiveButton("Yes") { dialogInterface: DialogInterface, _: Int ->
                Handler(Looper.getMainLooper()).postDelayed(
                    { dialogInterface.dismiss() },
                    700
                )
            }
            .setNegativeButtonColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red, mContext.theme))
            .setPositiveButtonColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_green, mContext.theme))
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
        val dialog = ProgressDialog(mContext)
        dialog.isIndeterminate = true
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setTitle("Title")
        dialog.setMessage("ProgressDialog")
        dialog.setButton(
            ProgressDialog.BUTTON_NEGATIVE,
            "Cancel",
            null as DialogInterface.OnClickListener?
        )
        dialog.setOnCancelListener { progressDialogCircleOnly(view) }
        dialog.show()
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    private fun progressDialogCircleOnly(view: View) {
        val dialog = ProgressDialog(mContext)
        dialog.setProgressStyle(ProgressDialog.STYLE_CIRCLE)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setOnCancelListener {
            Snackbar.make(view, "Text label", Snackbar.LENGTH_SHORT).setAction("Action") { }.show()
        }
        dialog.show()
    }
}