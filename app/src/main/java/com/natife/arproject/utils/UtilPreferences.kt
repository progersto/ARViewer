package com.natife.arproject.utils

import android.content.Context
import android.preference.PreferenceManager

private var FIRST_INIT = "first_init"
private var FIRST_INIT_AR = "first_init_ar"


fun fistInit(context: Context, value: Boolean) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = prefs.edit()
    editor.putBoolean(FIRST_INIT, value)
    editor.apply()
}

fun isInit(context: Context): Boolean {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    return prefs.getBoolean(FIRST_INIT, false)
}

fun fistInitAR(context: Context, value: Boolean) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = prefs.edit()
    editor.putBoolean(FIRST_INIT_AR, value)
    editor.apply()
}

fun isFistInitAR(context: Context): Boolean {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    return prefs.getBoolean(FIRST_INIT_AR, false)
}