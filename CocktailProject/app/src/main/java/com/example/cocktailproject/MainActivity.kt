package com.example.cocktailproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cocktailproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    lateinit var adapter: CtAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // https://developer.android.com/topic/libraries/view-binding : kotlin 1.4.20부터 findviewbyid용으로 썼던 android-kotlin-extensions 대신 view binding으로 사용됨
        binding= ActivityMainBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)
        show_cocktail_list()
    }

    private fun show_cocktail_list() {
        binding.ctRecyclerView.layoutManager=GridLayoutManager(this,2) //spancount : 열 개수
        //adapter 생성
        adapter= CtAdapter(ArrayList<Cocktail>())
        binding.ctRecyclerView.adapter=adapter
        for (i in 1..30)
            adapter.items.add(Cocktail("img1",R.drawable.cocktail_img2))

        adapter.itemClickListener=object : CtAdapter.OnItemClickListener{
            override fun OnItemClick(
                holder: CtAdapter.CtViewHolder,
                view: View,
                data: Cocktail,
                position: Int
            ) {
                Toast.makeText(this@MainActivity,data.toString(),Toast.LENGTH_SHORT).show()
                // Detail activity로 이동.
            }

        }
    }
}