package com.example.autohelper_pms

import MaintenanceRecord
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView

class MaintenanceAdapter(
    private val maintenanceRecords: List<MaintenanceRecord>,
    private val headerView: View?,
    private val fragmentManager: FragmentManager,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && headerView != null) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(headerView!!)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_maintenance_record, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val record = maintenanceRecords[position - if (headerView != null) 1 else 0]
            holder.bind(record, fragmentManager, onDeleteClick)
        }
    }

    override fun getItemCount(): Int {
        return maintenanceRecords.size + if (headerView != null) 1 else 0
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        private val mileageTextView: TextView = view.findViewById(R.id.mileageTextView)
        private val partTextView: TextView = view.findViewById(R.id.partTextView)
        private val serviceStationTextView: TextView = view.findViewById(R.id.serviceStationTextView)
        private val deleteButton: Button = view.findViewById(R.id.deleteButton)

        fun bind(record: MaintenanceRecord, fragmentManager: FragmentManager, onDeleteClick: (Int) -> Unit) {
            dateTextView.text = record.date
            mileageTextView.text = record.mileage.toString()
            partTextView.text = record.part
            serviceStationTextView.text = record.serviceStation

            deleteButton.setOnClickListener {
                DeleteConfirmationDialog {
                    onDeleteClick(adapterPosition - 1)
                }.show(fragmentManager, "DeleteConfirmationDialog")
            }
        }
    }
}