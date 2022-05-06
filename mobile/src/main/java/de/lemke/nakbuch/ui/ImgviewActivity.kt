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
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toFile
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager.widget.ViewPager
import de.dlyt.yanndroid.oneui.dialog.AlertDialog
import de.dlyt.yanndroid.oneui.layout.DrawerLayout
import de.dlyt.yanndroid.oneui.utils.CustomButtonClickListener
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.dlyt.yanndroid.oneui.view.TipPopup
import de.dlyt.yanndroid.oneui.widget.TabLayout
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.hymndata.*
import de.lemke.nakbuch.domain.hymns.GetHymnUseCase
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.HymnData
import de.lemke.nakbuch.domain.settings.GetBooleanSettingUseCase
import de.lemke.nakbuch.domain.settings.GetBuchModeUseCase
import de.lemke.nakbuch.domain.settings.SetBooleanSettingUseCase
import de.lemke.nakbuch.domain.utils.ViewPagerAdapterImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.min

class ImgviewActivity : AppCompatActivity() {
    private lateinit var mContext: Context
    private var tabLayout: TabLayout? = null
    private lateinit var imgViewPager: ViewPager
    private lateinit var viewPagerAdapterImageView: ViewPagerAdapterImageView
    private lateinit var tipPopupFoto: TipPopup
    private lateinit var tipPopupDelete: TipPopup
    private lateinit var hymn: Hymn
    private lateinit var hymnData: HymnData
    private lateinit var buchMode: BuchMode
    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil(this)
        mContext = this
        setContentView(R.layout.activity_imgview)
        imgViewPager = findViewById(R.id.img_view_pager)
        buchMode = GetBuchModeUseCase()()
        CoroutineScope(Dispatchers.IO).launch {
            hymn = GetHymnUseCase()(buchMode, intent.getIntExtra("nr", -1))
            hymnData = GetHymnDataUseCase()(hymn)
            withContext(Dispatchers.Main) {
                initViewPager()
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
                    drawerLayout.setNavigationButtonIcon(
                        AppCompatResources.getDrawable(mContext, de.dlyt.yanndroid.oneui.R.drawable.ic_oui_back)
                    )
                    drawerLayout.setNavigationButtonTooltip(getString(de.dlyt.yanndroid.oneui.R.string.sesl_navigate_up))
                    drawerLayout.setNavigationButtonOnClickListener { onBackPressed() }
                    drawerLayout.setTitle(hymn.numberAndTitle)
                    drawerLayout.setSubtitle(getString(if (buchMode == BuchMode.Gesangbuch) R.string.titleGesangbuch else R.string.titleChorbuch))
                    initBNV()
                    if (GetBooleanSettingUseCase()("imageviewTips", true)) {
                        SetBooleanSettingUseCase()("imageviewTips", false)

                        Handler(Looper.getMainLooper()).postDelayed({
                            initTipPopup()
                            tipPopupFoto.show(TipPopup.DIRECTION_TOP_LEFT)
                        }, 50)

                    }
                } else {
                    val windowInsetsController = ViewCompat.getWindowInsetsController(window.decorView)
                    windowInsetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars()) // Hide both the status bar and the navigation bar
                }
            }
        }

        cameraActivityResultLauncher = registerForActivityResult(TakePicture()) { result: Boolean ->
            if (result) {
                CoroutineScope(Dispatchers.IO).launch {
                    val index = if (viewPagerAdapterImageView.count > 0) (imgViewPager.currentItem + 1) else imgViewPager.currentItem
                    AddPhotoUseCase()(
                        mContext, hymn, hymnData,
                        index
                    ).invokeOnCompletion {
                        initViewPager(index)
                    }
                }
            } else {
                Toast.makeText(mContext, getString(R.string.fotoError), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initViewPager(index: Int = -1) {
        CoroutineScope(Dispatchers.IO).launch {
            hymnData = GetHymnDataUseCase()(hymn)
            withContext(Dispatchers.Main) {
                hymnData.photoList = hymnData.photoList.filter {Uri.parse(it).toFile().exists()} as ArrayList<String>
                viewPagerAdapterImageView = ViewPagerAdapterImageView(mContext, hymnData.photoList.map { Uri.parse(it)} as ArrayList<Uri>)
                imgViewPager.adapter = viewPagerAdapterImageView
                if (index > 0) imgViewPager.setCurrentItem(min(index,viewPagerAdapterImageView.count), true)
            }
        }
    }

    private fun takePhoto() {
        val maxImagesPerHymn = 20
        if (hymnData.photoList.size >= maxImagesPerHymn) {
            Toast.makeText(mContext, "Max. Bilder pro Lied: $maxImagesPerHymn", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            cameraActivityResultLauncher.launch(GetTempPhotoUriUseCase()())
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(mContext, "Kamera konnte nicht geöffnet werden...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initBNV() {
        if (tabLayout == null) {
            tabLayout = findViewById(R.id.imgView_bnv)
        } else {
            tabLayout!!.removeAllTabs()
        }

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
                CoroutineScope(Dispatchers.IO).launch {
                    SetHymnDataUseCase()(hymn, hymnData)
                }
                initBNV()
            }
        })

        val camIcon = AppCompatResources.getDrawable(mContext, de.dlyt.yanndroid.oneui.R.drawable.ic_oui4_camera)
        tabLayout!!.addTabCustomButton(camIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                takePhoto()
            }
        })

        val binIcon = AppCompatResources.getDrawable(mContext, de.dlyt.yanndroid.oneui.R.drawable.ic_oui4_delete)
        tabLayout!!.addTabCustomButton(binIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                val index = imgViewPager.currentItem
                if (hymnData.photoList.size > 0) {
                    val dialog = AlertDialog.Builder(mContext)
                        .setTitle(getString(R.string.deletePhoto) + "?")
                        .setMessage(getString(R.string.deleteCurrentPhoto) + "?")
                        .setNeutralButton(de.dlyt.yanndroid.oneui.R.string.sesl_cancel, null)
                        .setNegativeButton(R.string.delete) { dialogInterface: DialogInterface, _: Int ->
                            CoroutineScope(Dispatchers.IO).launch {
                                DeletePhotoUseCase()(hymn, hymnData, index)
                                withContext(Dispatchers.Main) {
                                    dialogInterface.dismiss()
                                    initViewPager(min(index, viewPagerAdapterImageView.count - 1))
                                }
                            }
                        }
                        .setNegativeButtonColor(resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red, mContext.theme))
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
       Log.d("test Files", files.contentToString())
       Arrays.sort(files)
       for (i in files.indices) {
           if (!files[i].renameTo(File(currentFolder, "$i.jpg"))) {
               throw IOException("prepareFolder(): Could not rename File:" + files[i].absolutePath)
           }
       }
       Log.d("test sorted Files", files.contentToString())
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
