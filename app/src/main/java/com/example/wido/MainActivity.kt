package com.example.wido

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wido.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var dao: TodoDao
    private lateinit var adapter: TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        db = AppDatabase.get(this)
        dao = db.todoDao()

        adapter = TodoAdapter(
            onToggle = { item ->
                lifecycleScope.launch {
                    dao.setDone(item.id, !item.isDone)
                    WidgetUpdater.updateAll(this@MainActivity)
                }
            },
            onEdit = { item ->
                showEditDialog(item)
            },
            onDelete = { item ->
                lifecycleScope.launch {
                    dao.delete(item)
                    WidgetUpdater.updateAll(this@MainActivity)
                }
            }
        )

        b.todoRecyclerView.layoutManager = LinearLayoutManager(this)
        b.todoRecyclerView.adapter = adapter

        b.addTodoButton.setOnClickListener { showAddDialog() }

        lifecycleScope.launch {
            dao.observeAll().collectLatest { list ->
                adapter.submitList(list)
            }
        }
    }

    private fun showAddDialog() {
        val input = EditText(this)
        input.hint = "Todo title"

        AlertDialog.Builder(this)
            .setTitle("Add ToDo")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val title = input.text.toString().trim()
                if (title.isNotEmpty()) {
                    lifecycleScope.launch {
                        dao.insert(TodoItem(title = title))
                        WidgetUpdater.updateAll(this@MainActivity)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDialog(item: TodoItem) {
        val input = EditText(this)
        input.setText(item.title)

        AlertDialog.Builder(this)
            .setTitle("Edit ToDo")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val title = input.text.toString().trim()
                if (title.isNotEmpty()) {
                    lifecycleScope.launch {
                        dao.updateTitle(item.id, title)
                        WidgetUpdater.updateAll(this@MainActivity)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
