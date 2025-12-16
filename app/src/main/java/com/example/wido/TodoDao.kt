package com.example.wido

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Query("SELECT * FROM todos ORDER BY isDone ASC, updatedAt DESC")
    fun observeAll(): Flow<List<TodoItem>>

    @Query("SELECT * FROM todos ORDER BY isDone ASC, updatedAt DESC")
    suspend fun getAll(): List<TodoItem>

    @Insert
    suspend fun insert(item: TodoItem): Long

    @Update
    suspend fun update(item: TodoItem)

    @Delete
    suspend fun delete(item: TodoItem)

    @Query("UPDATE todos SET isDone = :isDone, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setDone(id: Long, isDone: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE todos SET title = :title, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTitle(id: Long, title: String, updatedAt: Long = System.currentTimeMillis())
}
