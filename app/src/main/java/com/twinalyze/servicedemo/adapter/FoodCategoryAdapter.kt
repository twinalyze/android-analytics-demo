package com.twinalyze.servicedemo.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.twinalyze.servicedemo.R

class FoodCategoryAdapter(
    private var categories: List<String?>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<FoodCategoryAdapter.CategoryViewHolder>() {

    private var selectedPos = 0

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtCategory: TextView = itemView.findViewById(R.id.txtCategory)
        val card: CardView = itemView as CardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.txtCategory.text = category

        // highlight selected
        if (selectedPos == position) {
//            holder.card.setCardBackgroundColor(Color.BLACK)
            holder.card.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.context, R.color.black))
//            holder.txtCategory.setTextColor(Color.WHITE)
            holder.txtCategory.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )

        } else {
//            holder.card.setCardBackgroundColor(Color.WHITE)
            holder.card.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.context, R.color.tags_unselected_color))
//            holder.txtCategory.setTextColor(Color.GRAY)
            holder.txtCategory.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.unselected_color)
            )

        }

        holder.itemView.setOnClickListener {
            val prevPos = selectedPos
            selectedPos = position
            notifyItemChanged(prevPos)
            notifyItemChanged(position)
            category?.let { it1 -> onClick(it1) }
        }
    }

    override fun getItemCount(): Int = categories.size

    fun setSelectedIndex(i: Int) {
        val old = selectedPos
        selectedPos = i
        notifyItemChanged(old)
        notifyItemChanged(selectedPos)
    }
}