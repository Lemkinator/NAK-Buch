package de.lemke.nakbuch.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.lemke.nakbuch.R
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
@Suppress("UNCHECKED_CAST")
object AssetsHelper {
    fun validHymnr(buchMode: Boolean, hymnNr: String): Int {
        val result: Int
        try {
            result = hymnNr.toInt()
            if (!buchMode && 0 < result && result <= 462 || buchMode && 0 < result && result <= 438) return result
        } catch (e: NumberFormatException) {
            Log.d("InvalidHymNr: ", e.toString())
        }
        return -1
    }

    fun getHymnArrayList(
        mContext: Context,
        assetFilename: String,
        sp: SharedPreferences
    ): ArrayList<HashMap<String, String>> {
        val fis: InputStream
        val ois: ObjectInputStream
        var result: ArrayList<HashMap<String, String>>? = null
        if (assetFilename == mContext.getString(R.string.filename_hymnsGesangbuch) && sp.getString(
                "privateTextGesangbuch",
                null
            ) != null
        ) {
            return Gson().fromJson(
                sp.getString("privateTextGesangbuch", null),
                object : TypeToken<ArrayList<HashMap<String?, String?>?>?>() {}.type
            )
        } else if (assetFilename == mContext.getString(R.string.filename_hymnsChorbuch) && sp.getString(
                "privateTextChorbuch",
                null
            ) != null
        ) {
            return Gson().fromJson(
                sp.getString("privateTextChorbuch", null),
                object : TypeToken<ArrayList<HashMap<String?, String?>?>?>() {}.type
            )
        }
        try {
            fis = mContext.assets.open(assetFilename)
            ois = ObjectInputStream(fis)
            result = ois.readObject() as ArrayList<HashMap<String, String>>
            ois.close()
            fis.close()
        } catch (c: IOException) {
            c.printStackTrace()
            Toast.makeText(mContext, c.toString(), Toast.LENGTH_LONG).show()
        } catch (c: ClassNotFoundException) {
            c.printStackTrace()
            Toast.makeText(mContext, c.toString(), Toast.LENGTH_LONG).show()
        }
        return result!!
    }
}