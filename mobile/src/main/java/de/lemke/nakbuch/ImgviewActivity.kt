package de.lemke.nakbuch

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.FileProvider
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
import de.lemke.nakbuch.utils.Constants
import de.lemke.nakbuch.utils.HymnPrefsHelper.getFromList
import de.lemke.nakbuch.utils.HymnPrefsHelper.writeToList
import de.lemke.nakbuch.utils.ViewPagerAdapterImageView
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*

class ImgviewActivity : AppCompatActivity() {
    private lateinit var mContext: Context
    private var tabLayout: TabLayout? = null
    private lateinit var imgViewPager: ViewPager
    private lateinit var viewPagerAdapterImageView: ViewPagerAdapterImageView
    private lateinit var tipPopupFoto: TipPopup
    private lateinit var tipPopupDelete: TipPopup
    private lateinit var imageList: ArrayList<Uri>
    private var currentFile: File? = null
    private lateinit var sp: SharedPreferences
    private lateinit var spHymns: SharedPreferences
    private var nr = 0
    private var gesangbuchSelected = false
    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Uri>
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtil(this)
        super.onCreate(savedInstanceState)
        mContext = this
        sp =
            mContext.getSharedPreferences(getString(R.string.preference_file_default), MODE_PRIVATE)
        spHymns =
            mContext.getSharedPreferences(getString(R.string.preference_file_hymns), MODE_PRIVATE)
        gesangbuchSelected = sp.getBoolean("gesangbuchSelected", true)
        nr = intent.getIntExtra("nr", -1)

        setContentView(R.layout.activity_imgview)
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawerLayout.setNavigationButtonIcon(
                AppCompatResources.getDrawable(
                    mContext,
                    de.dlyt.yanndroid.oneui.R.drawable.ic_oui_back
                )
            )
            drawerLayout.setNavigationButtonTooltip(getString(de.dlyt.yanndroid.oneui.R.string.sesl_navigate_up))
            drawerLayout.setNavigationButtonOnClickListener { onBackPressed() }
            drawerLayout.setTitle(intent.getStringExtra("nrAndTitle"))
            drawerLayout.setSubtitle(getString(if (gesangbuchSelected) R.string.title_Gesangbuch else R.string.title_Chorbuch))
            initBNV()
        } else {
            val windowInsetsController =
                ViewCompat.getWindowInsetsController(window.decorView) ?: return
            // Configure the behavior of the hidden system bars
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            // Hide both the status bar and the navigation bar
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        }

        cameraActivityResultLauncher = registerForActivityResult(TakePicture()) { result: Boolean ->
            if (currentFile != null) {
                if (result) {
                    compressJPG(currentFile!!).invokeOnCompletion {
                        Handler(Looper.getMainLooper()).post {
                            initViewPager()
                            imgViewPager.setCurrentItem(viewPagerAdapterImageView.count - 1, true)
                        }
                    }
                } else {
                    if (currentFile!!.exists()) {
                        if (!currentFile!!.delete()) Log.e(
                            "Files",
                            "Could not delete file, after Take picture failed"
                        )
                    }
                    Toast.makeText(
                        mContext,
                        "Kein Foto ausgewählt oder Fehler beim Verarbeiten des Fotos...",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        imgViewPager = findViewById(R.id.img_view_pager)
        initViewPager()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (sp.getBoolean("showImageviewTips", true)) {
            Handler(Looper.getMainLooper()).postDelayed({
                initTipPopup()
                tipPopupFoto.show(TipPopup.DIRECTION_TOP_LEFT)
                sp.edit().putBoolean("showImageviewTips", false).apply()
            }, 50)
        }
    }

    private fun initViewPager() {
        try {
            prepareFolder()
            imageList = ArrayList()
            val files = currentFolder.listFiles()!!
            for (i in files.indices) {
                imageList.add(
                    FileProvider.getUriForFile(
                        mContext,
                        "de.lemke.nakbuch.fileprovider",
                        files[i]
                    )
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Log.d("test img List", imageList.toString())
        viewPagerAdapterImageView = ViewPagerAdapterImageView(mContext, imageList)
        imgViewPager.adapter = viewPagerAdapterImageView
    }

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

    private fun compressJPG(file: File): Job {
        Log.d("Compressor", "Compressing file: " + file.absolutePath)
        val resolution = when (sp.getString("imgResolution", "Mittel")) {
            "Sehr Niedrig" -> 512
            "Niedrig" -> 1024
            "Mittel" -> 2048
            "Hoch" -> 4096
            "Sehr Hoch" -> 8192
            else -> 2048
        }
        val quality = when (sp.getString("imgQuality", "Mittel")) {
            "Sehr Niedrig" -> 15
            "Niedrig" -> 25
            "Mittel" -> 50
            "Hoch" -> 75
            "Sehr Hoch" -> 100
            else -> 50
        }
        return GlobalScope.launch {
            Compressor.compress(mContext, file) {
                resolution(resolution, resolution)
                quality(quality)
                size(2_097_152) // 2 MB
                format(Bitmap.CompressFormat.JPEG)
                destination(File(currentFolder.absolutePath, file.name))
            }
        }
    }

    @get:Throws(IOException::class)
    private val currentFolder: File
        get() {
            val resultFolder = File(
                if (gesangbuchSelected) {
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
                Toast.makeText(mContext, "Datei konnte nicht gelöscht werden...", Toast.LENGTH_LONG)
                    .show()
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
                    if (!image.delete()) Toast.makeText(
                        mContext,
                        "Konnte altes Foto vor Kamera-öffnen nicht löschen",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return image
            }
        }
        throw IOException("Datei konnte nicht erstellt werden...")
    }

    private fun takePhoto() {
        if (imageList.size >= Constants.MAX_IMAGES_PER_HYMN) {
            Toast.makeText(
                mContext,
                "Max. Bilder pro Lied: ${Constants.MAX_IMAGES_PER_HYMN}",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        currentFile = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(mContext, "Fehler beim Erstellen einer Datei", Toast.LENGTH_LONG)
                .show()
            Log.e("Cam", "Error occurred while creating the File: ${ex.message}")
            return
        }
        try {
            cameraActivityResultLauncher.launch(
                FileProvider.getUriForFile(
                    mContext,
                    "de.lemke.nakbuch.fileprovider",
                    currentFile!!
                )
            )
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                mContext,
                "Kamera konnte nicht geöffnet werden...",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun initBNV() {
        if (tabLayout == null) {
            tabLayout = findViewById(R.id.textView_bnv)
        } else {
            tabLayout!!.removeAllTabs()
        }

        val favIcon: Drawable
        if (getFromList(gesangbuchSelected, spHymns, nr, "fav") == "1") {
            favIcon = AppCompatResources.getDrawable(
                mContext,
                de.dlyt.yanndroid.oneui.R.drawable.ic_oui_like_on
            )!!
            favIcon.colorFilter = PorterDuffColorFilter(
                resources.getColor(
                    de.dlyt.yanndroid.oneui.R.color.red,
                    mContext.theme
                ), PorterDuff.Mode.SRC_IN
            )
        } else {
            favIcon = AppCompatResources.getDrawable(
                mContext,
                de.dlyt.yanndroid.oneui.R.drawable.ic_oui_like_off
            )!!
        }
        tabLayout!!.addTabCustomButton(favIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                writeToList(
                    gesangbuchSelected,
                    spHymns,
                    nr,
                    "fav",
                    if (getFromList(gesangbuchSelected, spHymns, nr, "fav") == "1") "0" else "1"
                )
                initBNV()
            }
        })

        val camIcon = AppCompatResources.getDrawable(
            mContext,
            de.dlyt.yanndroid.oneui.R.drawable.ic_oui4_camera
        )
        tabLayout!!.addTabCustomButton(camIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                takePhoto()
            }
        })

        val binIcon = AppCompatResources.getDrawable(
            mContext,
            de.dlyt.yanndroid.oneui.R.drawable.ic_oui4_delete
        )
        tabLayout!!.addTabCustomButton(binIcon, object : CustomButtonClickListener(tabLayout) {
            override fun onClick(v: View) {
                val index = imgViewPager.currentItem
                if (imageList.size > 0) {
                    val dialog = AlertDialog.Builder(mContext)
                        .setTitle("Foto löschen?")
                        .setMessage("Aktuell angezeigtes Foto löschen?")
                        .setNeutralButton(de.dlyt.yanndroid.oneui.R.string.sesl_cancel, null)
                        .setNegativeButton(R.string.delete) { dialogInterface: DialogInterface, _: Int ->
                            deleteImage(index)
                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    dialogInterface.dismiss()
                                    initViewPager()
                                    imgViewPager.setCurrentItem(
                                        (index).coerceAtMost(
                                            viewPagerAdapterImageView.count - 1
                                        ), true
                                    )
                                }, 600
                            )

                        }
                        .setNegativeButtonColor(
                            resources.getColor(
                                de.dlyt.yanndroid.oneui.R.color.sesl_functional_red,
                                mContext.theme
                            )
                        )
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

    private fun initTipPopup() {
        tipPopupFoto = TipPopup(
            Objects.requireNonNull(
                tabLayout?.getTabAt(1)
            )?.seslGetTextView()
        )
        tipPopupDelete = TipPopup(
            Objects.requireNonNull(
                tabLayout?.getTabAt(2)
            )?.seslGetTextView()
        )
        tipPopupFoto.setBackgroundColor(
            resources.getColor(
                de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color,
                theme
            )
        )
        tipPopupDelete.setBackgroundColor(
            resources.getColor(
                de.dlyt.yanndroid.oneui.R.color.oui_tip_popup_background_color,
                theme
            )
        )
        tipPopupFoto.setExpanded(true)
        tipPopupDelete.setExpanded(true)
        tipPopupFoto.setOnDismissListener { tipPopupDelete.show(TipPopup.DIRECTION_TOP_LEFT) }
        tipPopupFoto.setMessage(getString(R.string.fotoTip))
        tipPopupDelete.setMessage(getString(R.string.deleteFotoTip))
    }

}