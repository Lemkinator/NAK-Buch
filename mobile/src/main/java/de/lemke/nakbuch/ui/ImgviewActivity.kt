package de.lemke.nakbuch.ui

import android.content.ActivityNotFoundException
import android.content.Context
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.math.min

@AndroidEntryPoint
class ImgviewActivity : AppCompatActivity() {
    private val coroutineContext: CoroutineContext = Dispatchers.Main
    private val coroutineScope: CoroutineScope = CoroutineScope(coroutineContext)
    private lateinit var context: Context
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
        context = this
        setContentView(R.layout.activity_imgview)
        imgViewPager = findViewById(R.id.img_view_pager)
        coroutineScope.launch {
            val nullableHymnId = HymnId.create(intent.getIntExtra("hymnId", -1))
            if (nullableHymnId == null) finish()
            else hymnId = nullableHymnId
            personalHymn = getPersonalHymn(hymnId)
            initViewPager()
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
                drawerLayout.setNavigationButtonIcon(
                    AppCompatResources.getDrawable(context, de.dlyt.yanndroid.oneui.R.drawable.ic_oui_back)
                )
                drawerLayout.setNavigationButtonTooltip(getString(de.dlyt.yanndroid.oneui.R.string.sesl_navigate_up))
                drawerLayout.setNavigationButtonOnClickListener { onBackPressed() }
                drawerLayout.setTitle(personalHymn.hymn.numberAndTitle)
                drawerLayout.setSubtitle(hymnId.buchMode.toString())
                initBNV()
                if (getUserSettings().showImageViewTips) {
                    updateUserSettings { it.copy(showImageViewTips = false) }
                    //Handler(Looper.getMainLooper()).postDelayed({
                        initTipPopup()
                        tipPopupFoto.show(TipPopup.DIRECTION_TOP_LEFT)
                    //}, 50)

                }
            } else {
                val windowInsetsController = ViewCompat.getWindowInsetsController(window.decorView)
                windowInsetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars()) // Hide both the status bar and the navigation bar
            }
        }
        cameraActivityResultLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { result: Boolean ->
            if (result) {
                coroutineScope.launch {
                    val index = if (viewPagerAdapterImageView.count > 0) (imgViewPager.currentItem + 1) else imgViewPager.currentItem
                    addPhoto(personalHymn, index).invokeOnCompletion {
                        coroutineScope.launch {
                            initViewPager(index)
                        }
                    }
                }
            } else {
                Toast.makeText(context, getString(R.string.fotoError), Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun initViewPager(index: Int = -1) {
        personalHymn = getPersonalHymn(hymnId)
        personalHymn.photoList = personalHymn.photoList.filter { it.toFile().exists() }.toMutableList()
        viewPagerAdapterImageView = ViewPagerAdapterImageView(context, personalHymn.photoList)
        imgViewPager.adapter = viewPagerAdapterImageView
        if (index > 0) imgViewPager.setCurrentItem(min(index, viewPagerAdapterImageView.count), true)
    }

    private fun takePhoto() {
        val maxImagesPerHymn = 20
        if (personalHymn.photoList.size >= maxImagesPerHymn) {
            Toast.makeText(context, "Max. Bilder pro Lied: $maxImagesPerHymn", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            cameraActivityResultLauncher.launch(getTempPhotoUri(true))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Kamera konnte nicht geöffnet werden...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initBNV() {
        if (tabLayout == null) {
            tabLayout = findViewById(R.id.imgView_bnv)
        } else {
            tabLayout!!.removeAllTabs()
        }

        val favIcon: Drawable
        if (personalHymn.favorite) {
            favIcon = AppCompatResources.getDrawable(context, de.dlyt.yanndroid.oneui.R.drawable.ic_oui_like_on)!!
            favIcon.colorFilter = PorterDuffColorFilter(
                resources.getColor(de.dlyt.yanndroid.oneui.R.color.red, context.theme), PorterDuff.Mode.SRC_IN
            )
        } else {
            favIcon = AppCompatResources.getDrawable(context, de.dlyt.yanndroid.oneui.R.drawable.ic_oui_like_off)!!
        }
        tabLayout!!.addTabCustomButton(favIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                personalHymn.favorite = !personalHymn.favorite
                coroutineScope.launch {
                    setPersonalHymn(personalHymn)
                }
                initBNV()
            }
        })

        val camIcon = AppCompatResources.getDrawable(context, de.dlyt.yanndroid.oneui.R.drawable.ic_oui4_camera)
        tabLayout!!.addTabCustomButton(camIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                takePhoto()
            }
        })

        val binIcon = AppCompatResources.getDrawable(context, de.dlyt.yanndroid.oneui.R.drawable.ic_oui4_delete)
        tabLayout!!.addTabCustomButton(binIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                val index = imgViewPager.currentItem
                if (personalHymn.photoList.isNotEmpty()) {
                    val dialog = AlertDialog.Builder(context)
                        .setTitle(getString(R.string.deletePhoto) + "?")
                        .setMessage(getString(R.string.deleteCurrentPhoto) + "?")
                        .setNeutralButton(de.dlyt.yanndroid.oneui.R.string.sesl_cancel, null)
                        .setNegativeButton(R.string.delete) { dialogInterface: DialogInterface, _: Int ->
                            coroutineScope.launch {
                                deletePhoto(personalHymn, index)
                                dialogInterface.dismiss()
                                initViewPager(min(index, viewPagerAdapterImageView.count - 1))
                            }
                        }
                        .setNegativeButtonColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red, context.theme))
                        .setNegativeButtonProgress(true)
                        .create()
                    dialog.show()
                }
            }
        })

        //Drawable rotateLeft = AppCompatResources.getDrawable(mContext, R.drawable.ic_samsung_refresh);
        //Drawable rotateLeft = AppCompatResources.getDrawable(mContext, R.drawable.ic_samsung_undo);
        /*Drawable rotateLeft = AppCompatResources.getDrawable(mContext, R.drawable.ic_samsung_sync);
        tabLayout.addTabCustomButton(rotateLeft, new CustomButtonClickListener(tabLayout) {
            @Override
            public void onClick(View v) {

            }
        });*/

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


/*
   @Throws(IOException::class)
   private fun prepareFolder() {
       val files = currentFolder.listFiles()!!
       Arrays.sort(files)
       for (i in files.indices) {
           if (!files[i].renameTo(File(currentFolder, "$i.jpg"))) {
               throw IOException("prepareFolder(): Could not rename File:" + files[i].absolutePath)
           }
       }
   }

   @get:Throws(IOException::class)
   private val currentFolder: File
       get() {
           val resultFolder = File(
               if (buchMode == BuchMode.Gesangbuch) {
                   "$filesDir/gesangbuch/$nr/"
               } else {
                   "$filesDir/chorbuch/$nr/"
               }
           )
           if (!resultFolder.exists()) {
               if (!resultFolder.mkdirs()) {
                   throw IOException("Ordner konnte nicht erstellt werden...")
               }
           }
           return resultFolder
       }

   private fun deleteImage(index: Int) {
       var f: File? = null
       try {
           f = File(currentFolder, "$index.jpg")
       } catch (e: IOException) {
           e.printStackTrace()
       }
       if (f!!.exists()) {
           if (!f.delete())
               Toast.makeText(mContext, "Datei konnte nicht gelöscht werden...", Toast.LENGTH_LONG).show()
       }
   }

   @Throws(IOException::class)
   private fun createImageFile(): File {
       var image: File
       for (i in 0 until Constants.MAX_IMAGES_PER_HYMN) {
           image = File(currentFolder, "$i.jpg")
           if (!image.exists()) {
               image = File(cacheDir, "$i.jpg")
               if (image.exists()) {
                   if (!image.delete())
                       Toast.makeText(mContext, "Konnte altes Foto vor Kamera-öffnen nicht löschen", Toast.LENGTH_LONG).show()
               }
               return image
           }
       }
       throw IOException("Datei konnte nicht erstellt werden...")
   }

   private fun takePhoto() {
       if (imageList.size >= Constants.MAX_IMAGES_PER_HYMN) {
           Toast.makeText(mContext, "Max. Bilder pro Lied: ${Constants.MAX_IMAGES_PER_HYMN}", Toast.LENGTH_SHORT).show()
           return
       }
       currentFile = try {
           createImageFile()
       } catch (ex: IOException) {
           Toast.makeText(mContext, "Fehler beim Erstellen einer Datei", Toast.LENGTH_LONG).show()
           Log.e("Cam", "Error occurred while creating the File: ${ex.message}")
           return
       }
       try {
           cameraActivityResultLauncher.launch(
               FileProvider.getUriForFile(mContext, "de.lemke.nakbuch.fileprovider", currentFile!!)
           )
       } catch (e: ActivityNotFoundException) {
           Toast.makeText(mContext, "Kamera konnte nicht geöffnet werden...", Toast.LENGTH_SHORT).show()
       }
   }
   */
