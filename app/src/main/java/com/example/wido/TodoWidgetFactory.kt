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
        // wywoływane przy notifyAppWidgetViewDataChanged
        items = runCatching { kotlinx.coroutines.runBlocking { dao.getAll() } }.getOrDefault(emptyList())
    }

    override fun getCount(): Int = items.size
    override fun getViewAt(position: Int): RemoteViews {
        val item = items[position]
        val rv = RemoteViews(context.packageName, R.layout.widget_item_todo)
        rv.setTextViewText(R.id.wItemTitle, item.title)
        rv.setTextViewText(R.id.wItemStatus, if (item.isDone) "✓" else "○")

        // klik w wiersz (albo status) → toggle
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
