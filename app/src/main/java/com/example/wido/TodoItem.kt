package com.example.wido

data class TodoItem(
    val id: Long,
    val title: String,
    val isDone: Boolean = false
)
