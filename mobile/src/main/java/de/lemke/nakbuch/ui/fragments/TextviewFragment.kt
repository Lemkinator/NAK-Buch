package de.lemke.nakbuch.ui.fragments

import android.content.*
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import de.dlyt.yanndroid.oneui.dialog.AlertDialog
import de.dlyt.yanndroid.oneui.dialog.DatePickerDialog
import de.dlyt.yanndroid.oneui.layout.DrawerLayout
import de.dlyt.yanndroid.oneui.menu.MenuItem
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager
import de.dlyt.yanndroid.oneui.sesl.utils.SeslRoundedCorner
import de.dlyt.yanndroid.oneui.utils.CustomButtonClickListener
import de.dlyt.yanndroid.oneui.view.RecyclerView
import de.dlyt.yanndroid.oneui.view.TipPopup
import de.dlyt.yanndroid.oneui.widget.NestedScrollView
import de.dlyt.yanndroid.oneui.widget.TabLayout
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.hymndata.*
import de.lemke.nakbuch.domain.hymns.GetHymnUseCase
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.HymnData
import de.lemke.nakbuch.domain.settings.*
import de.lemke.nakbuch.domain.utils.TextChangedListener
import de.lemke.nakbuch.ui.HelpActivity
import de.lemke.nakbuch.ui.ImgviewActivity
import de.lemke.nakbuch.ui.TextviewActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class TextviewFragment : Fragment() {
    private lateinit var mRootView: View
    private lateinit var mContext: Context
    private var nr = 0
    private var boldText: String? = null
    private lateinit var hymn: Hymn
    private lateinit var hymnData: HymnData
    private lateinit var buchMode: BuchMode
    private lateinit var tvText: TextView
    private lateinit var tvCopyright: TextView
    private lateinit var editTextNotiz: EditText
    private lateinit var jokeButton: MaterialButton
    private lateinit var notesGroup: LinearLayout
    private lateinit var calendarGroup: LinearLayout
    private lateinit var nestedScrollView: NestedScrollView
    private var tabLayout: TabLayout? = null
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var konfettiView: KonfettiView
    private lateinit var listView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var tipPopupMenu: TipPopup
    private lateinit var tipPopupNote: TipPopup
    private lateinit var tipPopupCalendar: TipPopup
    private lateinit var tipPopupFav: TipPopup
    private lateinit var tipPopupFoto: TipPopup
    private lateinit var tipPopupPlus: TipPopup
    private lateinit var tipPopupMinus: TipPopup
    private lateinit var selected: HashMap<Int, Boolean>
    private var mSelecting = false
    private var checkAllListening = true
    private lateinit var hymnSungOnList: ArrayList<LocalDate?>
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    companion object {
        fun newInstance(buchMode: BuchMode, nr: Int, boldText: String?): TextviewFragment {
            val f = TextviewFragment()
            val args = Bundle()
            args.putInt("nr", nr)
            args.putString("boldText", boldText)
            f.arguments = args
            return f
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mRootView = inflater.inflate(R.layout.fragment_textview, container, false)
        nr = requireArguments().getInt("nr")
        boldText = requireArguments().getString("boldText")
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buchMode = GetBuchModeUseCase()()
        drawerLayout = mRootView.findViewById(R.id.drawer_layout_textview)
        drawerLayout.setSubtitle(getString(if (buchMode == BuchMode.Gesangbuch) R.string.titleGesangbuch else R.string.titleChorbuch))
        drawerLayout.setNavigationButtonIcon(AppCompatResources.getDrawable(mContext, de.dlyt.yanndroid.oneui.R.drawable.ic_oui_back))
        drawerLayout.setNavigationButtonTooltip(getString(de.dlyt.yanndroid.oneui.R.string.sesl_navigate_up))
        drawerLayout.setNavigationButtonOnClickListener { requireActivity().onBackPressed() }
        drawerLayout.inflateToolbarMenu(R.menu.textview_menu)
        drawerLayout.setOnToolbarMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.switchBuchMode -> {
                    buchMode = if (buchMode == BuchMode.Gesangbuch) BuchMode.Chorbuch else BuchMode.Gesangbuch
                    CoroutineScope(Dispatchers.IO).launch {
                        SetBuchModeUseCase()(buchMode)
                    }
                    startActivity(Intent(mRootView.context, TextviewActivity::class.java).putExtra("nr", nr))
                    requireActivity().finish()
                }
                R.id.mute ->
                    if (!MuteUseCase()()) Toast.makeText(mContext, mContext.getString(R.string.failedToMuteStreams), Toast.LENGTH_SHORT)
                        .show()
                R.id.dnd -> DoNotDisturbUseCase()(mContext)
            }
            true
        }
        nestedScrollView = mRootView.findViewById(R.id.nestedScrollViewTextview)
        konfettiView = mRootView.findViewById(R.id.konfettiViewTextview)
        tvText = mRootView.findViewById(R.id.tvText)
        tvCopyright = mRootView.findViewById(R.id.tvCopyright)
        listView = mRootView.findViewById(R.id.hymnHistoryList)
        notesGroup = mRootView.findViewById(R.id.notesGroup)
        calendarGroup = mRootView.findViewById(R.id.calendarGroup)
        editTextNotiz = mRootView.findViewById(R.id.editTextNotiz)

        jokeButton = mRootView.findViewById(R.id.jokeButton)
        jokeButton.setOnClickListener {
            DiscoverEasterEggUseCase()(mContext, konfettiView, R.string.easterEggEntryPremium)
            AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.jokeTitle))
                .setMessage(getString(R.string.jokeMessage))
                .setNegativeButton(getString(R.string.ForeverICantTakeAJoke)) { _: DialogInterface?, _: Int ->
                    SetJokeButtonVisibleUseCase()(false)
                }
                .setPositiveButton(getString(R.string.onlyThisTime), null)
                .setNegativeButtonColor(
                    resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red, mContext.theme)
                ).show()
            jokeButton.visibility = View.GONE
        }

        val whyNoFullTextButton: MaterialButton = mRootView.findViewById(R.id.whyNoFullTextButton)
        whyNoFullTextButton.setOnClickListener {
            startActivity(Intent(mContext, HelpActivity::class.java))
        }
        val openOfficialAppButton: MaterialButton = mRootView.findViewById(R.id.openOfficialAppButton)
        openOfficialAppButton.setOnClickListener {
            OpenBischoffAppUseCase()(mContext, buchMode)
        }

        CoroutineScope(Dispatchers.IO).launch {
            hymn = GetHymnUseCase()(buchMode, nr)
            hymnData = GetHymnDataUseCase()(hymn)
            withContext(Dispatchers.Main) {
                if (boldText != null) {
                    val color = MaterialColors.getColor(
                        mContext, de.dlyt.yanndroid.oneui.R.attr.colorPrimary,
                        mContext.resources.getColor(R.color.primary_color, mContext.theme)
                    )
                    tvText.text = MakeSectionOfTextBoldUseCase()(hymn.text, boldText!!, color)
                    drawerLayout.setTitle(MakeSectionOfTextBoldUseCase()(hymn.numberAndTitle, boldText!!, color))
                    tvCopyright.text = MakeSectionOfTextBoldUseCase()(hymn.copyright, boldText!!, color)
                } else {
                    tvText.text = hymn.text
                    drawerLayout.setTitle(hymn.numberAndTitle)
                    tvCopyright.text = hymn.copyright
                }
                editTextNotiz.setText(hymnData.notes)
                if (hymn.text.contains("urheberrechtlich geschützt...", ignoreCase = true)) {
                    openOfficialAppButton.visibility = View.VISIBLE
                    if (IsJokeButtonVisibleUseCase()() && AreEasterEggsEnabledUseCase()()) {
                        jokeButton.visibility = View.VISIBLE
                        whyNoFullTextButton.visibility = View.GONE
                    } else {
                        jokeButton.visibility = View.GONE
                        whyNoFullTextButton.visibility = View.VISIBLE
                    }
                }
                initBNV()
                if (GetBooleanSettingUseCase()("textviewTips", true)) {
                    SetBooleanSettingUseCase()("textviewTips", false)
                    Handler(Looper.getMainLooper()).postDelayed({
                        initTipPopup()
                        tipPopupMenu.show(TipPopup.DIRECTION_BOTTOM_LEFT)
                    }, 50)
                }
                initList()
            }
        }

        updateTextSize(GetTextSizeUseCase()())

        notesGroup.visibility = if (AreNotesVisibleUseCase()()) View.VISIBLE else View.GONE
        calendarGroup.visibility = if (IsSungOnVisibleUseCase()()) View.VISIBLE else View.GONE

        editTextNotiz.addTextChangedListener(object :
            TextChangedListener<EditText>(editTextNotiz) {
            override fun onTextChanged(target: EditText, s: Editable) {
                hymnData.notes = editTextNotiz.text.toString()
                setHymnData(hymnData.copy())
                if (s.toString().replace(" ", "").equals("easteregg", ignoreCase = true)) {
                    DiscoverEasterEggUseCase()(mContext, konfettiView, R.string.easterEggEntryNote)
                    s.clear()
                    val inputManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(
                        requireActivity().currentFocus!!.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )
                }
            }
        })
        mRootView.findViewById<View>(R.id.buttonKopieren).setOnClickListener {
            Toast.makeText(mContext, getString(R.string.copied), Toast.LENGTH_SHORT).show()
            (requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                .setPrimaryClip(ClipData.newPlainText("Notiz (NAKBuch: $buchMode , $nr)", editTextNotiz.text.toString()))
        }
        mRootView.findViewById<View>(R.id.buttonSenden).setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.type = "text/plain"
            sendIntent.putExtra(Intent.EXTRA_TEXT, editTextNotiz.text.toString())
            sendIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.shareNote))
            sendIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(Intent.createChooser(sendIntent, "Share Via"))
        }

        val addDateButton: MaterialButton = mRootView.findViewById(R.id.addDateButton)
        addDateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                mContext, { _, year, month0, day ->
                    val month = month0 + 1
                    val date = LocalDate.of(year, month, day)
                    if (!hymnSungOnList.contains(date)) {
                        hymnSungOnList.add(date)
                        hymnData.sungOnList = ArrayList(hymnSungOnList.filterNotNull().distinct().sortedDescending())
                        setHymnData(hymnData.copy())
                        setSelecting(false)
                        initList()
                    }
                }, LocalDate.now().year, LocalDate.now().monthValue - 1, LocalDate.now().dayOfMonth
            )
            datePickerDialog.show()
        }

        onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                setSelecting(false)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            hymnData = GetHymnDataUseCase()(hymn)
            withContext(Dispatchers.Main) {
                initBNV()
            }
        }
        notesGroup.visibility = if (AreNotesVisibleUseCase()()) View.VISIBLE else View.GONE
        calendarGroup.visibility = if (IsSungOnVisibleUseCase()()) View.VISIBLE else View.GONE
    }

    private fun setHymnData(hymnData: HymnData) {
        CoroutineScope(Dispatchers.IO).launch {
            SetHymnDataUseCase()(hymn, hymnData)
        }
    }

    private fun initBNV() {
        if (tabLayout == null) tabLayout = mRootView.findViewById(R.id.textView_bnv)
        else tabLayout!!.removeAllTabs()
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            tabLayout!!.isVisible = false
            return
        } else {
            tabLayout!!.isVisible = true
        }
        val noteIcon = AppCompatResources.getDrawable(mContext, de.dlyt.yanndroid.oneui.R.drawable.ic_oui4_edit)
        if (AreNotesVisibleUseCase()()) {
            noteIcon!!.colorFilter = PorterDuffColorFilter(
                MaterialColors.getColor(
                    mContext, de.dlyt.yanndroid.oneui.R.attr.colorPrimary,
                    resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_orange, mContext.theme)
                ), PorterDuff.Mode.SRC_IN
            )
        }
        tabLayout!!.addTabCustomButton(noteIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                if (AreNotesVisibleUseCase()()) {
                    notesGroup.visibility = View.GONE
                    val inputManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(requireView().windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                    SetNotesVisibleUseCase()(false)
                } else {
                    notesGroup.visibility = View.VISIBLE
                    drawerLayout.setExpanded(false, true)
                    nestedScrollView.post {
                        nestedScrollView.smoothScrollTo(0, (notesGroup.top + notesGroup.bottom - nestedScrollView.height) / 2)
                    }
                    SetNotesVisibleUseCase()(true)
                }
                initBNV()
            }
        })
        val dateIcon = AppCompatResources.getDrawable(mContext, de.dlyt.yanndroid.oneui.R.drawable.ic_oui3_calendar_task)
        if (IsSungOnVisibleUseCase()()) {
            dateIcon!!.colorFilter = PorterDuffColorFilter(
                MaterialColors.getColor(
                    mContext, de.dlyt.yanndroid.oneui.R.attr.colorPrimary,
                    resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_orange, mContext.theme)
                ), PorterDuff.Mode.SRC_IN
            )
        }
        tabLayout!!.addTabCustomButton(dateIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                if (IsSungOnVisibleUseCase()()) {
                    calendarGroup.visibility = View.GONE
                    SetSungOnVisibleUseCase()(false)
                } else {
                    calendarGroup.visibility = View.VISIBLE
                    drawerLayout.setExpanded(false, true)
                    nestedScrollView.post {
                        nestedScrollView.smoothScrollTo(0, (calendarGroup.top + calendarGroup.bottom - nestedScrollView.height) / 2)
                    }
                    SetSungOnVisibleUseCase()(true)
                    initList()
                }
                initBNV()
            }
        })
        val favIcon: Drawable
        if (hymnData.favorite) {
            favIcon = AppCompatResources.getDrawable(mContext, de.dlyt.yanndroid.oneui.R.drawable.ic_oui_like_on)!!
            favIcon.colorFilter = PorterDuffColorFilter(
                resources.getColor(de.dlyt.yanndroid.oneui.R.color.red, mContext.theme), PorterDuff.Mode.SRC_IN
            )
        } else {
            favIcon = AppCompatResources.getDrawable(mContext, de.dlyt.yanndroid.oneui.R.drawable.ic_oui_like_off)!!
        }
        tabLayout!!.addTabCustomButton(favIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                hymnData.favorite = !hymnData.favorite
                setHymnData(hymnData.copy())
                initBNV()
            }
        })
        val camIcon = AppCompatResources.getDrawable(mContext, de.dlyt.yanndroid.oneui.R.drawable.ic_oui4_scan)
        tabLayout!!.addTabCustomButton(camIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                val myIntent = Intent(mContext, ImgviewActivity::class.java)
                myIntent.putExtra("nr", nr)
                startActivity(myIntent)
            }
        })
        val plusIcon = AppCompatResources.getDrawable(mContext, de.dlyt.yanndroid.oneui.R.drawable.ic_oui_plus)
        tabLayout!!.addTabCustomButton(plusIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) = updateTextSize(IncreaseTextSizeUseCase()())
        })
        val minusIcon = AppCompatResources.getDrawable(mContext, de.dlyt.yanndroid.oneui.R.drawable.ic_oui_minus)
        tabLayout!!.addTabCustomButton(minusIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) = updateTextSize(DecreaseTextSizeUseCase()())
        })
    }

    private fun initTipPopup() {
        val menuItemView =
            (drawerLayout.findViewById<View>(de.dlyt.yanndroid.oneui.R.id.toolbar_layout_action_menu_item_container) as ViewGroup)
                .getChildAt(0)
        val noteButton: View = tabLayout!!.getTabAt(0)!!.view
        val calendarButton: View = tabLayout!!.getTabAt(1)!!.view
        val favButton: View = tabLayout!!.getTabAt(2)!!.view
        val fotoButton: View = tabLayout!!.getTabAt(3)!!.view
        val plusButton: View = tabLayout!!.getTabAt(4)!!.view
        val minusButton: View = tabLayout!!.getTabAt(5)!!.view
        tipPopupMenu = TipPopup(menuItemView)
        tipPopupNote = TipPopup(noteButton)
        tipPopupCalendar = TipPopup(calendarButton)
        tipPopupFav = TipPopup(favButton)
        tipPopupFoto = TipPopup(fotoButton)
        tipPopupPlus = TipPopup(plusButton)
        tipPopupMinus = TipPopup(minusButton)
        tipPopupMenu.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, mContext.theme))
        tipPopupNote.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, mContext.theme))
        tipPopupCalendar.setBackgroundColor(
            resources.getColor(
                de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color,
                mContext.theme
            )
        )
        tipPopupFav.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, mContext.theme))
        tipPopupFoto.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, mContext.theme))
        tipPopupPlus.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, mContext.theme))
        tipPopupMinus.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, mContext.theme))
        tipPopupMenu.setExpanded(true)
        tipPopupNote.setExpanded(true)
        tipPopupCalendar.setExpanded(true)
        tipPopupFav.setExpanded(true)
        tipPopupFoto.setExpanded(true)
        tipPopupPlus.setExpanded(true)
        tipPopupMinus.setExpanded(true)
        tipPopupMenu.setOnDismissListener {
            //View noteButton = Objects.requireNonNull(tabLayout.getTabAt(0)).seslGetTextView();
            //int[] outLocation = new int[2];
            //noteButton.getLocationOnScreen(outLocation);
            //tipPopupNote.setTargetPosition(noteButton.getLeft(), outLocation[1] + (noteButton.getHeight() / 2) + getResources().getDimensionPixelSize(R.dimen.sesl_action_button_icon_size));
            tipPopupNote.show(TipPopup.DIRECTION_TOP_LEFT)
        }
        tipPopupNote.setOnDismissListener { tipPopupCalendar.show(TipPopup.DIRECTION_TOP_LEFT) }
        tipPopupCalendar.setOnDismissListener { tipPopupFav.show(TipPopup.DIRECTION_TOP_LEFT) }
        tipPopupFav.setOnDismissListener { tipPopupFoto.show(TipPopup.DIRECTION_TOP_LEFT) }
        tipPopupFoto.setOnDismissListener { tipPopupPlus.show(TipPopup.DIRECTION_TOP_LEFT) }
        tipPopupPlus.setOnDismissListener { tipPopupMinus.show(TipPopup.DIRECTION_TOP_LEFT) }
        tipPopupMenu.setMessage(
            """${getString(R.string.switchModeDescription)}, 
                ${getString(R.string.mute)} oder 
                ${getString(R.string.dndMode)}"""
        )
        tipPopupNote.setMessage(getString(R.string.noteTip))
        tipPopupCalendar.setMessage(getString(R.string.calendarTip))
        tipPopupFav.setMessage(getString(R.string.addToFav) + "/" + getString(R.string.removeFromFav))
        tipPopupFoto.setMessage(getString(R.string.galleryTip))
        tipPopupPlus.setMessage(getString(R.string.increaseTextsize))
        tipPopupMinus.setMessage(getString(R.string.decreaseTextsize))
    }

    private fun updateTextSize(textSize: Int) {
        tvText.textSize = textSize.toFloat()
        tvCopyright.textSize = (textSize - 4).toFloat()
    }

    private fun initList() {
        hymnSungOnList = ArrayList(hymnData.sungOnList)
        hymnSungOnList.add(null) //Placeholder
        Log.d("test", hymnSungOnList.toString())
        selected = HashMap()
        for (i in hymnSungOnList.indices) selected[i] = false
        val divider = TypedValue()
        mContext.theme.resolveAttribute(android.R.attr.listDivider, divider, true)
        listView.layoutManager = LinearLayoutManager(mContext)
        imageAdapter = ImageAdapter()
        listView.adapter = imageAdapter
        val decoration = ItemDecoration()
        listView.addItemDecoration(decoration)
        decoration.setDivider(AppCompatResources.getDrawable(mContext, divider.resourceId)!!)
        listView.itemAnimator = null
        listView.seslSetFastScrollerEnabled(true)
        listView.seslSetFillBottomEnabled(true)
        listView.seslSetGoToTopEnabled(true)
        listView.seslSetLastRoundedCorner(false)
    }

    fun setSelecting(enabled: Boolean) {
        if (enabled) {
            mSelecting = true
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.itemCount - 1)
            drawerLayout.setSelectModeBottomMenu(R.menu.remove_menu) { item: MenuItem ->
                if (item.itemId == R.id.menuButtonRemove) {
                    hymnData.sungOnList = ArrayList(hymnSungOnList.filterNotNull().filterIndexed { index, _ -> selected[index] == false })
                    setHymnData(hymnData.copy())
                    setSelecting(false)
                    initList()
                } else {
                    item.badge = item.badge + 1
                    Toast.makeText(mContext, item.title, Toast.LENGTH_SHORT).show()
                }
                true
            }
            drawerLayout.showSelectMode()
            drawerLayout.setSelectModeAllCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                if (checkAllListening) {
                    for (i in 0 until imageAdapter.itemCount - 1) {
                        selected[i] = isChecked
                        imageAdapter.notifyItemChanged(i)
                    }
                }
                var count = 0
                for (b in selected.values) if (b) count++
                drawerLayout.setSelectModeCount(count)
            }
            onBackPressedCallback.isEnabled = true
        } else {
            mSelecting = false
            for (i in 0 until imageAdapter.itemCount - 1) selected[i] = false
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.itemCount - 1)
            drawerLayout.setSelectModeCount(0)
            drawerLayout.dismissSelectMode()
            onBackPressedCallback.isEnabled = false
        }
    }

    fun toggleItemSelected(position: Int) {
        selected[position] = !selected[position]!!
        imageAdapter.notifyItemChanged(position)
        checkAllListening = false
        var count = 0
        for (b in selected.values) if (b) count++
        drawerLayout.setSelectModeAllChecked(count == imageAdapter.itemCount - 1)
        drawerLayout.setSelectModeCount(count)
        checkAllListening = true
    }

    //Adapter for the Icon RecyclerView
    inner class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
        override fun getItemCount(): Int = hymnSungOnList.size

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getItemViewType(position: Int): Int = if (hymnSungOnList[position] == null) 1 else 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            var resId = 0
            when (viewType) {
                0 -> resId = R.layout.listview_item
                1 -> resId = R.layout.listview_bottom_spacing
            }
            val view = LayoutInflater.from(parent.context).inflate(resId, parent, false)
            return ViewHolder(view, viewType)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder.isItem) {
                holder.checkBox.visibility = if (mSelecting) View.VISIBLE else View.GONE
                holder.checkBox.isChecked = selected[position]!!

                //holder.imageView.setImageResource(R.drawable.ic_samsung_audio);
                /*val date =
                    Instant.ofEpochMilli(hymnHistoryList[position]).atZone(ZoneId.systemDefault())
                        .toLocalDate()*/
                holder.textView.text = hymnSungOnList[position]!!.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
                holder.parentView.setOnClickListener {
                    if (!mSelecting) setSelecting(true)
                    toggleItemSelected(position)
                }
                holder.parentView.setOnLongClickListener {
                    if (!mSelecting) setSelecting(true)
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

            //lateinit var imageView: ImageView
            lateinit var textView: TextView
            lateinit var checkBox: CheckBox

            init {
                if (isItem) {
                    parentView = itemView as RelativeLayout
                    //imageView = parentView.findViewById(R.id.icon_tab_item_image)
                    textView = parentView.findViewById(R.id.icon_tab_item_text)
                    checkBox = parentView.findViewById(R.id.checkbox)
                }
            }
        }
    }

    inner class ItemDecoration : RecyclerView.ItemDecoration() {
        private val mSeslRoundedCornerTop: SeslRoundedCorner = SeslRoundedCorner(mContext, true)
        private val mSeslRoundedCornerBottom: SeslRoundedCorner
        private var mDivider: Drawable? = null
        private var mDividerHeight = 0
        override fun seslOnDispatchDraw(canvas: Canvas, recyclerView: RecyclerView, state: RecyclerView.State) {
            super.seslOnDispatchDraw(canvas, recyclerView, state)
            val childCount = recyclerView.childCount
            val width = recyclerView.width

            // draw divider for each item
            for (i in 0 until childCount) {
                val childAt = recyclerView.getChildAt(i)
                val viewHolder = recyclerView.getChildViewHolder(childAt) as ImageAdapter.ViewHolder
                val y = childAt.y.toInt() + childAt.height
                val shallDrawDivider: Boolean =
                    if (recyclerView.getChildAt(i + 1) != null) (recyclerView.getChildViewHolder(
                        recyclerView.getChildAt(i + 1)
                    ) as ImageAdapter.ViewHolder).isItem else false
                if (mDivider != null && viewHolder.isItem && shallDrawDivider) {
                    mDivider!!.setBounds(0, y, width, mDividerHeight + y)
                    mDivider!!.draw(canvas)
                }
                if (!viewHolder.isItem) {
                    if (recyclerView.getChildAt(i - 1) != null) mSeslRoundedCornerBottom.drawRoundedCorner(
                        recyclerView.getChildAt(i - 1),
                        canvas
                    )
                }
            }
            //mSeslRoundedCornerTop.drawRoundedCorner(canvas)
        }

        fun setDivider(d: Drawable) {
            mDivider = d
            mDividerHeight = d.intrinsicHeight
            listView.invalidateItemDecorations()
        }

        init {
            mSeslRoundedCornerTop.roundedCorners = 3
            mSeslRoundedCornerBottom = SeslRoundedCorner(mContext, true)
            mSeslRoundedCornerBottom.roundedCorners = 12
        }
    }
}