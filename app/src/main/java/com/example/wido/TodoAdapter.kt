package com.example.wido

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wido.databinding.ItemTodoBinding
import java.text.SimpleDateFormat
import java.util.*

class TodoAdapter(
    private val onToggle: (TodoItem) -> Unit,
    private val onEdit: (TodoItem) -> Unit,
    private val onDelete: (TodoItem) -> Unit
) : ListAdapter<TodoItem, TodoAdapter.VH>(Diff) {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    object Diff : DiffUtil.ItemCallback<TodoItem>() {
        override fun areItemsTheSame(oldItem: TodoItem, newItem: TodoItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TodoItem, newItem: TodoItem) = oldItem == newItem
    }

    inner class VH(val b: ItemTodoBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.b.todoTitle.text = item.title
        
        if (item.isDone) {
            holder.b.todoTitle.paintFlags = holder.b.todoTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.b.todoStatus.text = "✓"
            holder.b.todoTitle.alpha = 0.5f
        } else {
            holder.b.todoTitle.paintFlags = holder.b.todoTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.b.todoStatus.text = "○"
            holder.b.todoTitle.alpha = 1.0f
        }

        // Tag
        if (!item.tag.isNullOrEmpty()) {
            holder.b.todoTag.visibility = View.VISIBLE
            holder.b.todoTag.text = "#${item.tag}"
        } else {
            holder.b.todoTag.visibility = View.GONE
        }

        // Due Date
        if (item.dueDate != null) {
            holder.b.todoDueDate.visibility = View.VISIBLE
            holder.b.todoDueDate.text = dateFormat.format(Date(item.dueDate))
            
            // Highlight overdue
            if (!item.isDone && item.dueDate < System.currentTimeMillis()) {
                holder.b.todoDueDate.setTextColor(Color.RED)
            } else {
                holder.b.todoDueDate.setTextColor(Color.GRAY)
            }
        } else {
            holder.b.todoDueDate.visibility = View.GONE
        }

        // Priority
        holder.b.priorityIndicator.visibility = if (item.priority > 0) View.VISIBLE else View.GONE

        holder.b.todoStatus.setOnClickListener { onToggle(item) }
        holder.b.todoTitle.setOnClickListener { onEdit(item) }

        holder.b.root.setOnLongClickListener {
            onDelete(item)
            true
        }
    }
}
