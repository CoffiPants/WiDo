package com.example.wido

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object WidgetUpdater {
    fun updateAll(context: Context) {
        val appContext = context.applicationContext
        val mgr = AppWidgetManager.getInstance(appContext)
        val cn = ComponentName(appContext, TodoWidget::class.java)
        val ids = mgr.getAppWidgetIds(cn)
        
        if (ids.isNotEmpty()) {
            // 1. Notify the list data changed
            mgr.notifyAppWidgetViewDataChanged(ids, R.id.widgetList)
            
            // 2. Send broadcast to trigger onUpdate in TodoWidget
            val updateIntent = Intent(appContext, TodoWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            appContext.sendBroadcast(updateIntent)
        }
    }
}
