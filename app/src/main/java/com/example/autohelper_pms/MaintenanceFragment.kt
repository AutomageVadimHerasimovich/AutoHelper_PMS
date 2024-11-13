package com.example.autohelper_pms

import MaintenanceRecord
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class MaintenanceFragment : Fragment() {

    private lateinit var maintenanceRecyclerView: RecyclerView
    private lateinit var maintenanceAdapter: MaintenanceAdapter
    private val maintenanceRecords = mutableListOf<MaintenanceRecord>()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private lateinit var oilChangeTextView: TextView
    private lateinit var worldMapWebView: WebView

    companion object {
        private const val ARG_CAR_NAME = "car_name"

        fun newInstance(carName: String): MaintenanceFragment {
            val fragment = MaintenanceFragment()
            val args = Bundle()
            args.putString(ARG_CAR_NAME, carName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maintenance, container, false)

        oilChangeTextView = view.findViewById(R.id.oilChangeTextView)
        worldMapWebView = view.findViewById(R.id.worldMapWebView)
        maintenanceRecyclerView = view.findViewById(R.id.maintenanceRecyclerView)
        maintenanceRecyclerView.layoutManager = LinearLayoutManager(context)

        // Load world map into WebView
        worldMapWebView.webViewClient = WebViewClient()
        worldMapWebView.settings.javaScriptEnabled = true
        worldMapWebView.loadUrl("https://www.google.com/maps")

        val headerView = inflater.inflate(R.layout.header_maintenance_record, container, false)
        val deleteAllButton: Button = headerView.findViewById(R.id.deleteAllButton)

        // Set layout parameters to match parent width
        headerView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        maintenanceAdapter = MaintenanceAdapter(maintenanceRecords, headerView, parentFragmentManager) { position ->
            maintenanceRecords.removeAt(position)
            sortRecordsByDate()
            maintenanceAdapter.notifyDataSetChanged()
            saveDataToFile(arguments?.getString(ARG_CAR_NAME) ?: "")
            updateOilChangeText()
        }
        maintenanceRecyclerView.adapter = maintenanceAdapter

        val dateEditText: EditText = view.findViewById(R.id.dateEditText)
        val mileageEditText: EditText = view.findViewById(R.id.mileageEditText)
        val partEditText: EditText = view.findViewById(R.id.partEditText)
        val serviceStationEditText: EditText = view.findViewById(R.id.serviceStationEditText)
        val addRecordButton: Button = view.findViewById(R.id.addRecordButton)

        // Set input filter for date field
        dateEditText.filters = arrayOf(DateInputFilter())

        val carName = arguments?.getString(ARG_CAR_NAME) ?: return view

        addRecordButton.setOnClickListener {
            val date = dateEditText.text.toString()
            val mileage = mileageEditText.text.toString().toIntOrNull() ?: 0
            val part = partEditText.text.toString()
            val serviceStation = serviceStationEditText.text.toString()

            // Validate date format
            if (!isValidDate(date)) {
                dateEditText.error = "Неправильный формат даты. Используйте дд.мм.гггг"
                return@setOnClickListener
            }

            Log.d("MaintenanceFragment", "Adding record: date=$date, mileage=$mileage, part=$part, serviceStation=$serviceStation")

            val newRecord = MaintenanceRecord(date, mileage, part, serviceStation)
            maintenanceRecords.add(newRecord)
            sortRecordsByDate()
            maintenanceAdapter.notifyDataSetChanged()

            saveDataToFile(carName)
            updateOilChangeText()
            dateEditText.text.clear()
            mileageEditText.text.clear()
            partEditText.text.clear()
            serviceStationEditText.text.clear()
        }

        deleteAllButton.setOnClickListener {
            DeleteAllConfirmationDialog {
                maintenanceRecords.clear()
                maintenanceAdapter.notifyDataSetChanged()
                saveDataToFile(carName)
                updateOilChangeText()
            }.show(parentFragmentManager, "DeleteAllConfirmationDialog")
        }

        loadDataFromFile(carName)
        updateOilChangeText()
        return view
    }

    private fun isValidDate(date: String): Boolean {
        return try {
            dateFormat.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun saveDataToFile(carName: String) {
        try {
            val file = File(context?.filesDir, "maintenance_data_$carName.txt")
            file.writeText(maintenanceRecords.joinToString("\n") { "${it.date},${it.mileage},${it.part},${it.serviceStation}" })
            Log.d("MaintenanceFragment", "Data saved to file: ${file.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadDataFromFile(carName: String) {
        try {
            val file = File(context?.filesDir, "maintenance_data_$carName.txt")
            if (file.exists()) {
                val loadedRecords = file.readLines().map { line ->
                    val parts = line.split(",")
                    Log.d("MaintenanceFragment", "Loaded record: date=${parts[0]}, mileage=${parts[1]}, part=${parts[2]}, serviceStation=${parts[3]}")
                    MaintenanceRecord(parts[0], parts[1].toInt(), parts[2], parts[3])
                }
                maintenanceRecords.clear()
                maintenanceRecords.addAll(loadedRecords)
                sortRecordsByDate()
                maintenanceAdapter.notifyDataSetChanged()
                Log.d("MaintenanceFragment", "Data loaded from file: ${file.absolutePath}")
            } else {
                maintenanceRecords.clear()
                maintenanceAdapter.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sortRecordsByDate() {
        maintenanceRecords.sortBy { dateFormat.parse(it.date) }
    }

    private fun updateOilChangeText() {
        if (maintenanceRecords.isNotEmpty()) {
            val lastMileage = maintenanceRecords.last().mileage
            val remainingMileage = 10000 - (lastMileage % 10000)
            oilChangeTextView.text = "Замена масла через: $remainingMileage км"
        } else {
            oilChangeTextView.text = "Замена масла через: N/A"
        }
    }

    class DateInputFilter : InputFilter {

        override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val input = dest.toString().substring(0, dstart) + source + dest.toString().substring(dend)
            return if (isValidPartialDate(input)) null else ""
        }

        private fun isValidPartialDate(date: String): Boolean {
            return date.matches(Regex("^\\d{0,2}(\\.\\d{0,2})?(\\.\\d{0,4})?$"))
        }
    }
}