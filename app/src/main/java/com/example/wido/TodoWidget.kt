package com.example.wido

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import kotlinx.coroutines.runBlocking

class TodoWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        
        val title = prefs.getString("widget_title", "WiDo") ?: "WiDo"
        val fontSize = prefs.getInt("widget_font_size", 14).toFloat()
        val bgColor = ContextCompat.getColor(context, R.color.surface)

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_todo)
            
            // Apply customization
            views.setInt(R.id.widgetContainer, "setBackgroundColor", bgColor)
            views.setTextViewText(R.id.widgetTitle, title)
            views.setTextViewTextSize(R.id.widgetTitle, TypedValue.COMPLEX_UNIT_SP, fontSize + 4)

            // List service
            val svcIntent = Intent(context, TodoWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                data = android.net.Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widgetList, svcIntent)
            views.setEmptyView(R.id.widgetList, R.id.widgetEmpty)

            // "+" button
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPI = PendingIntent.getActivity(
                context, 
                id,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetAdd, openAppPI)

            // Template PendingIntent for list items
            val toggleIntent = Intent(context, TodoWidget::class.java).apply {
                action = ACTION_TOGGLE
                data = android.net.Uri.parse("wido://widget/$id")
            }
            
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val togglePI = PendingIntent.getBroadcast(
                context, 
                0,
                toggleIntent,
                flags
            )
            views.setPendingIntentTemplate(R.id.widgetList, togglePI)

            appWidgetManager.updateAppWidget(id, views)
        }
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
