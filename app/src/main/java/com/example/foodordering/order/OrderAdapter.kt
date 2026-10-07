package com.example.foodordering.order

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodordering.R
import com.example.foodordering.model.Order
import com.example.foodordering.model.OrderStatus
import java.text.NumberFormat
import java.util.Locale

class OrderAdapter(
    private val orders: List<Order>
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderCode: TextView = itemView.findViewById(R.id.tvOrderCode)
        val tvOrderTotal: TextView = itemView.findViewById(R.id.tvOrderTotal)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.tvOrderCode.text = "Mã đơn: ${order.orderCode}"
        holder.tvOrderTotal.text = formatter.format(order.total)
        holder.tvOrderDate.text = order.createdAt

        // Hiển thị trạng thái đẹp
        val statusText = when (OrderStatus.from(order.status)) {
            OrderStatus.PENDING -> "Chờ xác nhận"
            OrderStatus.CONFIRMED -> "Đã xác nhận"
            OrderStatus.PREPARING -> "Đang chuẩn bị"
            OrderStatus.READY -> "Sẵn sàng"
            OrderStatus.COMPLETED -> "Hoàn thành"
            OrderStatus.CANCELLED -> "Đã hủy"
        }
        holder.tvOrderStatus.text = statusText

        // Màu trạng thái
        val color = when (OrderStatus.from(order.status)) {
            OrderStatus.PENDING -> "#FF8F00"
            OrderStatus.CONFIRMED -> "#1976D2"
            OrderStatus.PREPARING -> "#7B1FA2"
            OrderStatus.READY -> "#388E3C"
            OrderStatus.COMPLETED -> "#2E7D32"
            OrderStatus.CANCELLED -> "#C62828"
        }
        holder.tvOrderStatus.setTextColor(android.graphics.Color.parseColor(color))
    }

    override fun getItemCount(): Int = orders.size
}