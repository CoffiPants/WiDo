package com.example.wido

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wido.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        adapter = TodoAdapter(
            onToggle = { item ->
                viewModel.toggleTodo(item)
            },
            onEdit = { item ->
                showEditDialog(item)
            },
            onDelete = { item ->
                viewModel.deleteTodo(item)
            }
        )

        b.todoRecyclerView.layoutManager = LinearLayoutManager(this)
        b.todoRecyclerView.adapter = adapter

        b.addTodoButton.setOnClickListener { showAddDialog() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.todos.collectLatest { list ->
                    adapter.submitList(list)
                }
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
                    viewModel.addTodo(title)
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
                    viewModel.updateTitle(item, title)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
