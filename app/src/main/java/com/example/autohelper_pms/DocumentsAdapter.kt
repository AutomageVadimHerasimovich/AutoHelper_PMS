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

class DocumentsAdapter(
    private val documents: MutableList<Pair<String, String>>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<DocumentsAdapter.DocumentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.document_list_item, parent, false)
        return DocumentViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        holder.bind(documents[position])
    }

    override fun getItemCount(): Int = documents.size

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        val fromDocument = documents.removeAt(fromPosition)
        documents.add(toPosition, fromDocument)
        notifyItemMoved(fromPosition, toPosition)
    }

    inner class DocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val documentText: TextView = itemView.findViewById(R.id.document_item_text)
        private val dateTextView: TextView = itemView.findViewById(R.id.document_date)
        private val notificationCheckbox: CheckBox = itemView.findViewById(R.id.notification_checkbox)

        fun bind(document: Pair<String, String>) {
            documentText.text = document.first
            dateTextView.text = document.second

            itemView.setOnClickListener {
                onItemClick(adapterPosition)
            }

            dateTextView.setOnClickListener {
                val calendar = Calendar.getInstance()
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                calendar.time = sdf.parse(document.second) ?: Date()
                DatePickerDialog(itemView.context, { _, year, month, dayOfMonth ->
                    val newDate = Calendar.getInstance()
                    newDate.set(year, month, dayOfMonth)
                    val dateString = sdf.format(newDate.time)
                    documents[adapterPosition] = document.first to dateString
                    dateTextView.text = dateString
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            }

            notificationCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    Toast.makeText(itemView.context, "Уведомление установлено на ${document.second}", Toast.LENGTH_SHORT).show()
                    setNotification(itemView.context, document.second)
                }
            }
        }

        private fun setNotification(context: Context, dateStr: String) {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: return
            val calendar = Calendar.getInstance()
            calendar.time = date

            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }
}