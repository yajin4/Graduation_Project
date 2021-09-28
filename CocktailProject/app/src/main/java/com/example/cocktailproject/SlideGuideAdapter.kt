package com.example.cocktailproject

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView




class SlideGuideAdapter(var guideImgList : ArrayList<String>) : RecyclerView.Adapter<SlideGuideAdapter.SlideGuideViewHolder>() {

    private lateinit var cxt: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideGuideViewHolder {
        cxt=parent.context
        return SlideGuideViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.guide_instance,parent,false))
    }

    override fun onBindViewHolder(holder: SlideGuideViewHolder, position: Int) {
        holder.bindGuideImg(guideImgList[position])
    }

    override fun getItemCount(): Int {
        return guideImgList.size
    }



    inner class SlideGuideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var gImg: ImageView = itemView.findViewById(R.id.guideImg)

        fun bindGuideImg(assetLoc:String){
            val bmp = BitmapFactory.decodeStream(cxt.assets.open(assetLoc))
            gImg.setImageBitmap(bmp)
        }
    }

}
