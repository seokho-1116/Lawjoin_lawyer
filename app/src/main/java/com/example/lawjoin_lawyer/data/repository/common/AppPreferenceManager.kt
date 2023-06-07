package com.example.lawjoin.common

import android.content.Context
import android.preference.PreferenceManager

object AppPreferenceManager {
    private const val PREF_FIRST_LAUNCH = "pref_first_launch"

    fun isNotFirstLaunch(context: Context): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return !sharedPreferences.getBoolean(PREF_FIRST_LAUNCH, true)
    }

    fun setFirstLaunch(context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putBoolean(PREF_FIRST_LAUNCH, true)
        editor.apply()
    }
}
