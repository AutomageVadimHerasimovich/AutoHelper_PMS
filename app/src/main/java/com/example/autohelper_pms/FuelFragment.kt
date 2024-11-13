package com.example.autohelper_pms

import FuelRecord
import FuelRecordAdapter
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class FuelFragment : Fragment() {

    private lateinit var fuelTableRecyclerView: RecyclerView
    private lateinit var fuelRecordAdapter: FuelRecordAdapter
    private val fuelRecords = mutableListOf<FuelRecord>()

    private lateinit var dateEditText: EditText
    private lateinit var mileageEditText: EditText
    private lateinit var litersEditText: EditText
    private lateinit var addRecordButton: Button
    private lateinit var deleteAllButton: Button
    private lateinit var averageConsumptionTextView: TextView
    private lateinit var fuelConsumptionChart: LineChart

    companion object {
        private const val ARG_CAR_NAME = "car_name"
        private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        private val decimalFormat = DecimalFormat("#.##")

        fun newInstance(carName: String): FuelFragment {
            val fragment = FuelFragment()
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
        val view = inflater.inflate(R.layout.fragment_fuel, container, false)

        fuelConsumptionChart = view.findViewById(R.id.fuelConsumptionChart)
        setupChart()

        fuelTableRecyclerView = view.findViewById(R.id.fuelTableRecyclerView)
        fuelTableRecyclerView.layoutManager = LinearLayoutManager(context)
        fuelRecordAdapter = FuelRecordAdapter(fuelRecords, parentFragmentManager) { position ->
            fuelRecords.removeAt(position)
            sortFuelRecords()
            fuelRecordAdapter.notifyDataSetChanged()
            saveDataToFile(arguments?.getString(ARG_CAR_NAME) ?: "")
            updateAverageConsumption()
            updateChart()
        }
        fuelTableRecyclerView.adapter = fuelRecordAdapter
        dateEditText = view.findViewById(R.id.dateEditText)
        mileageEditText = view.findViewById(R.id.mileageEditText)
        litersEditText = view.findViewById(R.id.litersEditText)
        addRecordButton = view.findViewById(R.id.addRecordButton)
        deleteAllButton = view.findViewById(R.id.deleteAllButton)
        averageConsumptionTextView = view.findViewById(R.id.averageConsumptionTextView)

        // Set input filter for date field
        dateEditText.filters = arrayOf(DateInputFilter())

        val carName = arguments?.getString(ARG_CAR_NAME) ?: return view

        addRecordButton.setOnClickListener {
            val date = dateEditText.text.toString()
            val mileage = mileageEditText.text.toString().toIntOrNull() ?: 0
            val liters = litersEditText.text.toString().toDoubleOrNull() ?: 0.0

            // Validate date format
            if (!isValidDate(date)) {
                dateEditText.error = "Неправильный формат даты. Используйте дд.мм.гггг"
                return@setOnClickListener
            }
            val consumption = if (mileage != 0) decimalFormat.format((liters / mileage) * 100).toDouble() else 0.0
            val newRecord = FuelRecord(date, mileage, liters, consumption)
            fuelRecords.add(newRecord)
            sortFuelRecords()
            fuelRecordAdapter.notifyDataSetChanged()

            saveDataToFile(carName)
            updateAverageConsumption()
            updateChart()

            dateEditText.text.clear()
            mileageEditText.text.clear()
            litersEditText.text.clear()
        }

        deleteAllButton.setOnClickListener {
            DeleteAllConfirmationDialog {
                fuelRecords.clear()
                fuelRecordAdapter.notifyDataSetChanged()
                saveDataToFile(carName)
                updateAverageConsumption()
                updateChart()
            }.show(parentFragmentManager, "DeleteAllConfirmationDialog")
        }

        loadDataFromFile(carName)
        updateAverageConsumption()
        updateChart()

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

    override fun onResume() {
        super.onResume()
        val carName = arguments?.getString(ARG_CAR_NAME) ?: return
        loadDataFromFile(carName)
        updateAverageConsumption()
        updateChart()
    }

    private fun saveDataToFile(carName: String) {
        try {
            val file = File(context?.filesDir, "fuel_data_$carName.txt")
            file.writeText(fuelRecords.joinToString("\n") { "${it.date},${it.mileage},${it.liters},${it.consumption}" })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadDataFromFile(carName: String) {
        try {
            val file = File(context?.filesDir, "fuel_data_$carName.txt")
            if (file.exists()) {
                val loadedRecords = file.readLines().map { line ->
                    val parts = line.split(",")
                    FuelRecord(parts[0], parts[1].toInt(), parts[2].toDouble(), parts[3].toDouble())
                }
                fuelRecords.clear()
                fuelRecords.addAll(loadedRecords)
                sortFuelRecords()
                fuelRecordAdapter.notifyDataSetChanged()
            } else {
                fuelRecords.clear()
                fuelRecordAdapter.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sortFuelRecords() {
        fuelRecords.sortBy { dateFormat.parse(it.date) }
    }

    private fun updateAverageConsumption() {
        val totalLiters = fuelRecords.sumOf { it.liters }
        val totalMileage = fuelRecords.sumOf { it.mileage }
        val averageConsumption = if (totalMileage != 0) totalLiters / totalMileage * 100 else 0.0
        averageConsumptionTextView.text = "Средний расход: ${decimalFormat.format(averageConsumption)} л/100км"
    }

    private fun setupChart() {
        fuelConsumptionChart.apply {
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisRight.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
        }
    }

    private fun updateChart() {
    val entries = fuelRecords.mapIndexed { index, record ->
        Entry(index.toFloat(), record.consumption.toFloat())
    }
    val dataSet = LineDataSet(entries, "Расход топлива").apply {
        color = Color.BLUE
        valueTextColor = Color.BLACK
        lineWidth = 2f
        setDrawCircles(true)
        setCircleColor(Color.RED)
        setDrawFilled(true)
        fillColor = Color.CYAN
    }
    val lineData = LineData(dataSet)
    fuelConsumptionChart.data = lineData

    // Customize X-Axis
    val xAxis: XAxis = fuelConsumptionChart.xAxis
    xAxis.position = XAxis.XAxisPosition.BOTTOM
    xAxis.setDrawGridLines(false)

    // Customize Y-Axis
    val leftAxis: YAxis = fuelConsumptionChart.axisLeft
    leftAxis.setDrawGridLines(false)
    val rightAxis: YAxis = fuelConsumptionChart.axisRight
    rightAxis.isEnabled = false

    fuelConsumptionChart.invalidate()
}

    // Input filter for date field
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