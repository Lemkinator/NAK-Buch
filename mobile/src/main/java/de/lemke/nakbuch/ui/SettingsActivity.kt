package de.lemke.nakbuch.ui

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.util.SeslMisc
import androidx.lifecycle.lifecycleScope
import androidx.preference.*
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.nakbuch.R
import de.lemke.nakbuch.data.Quality
import de.lemke.nakbuch.data.Resolution
import de.lemke.nakbuch.domain.*
import de.lemke.nakbuch.domain.hymnUseCases.SetPrivateTextsUseCase
import dev.oneuiproject.oneui.dialog.ProgressDialog
import dev.oneuiproject.oneui.layout.DrawerLayout
import dev.oneuiproject.oneui.preference.internal.PreferenceRelatedCard
import dev.oneuiproject.oneui.utils.PreferenceUtils.createRelatedCard
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.setNavigationButtonTooltip(getString(R.string.sesl_navigate_up))
        drawerLayout.setNavigationButtonIcon(AppCompatResources.getDrawable(this, R.drawable.ic_baseline_oui_back_24))
        drawerLayout.setNavigationButtonOnClickListener { onBackPressed() }
        if (savedInstanceState == null) supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment()).commit()
    }

    @AndroidEntryPoint
    class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
        private lateinit var settingsActivity: SettingsActivity
        private lateinit var pickTextsActivityResultLauncher: ActivityResultLauncher<String>
        private lateinit var pickFolderActivityResultLauncher: ActivityResultLauncher<Uri>
        //private lateinit var darkModePref: HorizontalRadioPreference
        private lateinit var darkModePref: SwitchPreferenceCompat
        private lateinit var autoDarkModePref: SwitchPreferenceCompat
        //private lateinit var colorPickerPref: ColorPickerPreference
        private lateinit var confirmExitPref: CheckBoxPreference
        private lateinit var easterEggsPref: SeslSwitchPreferenceScreen
        private lateinit var historyPref: SeslSwitchPreferenceScreen
        //private lateinit var hintsPref: MultiSelectListPreference
        private lateinit var qualityPref: DropDownPreference
        private lateinit var resolutionPref: DropDownPreference
        //private lateinit var versionHiddenMenuPref: PreferenceScreen
        //private var tipCard: TipsCardViewPreference? = null
        //private var tipCardSpacing: PreferenceCategory? = null
        private var relatedCard: PreferenceRelatedCard? = null
        private var lastTimeVersionClicked: Long = 0
        private var clickCounter = 0

        @Inject
        lateinit var setPrivateTexts: SetPrivateTextsUseCase

        @Inject
        lateinit var getUserSettings: GetUserSettingsUseCase

        @Inject
        lateinit var updateUserSettings: UpdateUserSettingsUseCase

        @Inject
        lateinit var getHints: GetHintsUseCase

        @Inject
        lateinit var setHints: SetHintsUseCase

        @Inject
        lateinit var getRecentColors: GetRecentColorsUseCase

        @Inject
        lateinit var setRecentColors: SetRecentColorsUseCase

        @Inject
        lateinit var mute: MuteUseCase

        @Inject
        lateinit var doNotDisturb: DoNotDisturbUseCase

        @Inject
        lateinit var initDataBase: InitDatabaseUseCase

        override fun onAttach(context: Context) {
            super.onAttach(context)
            if (activity is SettingsActivity) settingsActivity = activity as SettingsActivity
        }

        override fun onCreatePreferences(bundle: Bundle?, str: String?) {
            addPreferencesFromResource(R.xml.preferences)
        }

        override fun onCreate(bundle: Bundle?) {
            super.onCreate(bundle)
            lastTimeVersionClicked = System.currentTimeMillis()
            pickTextsActivityResultLauncher = registerForActivityResult(GetMultipleContents()) { uris: List<Uri>? ->
                lifecycleScope.launch {
                    val dialog = ProgressDialog(settingsActivity)
                    dialog.setCancelable(false)
                    dialog.setTitle("Eigene LiedTexte werden hinzugefügt...")
                    dialog.setButton(ProgressDialog.BUTTON_NEUTRAL, getString(R.string.ok)) { _: DialogInterface, _: Int -> dialog.dismiss() }
                    dialog.show()
                    updateUserSettings { it.copy(usingPrivateTexts = true) }
                    val result = setPrivateTexts(uris)
                    dialog.dismiss()
                    AlertDialog.Builder(settingsActivity)
                        .setTitle(R.string.result)
                        .setMessage(result)
                        .setPositiveButton(R.string.ok, null)
                        .create()
                        .show()
                }
            }
            pickFolderActivityResultLauncher =
                registerForActivityResult(OpenDocumentTree()) { result: Uri? ->
                    if (result == null) Toast.makeText(settingsActivity, "Fehler: Kein Ordner Ausgewählt", Toast.LENGTH_LONG).show()
                    else Toast.makeText(settingsActivity, getString(R.string.notYetImplemented), Toast.LENGTH_LONG).show()
                }
            initPreferences()
        }

        @SuppressLint("RestrictedApi", "UnspecifiedImmutableFlag")
        private fun initPreferences() {
            val darkMode = AppCompatDelegate.getDefaultNightMode()
            //darkModePref = findPreference("dark_mode")!!
            darkModePref = findPreference("dark_mode")!!
            autoDarkModePref = findPreference("dark_mode_auto")!!
            confirmExitPref = findPreference("confirmExit")!!
            historyPref = findPreference("historyEnabled")!!
            easterEggsPref = findPreference("easterEggsEnabled")!!
            //hintsPref = findPreference("hints")!!
            resolutionPref = findPreference("imgResolution")!!
            qualityPref = findPreference("imgQuality")!!
            //versionHiddenMenuPref = findPreference("version_hidden_menu")!!
            //colorPickerPref = findPreference("color")!!

            historyPref.onPreferenceChangeListener = this
            easterEggsPref.onPreferenceChangeListener = this
            /*
            darkModePref.onPreferenceChangeListener = this
            darkModePref.setDividerEnabled(false)
            darkModePref.setTouchEffectEnabled(false)
            darkModePref.isEnabled = darkMode != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            darkModePref.value = if (SeslMisc.isLightTheme(settingsActivity)) "0" else "1"
             */
            darkModePref.onPreferenceChangeListener = this
            darkModePref.isEnabled = darkMode != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM ||
                    darkMode != AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            darkModePref.isChecked = !(SeslMisc.isLightTheme(settingsActivity))
            autoDarkModePref.onPreferenceChangeListener = this
            autoDarkModePref.isChecked = darkMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM ||
                    darkMode == AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            lifecycleScope.launch {
                /*
                val recentColors = getRecentColors().toMutableList()
                for (recent_color in recentColors) colorPickerPref.onColorSet(recent_color)
                colorPickerPref.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { _: Preference?, colorInt: Any ->
                        recentColors.add(colorInt as Int)
                        lifecycleScope.launch { setRecentColors(recentColors) }
                        val color = Color.valueOf(colorInt)
                        ThemeUtil.setColor(settingsActivity, color.red(), color.green(), color.blue())
                        MainActivity.refreshView = true
                        true
                    }
                 */
            }
            findPreference<Preference>("audio_streams")!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    if (!mute()) Toast.makeText(
                        settingsActivity,
                        settingsActivity.getString(R.string.failedToMuteStreams),
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }
            findPreference<Preference>("dnd")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                doNotDisturb()
                true
            }
            confirmExitPref.onPreferenceChangeListener = this
            //hintsPref.onPreferenceChangeListener = this
            //lifecycleScope.launch { hintsPref.values = getHints() }
            resolutionPref.onPreferenceChangeListener = this
            qualityPref.onPreferenceChangeListener = this
            findPreference<Preference>("shortcut_gesangbuch")!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { createShortcut("gesangbuch") }
            findPreference<Preference>("shortcut_chorbuch")!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { createShortcut("chorbuch") }
            findPreference<Preference>("shortcut_jugendliederbuch")!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { createShortcut("jugendliederbuch") }
            findPreference<Preference>("shortcut_jb_ergaenzungsheft")!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { createShortcut("jb_ergaenzungsheft") }
            findPreference<PreferenceScreen>("privacy")!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacyWebsite))))
                    true
                }
            /*try {
                versionHiddenMenuPref.title =
                    settingsActivity.getString(dev.oneuiproject.oneui.R.string.version_info, settingsActivity.packageManager.getPackageInfo(
                        settingsActivity.packageName,
                        0
                    ).versionName)
            } catch (nnfe: NameNotFoundException) {
                nnfe.printStackTrace()
            }
            versionHiddenMenuPref.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    if (System.currentTimeMillis() - lastTimeVersionClicked < 400) {
                        clickCounter++
                        if (clickCounter > 10) {
                            clickCounter = 0
                            val hiddenMenu = arrayOf<CharSequence>(
                                getString(R.string.addOwnHymnTexts),
                                getString(R.string.deleteOwnHymnTexts),
                                getString(R.string.sheetMusic),
                                getString(R.string.deleteAppDataAndExit)
                            )
                            var option = 0
                            AlertDialog.Builder(settingsActivity)
                                .setCancelable(false)
                                .setTitle(R.string.hiddenMenu)
                                .setNegativeButton(R.string.sesl_cancel, null)
                                .setPositiveButton(R.string.ok) { dialogInterface: DialogInterface, _: Int ->
                                    when (option) {
                                        0 -> {
                                            pickTextsActivityResultLauncher.launch("text/plain")
                                            dialogInterface.dismiss()
                                            MainActivity.refreshView = true
                                        }
                                        1 -> lifecycleScope.launch {
                                            initDataBase(forceInit = true).invokeOnCompletion { dialogInterface.dismiss() }
                                            MainActivity.refreshView = true
                                        }
                                        2 -> {
                                            pickFolderActivityResultLauncher.launch(
                                                Uri.fromFile(File(Environment.getExternalStorageDirectory().absolutePath))
                                            )
                                            dialogInterface.dismiss()
                                        }
                                        3 -> {
                                            AlertDialog.Builder(settingsActivity)
                                                .setCancelable(false)
                                                .setTitle(R.string.deleteAppDataAndExit)
                                                .setMessage(R.string.deleteAppDataAndExitWarning)
                                                .setNegativeButton(R.string.sesl_cancel, null)
                                                .setPositiveButton(R.string.ok) { _: DialogInterface, _: Int ->
                                                    (settingsActivity.getSystemService(ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
                                                }
                                                .create()
                                                .show()
                                            dialogInterface.dismiss()
                                        }
                                    }
                                }
                                .setSingleChoiceItems(hiddenMenu, 1) { _: DialogInterface, i: Int -> option = i }
                                //.setPositiveButtonProgress(true)
                                .create()
                                .show()


                        }
                    } else clickCounter = 0
                    lastTimeVersionClicked = System.currentTimeMillis()
                    true
                }
            */
            /*
            tipCard = findPreference("tip_card_preference")
            tipCardSpacing = findPreference("spacing_tip_card")
            tipCard?.setTipsCardListener(object : TipsCardViewPreference.TipsCardListener {
                override fun onCancelClicked(view: View) {
                    tipCard!!.isVisible = false
                    tipCardSpacing?.isVisible = false
                    lifecycleScope.launch {
                        val hints: MutableSet<String> = getHints().toMutableSet()
                        hints.remove("tipcard")
                        hintsPref.values = hints
                        setHints(hints)
                    }
                }

                override fun onViewClicked(view: View) {
                    startActivity(Intent(settingsActivity, HelpActivity::class.java))
                }
            })
            */
        }

        private fun createShortcut(id: String): Boolean {
            val shortcutManager = settingsActivity.getSystemService(ShortcutManager::class.java)
            if (shortcutManager.isRequestPinShortcutSupported) {
                val pinShortcutInfo = ShortcutInfo.Builder(settingsActivity, id).build()
                val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo)
                val successCallback: PendingIntent =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        PendingIntent.getBroadcast(settingsActivity, 0, pinnedShortcutCallbackIntent, PendingIntent.FLAG_MUTABLE)
                    else
                        PendingIntent.getBroadcast(settingsActivity, 0, pinnedShortcutCallbackIntent, 0)
                shortcutManager.requestPinShortcut(pinShortcutInfo, successCallback.intentSender)
            }
            return true
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            requireView().setBackgroundColor(
                resources.getColor(dev.oneuiproject.oneui.R.color.oui_background_color, settingsActivity.theme)
            )
        }

        override fun onStart() {
            super.onStart()
            lifecycleScope.launch {
                confirmExitPref.isChecked = getUserSettings().confirmExit
                easterEggsPref.isChecked = getUserSettings().easterEggsEnabled
                historyPref.isChecked = getUserSettings().historyEnabled
                //val showTipCard = getHints().contains("tipcard")
                //tipCard?.isVisible = showTipCard
                //tipCardSpacing?.isVisible = showTipCard
            }
            setRelatedCardView()
        }

        @Suppress("UNCHECKED_CAST")
        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            //val currentDarkMode = ThemeUtil.getDarkMode(settingsActivity).toString()
            when (preference.key) {
                "dark_mode" -> {
                    val currentDarkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
                    if (currentDarkMode == newValue as Boolean) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    return true
                    /*
                    if (currentDarkMode !== newValue) {
                        ThemeUtil.setDarkMode(
                            settingsActivity,
                            if (newValue == "0") ThemeUtil.DARK_MODE_DISABLED else ThemeUtil.DARK_MODE_ENABLED
                        )
                    }
                    return true
                     */
                }
                "dark_mode_auto" -> {
                    if (newValue as Boolean) {
                        darkModePref.isEnabled = false
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    } else darkModePref.isEnabled = true
                    return true
                    /*
                    if (newValue as Boolean) {
                        darkModePref.isEnabled = false
                        ThemeUtil.setDarkMode(settingsActivity, ThemeUtil.DARK_MODE_AUTO)
                    } else darkModePref.isEnabled = true
                    return true
                     */
                }
                "confirmExit" -> {
                    lifecycleScope.launch { updateUserSettings { it.copy(confirmExit = newValue as Boolean) } }
                    return true
                }
                "historyEnabled" -> {
                    lifecycleScope.launch { updateUserSettings { it.copy(historyEnabled = newValue as Boolean) } }
                    return true
                }
                "easterEggsEnabled" -> {
                    lifecycleScope.launch { updateUserSettings { it.copy(easterEggsEnabled = newValue as Boolean) } }
                    return true
                }
                "hints" -> {
                    /*lifecycleScope.launch {
                        val hintSet = newValue as MutableSet<String>
                        setHints(hintSet)
                        hintsPref.values = hintSet
                        tipCard?.isVisible = hintSet.contains("tipcard")
                        tipCardSpacing?.isVisible = hintSet.contains("tipcard")
                    }
                    return true*/
                }
                "imgResolution" -> {
                    val resolution = when (newValue as String) {
                        getString(R.string.veryLow) -> Resolution.VERY_LOW
                        getString(R.string.low) -> Resolution.LOW
                        getString(R.string.medium) -> Resolution.MEDIUM
                        getString(R.string.high) -> Resolution.HIGH
                        getString(R.string.veryHigh) -> Resolution.VERY_HIGH
                        else -> Resolution.MEDIUM
                    }
                    lifecycleScope.launch { updateUserSettings { it.copy(photoResolution = resolution) } }
                    return true
                }
                "imgQuality" -> {
                    val quality = when (newValue as String) {
                        getString(R.string.veryLow) -> Quality.VERY_LOW
                        getString(R.string.low) -> Quality.LOW
                        getString(R.string.medium) -> Quality.MEDIUM
                        getString(R.string.high) -> Quality.HIGH
                        getString(R.string.veryHigh) -> Quality.VERY_HIGH
                        else -> Quality.MEDIUM
                    }
                    lifecycleScope.launch { updateUserSettings { it.copy(photoQuality = quality) } }
                    return true
                }
            }
            return false
        }

        private fun setRelatedCardView() {
            if (relatedCard == null) {
                relatedCard = createRelatedCard(settingsActivity)
                relatedCard?.setTitleText(getString(dev.oneuiproject.oneui.R.string.oui_relative_description))
                relatedCard?.addButton(getString(R.string.help)) { startActivity(Intent(settingsActivity, HelpActivity::class.java)) }
                    ?.addButton(getString(R.string.aboutMe)) { startActivity(Intent(settingsActivity, AboutMeActivity::class.java)) }
                    ?.show(this)
            }
        }
    }
}