package com.example.foodordering.admin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodordering.R
import com.example.foodordering.model.Product
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class AdminProductAdapter(
    private val products: List<Product>,
    private val categoryMap: Map<Int, String>,
    private val onEdit: (Product) -> Unit,
    private val onToggleStatus: (Product) -> Unit,
    private val onDelete: (Product) -> Unit
) : RecyclerView.Adapter<AdminProductAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvProductName)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvLabel: TextView = view.findViewById(R.id.tvLabel)
        val btnEdit: MaterialButton = view.findViewById(R.id.btnEdit)
        val btnToggleStatus: MaterialButton = view.findViewById(R.id.btnToggleStatus)
        val btnDelete: MaterialButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        holder.tvName.text = product.name
        holder.tvPrice.text = formatter.format(product.price)
        holder.tvCategory.text = "Danh mục: ${categoryMap[product.categoryId] ?: "Không rõ"}"

        // Trạng thái
        if (product.status == "available") {
            holder.tvStatus.text = "Đang bán"
            holder.tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9"))
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"))
            holder.btnToggleStatus.text = "Ẩn"
            holder.btnToggleStatus.backgroundTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#F9A825"))
        } else {
            holder.tvStatus.text = "Đã ẩn"
            holder.tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE"))
            holder.tvStatus.setTextColor(Color.parseColor("#C62828"))
            holder.btnToggleStatus.text = "Hiện"
            holder.btnToggleStatus.backgroundTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#2E7D32"))
        }

        // Nhãn
        if (product.label.isNotEmpty()) {
            holder.tvLabel.visibility = View.VISIBLE
            holder.tvLabel.text = product.label
            when (product.label) {
                "Bán chạy" -> holder.tvLabel.setBackgroundColor(Color.parseColor("#E65100"))
                "Hot" -> holder.tvLabel.setBackgroundColor(Color.parseColor("#D32F2F"))
                "Mới" -> holder.tvLabel.setBackgroundColor(Color.parseColor("#1976D2"))
                else -> holder.tvLabel.setBackgroundColor(Color.parseColor("#6D4C41"))
            }
        } else {
            holder.tvLabel.visibility = View.GONE
        }

        holder.btnEdit.setOnClickListener { onEdit(product) }
        holder.btnToggleStatus.setOnClickListener { onToggleStatus(product) }
        holder.btnDelete.setOnClickListener { onDelete(product) }
    }

    override fun getItemCount() = products.size
}