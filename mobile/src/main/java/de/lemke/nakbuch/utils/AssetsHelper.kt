package de.lemke.nakbuch.utils

import java.lang.NumberFormatException
import android.util.Log
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.Uri
import android.widget.Toast
import java.io.InputStream
import java.io.ObjectInputStream
import java.util.ArrayList
import java.util.HashMap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.lang.ClassNotFoundException
import java.util.Arrays

object AssetsHelper {
    @JvmStatic
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

    @JvmStatic
    fun setHymnsText(mContext: Context, sp: SharedPreferences, uri: Uri?, spKey: String?): Boolean {
        val fis: InputStream?
        val ois: ObjectInputStream
        val result: ArrayList<HashMap<String, String>>
        try {
            fis = mContext.contentResolver.openInputStream(uri!!)
            ois = ObjectInputStream(fis)
            result = ois.readObject() as ArrayList<HashMap<String, String>>
            sp.edit().putString(spKey, Gson().toJson(result)).apply()
            ois.close()
            fis!!.close()
        } catch (c: IOException) {
            c.printStackTrace()
            Toast.makeText(mContext, c.toString(), Toast.LENGTH_LONG).show()
            return false
        } catch (c: ClassNotFoundException) {
            c.printStackTrace()
            Toast.makeText(mContext, c.toString(), Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    @JvmStatic
    fun getHymnArrayList(
        mContext: Context,
        sp: SharedPreferences,
        gesangbuchSelected: Boolean
    ): ArrayList<HashMap<String, String>> {
        val fis: InputStream
        val ois: ObjectInputStream
        var result: ArrayList<HashMap<String, String>>? = null
        if (gesangbuchSelected && sp.getString("privateTextGesangbuch", null) != null) {
            return Gson().fromJson(
                sp.getString("privateTextGesangbuch", null),
                object : TypeToken<ArrayList<HashMap<String, String>>>() {}.type
            )
        } else if (!gesangbuchSelected && sp.getString("privateTextChorbuch", null) != null) {
            return Gson().fromJson(
                sp.getString("privateTextChorbuch", null),
                object : TypeToken<ArrayList<HashMap<String, String>>>() {}.type
            )
        }
        try {
            fis =
                if (gesangbuchSelected) mContext.assets.open("hymnsGesangbuchNoCopyright.txt") else mContext.assets.open(
                    "hymnsChorbuchNoCopyright.txt"
                )
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

    @JvmStatic
    fun getRubricListItemArrayList(gesangbuchSelected: Boolean): ArrayList<Int> {
        return if (gesangbuchSelected) { ArrayList(listOf(0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1))
        } else { ArrayList(listOf(0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1)) }
    }

    @JvmStatic
    fun getRubricTitlesArrayList(gesangbuchSelected: Boolean): ArrayList<String> {
        return if (gesangbuchSelected) { ArrayList(listOf("Das geistliche Jahr", "Advent", "Weihnachten", "Jahreswende", "Palmsonntag", "Karfreitag - Christi Leiden", "Ostern",
            "Christi Himmelfahrt", "Pfingsten", "Bußtag - Einsicht und Umkehr", "Gottesdienst", "Einladung - Heilsverlangen - Heiligung", "Glaube - Vertrauen - Trost",
            "Gottes Liebe - Nächstenliebe", "Vergebung - Gnade", "Lob - Dank - Anbetung", "Sakramente", "Heilige Taufe", "Heiliges Abendmahl",
            "Heilige Versiegelung", "Segenshandlungen", "Konfirmation", "Trauung", "Den Glauben leben", "Morgen und Abend", "Gemeinde - Gemeinschaft",
            "Sendung - Nachfolge - Bekenntnis", "Verheißung - Erwartung - Erfüllung", "Sterben - Ewiges Leben"))
        } else { ArrayList(listOf("Das geistliche Jahr", "Advent", "Weihnachten", "Jahreswechsel", "Palmsonntag", "Passion", "Ostern", "Himmelfahrt", "Pfingsten",
            "Erntedank", "Gottesdienst", "Einladung - Heilsverlangen - Heiligung", "Anbetung", "Glaube - Vertrauen", "Trost - Mut - Frieden",
            "Gnade - Vergebung", "Lobpreis Gottes", "Sakramente", "Heilige Taufe", "Heiliges Abendmahl", "Heilige Versiegelung",
            "Segenshandlungen", "Konfirmation", "Trauung", "Den Glauben leben", "Morgen - Abend", "Gottes Liebe - Nächstenliebe",
            "Mitarbeit - Gemeinschaft", "Sendung - Nachfolge - Bekenntnis", "Verheißung - Erwartung - Erfüllung", "Sterben - Ewiges Leben"))
        }
    }
}