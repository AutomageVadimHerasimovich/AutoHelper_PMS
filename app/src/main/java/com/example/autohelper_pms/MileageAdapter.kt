package com.example.autohelper_pms

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class MileageAdapter(
    private val records: MutableList<MileageRecord>,
    private val fragmentManager: FragmentManager,
    private val onDelete: (Int) -> Unit,
    private val onDeleteAll: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deleteAllButton: Button = view.findViewById(R.id.deleteAllButton)
    }

    class MileageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val changeTextView: TextView = itemView.findViewById(R.id.changeTextView)
        val mileageTextView: TextView = itemView.findViewById(R.id.mileageTextView)
        val reasonTextView: TextView = itemView.findViewById(R.id.reasonTextView)
        val placeTextView: TextView = itemView.findViewById(R.id.placeTextView)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mileage_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mileage_record, parent, false)
            MileageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MileageViewHolder) {
            val record = records[position - 1]
            holder.dateTextView.text = record.date
            holder.changeTextView.text = record.change
            holder.mileageTextView.text = record.mileage
            holder.reasonTextView.text = record.reason
            holder.placeTextView.text = record.place

            holder.deleteButton.setOnClickListener {
                DeleteConfirmationDialog {
                    onDelete(position - 1)
                }.show(fragmentManager, "DeleteConfirmationDialog")
            }
        } else if (holder is HeaderViewHolder) {
            holder.deleteAllButton.setOnClickListener {
                DeleteAllConfirmationDialog {
                    onDeleteAll()
                }.show(fragmentManager, "DeleteAllConfirmationDialog")
            }
        }
    }

    override fun getItemCount() = records.size + 1

    fun addRecord(record: MileageRecord) {
        records.add(record)
        sortRecordsByDate()
        notifyDataSetChanged()
    }

    private fun sortRecordsByDate() {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        records.sortBy { dateFormat.parse(it.date) }
    }

    fun removeRecord(position: Int) {
        if (position < records.size) {
            records.removeAt(position)
            notifyItemRemoved(position + 1)
        }
    }
}