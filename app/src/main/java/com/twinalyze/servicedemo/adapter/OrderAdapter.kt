package com.twinalyze.servicedemo.adapter

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.twinalyze.servicedemo.OrdersDetailsActivity
import com.twinalyze.servicedemo.R
import com.twinalyze.servicedemo.model.Food
import com.twinalyze.servicedemo.model.Order

class OrderAdapter (
    private var orders: List<Order>,

) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)
        val txtOrderId: TextView = itemView.findViewById(R.id.txtOrderId)
        val txtRestaurantName: TextView = itemView.findViewById(R.id.txtRestaurantName)
        val txtAddress: TextView = itemView.findViewById(R.id.txtAddress)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        val layoutFoodItems: LinearLayout = itemView.findViewById(R.id.layoutFoodItems)
        val nextBtn: ImageView = itemView.findViewById(R.id.next_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.txtDate.text = order.date
        holder.txtOrderId.text = "${order.orderId}"
        holder.txtRestaurantName.text = order.restaurantName
        holder.txtAddress.text = order.address
        holder.txtStatus.text = order.status

        // 🔹 Status color
        holder.txtStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, order.statusColor))
        holder.txtStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF7DB")) // new tint
        holder.txtStatus.setTextColor(Color.parseColor("#E39F00"))

        // 🔹 Remove old views
        holder.layoutFoodItems.removeAllViews()

        val displayedItems = mutableListOf<Food>()

        // 🔹 Dynamically add food items
        for (food in order.items) {
            val row = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.item_food_row, holder.layoutFoodItems, false)

            val txtFoodName = row.findViewById<TextView>(R.id.txtFoodName)
            val txtPrice = row.findViewById<TextView>(R.id.txtPrice)

            txtFoodName.text = "1x ${food.name}"
            txtPrice.text = food.price

            holder.layoutFoodItems.addView(row)

            // ✅ Keep track of added items
            displayedItems.add(food)
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val clickedOrder = orders[position]

            val intent = Intent(context, OrdersDetailsActivity::class.java)
            intent.putExtra("OrderId", "${clickedOrder.orderId}")
            intent.putExtra("OrderDate", clickedOrder.date)
            intent.putExtra("RestaurantName", clickedOrder.restaurantName)
            intent.putExtra("RestaurantAddress", clickedOrder.address)

            intent.putExtra("OrderItemList", ArrayList(displayedItems))

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateData(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}