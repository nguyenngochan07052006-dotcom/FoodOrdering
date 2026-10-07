package com.example.foodordering.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodordering.R
import com.example.foodordering.model.Order
import com.example.foodordering.model.OrderStatus
import java.text.NumberFormat
import java.util.Locale

class AdminOrderAdapter(
    private val orders: List<Order>,
    private val onUpdateClick: (Order) -> Unit
) : RecyclerView.Adapter<AdminOrderAdapter.ViewHolder>() {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCode: TextView = itemView.findViewById(R.id.tvAdminOrderCode)
        val tvTotal: TextView = itemView.findViewById(R.id.tvAdminOrderTotal)
        val tvStatus: TextView = itemView.findViewById(R.id.tvAdminOrderStatus)
        val tvDate: TextView = itemView.findViewById(R.id.tvAdminOrderDate)
        val btnUpdate: Button = itemView.findViewById(R.id.btnUpdateStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        holder.tvCode.text = order.orderCode
        holder.tvTotal.text = formatter.format(order.total)
        holder.tvDate.text = order.createdAt

        val statusText = when (OrderStatus.from(order.status)) {
            OrderStatus.PENDING -> "Chờ xác nhận"
            OrderStatus.CONFIRMED -> "Đã xác nhận"
            OrderStatus.PREPARING -> "Đang chuẩn bị"
            OrderStatus.READY -> "Sẵn sàng"
            OrderStatus.COMPLETED -> "Hoàn thành"
            OrderStatus.CANCELLED -> "Đã hủy"
        }
        holder.tvStatus.text = statusText

        holder.btnUpdate.setOnClickListener { onUpdateClick(order) }
    }

    override fun getItemCount() = orders.size
}