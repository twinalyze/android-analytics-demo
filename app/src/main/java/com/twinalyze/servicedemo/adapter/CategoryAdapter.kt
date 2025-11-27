package com.twinalyze.servicedemo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.twinalyze.servicedemo.R
import com.twinalyze.servicedemo.model.CategoriesData

class CategoryAdapter (private val categories: MutableList<CategoriesData>,private val onAddClick: (CategoriesData) -> Unit) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imgCategory)
        val name: TextView = itemView.findViewById(R.id.txtCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
//        holder.image.setImageResource(category.imageRes)
        Glide.with(holder.itemView.context)
            .load(category.categoriesImage)
            .placeholder(R.drawable.ic_img)
            .into(holder.image)
        holder.name.text = category.categoriesName

        holder.itemView.setOnClickListener {
            onAddClick(category) // ✅ notify activity
            Toast.makeText(holder.itemView.context, "${category.categoriesName} added!", Toast.LENGTH_SHORT).show()

        }
    }

    override fun getItemCount(): Int = categories.size

    fun setItems(newItems: List<CategoriesData>) {
        categories.clear()
        categories.addAll(newItems)
        notifyDataSetChanged()
    }
}
