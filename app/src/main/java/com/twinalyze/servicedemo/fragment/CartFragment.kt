package com.twinalyze.servicedemo.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.twinalyze.servicedemo.MainActivity
import com.twinalyze.servicedemo.R
import com.twinalyze.servicedemo.adapter.CartFoodAdapter
import com.twinalyze.servicedemo.model.Food


class CartFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private lateinit var prefs: SharedPreferences
    private lateinit var txtSubTotal: TextView
    private lateinit var txtDiscount: TextView
    private lateinit var txtTotal: TextView
    private lateinit var txtTotalAmount: TextView
    private lateinit var txtAddMore: TextView
    private lateinit var layoutPlaceOrder: LinearLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        txtSubTotal = view.findViewById<TextView>(R.id.txtSubTotal)
        txtDiscount = view.findViewById<TextView>(R.id.txtDiscount)
        txtTotal = view.findViewById<TextView>(R.id.txtTotal)
        txtTotalAmount = view.findViewById<TextView>(R.id.txtTotalAmount)
        txtAddMore = view.findViewById<TextView>(R.id.txtAddMore)
        layoutPlaceOrder = view.findViewById<LinearLayout>(R.id.layoutPlaceOrder)

        // Get data from intent
        val name = requireActivity().intent.getStringExtra("restaurantName")
        val star = requireActivity().intent.getStringExtra("restaurantStar")
        val address = requireActivity().intent.getStringExtra("restaurantAddress")
        val type = requireActivity().intent.getStringExtra("restaurantType")
        val near = requireActivity().intent.getStringExtra("restaurantNear")
        val imageUrl = requireActivity().intent.getStringExtra("restaurantImage")
        val description = requireActivity().intent.getStringExtra("restaurantDescription")

        Log.d("CardData", "onCreateView: "+name+ " address: "+address+" star: "+star)


        // get prefs
        prefs = requireContext().getSharedPreferences("my_prefs", MODE_PRIVATE)   // Fragment: requireContext().getSharedPreferences(...)

        val gson = com.google.gson.Gson()
        val type1 = object : com.google.gson.reflect.TypeToken<List<Food>>() {}.type

        val cart: List<Food> = gson.fromJson(prefs.getString("cart_json", "[]"), type1)
        Log.d("CardData", "onCreateView: "+cart.toString())


        val recyclerFood = view.findViewById<RecyclerView>(R.id.recyclerItems)
        recyclerFood.layoutManager = LinearLayoutManager(requireContext())


        val foodList = cart.toMutableList()

        val adapter = CartFoodAdapter(foodList) { totalPrice ->
            txtSubTotal.text = totalPrice.toString()   // update your total TextView

            val sub = txtSubTotal.text.toString().toIntOrNull() ?: 0
            val disc = txtDiscount.text.toString().toIntOrNull() ?: 0
            val total = (sub - disc).coerceAtLeast(0)
            txtTotal.text = total.toString()

            txtTotalAmount.text = total.toString()

        }
        recyclerFood.adapter = adapter
        adapter.emitTotalsNow()  // 👉 first-time totals

        txtAddMore.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }


        layoutPlaceOrder.setOnClickListener {

            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.putExtra("fragmentName", "CartFragment")
            intent.putExtra("restaurantName", name)
            intent.putExtra("restaurantAddress", address)
            val json = Gson().toJson(foodList)
            intent.putExtra("food_list", json)
            startActivity(intent)
            prefs.edit().clear().apply()


        }

        return view
    }

}