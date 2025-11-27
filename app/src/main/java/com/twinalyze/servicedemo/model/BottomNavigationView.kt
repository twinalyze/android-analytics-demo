package com.twinalyze.servicedemo.model

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView

private fun BottomNavigationView.dp(v: Int) =
    (v * resources.displayMetrics.density).toInt()

/*fun BottomNavigationView.setIconTextGapSafe(gapDp: Int = 8) {
    post {
        val gap = dp(gapDp)
        val menu = getChildAt(0) as ViewGroup
        for (i in 0 until menu.childCount) {
            val item = menu.getChildAt(i)
            val small = item.findViewById<TextView>(
                com.google.android.material.R.id.navigation_bar_item_small_label_view
            )
            val large = item.findViewById<TextView>(
                com.google.android.material.R.id.navigation_bar_item_large_label_view
            )
            listOfNotNull(small, large).forEach { tv ->
                val lp = tv.layoutParams
                if (lp is ViewGroup.MarginLayoutParams) {
                    lp.topMargin = gap
                    tv.layoutParams = lp
                } else {
                    tv.setPadding(tv.paddingLeft, tv.paddingTop + gap, tv.paddingRight, tv.paddingBottom)
                }
            }
        }
    }
}*/

fun BottomNavigationView.setIconAndLabelSpacing(
    gapDp: Int = 8,
    iconSizeDp: Int? = 20,
    iconBottomPadDp: Int = 0
) {
    post {
        val gap = dp(gapDp)
        val iconSizePx = iconSizeDp?.let { dp(it) }
        val iconExtraBottomPad = dp(iconBottomPadDp)

        // The first child is the menu view
        val menu = getChildAt(0) as? ViewGroup ?: return@post
        for (i in 0 until menu.childCount) {
            val item = menu.getChildAt(i)

            // Text views (small/large label)
            val small = item.findViewById<TextView>(
                com.google.android.material.R.id.navigation_bar_item_small_label_view
            )
            val large = item.findViewById<TextView>(
                com.google.android.material.R.id.navigation_bar_item_large_label_view
            )

            // Icon view id works for Material 2/3 Bottom/NavigationBar
            val iconView = item.findViewById<ImageView>(
                com.google.android.material.R.id.navigation_bar_item_icon_view
            )

            // 1) Increase space between icon and label by raising label's topMargin
            listOfNotNull(small, large).forEach { tv ->
                (tv.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
                    lp.topMargin = gap
                    tv.layoutParams = lp
                    tv.includeFontPadding = false
                } ?: run {
                    tv.setPadding(tv.paddingLeft, tv.paddingTop + gap, tv.paddingRight, tv.paddingBottom)
                    tv.includeFontPadding = false
                }
            }

            // 2) Optionally resize the icon
            if (iconView != null && iconSizePx != null) {
                (iconView.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
                    lp.width = iconSizePx
                    lp.height = iconSizePx
                    lp.bottomMargin = (lp.bottomMargin + iconExtraBottomPad)
                    iconView.layoutParams = lp
                } ?: run {
                    iconView.layoutParams = ViewGroup.LayoutParams(iconSizePx, iconSizePx)
                    iconView.setPadding(
                        iconView.paddingLeft,
                        iconView.paddingTop,
                        iconView.paddingRight,
                        iconView.paddingBottom + iconExtraBottomPad
                    )
                }
                iconView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                iconView.adjustViewBounds = true
            }
        }
    }
}
