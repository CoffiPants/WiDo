package com.example.wido

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val isDone: Boolean = false,
    val dueDate: Long? = null,
    val tag: String? = null,
    val priority: Int = 0, // 0: Normal, 1: High
    val updatedAt: Long = System.currentTimeMillis()
)
