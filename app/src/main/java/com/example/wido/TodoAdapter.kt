package com.example.wido

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wido.databinding.ItemTodoBinding

class TodoAdapter(
    private val onToggle: (TodoItem) -> Unit,
    private val onEdit: (TodoItem) -> Unit,
    private val onDelete: (TodoItem) -> Unit
) : ListAdapter<TodoItem, TodoAdapter.VH>(Diff) {

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

        // Remove listener to avoid triggering it while setting state
        holder.b.todoStatus.setOnCheckedChangeListener(null)
        holder.b.todoStatus.isChecked = item.isDone

        // Use OnClickListener for CheckBox to handle user interaction primarily
        holder.b.todoStatus.setOnClickListener {
            onToggle(item)
        }

        holder.b.todoTitle.setOnClickListener { onEdit(item) }

        holder.b.deleteButton.setOnClickListener {
            onDelete(item)
        }

        // Long click logic removed as we now have a dedicated delete button
        holder.b.root.setOnLongClickListener(null)
    }
}
