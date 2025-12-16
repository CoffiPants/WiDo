package com.example.wido

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TodoAdapter(
    private val todos: MutableList<TodoItem>
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.todoItemText)

        init {
            // klik → toggle isDone
            itemView.setOnClickListener {
                val todo = todos[bindingAdapterPosition]
                todos[bindingAdapterPosition] =
                    todo.copy(isDone = !todo.isDone)
                notifyItemChanged(bindingAdapterPosition)
            }

            // długi klik → usuń
            itemView.setOnLongClickListener {
                todos.removeAt(bindingAdapterPosition)
                notifyItemRemoved(bindingAdapterPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = todos[position]
        holder.text.text = todo.title

        if (todo.isDone) {
            holder.text.paintFlags =
                holder.text.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.text.alpha = 0.5f
        } else {
            holder.text.paintFlags =
                holder.text.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.text.alpha = 1f
        }
    }

    override fun getItemCount(): Int = todos.size
}
