package com.example.wido

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlinx.coroutines.runBlocking

class TodoWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_todo)

            // List service
            val svcIntent = Intent(context, TodoWidgetService::class.java)
            views.setRemoteAdapter(R.id.widgetList, svcIntent)
            views.setEmptyView(R.id.widgetList, R.id.widgetEmpty)

            // Klik na "+" → otwórz app
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPI = PendingIntent.getActivity(
                context, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetAdd, openAppPI)

            // Template PendingIntent dla klików w elementy listy (fillIn)
            val toggleIntent = Intent(context, TodoWidget::class.java).apply {
                action = ACTION_TOGGLE
            }
            val togglePI = PendingIntent.getBroadcast(
                context, id, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setPendingIntentTemplate(R.id.widgetList, togglePI)

            appWidgetManager.updateAppWidget(id, views)
        }

        // odśwież listę
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widgetList)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_TOGGLE) {
            val todoId = intent.getLongExtra(EXTRA_TODO_ID, -1L)
            if (todoId != -1L) {
                val dao = AppDatabase.get(context).todoDao()
                runBlocking {
                    val all = dao.getAll()
                    val item = all.firstOrNull { it.id == todoId } ?: return@runBlocking
                    dao.setDone(todoId, !item.isDone)
                }
                WidgetUpdater.updateAll(context)
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE = "com.example.wido.ACTION_TOGGLE"
        const val EXTRA_TODO_ID = "extra_todo_id"
    }
}
