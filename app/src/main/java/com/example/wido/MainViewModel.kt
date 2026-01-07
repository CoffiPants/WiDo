package com.example.wido

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.get(application)
    private val dao = db.todoDao()

    val todos: Flow<List<TodoItem>> = dao.observeAll()

    fun addTodo(title: String) {
        viewModelScope.launch {
            dao.insert(TodoItem(title = title))
            updateWidget()
        }
    }

    fun toggleTodo(item: TodoItem) {
        viewModelScope.launch {
            dao.setDone(item.id, !item.isDone)
            updateWidget()
        }
    }

    fun updateTitle(item: TodoItem, newTitle: String) {
        viewModelScope.launch {
            dao.updateTitle(item.id, newTitle)
            updateWidget()
        }
    }

    fun deleteTodo(item: TodoItem) {
        viewModelScope.launch {
            dao.delete(item)
            updateWidget()
        }
    }

    private fun updateWidget() {
        WidgetUpdater.updateAll(getApplication())
    }
}
