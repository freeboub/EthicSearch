/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <freeboub@gmail.com> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return Olivier Bouillet
 * ----------------------------------------------------------------------------
 */

package com.boub.ethicsearchwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews

class ESWAppWidgetProvider : AppWidgetProvider() {

    private fun getPendingSelfIntent(
        context: Context,
        action: String
    ): PendingIntent {
        // An explicit intent directed at the current class (the "self").
        val intent = Intent(context, javaClass)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    private fun setupWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        sInstance = this
        sAppWidgetManager = appWidgetManager
        val remoteViews = RemoteViews(context.packageName, R.layout.esw_layout_standard_search_bar)
        if ( ESWPersistentConfiguration.isWidgetConfigured(context, widgetId)) {
            val url = ESWPersistentConfiguration.getSearchProvider(context, widgetId)
            val prov = ESWSearchConfiguration.instance().getProvider(url)
            val intent: PendingIntent
            val bitmap: Bitmap

            // hide default text
            remoteViews.setViewVisibility(R.id.search_text_default, View.GONE)
            remoteViews.setViewVisibility(R.id.engine_button, View.VISIBLE)

            if (ESWPersistentConfiguration.isVoiceSearchEnabled(context, widgetId)) {
                intent = getPendingSelfIntent(context, ACTION_BUTTON_VOICE_SEARCH + widgetId)
                bitmap = prov.getLogoVoiceBitmap(context)
            } else {
                intent = getPendingSelfIntent(context, ACTION_TEXT_CLICK + widgetId)
                bitmap = prov.getLogoBitmap(context)
            }
            remoteViews.setImageViewBitmap(R.id.engine_button, bitmap)

            // Register an onClickListener
            remoteViews.setOnClickPendingIntent(R.id.engine_button, intent)
            remoteViews.setOnClickPendingIntent(R.id.widget_background, intent)
        } else {
            // Register an onClickListener
            val intent =  getPendingSelfIntent(context, ACTION_CONFIGURE_WIDGET + widgetId)
            remoteViews.setOnClickPendingIntent(R.id.engine_button, intent)
            remoteViews.setOnClickPendingIntent(R.id.widget_background, intent)
        }
        appWidgetManager.updateAppWidget(widgetId, remoteViews)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Get all ids
        val thisWidget = ComponentName( context, ESWAppWidgetProvider::class.java )
        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
        for (widgetId in allWidgetIds) {
            setupWidget(context, appWidgetManager, widgetId)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        setupWidget(context, appWidgetManager, appWidgetId)
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    override fun onDeleted(
        context: Context,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            ESWPersistentConfiguration.remove(context, widgetId)
        }
        super.onDeleted(context, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val strAction = intent.action
        if (strAction != null) {
            when {
                strAction.startsWith(ACTION_BUTTON_CLICK) -> {
                    val activityIntent = Intent(Intent.ACTION_VIEW)
                    val widgetId = intent.action!!.replace(ACTION_BUTTON_CLICK, "").toInt()
                    val providerId = ESWPersistentConfiguration.getSearchProvider(context, widgetId)
                    activityIntent.data = Uri.parse( ESWSearchConfiguration.instance().getProvider(providerId).webSite )
                    activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(activityIntent)
                }
                strAction.startsWith(ACTION_TEXT_CLICK) -> {
                    val activityIntent = Intent(context, ESWSearchActivity::class.java)
                    activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val widgetId = intent.action!!.replace(ACTION_TEXT_CLICK, "").toInt()
                    activityIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    context.startActivity(activityIntent)
                }
                strAction.startsWith(ACTION_BUTTON_VOICE_SEARCH) -> {
                    val activityIntent = Intent(context, ESWSearchActivity::class.java)
                    activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val widgetId =
                        intent.action!!.replace(ACTION_BUTTON_VOICE_SEARCH, "").toInt()
                    activityIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    activityIntent.action = ESWSearchActivity.INTENT_ACTION_VOICE_SEARCH + widgetId
                    context.startActivity(activityIntent)
                }
                strAction.startsWith(ACTION_CONFIGURE_WIDGET) -> {
                    val activityIntent = Intent(context, ESWAppWidgetConfigure::class.java)
                    activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val widgetId = intent.action!!.replace(ACTION_CONFIGURE_WIDGET, "").toInt()
                    activityIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    context.startActivity(activityIntent)
                }
            }
        }
    }

    companion object {
        private const val ACTION_BUTTON_CLICK = "com.boub.lilo.ACTION_BUTTON_CLICK"
        private const val ACTION_TEXT_CLICK = "com.boub.lilo.ACTION_TEXT_CLICK"
        private const val ACTION_BUTTON_VOICE_SEARCH = "com.boub.lilo.ACTION_VOICE_SEARCH_CLICK"
        private const val ACTION_CONFIGURE_WIDGET = "com.boub.lilo.ACTION_CONFIGURE_WIDGET"

        var sAppWidgetManager: AppWidgetManager? = null
        var sInstance: ESWAppWidgetProvider? = null
        fun forceUpdate(context: Context, widgetId: Int) {
            if (sInstance != null) {
                sInstance!!.onUpdate( context, sAppWidgetManager!!, intArrayOf(widgetId) )
            }
        }
    }
}