package com.example.cocktailproject

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class DtAdapter(var items:ArrayList<CocktailDetail>):
RecyclerView.Adapter<DtAdapter.DtViewHolder>(){

    interface OnItemClickListener{
        fun OnItemClick(holder: DtAdapter.DtViewHolder, view:View, data:CocktailDetail, position: Int)
    }

    var itemClickListener: OnItemClickListener?=null


    inner class DtViewHolder(var itemView: View):RecyclerView.ViewHolder(itemView) { // 상위 클래스 바로 참조 가능
        var ing_img:ImageView=itemView.findViewById(R.id.ing_img)
        var ing_name:TextView=itemView.findViewById(R.id.ing_name)
        var ing_amount:TextView=itemView.findViewById(R.id.ing_amount)

        init {
            //itemView눌리면 itemClickListener에 등록된 함수에 itemView 정보 줘서 호출
            itemView.setOnClickListener {
                itemClickListener?.OnItemClick(this,it,items[adapterPosition],adapterPosition)
            }
        }
    }

    private lateinit var cxt: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DtViewHolder {
        cxt=parent.context
        val v=LayoutInflater.from(parent.context).inflate(R.layout.dtlist_instance,parent,false)
        return DtViewHolder(v)
    }

    override fun onBindViewHolder(holder: DtViewHolder, position: Int) {
        val dBmp= BitmapFactory.decodeStream(cxt.assets.open(items[position].Ing_Photo))
        holder.ing_img.setImageBitmap(dBmp)
        holder.ing_name.text=items[position].Ing_name
        holder.ing_amount.text=items[position].Ing_amount.toString()
    }

    override fun getItemCount(): Int {
        return items.size
    }
}