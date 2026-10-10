package com.example.foodordering.admin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodordering.R
import com.example.foodordering.model.InventoryItem
import com.google.android.material.button.MaterialButton

class AdminInventoryAdapter(
    private val items: List<InventoryItem>,
    private val onUpdateClick: (InventoryItem) -> Unit
) : RecyclerView.Adapter<AdminInventoryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCode: TextView = view.findViewById(R.id.tvCode)
        val tvGroup: TextView = view.findViewById(R.id.tvGroup)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val btnUpdateQty: MaterialButton = view.findViewById(R.id.btnUpdateQty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventory, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvCode.text = item.code
        holder.tvGroup.text = item.groupName
        holder.tvName.text = item.name
        holder.tvQuantity.text = "Tồn: ${formatQty(item.quantity)} ${item.unit}"

        // Cảnh báo sắp hết
        if (item.quantity <= item.minQuantity) {
            holder.tvStatus.text = "Sắp hết"
            holder.tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE"))
            holder.tvStatus.setTextColor(Color.parseColor("#C62828"))
        } else {
            holder.tvStatus.text = "Đủ hàng"
            holder.tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9"))
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"))
        }

        holder.btnUpdateQty.setOnClickListener {
            onUpdateClick(item)
        }
    }

    override fun getItemCount() = items.size

    private fun formatQty(qty: Double): String {
        return if (qty % 1.0 == 0.0) qty.toInt().toString() else qty.toString()
    }
}