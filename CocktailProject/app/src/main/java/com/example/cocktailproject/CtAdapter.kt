package com.example.cocktailproject

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
        var imgBtn: ImageView=itemView.findViewById(R.id.ct_img_btn)
        //var imageView: ImageView =itemView.findViewById(R.id.imageView)
        var nameTextView: TextView = itemView.findViewById(R.id.ct_name)

        init {
            itemView.setOnClickListener {
                itemClickListener?.OnItemClick(this, it, items[adapterPosition], adapterPosition)
            }
            imgBtn.setOnClickListener {
                itemClickListener?.OnItemClick(this, it, items[adapterPosition], adapterPosition)
            }
            nameTextView.setOnClickListener {
                itemClickListener?.OnItemClick(this, it, items[adapterPosition], adapterPosition)
            }
        }
    }

    private lateinit var cxt:Context

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CtViewHolder {
        // Create a new view, which defines the UI of the list item
        cxt=parent.context
        val v=LayoutInflater.from(parent.context).inflate(R.layout.ctlist_instance, parent,false)
        return CtViewHolder(v)
    }
    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: CtViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val cBmp=BitmapFactory.decodeStream(cxt.assets.open(items[position].ctPhoto))
        holder.imgBtn.setImageBitmap(cBmp)
        holder.nameTextView.text=items[position].ctName
    }
    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return items.size
    }
}