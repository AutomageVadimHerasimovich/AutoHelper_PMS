// CarDetailActivity.kt
package com.example.autohelper_pms

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2

class CarDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_detail)

        val carName = intent.getStringExtra("CAR_NAME") ?: "default_car" // Provide a default value
        supportActionBar?.title = carName

        val sectionsPagerAdapter = SectionsPagerAdapter(this, carName) // Pass the car name
        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter

        val tabs: TabLayout = findViewById(R.id.tabs)
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "История пробега"
                1 -> "Тех обслуживание"
                2 -> "Документы"
                3 -> "Топливо"
                4 -> "Заметки"
                else -> null
            }
        }.attach()
    }
}