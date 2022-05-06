package de.lemke.nakbuch.ui

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.NameNotFoundException
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.util.SeslMisc
import de.dlyt.yanndroid.oneui.dialog.AlertDialog
import de.dlyt.yanndroid.oneui.layout.DrawerLayout
import de.dlyt.yanndroid.oneui.layout.PreferenceFragment
import de.dlyt.yanndroid.oneui.preference.*
import de.dlyt.yanndroid.oneui.preference.internal.PreferencesRelatedCard
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.hymns.DeletePrivateTextsUseCase
import de.lemke.nakbuch.domain.hymns.SetPrivateTextsUseCase
import de.lemke.nakbuch.domain.settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil(this)
        setContentView(R.layout.activity_settings)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.setNavigationButtonTooltip(getString(de.dlyt.yanndroid.oneui.R.string.sesl_navigate_up))
        drawerLayout.setNavigationButtonIcon(
            AppCompatResources.getDrawable(this, de.dlyt.yanndroid.oneui.R.drawable.ic_oui_back)
        )
        drawerLayout.setNavigationButtonOnClickListener { onBackPressed() }
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment()).commit()
        }
    }

    class SettingsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {
        private lateinit var mContext: Context
        private lateinit var mActivity: SettingsActivity
        private lateinit var sp: SharedPreferences
        private var lastTimeVersionClicked: Long = 0
        private var clickCounter = 0
        private var tipCard: TipsCardViewPreference? = null
        private var tipCardSpacing: PreferenceCategory? = null
        private var mRelatedCard: PreferencesRelatedCard? = null
        private lateinit var pickTextsActivityResultLauncher: ActivityResultLauncher<String>
        private lateinit var pickFolderActivityResultLauncher: ActivityResultLauncher<Uri>

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
            sp = mContext.getSharedPreferences(getString(R.string.preferenceFileDefault), MODE_PRIVATE)
            pickTextsActivityResultLauncher = registerForActivityResult(GetMultipleContents()) { result: List<Uri>? ->
                Toast.makeText(mContext, SetPrivateTextsUseCase()(mActivity, result), Toast.LENGTH_LONG).show()
            }
            pickFolderActivityResultLauncher =
                registerForActivityResult(OpenDocumentTree()) { result: Uri? ->
                    if (result == null) Toast.makeText(mContext, "Fehler: Kein Ordner Ausgewählt", Toast.LENGTH_LONG).show()
                    else Log.e("Uri", result.toString())
                }
            initPreferences()
        }

        @SuppressLint("RestrictedApi", "UnspecifiedImmutableFlag")
        private fun initPreferences() {
            val darkMode = ThemeUtil.getDarkMode(mContext)
            val darkModePref = findPreference<HorizontalRadioPreference>("dark_mode")
            darkModePref!!.onPreferenceChangeListener = this
            darkModePref.setDividerEnabled(false)
            darkModePref.setTouchEffectEnabled(false)
            darkModePref.isEnabled = darkMode != ThemeUtil.DARK_MODE_AUTO
            darkModePref.value = if (SeslMisc.isLightTheme(mContext)) "0" else "1"
            val autoDarkModePref = findPreference<SwitchPreference>("dark_mode_auto")
            autoDarkModePref!!.onPreferenceChangeListener = this
            autoDarkModePref.isChecked = darkMode == ThemeUtil.DARK_MODE_AUTO
            val colorPickerPref = findPreference<ColorPickerPreference>("color")
            val recentColors = GetRecentColorListUseCase()()
            for (recent_color in recentColors) colorPickerPref!!.onColorSet(recent_color)
            colorPickerPref!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, var2: Any ->
                    val color = Color.valueOf((var2 as Int))
                    recentColors.add(var2)
                    SetRecentColorListUseCase()(recentColors)
                    ThemeUtil.setColor(mActivity, color.red(), color.green(), color.blue())
                    MainActivity.colorSettingChanged = true
                    true
                }
            findPreference<Preference>("dnd")!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    DoNotDisturbUseCase()(mContext)
                    true
                }
            findPreference<Preference>("audio_streams")!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    if (!MuteUseCase()()) Toast.makeText(mContext, mContext.getString(R.string.failedToMuteStreams), Toast.LENGTH_SHORT)
                        .show()
                    true
                }
            findPreference<CheckBoxPreference>("confirmExit")!!.isChecked = GetBooleanSettingUseCase()("confirmExit", true)
            findPreference<Preference>("shortcut_gesangbuch")!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val shortcutManager = mContext.getSystemService(ShortcutManager::class.java)
                    if (shortcutManager.isRequestPinShortcutSupported) {
                        val pinShortcutInfo = ShortcutInfo.Builder(mContext, "gesangbuch").build()
                        val pinnedShortcutCallbackIntent =
                            shortcutManager.createShortcutResultIntent(pinShortcutInfo)
                        val successCallback: PendingIntent =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                PendingIntent.getBroadcast(mContext, 0, pinnedShortcutCallbackIntent, PendingIntent.FLAG_MUTABLE)
                            } else {
                                PendingIntent.getBroadcast(mContext, 0, pinnedShortcutCallbackIntent, 0)
                            }
                        shortcutManager.requestPinShortcut(
                            pinShortcutInfo,
                            successCallback.intentSender
                        )
                    }
                    true
                }
            findPreference<Preference>("shortcut_chorbuch")!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val shortcutManager = mContext.getSystemService(ShortcutManager::class.java)
                    if (shortcutManager.isRequestPinShortcutSupported) {
                        val pinShortcutInfo = ShortcutInfo.Builder(mContext, "chorbuch").build()
                        val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo)
                        val successCallback: PendingIntent =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                PendingIntent.getBroadcast(mContext, 0, pinnedShortcutCallbackIntent, PendingIntent.FLAG_MUTABLE)
                            } else {
                                PendingIntent.getBroadcast(mContext, 0, pinnedShortcutCallbackIntent, 0)
                            }
                        shortcutManager.requestPinShortcut(
                            pinShortcutInfo,
                            successCallback.intentSender
                        )
                    }
                    true
                }
            findPreference<PreferenceScreen>("privacy")!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacyWebsite))))
                    true
                }
            val prefScreenVersion = findPreference<PreferenceScreen>("version_hidden_menu")
            try {
                prefScreenVersion!!.title =
                    mContext.getString(de.dlyt.yanndroid.oneui.R.string.sesl_version) + " " + mContext.packageManager.getPackageInfo(
                        mContext.packageName,
                        0
                    ).versionName
            } catch (nnfe: NameNotFoundException) {
                nnfe.printStackTrace()
            }
            prefScreenVersion!!.onPreferenceClickListener =
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
                                .setPositiveButton(R.string.ok) { _: DialogInterface, _: Int ->
                                    when (option) {
                                        0 -> pickTextsActivityResultLauncher.launch("text/plain")
                                        1 -> CoroutineScope(Dispatchers.IO).launch { DeletePrivateTextsUseCase()() }
                                        2 -> pickFolderActivityResultLauncher.launch(
                                            Uri.fromFile(File(Environment.getExternalStorageDirectory().absolutePath))
                                        )
                                        3 -> (mContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
                                    }
                                }
                                .setSingleChoiceItems(hiddenMenu, 1) { _: DialogInterface, i: Int -> option = i }
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
                    val s: HashSet<String> = GetHintsUseCase()()
                    s.remove("tipcard")
                    SetHintsUseCase()(s)
                }

                override fun onViewClicked(view: View) {
                    startActivity(Intent(mContext, HelpActivity::class.java))
                }
            })
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            requireView().setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.item_background_color, mContext.theme))
        }

        override fun onStart() {
            super.onStart()
            findPreference<SwitchPreferenceScreen>("easterEggs")!!.isChecked = AreEasterEggsEnabledUseCase()()
            findPreference<SwitchPreferenceScreen>("historyEnabled")!!.isChecked = IsHistroyEnabledUseCase()()
            val showTipCard = GetHintsUseCase()().contains("tipcard")
            tipCard?.isVisible = showTipCard
            tipCardSpacing?.isVisible = showTipCard
            setRelatedCardView()
        }

        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            val currentDarkMode = ThemeUtil.getDarkMode(mContext).toString()
            val darkModePref = findPreference<HorizontalRadioPreference>("dark_mode")
            when (preference.key) {
                "dark_mode" -> {
                    if (currentDarkMode !== newValue) {
                        ThemeUtil.setDarkMode(mActivity,
                            if (newValue == "0") ThemeUtil.DARK_MODE_DISABLED else ThemeUtil.DARK_MODE_ENABLED
                        )
                    }
                    return true
                }
                "dark_mode_auto" -> {
                    if (newValue as Boolean) {
                        darkModePref!!.isEnabled = false
                        ThemeUtil.setDarkMode(mActivity, ThemeUtil.DARK_MODE_AUTO)
                    } else {
                        darkModePref!!.isEnabled = true
                    }
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
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website))))
                    }
                    ?.addButton(getString(R.string.supportMe)) {
                        startActivity(Intent(mContext, SupportMeActivity::class.java))
                    }
                    ?.show(this)
            }
        }
    }
}