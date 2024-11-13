package com.example.autohelper_pms

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SectionsPagerAdapter(
    fa: FragmentActivity,
    private val carName: String
) : FragmentStateAdapter(fa) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MileageHistoryFragment.newInstance(carName)
            1 -> MaintenanceFragment.newInstance(carName)
            2 -> DocumentsFragment.newInstance(carName)
            3 -> FuelFragment.newInstance(carName)
            4 -> NotesFragment.newInstance(carName)
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }

    override fun getItemCount(): Int {
        return 5
    }
}