package com.example.wido

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val todos = mutableListOf<TodoItem>()
    private var counter = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val addButton = findViewById<Button>(R.id.addTodoButton)
        val todoText = findViewById<TextView>(R.id.todoListText)

        addButton.setOnClickListener {
            val todo = TodoItem(
                id = counter.toLong(),
                title = "Todo $counter"
            )
            counter++
            todos.add(todo)

            todoText.text = todos.joinToString("\n") { it.title }
        }
    }
}
