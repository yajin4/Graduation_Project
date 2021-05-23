package com.example.cocktailproject

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CtAdapter(var items:ArrayList<Cocktail>)
    :RecyclerView.Adapter<CtAdapter.CtViewHolder>()
{

    interface OnItemClickListener{
        fun OnItemClick(holder: CtViewHolder, view:View, data:Cocktail, position: Int)
    }
    var itemClickListener:OnItemClickListener?=null

    inner class CtViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var imgBtn: Button=itemView.findViewById(R.id.ct_img_btn)
        //var imageView: ImageView =itemView.findViewById(R.id.imageView)
        var textView: TextView = itemView.findViewById(R.id.ct_name)

        init {
            itemView.setOnClickListener {
                itemClickListener?.OnItemClick(this, it, items[adapterPosition], adapterPosition)
            }
        }
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CtViewHolder {
        // Create a new view, which defines the UI of the list item
        val v=LayoutInflater.from(parent.context).inflate(R.layout.ctlist_instance, parent,false)
        return CtViewHolder(v)
    }
    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: CtViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        holder.imgBtn.setBackgroundResource(items[position].ctPhoto)
        //holder.imageView.setImageResource(items[position].ctPhoto)
        holder.textView.text=items[position].ctName
    }
    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return items.size
    }
}