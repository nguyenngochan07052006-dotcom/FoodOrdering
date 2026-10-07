package com.example.foodordering.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodordering.R
import com.example.foodordering.model.CartDisplayItem
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val items: List<CartDisplayItem>,
    private val onIncrease: (CartDisplayItem) -> Unit,
    private val onDecrease: (CartDisplayItem) -> Unit,
    private val onDelete: (CartDisplayItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvCartName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvCartPrice)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvCartQuantity)
        val tvItemTotal: TextView = itemView.findViewById(R.id.tvCartItemTotal)
        val btnPlus: Button = itemView.findViewById(R.id.btnCartPlus)
        val btnMinus: Button = itemView.findViewById(R.id.btnCartMinus)
        val btnDelete: Button = itemView.findViewById(R.id.btnCartDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]

        holder.tvName.text = item.name
        holder.tvPrice.text = formatter.format(item.price)
        holder.tvQuantity.text = item.quantity.toString()
        holder.tvItemTotal.text = formatter.format(item.total)

        holder.btnPlus.setOnClickListener { onIncrease(item) }
        holder.btnMinus.setOnClickListener { onDecrease(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount(): Int = items.size
}