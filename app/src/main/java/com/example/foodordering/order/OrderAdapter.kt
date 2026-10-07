package com.example.foodordering.order

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodordering.R
import com.example.foodordering.model.Order
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class OrderAdapter(
    private val orders: List<Order>,
    private val onReorder: (Order) -> Unit,
    private val onItemClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderCode: TextView = itemView.findViewById(R.id.tvOrderCode)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        val tvOrderTotal: TextView = itemView.findViewById(R.id.tvOrderTotal)
        val btnReorder: MaterialButton = itemView.findViewById(R.id.btnReorder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        holder.tvOrderCode.text = "Mã đơn: ${order.orderCode}"
        holder.tvOrderDate.text = order.createdAt
        holder.tvOrderTotal.text = formatter.format(order.total)

        // Hiển thị trạng thái đẹp
        when (order.status.lowercase()) {
            "pending", "paid" -> {
                holder.tvOrderStatus.text = "Chờ xác nhận"
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#FFF3E0"))
                holder.tvOrderStatus.setTextColor(Color.parseColor("#E65100"))
            }
            "confirmed" -> {
                holder.tvOrderStatus.text = "Đã xác nhận"
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#E3F2FD"))
                holder.tvOrderStatus.setTextColor(Color.parseColor("#1565C0"))
            }
            "preparing" -> {
                holder.tvOrderStatus.text = "Đang pha chế"
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#FFF8E1"))
                holder.tvOrderStatus.setTextColor(Color.parseColor("#F9A825"))
            }
            "ready" -> {
                holder.tvOrderStatus.text = "Sẵn sàng"
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#E8F5E9"))
                holder.tvOrderStatus.setTextColor(Color.parseColor("#2E7D32"))
            }
            "completed" -> {
                holder.tvOrderStatus.text = "Hoàn thành"
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#E8F5E9"))
                holder.tvOrderStatus.setTextColor(Color.parseColor("#1B5E20"))
            }
            "cancelled" -> {
                holder.tvOrderStatus.text = "Đã hủy"
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#FFEBEE"))
                holder.tvOrderStatus.setTextColor(Color.parseColor("#C62828"))
            }
            else -> {
                holder.tvOrderStatus.text = order.status
            }
        }

        // Nút Đặt lại
        holder.btnReorder.setOnClickListener {
            onReorder(order)
        }

        // Click cả card để xem chi tiết đơn
        holder.itemView.setOnClickListener {
            onItemClick(order)
        }
    }

    override fun getItemCount() = orders.size
}