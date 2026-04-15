package com.twinalyze.servicedemo

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.twinalyze.servicedemo.fragment.RestaurantsListFragment

class RestaurantsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_restaurants)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        val btnBack: ImageView = findViewById(R.id.btn_back)

        btnBack.setOnClickListener {
//            onBackPressed()
            startActivity(Intent(this, MainActivity::class.java))

        }

        setSupportActionBar(toolbar)

        // Title set
        supportActionBar?.title = "Restaurants"
        toolbar.setContentInsetsRelative(0, 0)
        toolbar.setContentInsetsAbsolute(0, 0)

        // back button click listener
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)

        val adapter = TabPagerAdapter(this)
        viewPager.adapter = adapter

        tabLayout.addTab(tabLayout.newTab().setText("Veg"))
        tabLayout.addTab(tabLayout.newTab().setText("Non Veg"))
        tabLayout.addTab(tabLayout.newTab().setText("Top Rated"))

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

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                (tab.view.getChildAt(1) as? TextView)?.isAllCaps = false

                // 🔹 Switch fragment in ViewPager
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                (tab.view.getChildAt(1) as? TextView)?.isAllCaps = false
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                (tab.view.getChildAt(1) as? TextView)?.isAllCaps = false
            }
        })

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.getTabAt(position)?.select()
            }
        })

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

    class TabPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        private val categories = listOf("Veg", "Non Veg", "Top Rated")
        override fun getItemCount() = categories.size
        override fun createFragment(position: Int): Fragment =
            RestaurantsListFragment.newInstance(categories[position])
    }

}