/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <freeboub@gmail.com> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return Olivier Bouillet
 * ----------------------------------------------------------------------------
 */
package com.boub.ethicsearchwidget

import android.content.Context
import android.content.SharedPreferences

/**
 * here ae helpers to configure the application.
 */
internal object ESWPersistentConfiguration {
    /*
    ** Global helpers
     */
    private fun getSharedPreferencesNameForAppWidget(
        context: Context,
        appWidgetId: Int
    ): String {
        return context.packageName + "_preferences_" + appWidgetId
    }

    private fun getSharedPreferencesForAppWidget(
        context: Context,
        appWidgetId: Int
    ): SharedPreferences {
        return context.getSharedPreferences(
            getSharedPreferencesNameForAppWidget(
                context,
                appWidgetId
            ), 0
        )
    }

    fun remove(context: Context, appWidgetId: Int) {
        val pref =
            getSharedPreferencesForAppWidget(context, appWidgetId)
        val edit = pref.edit()
        edit.remove("voiceSearch")
        edit.remove("searchProvider")
        edit.apply()
    }

    /*
    ** voice search configuration
     */
    fun enableVoiceSearch(context: Context, appWidgetId: Int) {
        val pref =
            getSharedPreferencesForAppWidget(context, appWidgetId)
        val edit = pref.edit()
        edit.putBoolean("voiceSearch", true)
        edit.apply()
    }

    fun disableVoiceSearch(context: Context, appWidgetId: Int) {
        val pref =
            getSharedPreferencesForAppWidget(context, appWidgetId)
        val edit = pref.edit()
        edit.putBoolean("voiceSearch", false)
        edit.apply()
    }

    fun isVoiceSearchEnabled(context: Context, appWidgetId: Int): Boolean {
        val pref =
            getSharedPreferencesForAppWidget(context, appWidgetId)
        return pref.getBoolean("voiceSearch", false)
    }

    /*
    * Search provider configuration
     */
    fun setSearchProvider(
        context: Context,
        appWidgetId: Int,
        strSearchProvider: String?
    ) {
        val pref =
            getSharedPreferencesForAppWidget(context, appWidgetId)
        val edit = pref.edit()
        edit.putString("searchProvider", strSearchProvider)
        edit.apply()
    }

    fun getSearchProvider(context: Context, appWidgetId: Int): String {
        val pref =
            getSharedPreferencesForAppWidget(context, appWidgetId)
        return pref.getString("searchProvider", "Lilo").toString()
    }

    /*
    * isConfigured
    */
    @JvmStatic
    fun isWidgetConfigured(context: Context, appWidgetId: Int): Boolean {
        val pref =
            getSharedPreferencesForAppWidget(context, appWidgetId)
        return pref.getBoolean("isWidgetConfigured", false)
    }

    fun setWidgetConfigured(context: Context, appWidgetId: Int) {
        val pref =
            getSharedPreferencesForAppWidget(context, appWidgetId)
        val edit = pref.edit()
        edit.putBoolean("isWidgetConfigured", true)
        edit.apply()
    }
}