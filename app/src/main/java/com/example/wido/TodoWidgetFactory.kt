package com.example.wido

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService

class TodoWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private val dao = AppDatabase.get(context).todoDao()
    private var items: List<TodoItem> = emptyList()

    override fun onCreate() {}
    override fun onDestroy() {}

    override fun onDataSetChanged() {
        items = runCatching { kotlinx.coroutines.runBlocking { dao.getAll() } }.getOrDefault(emptyList())
    }

    override fun getCount(): Int = items.size
    override fun getViewAt(position: Int): RemoteViews {
        val item = items[position]
        val rv = RemoteViews(context.packageName, R.layout.widget_item_todo)
        rv.setTextViewText(R.id.wItemTitle, item.title)

        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val style = prefs.getString("widget_style", "classic")

        val statusText = if (item.isDone) {
            when (style) {
                "emoji" -> "✅"
                "check" -> "[x]"
                "crazy" -> "🔥"
                "stars" -> "⭐"
                else -> "●"
            }
        } else {
            when (style) {
                "emoji" -> "📝"
                "check" -> "[ ]"
                "crazy" -> "🧊"
                "stars" -> "☆"
                else -> "○"
            }
        }
        
        rv.setTextViewText(R.id.wItemStatus, statusText)

        val fillIn = Intent().apply {
            putExtra(TodoWidget.EXTRA_TODO_ID, item.id)
        }
        rv.setOnClickFillInIntent(R.id.wItemStatus, fillIn)
        rv.setOnClickFillInIntent(R.id.wItemTitle, fillIn)

        return rv
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = items.getOrNull(position)?.id ?: position.toLong()
    override fun hasStableIds(): Boolean = true
}
