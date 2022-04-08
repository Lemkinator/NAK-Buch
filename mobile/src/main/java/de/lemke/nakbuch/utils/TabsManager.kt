package de.lemke.nakbuch.utils

import android.content.Context
import android.content.SharedPreferences

class TabsManager(context: Context, spName: String?) {
    companion object {
        private const val KEY = "current_tab"
    }
    private val sp: SharedPreferences = context.getSharedPreferences(spName, Context.MODE_PRIVATE)
    var currentTab = 0
    private var previousTab = currentTab

    fun initTabPosition() {
        setTabPosition(tabFromSharedPreference)
    }

    fun setTabPosition(position: Int) {
        setTabPositionToSharedPreference(position)
        previousTab = currentTab
        currentTab = position
    }

    private val tabFromSharedPreference: Int get() = sp.getInt(KEY, 0)

    private fun setTabPositionToSharedPreference(position: Int) {
        sp.edit().putInt(KEY, position).apply()
    }
}