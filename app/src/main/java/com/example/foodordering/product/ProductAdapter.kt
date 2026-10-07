package com.example.foodordering.product

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodordering.R
import com.example.foodordering.model.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val products: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvImage: TextView = itemView.findViewById(R.id.tvProductImage)
        val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvDesc: TextView = itemView.findViewById(R.id.tvProductDesc)
        val tvPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.tvName.text = product.name
        holder.tvDesc.text = product.description

        // Format giá tiền
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
        holder.tvPrice.text = "${formatter.format(product.price)}đ"

        // Chữ cái đầu làm ảnh tạm
        if (product.name.isNotEmpty()) {
            holder.tvImage.text = product.name.take(2).uppercase()
        }

        holder.itemView.setOnClickListener {
            onItemClick(product)
        }
    }

    override fun getItemCount(): Int = products.size
}