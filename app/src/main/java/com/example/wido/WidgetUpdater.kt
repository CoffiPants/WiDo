package com.example.wido

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

object WidgetUpdater {
    fun updateAll(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        val ids = mgr.getAppWidgetIds(ComponentName(context, TodoWidget::class.java))
        if (ids.isNotEmpty()) {
            mgr.notifyAppWidgetViewDataChanged(ids, R.id.widgetList)
            // wymuś onUpdate
            val provider = TodoWidget()
            provider.onUpdate(context, mgr, ids)
        }
    }
}
