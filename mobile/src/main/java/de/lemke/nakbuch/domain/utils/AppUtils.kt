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
        @JvmStatic
        fun openBischoffApp(context: Context, buchMode: BuchMode) {
            openAppWithPackageName(
                context, context.getString(
                    if (buchMode == BuchMode.Gesangbuch) R.string.bischoffGesangbuchPackageName else R.string.bischoffChorbuchPackageName
                )
            )
        }

        @JvmStatic
        fun openAppWithPackageName(mContext: Context, packageName: String) {
            val intent = mContext.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                mContext.startActivity(intent)
            } else {
                openAppWithPackageNameOnStore(mContext, packageName)
            }
        }
        @JvmStatic
        fun openAppWithPackageNameOnStore(mContext: Context, packageName: String) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(mContext.getString(R.string.playStoreAppLink) + packageName)
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                mContext.startActivity(intent)
            } catch (anfe: ActivityNotFoundException) {
                intent.data =
                    Uri.parse(mContext.getString(R.string.playStoreLink) + packageName)
                mContext.startActivity(intent)
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
        @JvmStatic
        fun isPackageInstalled(context: Context, packageName: String): Boolean {
            return try {
                context.packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }

        @JvmStatic
        @SuppressLint("ApplySharedPref")
        fun checkAppStart(sp: SharedPreferences): AppStart {
            val lastVersionName: String = sp.getString("lastAppVersionName", "undefined")!!
            val lastVersionCode: Int = sp.getInt("lastAppVersionCode", -1)
            val versionCode: Int = BuildConfig.VERSION_CODE
            val versionName: String = BuildConfig.VERSION_NAME
            sp.edit().putInt("lastAppVersionCode", versionCode).commit()
            sp.edit().putString("lastAppVersionName", versionName).commit()
            Log.d("CheckAppStart", "Current version code: $versionCode , last version code: $lastVersionCode")
            Log.d("CheckAppStart", "Current version name: $versionName , last version name: $lastVersionName")
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