package de.lemke.nakbuch.domain.utils

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import de.lemke.nakbuch.BuildConfig
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.model.BuchMode


class AppUtils {
    companion object {
        fun openBischoffApp(context: Context, buchMode: BuchMode) {
            openAppWithPackageName(
                context, context.getString(
                    if (buchMode == BuchMode.Gesangbuch) R.string.bischoffGesangbuchPackageName else R.string.bischoffChorbuchPackageName
                )
            )
        }

        fun openAppWithPackageName(context: Context, packageName: String) {
            var intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent == null) {
                intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("market://details?id=$packageName")
            }
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (anfe: ActivityNotFoundException) {
                intent.data =
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                context.startActivity(intent)
            }
        }

        /* Since Android 11 (API level 30), most user-installed apps are not visible by default.
        In your manifest, you must statically declare which apps you are going to get info about, as in the following:
        <manifest>
            <queries>
                <!-- Explicit apps you know in advance about: -->
                <package android:name="com.example.this.app"/>
                <package android:name="com.example.this.other.app"/>
            </queries>
            ...
        </manifest> */
        fun isPackageInstalled(context: Context, packageName: String): Boolean {
            return try {
                context.packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }

        @SuppressLint("ApplySharedPref")
        fun checkAppStart(sp: SharedPreferences): AppStart {
            val lastVersionCode = sp.getInt("lastAppVersion", -1)
            val versionCode: Int = BuildConfig.VERSION_CODE
            val versionName: String = BuildConfig.VERSION_NAME
            sp.edit().putInt("lastAppVersion", versionCode).commit()
            Log.d("CheckAppStart", "Current version code: $versionCode , last version code: $lastVersionCode")
            return when {
                lastVersionCode == -1 -> {
                    AppStart.FIRST_TIME
                }
                lastVersionCode <= 60 -> {
                    AppStart.OLD_ARCHITECTURE
                }
                lastVersionCode < versionCode -> {
                    AppStart.FIRST_TIME_VERSION
                }
                lastVersionCode > versionCode -> {
                    Log.w(
                        "checkAppStart",
                        "Current version code ($versionCode) is less then the one recognized on last startup ($lastVersionCode). Defensively assuming normal app start."
                    )
                    AppStart.NORMAL
                }
                else -> {
                    AppStart.NORMAL
                }
            }
        }

    }

    enum class AppStart {
        FIRST_TIME, FIRST_TIME_VERSION, NORMAL, OLD_ARCHITECTURE
    }
}