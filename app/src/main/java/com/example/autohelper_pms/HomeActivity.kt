package com.example.autohelper_pms

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import java.io.File

fun saveCarList(context: Context, username: String, carList: List<String>) {
    val file = File(context.filesDir, "${username}_car_list.txt")
    file.writeText(carList.joinToString("\n"))
}

fun loadCarList(context: Context, username: String): MutableList<String> {
    val file = File(context.filesDir, "${username}_car_list.txt")
    return if (file.exists()) {
        file.readLines().toMutableList()
    } else {
        mutableListOf()
    }
}

class HomeActivity : AppCompatActivity() {
    private lateinit var carList: MutableList<String>
    private lateinit var adapter: HomeAdapter
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isTablet = resources.getBoolean(R.bool.isTablet)
        requestedOrientation = if (isTablet) {
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContentView(R.layout.activity_home)

        // Assume username is passed via Intent
        username = intent.getStringExtra("USERNAME") ?: "default_user"

        carList = loadCarList(this, username)

        val carInput: EditText = findViewById(R.id.car_input)
        val addCarButton: Button = findViewById(R.id.add_car_button)
        val recyclerView: RecyclerView = findViewById(R.id.home_recycler_view)

        adapter = HomeAdapter(carList) { position ->
            val context = this
            val intent = Intent(context, CarDetailActivity::class.java).apply {
                putExtra("CAR_NAME", carList[position])
                putExtra("USERNAME", username) // Pass the username
            }
            context.startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, LinearLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                adapter.onItemMove(fromPosition, toPosition)
                saveCarList(this@HomeActivity, username, carList)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val carName = carList[position]
                carList.removeAt(position)
                adapter.notifyItemRemoved(position)
                saveCarList(this@HomeActivity, username, carList)

                // Delete the mileage history file
                val file0 = File(this@HomeActivity.filesDir, "mileage_data_$carName.txt")
                val file1 = File(this@HomeActivity.filesDir, "fuel_data_$carName.txt")
                val file2 = File(this@HomeActivity.filesDir, "maintenance_data_$carName.txt")
                val file3 = File(this@HomeActivity.filesDir, "document_data_$carName.txt")
                val file4 = File(this@HomeActivity.filesDir, "note_data_$carName.txt")
                if (file0.exists()) {
                    file0.delete()
                }
                if (file1.exists()) {
                    file1.delete()
                }
                if (file2.exists()) {
                    file2.delete()
                }
                if (file3.exists()) {
                    file3.delete()
                }
                if (file4.exists()) {
                    file4.delete()
                }
            }

            override fun isLongPressDragEnabled(): Boolean {
                return true
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        addCarButton.setOnClickListener {
            val carName = carInput.text.toString().trim()
            if (carName.isNotEmpty()) {
                carList.add(carName)
                adapter.notifyItemInserted(carList.size - 1)
                carInput.text.clear()
                saveCarList(this, username, carList)
            }
        }
    }
}