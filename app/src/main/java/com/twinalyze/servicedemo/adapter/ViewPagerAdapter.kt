package com.twinalyze.servicedemo.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.twinalyze.servicedemo.fragment.HomeFragment
import com.twinalyze.servicedemo.fragment.OrdersFragment
import com.twinalyze.servicedemo.fragment.ProfileFragment

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> OrdersFragment()
            2 -> ProfileFragment()
            else -> HomeFragment()
        }
    }
}