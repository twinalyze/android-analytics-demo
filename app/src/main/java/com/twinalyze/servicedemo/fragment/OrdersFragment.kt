package com.twinalyze.servicedemo.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.twinalyze.servicedemo.R

class OrdersFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    var foodListJsonList: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_orders, container, false)


        foodListJsonList = requireArguments().getString("food_list").orEmpty()

        val tabLayout: TabLayout = view.findViewById(R.id.tabLayoutStatus)
        val orderViewPager: ViewPager2 = view.findViewById(R.id.orderViewPager)


        val adapter = OrdersAdapter(requireActivity(),foodListJsonList)
        orderViewPager.adapter = adapter
        orderViewPager.isSaveEnabled = false

        // Add tabs programmatically
        val statuses = listOf("Pending", "Complete", "Cancelled")
        statuses.forEach {
            tabLayout.addTab(tabLayout.newTab().setText(it))
        }

        for (i in 0 until tabLayout.tabCount) {
            val tab = (tabLayout.getChildAt(0) as ViewGroup).getChildAt(i) as ViewGroup
            for (j in 0 until tab.childCount) {
                val tabViewChild = tab.getChildAt(j)
                if (tabViewChild is TextView) {
                    tabViewChild.isAllCaps = false   // 👈 force disable CAPS
                }
            }
        }

        addTabMargins(tabLayout)


        // Handle tab selection
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                (tab.view.getChildAt(1) as? TextView)?.isAllCaps = false

                // 🔹 Switch fragment in ViewPager
                orderViewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                (tab.view.getChildAt(1) as? TextView)?.isAllCaps = false

            }
            override fun onTabReselected(tab: TabLayout.Tab) {
                (tab.view.getChildAt(1) as? TextView)?.isAllCaps = false
            }
        })

        orderViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.getTabAt(position)?.select()
            }
        })


        return view
    }

    private fun addTabMargins(tabLayout: TabLayout) {
        val tabStrip = tabLayout.getChildAt(0) as ViewGroup
        for (i in 0 until tabStrip.childCount) {
            val tab = tabStrip.getChildAt(i)
            val params = tab.layoutParams as ViewGroup.MarginLayoutParams
            params.marginStart = 10   // left margin
            params.marginEnd = 10     // right margin
            tab.layoutParams = params
            tabLayout.requestLayout()
        }
    }

    class OrdersAdapter(
        activity: FragmentActivity,
        private val foodListJson: String
    ) : FragmentStateAdapter(activity) {

        private val categories = listOf("Pending", "Complete", "Cancelled")

        override fun getItemCount() = categories.size

        override fun createFragment(position: Int): Fragment {
            return OrdersListFragment.newInstance(categories[position], foodListJson)
        }
    }


}