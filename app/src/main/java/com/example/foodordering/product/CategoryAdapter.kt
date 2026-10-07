package com.example.foodordering.product

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodordering.R
import com.example.foodordering.model.Category
import com.google.android.material.card.MaterialCardView

class CategoryAdapter(
    private val categories: List<Category>,
    private var selectedId: Int = -1,
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.cardCategory)
        val tvName: TextView = itemView.findViewById(R.id.tvCategoryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.tvName.text = category.name

        if (category.id == selectedId) {
            holder.card.setCardBackgroundColor(Color.parseColor("#6D4C41"))
            holder.tvName.setTextColor(Color.WHITE)
        } else {
            holder.card.setCardBackgroundColor(Color.parseColor("#EFEBE9"))
            holder.tvName.setTextColor(Color.parseColor("#5D4037"))
        }

        holder.itemView.setOnClickListener {
            selectedId = category.id
            notifyDataSetChanged()
            onClick(category)
        }
    }

    override fun getItemCount() = categories.size

    fun setSelected(id: Int) {
        selectedId = id
        notifyDataSetChanged()
    }
}