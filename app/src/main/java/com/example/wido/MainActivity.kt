package com.example.wido

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wido.databinding.ActivityMainBinding
import com.example.wido.databinding.DialogAddTodoBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var dao: TodoDao
    private lateinit var adapter: TodoAdapter

    private var currentFilter = "All"
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        db = AppDatabase.get(this)
        dao = db.todoDao()

        setupBottomNavigation()
        setupCustomization()
        setupFilters()
        setupSearch()

        adapter = createAdapter()
        b.todoRecyclerView.layoutManager = LinearLayoutManager(this)
        b.todoRecyclerView.adapter = adapter

        b.addTodoButton.setOnClickListener { showAddEditDialog(null) }

        observeTodos()
    }

    private fun createAdapter() = TodoAdapter(
        onToggle = { item ->
            lifecycleScope.launch {
                dao.setDone(item.id, !item.isDone)
                WidgetUpdater.updateAll(this@MainActivity)
            }
        },
        onEdit = { item -> showAddEditDialog(item) },
        onDelete = { item ->
            lifecycleScope.launch {
                dao.delete(item)
                WidgetUpdater.updateAll(this@MainActivity)
            }
        }
    )

    private fun observeTodos() {
        lifecycleScope.launch {
            dao.observeAll().collectLatest { list ->
                val filtered = list.filter { item ->
                    val matchesFilter = when (currentFilter) {
                        "Active" -> !item.isDone
                        "Done" -> item.isDone
                        "High Priority" -> item.priority > 0
                        else -> true
                    }
                    val matchesSearch = if (searchQuery.isEmpty()) {
                        true
                    } else {
                        item.title.contains(searchQuery, ignoreCase = true) ||
                        (item.tag?.contains(searchQuery, ignoreCase = true) == true)
                    }
                    matchesFilter && matchesSearch
                }
                adapter.submitList(filtered)
            }
        }
    }

    private fun setupFilters() {
        b.filterChipGroup.setOnCheckedChangeListener { _, checkedId ->
            currentFilter = when (checkedId) {
                R.id.filterActive -> "Active"
                R.id.filterDone -> "Done"
                R.id.filterHighPriority -> "High Priority"
                else -> "All"
            }
            observeTodos()
        }
    }

    private fun setupSearch() {
        b.mainSearchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString().trim()
                observeTodos()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupBottomNavigation() {
        b.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    b.homeContainer.visibility = View.VISIBLE
                    b.customContainer.visibility = View.GONE
                    b.addTodoButton.visibility = View.VISIBLE
                    true
                }
                R.id.nav_custom -> {
                    b.homeContainer.visibility = View.GONE
                    b.customContainer.visibility = View.VISIBLE
                    b.addTodoButton.visibility = View.GONE
                    true
                }
                else -> false
            }
        }
    }

    private fun setupCustomization() {
        val prefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

        // Title Edit
        b.editWidgetTitle.setText(prefs.getString("widget_title", "WiDo"))
        b.editWidgetTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                prefs.edit().putString("widget_title", s.toString()).apply()
                WidgetUpdater.updateAll(this@MainActivity)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Font Size SeekBar
        val savedSize = prefs.getInt("widget_font_size", 14)
        b.fontSizeSeekBar.progress = (savedSize - 10).coerceIn(0, 14)
        b.fontSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    prefs.edit().putInt("widget_font_size", progress + 10).apply()
                    WidgetUpdater.updateAll(this@MainActivity)
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Style RadioGroup
        val savedStyle = prefs.getString("widget_style", "classic")
        when (savedStyle) {
            "classic" -> b.styleClassic.isChecked = true
            "emoji" -> b.styleEmoji.isChecked = true
            "check" -> b.styleCheck.isChecked = true
            "crazy" -> b.styleCrazy.isChecked = true
            "stars" -> b.styleStars.isChecked = true
        }
        b.styleGroup.setOnCheckedChangeListener { _, checkedId ->
            val style = when (checkedId) {
                R.id.styleEmoji -> "emoji"
                R.id.styleCheck -> "check"
                R.id.styleCrazy -> "crazy"
                R.id.styleStars -> "stars"
                else -> "classic"
            }
            prefs.edit().putString("widget_style", style).apply()
            WidgetUpdater.updateAll(this@MainActivity)
        }
    }

    private fun setupStyleGroup() {
        // Obsolete, replaced by setupCustomization
    }

    private fun showAddEditDialog(item: TodoItem?) {
        val dbinding = DialogAddTodoBinding.inflate(layoutInflater)
        val isEdit = item != null
        
        var selectedDate: Long? = item?.dueDate
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        if (isEdit) {
            dbinding.editTitle.setText(item?.title)
            dbinding.editTag.setText(item?.tag)
            dbinding.checkHighPriority.isChecked = item?.priority == 1
            item?.dueDate?.let { dbinding.btnDueDate.text = sdf.format(Date(it)) }
        }

        dbinding.btnDueDate.setOnClickListener {
            val cal = Calendar.getInstance()
            selectedDate?.let { cal.timeInMillis = it }
            DatePickerDialog(this, { _, year, month, day ->
                val newCal = Calendar.getInstance()
                newCal.set(year, month, day)
                selectedDate = newCal.timeInMillis
                dbinding.btnDueDate.text = sdf.format(newCal.time)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        AlertDialog.Builder(this)
            .setTitle(if (isEdit) "Edit Task" else "New Task")
            .setView(dbinding.root)
            .setPositiveButton(if (isEdit) "Save" else "Add") { _, _ ->
                val title = dbinding.editTitle.text.toString().trim()
                val tag = dbinding.editTag.text.toString().trim().removePrefix("#")
                val priority = if (dbinding.checkHighPriority.isChecked) 1 else 0
                
                if (title.isNotEmpty()) {
                    lifecycleScope.launch {
                        if (isEdit) {
                            dao.update(item!!.copy(title = title, tag = tag, priority = priority, dueDate = selectedDate, updatedAt = System.currentTimeMillis()))
                        } else {
                            dao.insert(TodoItem(title = title, tag = tag, priority = priority, dueDate = selectedDate))
                        }
                        WidgetUpdater.updateAll(this@MainActivity)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
