package com.example.autohelper_pms

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class NotesAdapter(
    private val notes: MutableList<Pair<String, String>>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_list_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount(): Int = notes.size

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        val fromNote = notes.removeAt(fromPosition)
        notes.add(toPosition, fromNote)
        notifyItemMoved(fromPosition, toPosition)
    }

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val noteText: TextView = itemView.findViewById(R.id.note_item_text)
        private val dateTextView: TextView = itemView.findViewById(R.id.note_date)
        private val notificationCheckbox: CheckBox = itemView.findViewById(R.id.notification_checkbox)

        fun bind(note: Pair<String, String>) {
            noteText.text = note.first
            dateTextView.text = note.second

            itemView.setOnClickListener {
                onItemClick(adapterPosition)
            }

            dateTextView.setOnClickListener {
                val calendar = Calendar.getInstance()
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                calendar.time = sdf.parse(note.second) ?: Date()
                DatePickerDialog(itemView.context, { _, year, month, dayOfMonth ->
                    val newDate = Calendar.getInstance()
                    newDate.set(year, month, dayOfMonth)
                    val dateString = sdf.format(newDate.time)
                    notes[adapterPosition] = note.first to dateString
                    dateTextView.text = dateString
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            }

            notificationCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    Toast.makeText(itemView.context, "Уведомление установлено на ${note.second}", Toast.LENGTH_SHORT).show()
                    setNotification(itemView.context, note.second)
                }
            }
        }

        private fun setNotification(context: Context, dateStr: String) {
    val notificationHelper = NotificationHelper(context)
    notificationHelper.setNotification(dateStr)
}
    }
}