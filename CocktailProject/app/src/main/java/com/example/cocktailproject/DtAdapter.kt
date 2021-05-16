package com.example.cocktailproject

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class DtAdapter(var items:ArrayList<Detail>):
RecyclerView.Adapter<DtAdapter.DtViewHolder>(){

    inner class DtViewHolder(var itemView: View):RecyclerView.ViewHolder(itemView) {
        var ing_img:ImageView=itemView.findViewById(R.id.ing_img)
        var ing_name:TextView=itemView.findViewById(R.id.ing_name)
        var ing_amount:TextView=itemView.findViewById(R.id.ing_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DtViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: DtViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }
}