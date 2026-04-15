package com.twinalyze.servicedemo

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.twinalyze.servicedemo.fragment.HomeFragment
import com.twinalyze.servicedemo.fragment.OrdersFragment
import com.twinalyze.servicedemo.fragment.ProfileFragment
import com.twinalyze.servicedemo.model.setIconAndLabelSpacing

class MainActivity : AppCompatActivity() {

    private lateinit var frameContainer: FrameLayout
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var indicatorHome: View
    private lateinit var indicatorOrders: View
    private lateinit var indicatorProfile: View
    var foodListJson: String = ""

    // Keep all fragments alive for smooth switching
    private val homeFragment = HomeFragment()
    private val ordersFragment = OrdersFragment()
    private val profileFragment = ProfileFragment()
    private var activeFragment: Fragment = homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        frameContainer = findViewById(R.id.fragment_container)
        bottomNav = findViewById(R.id.bottom_navigation)
        indicatorHome = findViewById(R.id.indicator_home)
        indicatorOrders = findViewById(R.id.indicator_orders)
        indicatorProfile = findViewById(R.id.indicator_profile)

        val selectedColor = ContextCompat.getColor(this, R.color.selected_color)
        val unselectedColor = ContextCompat.getColor(this, R.color.unselected_color)

        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(selectedColor, unselectedColor)
        )

        bottomNav.itemTextColor = colorStateList
        bottomNav.itemIconTintList = colorStateList

//        bottomNav.setIconTextGapSafe(6)
        bottomNav.setIconAndLabelSpacing(
            gapDp = 8,        // space between icon & text
            iconSizeDp = 18,   // smaller icon
            iconBottomPadDp = 0
        )


        foodListJson = this.intent?.getStringExtra("food_list").orEmpty()
        Log.d("ordersAdapter", "onCreate: "+foodListJson)


        // 🔹 Set bundle once before adding fragment
        val bundle = Bundle().apply {
            putString("food_list", foodListJson)
        }
        ordersFragment.arguments = bundle


        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, homeFragment, "home")
                .add(R.id.fragment_container, ordersFragment, "orders").hide(ordersFragment)
                .hide(ordersFragment)
                .setMaxLifecycle(ordersFragment, Lifecycle.State.STARTED)
                .add(R.id.fragment_container, profileFragment, "profile").hide(profileFragment)
                .hide(profileFragment)
                .setMaxLifecycle(profileFragment, Lifecycle.State.STARTED)
                .commit()
            activeFragment = homeFragment

        }


        // 🔹 Handle navigation clicks
        window.decorView.post {
            bottomNav.setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        loadFragment(homeFragment)
                        indicatorHome.isVisible = true
                        indicatorOrders.isInvisible = true
                        indicatorProfile.isInvisible = true
                        true
                    }

                    R.id.nav_orders -> {
                        loadFragment(ordersFragment)
                        indicatorHome.isInvisible = true
                        indicatorOrders.isVisible = true
                        indicatorProfile.isInvisible = true
                        true
                    }

                    R.id.nav_profile -> {
                        loadFragment(profileFragment)
                        indicatorHome.isInvisible = true
                        indicatorOrders.isInvisible = true
                        indicatorProfile.isVisible = true

                        true
                    }

                    else -> false
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        val fragName = intent.getStringExtra("fragmentName")
        if (fragName == "CartFragment") {
            bottomNav.selectedItemId = R.id.nav_orders
            switchFragment(ordersFragment)
            indicatorHome.isInvisible = true
            indicatorOrders.isVisible = true
            indicatorProfile.isInvisible = true
        }
    }

    private fun switchFragment(target: Fragment) {
        if (target === activeFragment) return
        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(target)
            .setMaxLifecycle(activeFragment, Lifecycle.State.STARTED)
            .setMaxLifecycle(target, Lifecycle.State.RESUMED)
            .commitAllowingStateLoss()
        activeFragment = target
    }


    private fun loadFragment(target: Fragment) {
        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(target)
            .setMaxLifecycle(activeFragment, Lifecycle.State.STARTED)
            .setMaxLifecycle(target, Lifecycle.State.RESUMED)
            .commit()
        if (target === activeFragment) return
        activeFragment = target
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }


}