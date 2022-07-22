package de.lemke.nakbuch.ui.fragments

import android.content.*
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.picker.app.SeslDatePickerDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.*
import de.lemke.nakbuch.domain.hymndataUseCases.GetPersonalHymnUseCase
import de.lemke.nakbuch.domain.hymndataUseCases.SetPersonalHymnUseCase
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.HymnId
import de.lemke.nakbuch.domain.model.PersonalHymn
import de.lemke.nakbuch.domain.utils.TextChangedListener
import de.lemke.nakbuch.ui.HelpActivity
import de.lemke.nakbuch.ui.ImgviewActivity
import dev.oneuiproject.oneui.layout.DrawerLayout
import dev.oneuiproject.oneui.widget.MarginsTabLayout
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject


@AndroidEntryPoint
class TextviewFragment : Fragment() {
    private lateinit var rootView: View
    private lateinit var hymnId: HymnId
    private lateinit var personalHymn: PersonalHymn
    private lateinit var tvText: TextView
    private lateinit var tvCopyright: TextView
    private lateinit var editTextNotiz: EditText
    private lateinit var jokeButton: AppCompatButton
    private lateinit var whyNoFullTextButton: AppCompatButton
    private lateinit var notesGroup: LinearLayout
    private lateinit var calendarGroup: LinearLayout
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var konfettiView: KonfettiView
    private lateinit var listView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter

    /*
    private lateinit var tipPopupMenu: TipPopup
    private lateinit var tipPopupNote: TipPopup
    private lateinit var tipPopupCalendar: TipPopup
    private lateinit var tipPopupFav: TipPopup
    private lateinit var tipPopupFoto: TipPopup
    private lateinit var tipPopupPlus: TipPopup
    private lateinit var tipPopupMinus: TipPopup
    */
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private lateinit var hymnSungOnList: MutableList<LocalDate>
    private lateinit var selected: HashMap<Int, Boolean>
    private var selecting = false
    private var checkAllListening = true
    private var boldText: String? = null
    private var textSize: Int = 0
    private var tabLayout: MarginsTabLayout? = null

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    @Inject
    lateinit var getPersonalHymn: GetPersonalHymnUseCase

    @Inject
    lateinit var mute: MuteUseCase

    @Inject
    lateinit var doNotDisturb: DoNotDisturbUseCase

    @Inject
    lateinit var openBischoffApp: OpenBischoffAppUseCase

    @Inject
    lateinit var makeSectionOfTextBold: MakeSectionOfTextBoldUseCase

    @Inject
    lateinit var discoverEasterEgg: DiscoverEasterEggUseCase

    @Inject
    lateinit var addHymnToHistoryList: AddHymnToHistoryListUseCase

    @Inject
    lateinit var setPersonalHymn: SetPersonalHymnUseCase

    companion object {
        fun newInstance(hymnId: Int, boldText: String?): TextviewFragment {
            val f = TextviewFragment()
            val args = Bundle()
            args.putInt("hymnId", hymnId)
            args.putString("boldText", boldText)
            f.arguments = args
            return f
        }

        const val TEXTSIZE_STEP = 2
        const val TEXTSIZE_MIN = 10
        const val TEXTSIZE_MAX = 50
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_textview, container, false)
        hymnId = HymnId.create(requireArguments().getInt("hymnId"))!!
        boldText = requireArguments().getString("boldText")
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nestedScrollView = rootView.findViewById(R.id.nestedScrollViewTextview)
        konfettiView = rootView.findViewById(R.id.konfettiViewTextview)
        tvText = rootView.findViewById(R.id.tvText)
        tvCopyright = rootView.findViewById(R.id.tvCopyright)
        listView = rootView.findViewById(R.id.hymnHistoryList)
        notesGroup = rootView.findViewById(R.id.notesGroup)
        calendarGroup = rootView.findViewById(R.id.calendarGroup)
        editTextNotiz = rootView.findViewById(R.id.editTextNotiz)
        jokeButton = rootView.findViewById(R.id.jokeButton)
        whyNoFullTextButton = rootView.findViewById(R.id.whyNoFullTextButton)
        drawerLayout = rootView.findViewById(R.id.drawer_layout_textview)
        drawerLayout.setNavigationButtonIcon(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_oui_back_24))
        drawerLayout.setExpandedSubtitle(hymnId.buchMode.toString())
        drawerLayout.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.increaseTextSize -> lifecycleScope.launch { updateTextSize(textSize + TEXTSIZE_STEP) }
                R.id.decreaseTextSize -> lifecycleScope.launch { updateTextSize(textSize - TEXTSIZE_STEP) }
                R.id.dnd -> doNotDisturb()
                R.id.mute ->
                    if (!mute()) Toast.makeText(
                        context,
                        requireContext().getString(R.string.failedToMuteStreams),
                        Toast.LENGTH_SHORT
                    ).show()
                R.id.openOfficialApp -> lifecycleScope.launch {
                    if (hymnId.buchMode == BuchMode.Gesangbuch || hymnId.buchMode == BuchMode.Chorbuch) openBischoffApp(
                        hymnId.buchMode,
                        true
                    )
                    else discoverEasterEgg(konfettiView, R.string.easterEggWhichOfficialApp)
                }
            }
            return@setOnMenuItemClickListener true
        }

        lifecycleScope.launch {
            personalHymn = getPersonalHymn(hymnId)
            val color = MaterialColors.getColor(
                requireContext(), androidx.appcompat.R.attr.colorPrimary, //TODO
                requireContext().resources.getColor(R.color.primary_color, context?.theme)
            )
            val userSettings = getUserSettings()
            updateTextSize(userSettings.textSize)
            drawerLayout.setTitle(
                makeSectionOfTextBold(personalHymn.hymn.numberAndTitle, boldText, color, userSettings.alternativeSearchModeEnabled)
            )
            tvText.text = makeSectionOfTextBold(personalHymn.hymn.text, boldText, color, userSettings.alternativeSearchModeEnabled)
            tvCopyright.text =
                makeSectionOfTextBold(personalHymn.hymn.copyright, boldText, color, userSettings.alternativeSearchModeEnabled)
            if (personalHymn.hymn.containsCopyright) {
                if (userSettings.jokeButtonVisible && userSettings.easterEggsEnabled) {
                    jokeButton.visibility = View.VISIBLE
                    whyNoFullTextButton.visibility = View.GONE
                } else {
                    jokeButton.visibility = View.GONE
                    whyNoFullTextButton.visibility = View.VISIBLE
                }
            }
            editTextNotiz.setText(personalHymn.notes)
            initList()
            whyNoFullTextButton.setOnClickListener { startActivity(Intent(context, HelpActivity::class.java)) }
            jokeButton.setOnClickListener {
                lifecycleScope.launch {
                    discoverEasterEgg(konfettiView, R.string.easterEggEntryPremium)
                    AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.jokeTitle))
                        .setMessage(getString(R.string.jokeMessage))
                        .setNegativeButton(getString(R.string.ForeverICantTakeAJoke)) { _: DialogInterface?, _: Int ->
                            lifecycleScope.launch { updateUserSettings { it.copy(jokeButtonVisible = false) } }
                        }
                        .setPositiveButton(getString(R.string.onlyThisTime), null)
                        //.setNegativeButtonColor(resources.getColor(R.color.red, context?.theme))
                        .show()
                }
                jokeButton.visibility = View.GONE
            }
            editTextNotiz.addTextChangedListener(object : TextChangedListener<EditText>(editTextNotiz) {
                override fun onTextChanged(target: EditText, s: Editable) {
                    lifecycleScope.launch {
                        if (::personalHymn.isInitialized) {
                            personalHymn.notes = editTextNotiz.text.toString()
                            setPersonalHymn(personalHymn.copy())
                        }
                        if (s.toString().replace(" ", "").equals("easteregg", ignoreCase = true)) {
                            (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                                .hideSoftInputFromWindow(requireActivity().currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                            s.clear()
                            discoverEasterEgg(konfettiView, R.string.easterEggEntryNote)
                        }
                    }
                }
            })
            rootView.findViewById<View>(R.id.buttonKopieren).setOnClickListener {
                Toast.makeText(context, getString(R.string.copied), Toast.LENGTH_SHORT).show()
                (requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                    .setPrimaryClip(ClipData.newPlainText("Notiz ($hymnId)", editTextNotiz.text.toString()))
            }
            rootView.findViewById<View>(R.id.buttonSenden).setOnClickListener {
                val sendIntent = Intent(Intent.ACTION_SEND)
                sendIntent.type = "text/plain"
                sendIntent.putExtra(Intent.EXTRA_TEXT, editTextNotiz.text.toString())
                sendIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.shareNote))
                sendIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                startActivity(Intent.createChooser(sendIntent, "Share Via"))
            }
            val datePickerDialog = SeslDatePickerDialog(
                requireContext(), { _, year, month, day ->
                    val date = LocalDate.of(year, month + 1, day)
                    if (!hymnSungOnList.contains(date)) {
                        hymnSungOnList.add(date)
                        personalHymn.sungOnList = hymnSungOnList.distinct().sortedDescending()
                        lifecycleScope.launch { setPersonalHymn(personalHymn.copy()) }
                        setSelecting(false)
                        initList()
                    }
                }, LocalDate.now().year, LocalDate.now().monthValue - 1, LocalDate.now().dayOfMonth
            )
            rootView.findViewById<AppCompatButton>(R.id.addDateButton).setOnClickListener { datePickerDialog.show() }
            drawerLayout.setNavigationButtonOnClickListener { requireActivity().onBackPressed() }
            drawerLayout.setNavigationButtonTooltip(getString(R.string.sesl_navigate_up))
            onBackPressedCallback = object : OnBackPressedCallback(false) {
                override fun handleOnBackPressed() {
                    setSelecting(false)
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
            addHymnToHistoryList(personalHymn.hymn)
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val userSettings = getUserSettings()
            notesGroup.visibility = if (userSettings.notesVisible) View.VISIBLE else View.GONE
            calendarGroup.visibility = if (userSettings.sungOnVisible) View.VISIBLE else View.GONE
            textSize = userSettings.textSize
            updateTextSize(textSize)
            personalHymn = getPersonalHymn(hymnId)
            initBNV()
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                tabLayout!!.isVisible = true
                drawerLayout.toolbar.inflateMenu(R.menu.textview_menu)
                if (userSettings.showTextViewTips) {
                    /*updateUserSettings { it.copy(showTextViewTips = false) }
                    initTipPopup()
                    tipPopupMenu.show(TipPopup.DIRECTION_BOTTOM_LEFT)*/
                }
            } else {
                tabLayout!!.isVisible = false
                drawerLayout.toolbar.inflateMenu(R.menu.textview_menu_landscape)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.increaseTextSize -> lifecycleScope.launch { updateTextSize(textSize + TEXTSIZE_STEP) }
            R.id.decreaseTextSize -> lifecycleScope.launch { updateTextSize(textSize - TEXTSIZE_STEP) }
            R.id.dnd -> doNotDisturb()
            R.id.mute ->
                if (!mute()) Toast.makeText(context, requireContext().getString(R.string.failedToMuteStreams), Toast.LENGTH_SHORT).show()
            R.id.openOfficialApp -> lifecycleScope.launch {
                if (hymnId.buchMode == BuchMode.Gesangbuch || hymnId.buchMode == BuchMode.Chorbuch) openBischoffApp(hymnId.buchMode, true)
                else discoverEasterEgg(konfettiView, R.string.easterEggWhichOfficialApp)
            }
        }
        return true
    }

    private fun initBNV() {
        val noteIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_oui_notes_new_24)
        val noteIconColored = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_oui_notes_new_24)
        noteIconColored?.colorFilter = PorterDuffColorFilter(
            MaterialColors.getColor(
                requireContext(), androidx.appcompat.R.attr.colorPrimary,
                resources.getColor(R.color.primary_color, context?.theme)
            ), PorterDuff.Mode.SRC_IN
        )
        val dateIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_oui_calendar_24)
        val dateIconColored = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_oui_calendar_24)
        dateIconColored!!.colorFilter = PorterDuffColorFilter(
            MaterialColors.getColor(
                requireContext(), androidx.appcompat.R.attr.colorPrimary,
                resources.getColor(R.color.primary_color, context?.theme)
            ), PorterDuff.Mode.SRC_IN
        )
        val favIconFilled = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_oui_favorite_24)!!
        favIconFilled.colorFilter = PorterDuffColorFilter(resources.getColor(R.color.red, context?.theme), PorterDuff.Mode.SRC_IN)
        val favIconOutline = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_oui_favorite_outline_24)!!
        val camIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_oui_image_visual_24)
        val plusIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_oui_add_24)
        val minusIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_oui_minus_24)

        if (tabLayout == null) {
            tabLayout = rootView.findViewById(R.id.textView_tabLayout)
            lifecycleScope.launch {
                val userSettings = getUserSettings()
                if (userSettings.notesVisible) tabLayout!!.addTab(tabLayout!!.newTab().setIcon(noteIconColored))
                else tabLayout!!.addTab(tabLayout!!.newTab().setIcon(noteIcon))
                if (userSettings.sungOnVisible) tabLayout!!.addTab(tabLayout!!.newTab().setIcon(dateIconColored))
                else tabLayout!!.addTab(tabLayout!!.newTab().setIcon(dateIcon))
                if (personalHymn.favorite) tabLayout!!.addTab(tabLayout!!.newTab().setIcon(favIconFilled))
                else tabLayout!!.addTab(tabLayout!!.newTab().setIcon(favIconOutline))
                tabLayout!!.addTab(tabLayout!!.newTab().setIcon(camIcon))
                tabLayout!!.addTab(tabLayout!!.newTab().setIcon(plusIcon))
                tabLayout!!.addTab(tabLayout!!.newTab().setIcon(minusIcon))

                tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        when (tab.position) {
                            0 -> lifecycleScope.launch {
                                if ((updateUserSettings { it.copy(notesVisible = !it.notesVisible) }).notesVisible) {
                                    notesGroup.visibility = View.VISIBLE
                                    drawerLayout.setExpanded(false, true)
                                    nestedScrollView.post {
                                        nestedScrollView.smoothScrollTo(0, (notesGroup.top + notesGroup.bottom - nestedScrollView.height) / 2)
                                    }
                                    tabLayout?.getTabAt(tab.position)?.icon = noteIconColored
                                } else {
                                    notesGroup.visibility = View.GONE
                                    val inputManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    inputManager.hideSoftInputFromWindow(requireView().windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                                    tabLayout?.getTabAt(tab.position)?.icon = noteIcon
                                }

                            }
                            1 -> lifecycleScope.launch {
                                if ((updateUserSettings { it.copy(sungOnVisible = !it.sungOnVisible) }).sungOnVisible) {
                                    calendarGroup.visibility = View.VISIBLE
                                    drawerLayout.setExpanded(false, true)
                                    nestedScrollView.post {
                                        nestedScrollView.smoothScrollTo(
                                            0,
                                            (calendarGroup.top + calendarGroup.bottom - nestedScrollView.height) / 2
                                        )
                                    }
                                    tabLayout?.getTabAt(tab.position)?.icon = dateIconColored
                                } else {
                                    calendarGroup.visibility = View.GONE
                                    tabLayout?.getTabAt(tab.position)?.icon = dateIcon
                                }
                            }
                            2 -> lifecycleScope.launch {
                                personalHymn.favorite = !personalHymn.favorite
                                tabLayout?.getTabAt(tab.position)?.icon = if (personalHymn.favorite) favIconFilled else favIconOutline
                                setPersonalHymn(personalHymn.copy())
                            }
                            3 -> startActivity(Intent(context, ImgviewActivity::class.java).putExtra("hymnId", hymnId.toInt()))
                            4 -> lifecycleScope.launch { updateTextSize(textSize + TEXTSIZE_STEP) }
                            5 -> lifecycleScope.launch { updateTextSize(textSize - TEXTSIZE_STEP) }
                            else -> {}
                        }
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {}
                    override fun onTabReselected(tab: TabLayout.Tab) {onTabSelected(tab)}
                })
            }

        } else {
            //tabLayout!!.removeAllTabs()
        }




        /*
        old
        tabLayout!!.addTabCustomButton(noteIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                lifecycleScope.launch {
                    if ((updateUserSettings { it.copy(notesVisible = !it.notesVisible) }).notesVisible) {
                        notesGroup.visibility = View.VISIBLE
                        drawerLayout.setExpanded(false, true)
                        nestedScrollView.post {
                            nestedScrollView.smoothScrollTo(0, (notesGroup.top + notesGroup.bottom - nestedScrollView.height) / 2)
                        }
                    } else {
                        notesGroup.visibility = View.GONE
                        val inputManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputManager.hideSoftInputFromWindow(requireView().windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                    }
                    initBNV()
                }
            }
        })
        tabLayout!!.addTabCustomButton(dateIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                lifecycleScope.launch {
                    if ((updateUserSettings { it.copy(sungOnVisible = !it.sungOnVisible) }).sungOnVisible) {
                        calendarGroup.visibility = View.VISIBLE
                        drawerLayout.setExpanded(false, true)
                        nestedScrollView.post {
                            nestedScrollView.smoothScrollTo(0, (calendarGroup.top + calendarGroup.bottom - nestedScrollView.height) / 2)
                        }
                        initList()
                    } else {
                        calendarGroup.visibility = View.GONE
                    }
                    initBNV()
                }
            }
        })
        tabLayout!!.addTabCustomButton(if (personalHymn.favorite) favIconFilled else favIconOutline, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                lifecycleScope.launch {
                    personalHymn.favorite = !personalHymn.favorite
                    initBNV()
                    setPersonalHymn(personalHymn.copy())
                }
            }
        })
        tabLayout!!.addTabCustomButton(camIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                val myIntent = Intent(context, ImgviewActivity::class.java)
                myIntent.putExtra("hymnId", hymnId.toInt())
                startActivity(myIntent)
            }
        })
        tabLayout!!.addTabCustomButton(plusIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                lifecycleScope.launch {
                    updateTextSize(textSize + TEXTSIZE_STEP)
                }
            }
        })
        tabLayout!!.addTabCustomButton(minusIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                lifecycleScope.launch {
                    updateTextSize(textSize - TEXTSIZE_STEP)
                }
            }
        })
        */
    }

    private fun initTipPopup() {
        /*
        val menuItemView =
            (drawerLayout.findViewById<View>(de.dlyt.yanndroid.oneui.R.id.toolbar_layout_action_menu_item_container) as ViewGroup)
                .getChildAt(0)
        val noteButton: View? = tabLayout?.getTabAt(0)?.view
        val calendarButton: View? = tabLayout?.getTabAt(1)?.view
        val favButton: View? = tabLayout?.getTabAt(2)?.view
        val fotoButton: View? = tabLayout?.getTabAt(3)?.view
        val plusButton: View? = tabLayout?.getTabAt(4)?.view
        val minusButton: View? = tabLayout?.getTabAt(5)?.view
        tipPopupMenu = TipPopup(menuItemView)
        tipPopupNote = TipPopup(noteButton)
        tipPopupCalendar = TipPopup(calendarButton)
        tipPopupFav = TipPopup(favButton)
        tipPopupFoto = TipPopup(fotoButton)
        tipPopupPlus = TipPopup(plusButton)
        tipPopupMinus = TipPopup(minusButton)
        tipPopupMenu.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, context?.theme))
        tipPopupNote.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, context?.theme))
        tipPopupCalendar.setBackgroundColor(
            resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, context?.theme)
        )
        tipPopupFav.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, context?.theme))
        tipPopupFoto.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, context?.theme))
        tipPopupPlus.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, context?.theme))
        tipPopupMinus.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, context?.theme))
        tipPopupMenu.setExpanded(true)
        tipPopupNote.setExpanded(true)
        tipPopupCalendar.setExpanded(true)
        tipPopupFav.setExpanded(true)
        tipPopupFoto.setExpanded(true)
        tipPopupPlus.setExpanded(true)
        tipPopupMinus.setExpanded(true)
        tipPopupMenu.setOnDismissListener { tipPopupNote.show(TipPopup.DIRECTION_TOP_LEFT) }
        tipPopupNote.setOnDismissListener { tipPopupCalendar.show(TipPopup.DIRECTION_TOP_LEFT) }
        tipPopupCalendar.setOnDismissListener { tipPopupFav.show(TipPopup.DIRECTION_TOP_LEFT) }
        tipPopupFav.setOnDismissListener { tipPopupFoto.show(TipPopup.DIRECTION_TOP_LEFT) }
        tipPopupFoto.setOnDismissListener { tipPopupPlus.show(TipPopup.DIRECTION_TOP_LEFT) }
        tipPopupPlus.setOnDismissListener { tipPopupMinus.show(TipPopup.DIRECTION_TOP_LEFT) }
        tipPopupMenu.setMessage("${getString(R.string.mute)} oder ${getString(R.string.dndDescription)}")
        tipPopupNote.setMessage(getString(R.string.noteTip))
        tipPopupCalendar.setMessage(getString(R.string.calendarTip))
        tipPopupFav.setMessage(getString(R.string.addToFav) + "/" + getString(R.string.removeFromFav))
        tipPopupFoto.setMessage(getString(R.string.galleryTip))
        tipPopupPlus.setMessage(getString(R.string.increaseTextsize))
        tipPopupMinus.setMessage(getString(R.string.decreaseTextsize))
         */
    }

    private fun updateTextSize(newTextSize: Int): Int {
        textSize = newTextSize.coerceIn(TEXTSIZE_MIN, TEXTSIZE_MAX)
        tvText.textSize = textSize.toFloat()
        tvCopyright.textSize = (textSize - 4).toFloat()
        lifecycleScope.launch { updateUserSettings { it.copy(textSize = textSize) } }
        return textSize
    }

    private fun initList() {
        hymnSungOnList = personalHymn.sungOnList.toMutableList()
        selected = HashMap()
        hymnSungOnList.indices.forEach { i -> selected[i] = false }
        listView.layoutManager = LinearLayoutManager(context)
        imageAdapter = ImageAdapter()
        listView.adapter = imageAdapter
        listView.addItemDecoration(ItemDecoration(requireContext()))
        listView.itemAnimator = null
        listView.seslSetFastScrollerEnabled(true)
        listView.seslSetFillBottomEnabled(true)
        listView.seslSetGoToTopEnabled(true)
        listView.seslSetLastRoundedCorner(true)
    }

    fun setSelecting(enabled: Boolean) {
        if (enabled) {
            selecting = true
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.itemCount)
            drawerLayout.setActionModeBottomMenu(R.menu.remove_menu)
            drawerLayout.setActionModeBottomMenuListener { item: MenuItem ->
                if (item.itemId == R.id.menuButtonRemove) {
                    personalHymn.sungOnList = hymnSungOnList.filterIndexed { index, _ -> selected[index] == false }
                    lifecycleScope.launch { setPersonalHymn(personalHymn.copy()) }
                    setSelecting(false)
                    initList()
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
            onBackPressedCallback.isEnabled = true
        } else {
            selecting = false
            selected.replaceAll { _, _ -> false }
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.itemCount)
            drawerLayout.setActionModeCount(0, imageAdapter.itemCount)
            drawerLayout.dismissActionMode()
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
        override fun getItemCount(): Int = hymnSungOnList.size

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getItemViewType(position: Int): Int = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            var resId = 0
            when (viewType) {
                0 -> resId = R.layout.listview_item
                //1 -> resId = R.layout.listview_bottom_spacing
            }
            val view = LayoutInflater.from(parent.context).inflate(resId, parent, false)
            return ViewHolder(view, viewType)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder.isItem) {
                holder.checkBox.visibility = if (selecting) View.VISIBLE else View.GONE
                holder.checkBox.isChecked = selected[position]!!
                holder.textView.text = hymnSungOnList[position].format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
                holder.parentView.setOnClickListener {
                    if (!selecting) setSelecting(true)
                    toggleItemSelected(position)
                }
                holder.parentView.setOnLongClickListener {
                    if (!selecting) setSelecting(true)
                    toggleItemSelected(position)
                    listView.seslStartLongPressMultiSelection()
                    listView.seslSetLongPressMultiSelectionListener(object :
                        RecyclerView.SeslLongPressMultiSelectionListener {
                        override fun onItemSelected(var1: RecyclerView, var2: View, var3: Int, var4: Long) {
                            if (getItemViewType(var3) == 0) toggleItemSelected(var3)
                        }

                        override fun onLongPressMultiSelectionEnded(var1: Int, var2: Int) {}
                        override fun onLongPressMultiSelectionStarted(var1: Int, var2: Int) {}
                    })
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
