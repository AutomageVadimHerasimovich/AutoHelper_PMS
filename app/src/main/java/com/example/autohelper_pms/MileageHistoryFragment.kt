package com.example.autohelper_pms

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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class MileageHistoryFragment : Fragment() {

    private lateinit var latestMileageTextView: TextView
    private lateinit var mileageChart: LineChart
    private lateinit var adapter: MileageAdapter
    private val records = mutableListOf<MileageRecord>()

    private fun saveDataToFile(carName: String) {
        try {
            val file = File(context?.filesDir, "mileage_data_$carName.txt")
            file.writeText(records.joinToString("\n") { "${it.date},${it.change},${it.mileage},${it.reason},${it.place}" })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadDataFromFile(carName: String) {
        try {
            val file = File(context?.filesDir, "mileage_data_$carName.txt")
            if (file.exists()) {
                val loadedRecords = file.readLines().map { line ->
                    val parts = line.split(",")
                    MileageRecord(parts[0], parts[1], parts[2], parts[3], parts[4])
                }
                records.clear()
                records.addAll(loadedRecords)
                adapter.notifyDataSetChanged()
                updateLatestMileage()
                updateChart()
            } else {
                records.clear()
                adapter.notifyDataSetChanged()
                updateLatestMileage()
                updateChart()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateLatestMileage() {
        if (records.isNotEmpty()) {
            val latestRecord = records.last()
            latestMileageTextView.text = "Текущий пробег: ${latestRecord.mileage}км"
        } else {
            latestMileageTextView.text = "Добавьте новую запись"
        }
    }

    private fun updateChart() {
        val entries = records.mapIndexed { index, record ->
            Entry(index.toFloat(), record.mileage.toFloat())
        }
        val dataSet = LineDataSet(entries, "Пробег").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            lineWidth = 2f
            setDrawCircles(true)
            setCircleColor(Color.RED)
            setDrawFilled(true)
            fillColor = Color.CYAN
        }
        val lineData = LineData(dataSet)
        mileageChart.data = lineData

        // Customize X-Axis
        val xAxis: XAxis = mileageChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        // Customize Y-Axis
        val leftAxis: YAxis = mileageChart.axisLeft
        leftAxis.setDrawGridLines(false)
        val rightAxis: YAxis = mileageChart.axisRight
        rightAxis.isEnabled = false

        mileageChart.invalidate()
    }

    companion object {
        private const val ARG_CAR_NAME = "car_name"

        fun newInstance(carName: String): MileageHistoryFragment {
            val fragment = MileageHistoryFragment()
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
        val view = inflater.inflate(R.layout.fragment_mileage_history, container, false)
        latestMileageTextView = view.findViewById(R.id.latestMileageTextView)
        mileageChart = view.findViewById(R.id.mileageChart)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val carName = arguments?.getString(ARG_CAR_NAME) ?: return view

        adapter = MileageAdapter(records, parentFragmentManager, { position ->
            records.removeAt(position)
            adapter.notifyItemRemoved(position + 1)
            saveDataToFile(carName)
            updateLatestMileage()
            updateChart()
        }, {
            records.clear()
            adapter.notifyDataSetChanged()
            saveDataToFile(carName)
            updateLatestMileage()
            updateChart()
        })
        recyclerView.adapter = adapter

        val dateEditText: EditText = view.findViewById(R.id.dateEditText)
        val changeEditText: EditText = view.findViewById(R.id.changeEditText)
        val mileageEditText: EditText = view.findViewById(R.id.mileageEditText)
        val reasonEditText: EditText = view.findViewById(R.id.reasonEditText)
        val placeEditText: EditText = view.findViewById(R.id.placeEditText)
        val addButton: Button = view.findViewById(R.id.addButton)

        // Set input filters
        dateEditText.filters = arrayOf(DateInputFilter())
        mileageEditText.filters = arrayOf(MileageInputFilter())

        addButton.setOnClickListener {
            val date = dateEditText.text.toString()
            val change = changeEditText.text.toString()
            val mileage = mileageEditText.text.toString()
            val reason = reasonEditText.text.toString()
            val place = placeEditText.text.toString()

            // Validate date format
            if (!isValidDate(date)) {
                dateEditText.error = "Неправильный формат даты. Используйте дд.мм.гггг"
                return@setOnClickListener
            }

            if (date.isNotEmpty() && change.isNotEmpty() && mileage.isNotEmpty() && reason.isNotEmpty() && place.isNotEmpty()) {
                val record = MileageRecord(date, change, mileage, reason, place)
                adapter.addRecord(record)
                saveDataToFile(carName)
                updateLatestMileage()
                updateChart()
                dateEditText.text.clear()
                changeEditText.text.clear()
                mileageEditText.text.clear()
                reasonEditText.text.clear()
                placeEditText.text.clear()
            }
        }

        loadDataFromFile(carName)

        return view
    }

    private fun isValidDate(date: String): Boolean {
        return try {
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).apply {
                isLenient = false
            }.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun onResume() {
        super.onResume()
        val carName = arguments?.getString(ARG_CAR_NAME) ?: return
        loadDataFromFile(carName)
    }

    // Input filter for date
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

    // Input filter for mileage
    class MileageInputFilter : InputFilter {
        override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val input = dest.toString().substring(0, dstart) + source + dest.toString().substring(dend)
            return if (input.matches(Regex("^-?\\d*$"))) null else ""
        }
    }
}