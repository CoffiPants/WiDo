package com.example.wido

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val todos = mutableListOf<TodoItem>()
    private lateinit var adapter: TodoAdapter
    private var counter = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.todoRecyclerView)
        val addButton = findViewById<Button>(R.id.addTodoButton)

        adapter = TodoAdapter(todos)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        addButton.setOnClickListener {
            todos.add(
                TodoItem(
                    id = counter.toLong(),
                    title = "Todo $counter"
                )
            )
            counter++
            adapter.notifyItemInserted(todos.size - 1)
        }
    }
}
