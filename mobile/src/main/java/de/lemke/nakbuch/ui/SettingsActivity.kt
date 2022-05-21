package de.lemke.nakbuch.ui

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.util.SeslMisc
import dagger.hilt.android.AndroidEntryPoint
import de.dlyt.yanndroid.oneui.dialog.AlertDialog
import de.dlyt.yanndroid.oneui.dialog.ProgressDialog
import de.dlyt.yanndroid.oneui.layout.DrawerLayout
import de.dlyt.yanndroid.oneui.layout.PreferenceFragment
import de.dlyt.yanndroid.oneui.preference.*
import de.dlyt.yanndroid.oneui.preference.internal.PreferencesRelatedCard
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.lemke.nakbuch.R
import de.lemke.nakbuch.data.Quality
import de.lemke.nakbuch.data.Resolution
import de.lemke.nakbuch.domain.*
import de.lemke.nakbuch.domain.hymnUseCases.SetPrivateTextsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil(this)
        setContentView(R.layout.activity_settings)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.setNavigationButtonTooltip(getString(de.dlyt.yanndroid.oneui.R.string.sesl_navigate_up))
        drawerLayout.setNavigationButtonIcon(AppCompatResources.getDrawable(this, de.dlyt.yanndroid.oneui.R.drawable.ic_oui_back))
        drawerLayout.setNavigationButtonOnClickListener { onBackPressed() }
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment()).commit()
        }
    }

    @AndroidEntryPoint
    class SettingsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {
        private val coroutineContext: CoroutineContext = Dispatchers.Main
        private val coroutineScope: CoroutineScope = CoroutineScope(coroutineContext)
        private lateinit var mContext: Context
        private lateinit var mActivity: SettingsActivity
        private var lastTimeVersionClicked: Long = 0
        private var clickCounter = 0
        private lateinit var pickTextsActivityResultLauncher: ActivityResultLauncher<String>
        private lateinit var pickFolderActivityResultLauncher: ActivityResultLauncher<Uri>

        private lateinit var darkModePref: HorizontalRadioPreference
        private lateinit var autoDarkModePref: SwitchPreference
        private lateinit var colorPickerPref: ColorPickerPreference
        private lateinit var confirmExitPref: CheckBoxPreference
        private lateinit var easterEggsPref: SwitchPreferenceScreen
        private lateinit var historyPref: SwitchPreferenceScreen
        private lateinit var hintsPref: MultiSelectListPreference
        private lateinit var qualityPref: DropDownPreference
        private lateinit var resolutionPref: DropDownPreference
        private lateinit var versionHiddenMenuPref: PreferenceScreen
        private var tipCard: TipsCardViewPreference? = null
        private var tipCardSpacing: PreferenceCategory? = null
        private var mRelatedCard: PreferencesRelatedCard? = null

        @Inject
        lateinit var setPrivateTexts: SetPrivateTextsUseCase

        @Inject
        lateinit var getUserSettings: GetUserSettingsUseCase

        @Inject
        lateinit var updateUserSettings: UpdateUserSettingsUseCase

        @Inject
        lateinit var mute: MuteUseCase

        //@Inject lateinit var doNotDisturb: DoNotDisturbUseCase

        @Inject
        lateinit var initDataBase: InitDataBaseUseCase

        override fun onAttach(context: Context) {
            super.onAttach(context)
            mContext = context
            if (activity is SettingsActivity) mActivity = activity as SettingsActivity
        }

        override fun onCreatePreferences(bundle: Bundle?, str: String?) {
            addPreferencesFromResource(R.xml.preferences)
        }

        override fun onCreate(bundle: Bundle?) {
            super.onCreate(bundle)
            lastTimeVersionClicked = System.currentTimeMillis()
            pickTextsActivityResultLauncher = registerForActivityResult(GetMultipleContents()) { result: List<Uri>? ->
                coroutineScope.launch {
                    val dialog = ProgressDialog(mContext)
                    dialog.isIndeterminate = false
                    dialog.setTitle("Eigene LiedTexte werden hinzugefügt...")
                    dialog.setButton(
                        ProgressDialog.BUTTON_NEUTRAL,
                        getString(R.string.ok)
                    ) { _: DialogInterface, _: Int -> dialog.dismiss() }
                    dialog.show()
                    val setPrivateTextsResult = setPrivateTexts(result)
                    dialog.setTitle("Eigene LiedTexte wurden hinzugefügt")
                    dialog.setMessage(setPrivateTextsResult)
                }
            }
            pickFolderActivityResultLauncher =
                registerForActivityResult(OpenDocumentTree()) { result: Uri? ->
                    if (result == null) Toast.makeText(mContext, "Fehler: Kein Ordner Ausgewählt", Toast.LENGTH_LONG).show()
                    else Toast.makeText(mContext, getString(R.string.notYetImplemented), Toast.LENGTH_LONG).show()
                }
            initPreferences()
        }

        @SuppressLint("RestrictedApi", "UnspecifiedImmutableFlag")
        private fun initPreferences() {
            val darkMode = ThemeUtil.getDarkMode(mContext)
            darkModePref = findPreference("dark_mode")!!
            autoDarkModePref = findPreference("dark_mode_auto")!!
            confirmExitPref = findPreference("confirmExit")!!
            historyPref = findPreference("historyEnabled")!!
            easterEggsPref = findPreference("easterEggs")!!
            hintsPref = findPreference("hints")!!
            resolutionPref = findPreference("imgResolution")!!
            qualityPref = findPreference("imgQuality")!!
            versionHiddenMenuPref = findPreference("version_hidden_menu")!!
            colorPickerPref = findPreference("color")!!

            darkModePref.onPreferenceChangeListener = this
            darkModePref.setDividerEnabled(false)
            darkModePref.setTouchEffectEnabled(false)
            darkModePref.isEnabled = darkMode != ThemeUtil.DARK_MODE_AUTO
            darkModePref.value = if (SeslMisc.isLightTheme(mContext)) "0" else "1"
            autoDarkModePref.onPreferenceChangeListener = this
            autoDarkModePref.isChecked = darkMode == ThemeUtil.DARK_MODE_AUTO
            coroutineScope.launch {
                val recentColors = getUserSettings().recentColorList
                for (recent_color in recentColors) colorPickerPref.onColorSet(recent_color)
                colorPickerPref.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { _: Preference?, colorInt: Any ->
                        val color = Color.valueOf((colorInt as Int))
                        recentColors.add(colorInt)
                        coroutineScope.launch { updateUserSettings { it.copy(recentColorList = recentColors) } }
                        ThemeUtil.setColor(mActivity, color.red(), color.green(), color.blue())
                        MainActivity.colorSettingChanged = true
                        true
                    }
            }
            findPreference<Preference>("audio_streams")!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    if (!mute()) Toast.makeText(mContext, mContext.getString(R.string.failedToMuteStreams), Toast.LENGTH_SHORT).show()
                    true
                }
            findPreference<Preference>("dnd")!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    DoNotDisturbUseCase(mContext)()//doNotDisturb()
                    true
                }
            confirmExitPref.onPreferenceChangeListener = this
            hintsPref.onPreferenceChangeListener = this
            coroutineScope.launch { hintsPref.values = getUserSettings().hintSet }
            resolutionPref.onPreferenceChangeListener = this
            qualityPref.onPreferenceChangeListener = this
            findPreference<Preference>("shortcut_gesangbuch")!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { createShortcut("gesangbuch") }
            findPreference<Preference>("shortcut_chorbuch")!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { createShortcut("chorbuch") }
            findPreference<Preference>("shortcut_jugendliederbuch")!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { createShortcut("jugendliederbuch") }
            findPreference<PreferenceScreen>("privacy")!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacyWebsite))))
                    true
                }
            try {
                versionHiddenMenuPref.title =
                    mContext.getString(de.dlyt.yanndroid.oneui.R.string.sesl_version) + " " + mContext.packageManager.getPackageInfo(
                        mContext.packageName,
                        0
                    ).versionName
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
                            AlertDialog.Builder(mContext)
                                .setCancelable(false)
                                .setTitle(R.string.hiddenMenu)
                                .setNegativeButton(de.dlyt.yanndroid.oneui.R.string.sesl_cancel, null)
                                .setPositiveButton(R.string.ok) { dialogInterface: DialogInterface, _: Int ->
                                    when (option) {
                                        0 -> {
                                            pickTextsActivityResultLauncher.launch("text/plain")
                                            dialogInterface.dismiss()
                                        }
                                        1 -> coroutineScope.launch {
                                            initDataBase(forceInit = true).invokeOnCompletion { dialogInterface.dismiss() }
                                        }
                                        2 -> {
                                            pickFolderActivityResultLauncher.launch(
                                                Uri.fromFile(File(Environment.getExternalStorageDirectory().absolutePath))
                                            )
                                            dialogInterface.dismiss()
                                        }
                                        3 -> {
                                            AlertDialog.Builder(mContext)
                                                .setCancelable(false)
                                                .setTitle(R.string.deleteAppDataAndExit)
                                                .setMessage(R.string.deleteAppDataAndExitWarning)
                                                .setNegativeButton(de.dlyt.yanndroid.oneui.R.string.sesl_cancel, null)
                                                .setPositiveButton(R.string.ok) { _: DialogInterface, _: Int ->
                                                    (mContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
                                                }
                                                .create()
                                                .show()
                                            dialogInterface.dismiss()
                                        }
                                    }
                                }
                                .setSingleChoiceItems(hiddenMenu, 1) { _: DialogInterface, i: Int -> option = i }
                                .setPositiveButtonProgress(true)
                                .create()
                                .show()


                        }
                    } else clickCounter = 0
                    lastTimeVersionClicked = System.currentTimeMillis()
                    true
                }
            tipCard = findPreference("tip_card_preference")
            tipCardSpacing = findPreference("spacing_tip_card")
            tipCard?.setTipsCardListener(object : TipsCardViewPreference.TipsCardListener {
                override fun onCancelClicked(view: View) {
                    tipCard!!.isVisible = false
                    tipCardSpacing?.isVisible = false
                    coroutineScope.launch {
                        val s: MutableSet<String> = getUserSettings().hintSet
                        s.remove("tipcard")
                        hintsPref.values = updateUserSettings { it.copy(hintSet = s) }.hintSet
                    }
                }

                override fun onViewClicked(view: View) {
                    startActivity(Intent(mContext, HelpActivity::class.java))
                }
            })
        }

        private fun createShortcut(id: String): Boolean {
            val shortcutManager = mContext.getSystemService(ShortcutManager::class.java)
            if (shortcutManager.isRequestPinShortcutSupported) {
                val pinShortcutInfo = ShortcutInfo.Builder(mContext, id).build()
                val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo)
                val successCallback: PendingIntent =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.getBroadcast(mContext, 0, pinnedShortcutCallbackIntent, PendingIntent.FLAG_MUTABLE)
                    } else {
                        PendingIntent.getBroadcast(mContext, 0, pinnedShortcutCallbackIntent, 0)
                    }
                shortcutManager.requestPinShortcut(pinShortcutInfo, successCallback.intentSender)
            }
            return true
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            requireView().setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.item_background_color, mContext.theme))
        }

        override fun onStart() {
            super.onStart()
            coroutineScope.launch {
                confirmExitPref.isChecked = getUserSettings().confirmExit
                easterEggsPref.isChecked = getUserSettings().easterEggsEnabled
                historyPref.isChecked = getUserSettings().historyEnabled
                val showTipCard = getUserSettings().hintSet.contains("tipcard")
                tipCard?.isVisible = showTipCard
                tipCardSpacing?.isVisible = showTipCard
            }
            setRelatedCardView()
        }

        @Suppress("UNCHECKED_CAST")
        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            val currentDarkMode = ThemeUtil.getDarkMode(mContext).toString()
            when (preference.key) {
                "dark_mode" -> {
                    if (currentDarkMode !== newValue) {
                        ThemeUtil.setDarkMode(
                            mActivity,
                            if (newValue == "0") ThemeUtil.DARK_MODE_DISABLED else ThemeUtil.DARK_MODE_ENABLED
                        )
                    }
                    return true
                }
                "dark_mode_auto" -> {
                    if (newValue as Boolean) {
                        darkModePref.isEnabled = false
                        ThemeUtil.setDarkMode(mActivity, ThemeUtil.DARK_MODE_AUTO)
                    } else {
                        darkModePref.isEnabled = true
                    }
                    return true
                }
                "confirmExit" -> {
                    coroutineScope.launch { updateUserSettings { it.copy(confirmExit = newValue as Boolean) } }
                    return true
                }
                "hints" -> {
                    coroutineScope.launch {
                        val hintSet = updateUserSettings { it.copy(hintSet = newValue as MutableSet<String>) }.hintSet
                        hintsPref.values = hintSet
                        tipCard?.isVisible = hintSet.contains("tipcard")
                        tipCardSpacing?.isVisible = hintSet.contains("tipcard")
                    }
                    return true
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
                    coroutineScope.launch { updateUserSettings { it.copy(photoResolution = resolution) } }
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
                    coroutineScope.launch { updateUserSettings { it.copy(photoQuality = quality) } }
                    return true
                }
            }
            return false
        }

        private fun setRelatedCardView() {
            if (mRelatedCard == null) {
                mRelatedCard = createRelatedCard(mContext)
                mRelatedCard?.setTitleText(getString(de.dlyt.yanndroid.oneui.R.string.sec_relative_description))
                mRelatedCard?.addButton(getString(R.string.help)) { startActivity(Intent(mContext, HelpActivity::class.java)) }
                    ?.addButton(getString(R.string.aboutMe)) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(getString(R.string.website))
                            )
                        )
                    }
                    ?.addButton(getString(R.string.supportMe)) { startActivity(Intent(mContext, SupportMeActivity::class.java)) }
                    ?.show(this)
            }
        }
    }
}