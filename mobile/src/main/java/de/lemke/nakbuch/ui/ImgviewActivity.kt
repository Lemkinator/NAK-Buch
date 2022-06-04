package de.lemke.nakbuch.ui

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toFile
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import dagger.hilt.android.AndroidEntryPoint
import de.dlyt.yanndroid.oneui.dialog.AlertDialog
import de.dlyt.yanndroid.oneui.layout.DrawerLayout
import de.dlyt.yanndroid.oneui.utils.CustomButtonClickListener
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.dlyt.yanndroid.oneui.view.TipPopup
import de.dlyt.yanndroid.oneui.widget.TabLayout
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.GetUserSettingsUseCase
import de.lemke.nakbuch.domain.UpdateUserSettingsUseCase
import de.lemke.nakbuch.domain.hymndataUseCases.*
import de.lemke.nakbuch.domain.model.HymnId
import de.lemke.nakbuch.domain.model.PersonalHymn
import de.lemke.nakbuch.domain.utils.ViewPagerAdapterImageView
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class ImgviewActivity : AppCompatActivity() {
    private var tabLayout: TabLayout? = null
    private lateinit var imgViewPager: ViewPager
    private lateinit var viewPagerAdapterImageView: ViewPagerAdapterImageView
    private lateinit var tipPopupFoto: TipPopup
    private lateinit var tipPopupDelete: TipPopup
    private lateinit var hymnId: HymnId
    private lateinit var personalHymn: PersonalHymn
    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Uri>

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    @Inject
    lateinit var getPersonalHymn: GetPersonalHymnUseCase

    @Inject
    lateinit var setPersonalHymn: SetPersonalHymnUseCase

    @Inject
    lateinit var addPhoto: AddPhotoUseCase

    @Inject
    lateinit var deletePhoto: DeletePhotoUseCase

    @Inject
    lateinit var getTempPhotoUri: GetTempPhotoUriUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil(this)
        setContentView(R.layout.activity_imgview)
        imgViewPager = findViewById(R.id.img_view_pager)
        lifecycleScope.launch {
            val nullableHymnId = HymnId.create(intent.getIntExtra("hymnId", -1))
            if (nullableHymnId == null) finish()
            else hymnId = nullableHymnId
            personalHymn = getPersonalHymn(hymnId)
            initViewPager()
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
                drawerLayout.setNavigationButtonIcon(
                    AppCompatResources.getDrawable(this@ImgviewActivity, de.dlyt.yanndroid.oneui.R.drawable.ic_oui_back)
                )
                drawerLayout.setNavigationButtonTooltip(getString(de.dlyt.yanndroid.oneui.R.string.sesl_navigate_up))
                drawerLayout.setNavigationButtonOnClickListener { onBackPressed() }
                drawerLayout.setTitle(personalHymn.hymn.numberAndTitle)
                drawerLayout.setSubtitle(hymnId.buchMode.toString())
                initBNV()
                if (getUserSettings().showImageViewTips) {
                    updateUserSettings { it.copy(showImageViewTips = false) }
                    initTipPopup()
                    tipPopupFoto.show(TipPopup.DIRECTION_TOP_LEFT)
                }
            } else {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                WindowInsetsControllerCompat(window, findViewById<DrawerLayout>(R.id.drawer_layout)).let { controller ->
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        }
        cameraActivityResultLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { result: Boolean ->
            if (result) {
                lifecycleScope.launch {
                    val index = if (viewPagerAdapterImageView.count > 0) (imgViewPager.currentItem + 1) else 0
                    addPhoto(personalHymn, index).invokeOnCompletion {
                        lifecycleScope.launch { initViewPager(index) }
                    }
                }
            } else Toast.makeText(this@ImgviewActivity, getString(R.string.fotoError), Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun initViewPager(index: Int = -1) {
        personalHymn = getPersonalHymn(hymnId)
        personalHymn.photoList = personalHymn.photoList.filter { it.toFile().exists() }.toMutableList()
        viewPagerAdapterImageView = ViewPagerAdapterImageView(this, personalHymn.photoList)
        imgViewPager.adapter = viewPagerAdapterImageView
        if (index > 0) imgViewPager.setCurrentItem(min(index, viewPagerAdapterImageView.count), true)
    }

    private fun takePhoto() {
        val maxImagesPerHymn = 20
        if (personalHymn.photoList.size >= maxImagesPerHymn) {
            Toast.makeText(this, "Max. Bilder pro Lied: $maxImagesPerHymn", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            cameraActivityResultLauncher.launch(getTempPhotoUri(true))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Kamera konnte nicht geöffnet werden...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initBNV() {
        if (tabLayout == null) tabLayout = findViewById(R.id.imgView_bnv)
        else tabLayout!!.removeAllTabs()

        val favIcon: Drawable
        if (personalHymn.favorite) {
            favIcon = AppCompatResources.getDrawable(this, de.dlyt.yanndroid.oneui.R.drawable.ic_oui_like_on)!!
            favIcon.colorFilter = PorterDuffColorFilter(
                resources.getColor(de.dlyt.yanndroid.oneui.R.color.red, this.theme), PorterDuff.Mode.SRC_IN
            )
        } else favIcon = AppCompatResources.getDrawable(this, de.dlyt.yanndroid.oneui.R.drawable.ic_oui_like_off)!!
        tabLayout!!.addTabCustomButton(favIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                personalHymn.favorite = !personalHymn.favorite
                lifecycleScope.launch { setPersonalHymn(personalHymn) }
                initBNV()
            }
        })

        val camIcon = AppCompatResources.getDrawable(this, de.dlyt.yanndroid.oneui.R.drawable.ic_oui4_camera)
        tabLayout!!.addTabCustomButton(camIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                takePhoto()
            }
        })

        val binIcon = AppCompatResources.getDrawable(this, de.dlyt.yanndroid.oneui.R.drawable.ic_oui4_delete)
        tabLayout!!.addTabCustomButton(binIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                val index = imgViewPager.currentItem
                if (personalHymn.photoList.isNotEmpty()) {
                    val dialog = AlertDialog.Builder(this@ImgviewActivity)
                        .setTitle(getString(R.string.deletePhoto) + "?")
                        .setMessage(getString(R.string.deleteCurrentPhoto) + "?")
                        .setNeutralButton(de.dlyt.yanndroid.oneui.R.string.sesl_cancel, null)
                        .setNegativeButton(R.string.delete) { dialogInterface: DialogInterface, _: Int ->
                            lifecycleScope.launch {
                                deletePhoto(personalHymn, index)
                                dialogInterface.dismiss()
                                initViewPager(min(index, viewPagerAdapterImageView.count - 1))
                            }
                        }
                        .setNegativeButtonColor(
                            resources.getColor(
                                de.dlyt.yanndroid.oneui.R.color.sesl_functional_red,
                                this@ImgviewActivity.theme
                            )
                        )
                        .setNegativeButtonProgress(true)
                        .create()
                    dialog.show()
                }
            }
        })
    }

    @Suppress("unnecessary_safe_call")
    private fun initTipPopup() {
        tipPopupFoto = TipPopup(Objects.requireNonNull(tabLayout?.getTabAt(1))?.seslGetTextView())
        tipPopupDelete = TipPopup(Objects.requireNonNull(tabLayout?.getTabAt(2))?.seslGetTextView())
        tipPopupFoto.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, theme))
        tipPopupDelete.setBackgroundColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color, theme))
        tipPopupFoto.setExpanded(true)
        tipPopupDelete.setExpanded(true)
        tipPopupFoto.setOnDismissListener { tipPopupDelete.show(TipPopup.DIRECTION_TOP_LEFT) }
        tipPopupFoto.setMessage(getString(R.string.fotoTip))
        tipPopupDelete.setMessage(getString(R.string.deleteFotoTip))
    }
}