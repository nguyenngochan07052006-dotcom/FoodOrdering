package com.example.foodordering.admin

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

class AdminOrderAdapter(
    private val orders: List<Order>,
    private val onNextStatus: (Order) -> Unit,
    private val onCancel: (Order) -> Unit
) : RecyclerView.Adapter<AdminOrderAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderCode: TextView = itemView.findViewById(R.id.tvOrderCode)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        val tvOrderTotal: TextView = itemView.findViewById(R.id.tvOrderTotal)
        val btnNextStatus: MaterialButton = itemView.findViewById(R.id.btnNextStatus)
        val btnCancel: MaterialButton = itemView.findViewById(R.id.btnCancel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        holder.tvOrderCode.text = "Mã đơn: ${order.orderCode}"
        holder.tvOrderDate.text = order.createdAt
        holder.tvOrderTotal.text = formatter.format(order.total)

        // Hiển thị trạng thái
        when (order.status.lowercase()) {
            "pending", "paid" -> {
                holder.tvOrderStatus.text = "Chờ xác nhận"
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#FFF3E0"))
                holder.tvOrderStatus.setTextColor(Color.parseColor("#E65100"))
                holder.btnNextStatus.text = "Xác nhận"
                holder.btnNextStatus.visibility = View.VISIBLE
                holder.btnCancel.visibility = View.VISIBLE
            }
            "confirmed" -> {
                holder.tvOrderStatus.text = "Đã xác nhận"
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#E3F2FD"))
                holder.tvOrderStatus.setTextColor(Color.parseColor("#1565C0"))
                holder.btnNextStatus.text = "Bắt đầu pha chế"
                holder.btnNextStatus.visibility = View.VISIBLE
                holder.btnCancel.visibility = View.VISIBLE
            }
            "preparing" -> {
                holder.tvOrderStatus.text = "Đang pha chế"
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#FFF8E1"))
                holder.tvOrderStatus.setTextColor(Color.parseColor("#F9A825"))
                holder.btnNextStatus.text = "Sẵn sàng"
                holder.btnNextStatus.visibility = View.VISIBLE
                holder.btnCancel.visibility = View.GONE
            }
            "ready" -> {
                holder.tvOrderStatus.text = "Sẵn sàng"
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#E8F5E9"))
                holder.tvOrderStatus.setTextColor(Color.parseColor("#2E7D32"))
                holder.btnNextStatus.text = "Hoàn thành"
                holder.btnNextStatus.visibility = View.VISIBLE
                holder.btnCancel.visibility = View.GONE
            }
            "completed" -> {
                holder.tvOrderStatus.text = "Hoàn thành"
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#E8F5E9"))
                holder.tvOrderStatus.setTextColor(Color.parseColor("#1B5E20"))
                holder.btnNextStatus.visibility = View.GONE
                holder.btnCancel.visibility = View.GONE
            }
            "cancelled" -> {
                holder.tvOrderStatus.text = "Đã hủy"
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#FFEBEE"))
                holder.tvOrderStatus.setTextColor(Color.parseColor("#C62828"))
                holder.btnNextStatus.visibility = View.GONE
                holder.btnCancel.visibility = View.GONE
            }
            else -> {
                holder.tvOrderStatus.text = order.status
                holder.btnNextStatus.visibility = View.GONE
                holder.btnCancel.visibility = View.GONE
            }
        }

        holder.btnNextStatus.setOnClickListener {
            onNextStatus(order)
        }

        holder.btnCancel.setOnClickListener {
            onCancel(order)
        }
    }

    override fun getItemCount() = orders.size
}