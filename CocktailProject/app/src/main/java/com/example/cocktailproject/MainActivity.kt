package com.example.cocktailproject

import android.content.Intent
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
        //add data
        manage_cocktail()
        //register click event
        adapter.itemClickListener=object : CtAdapter.OnItemClickListener{
            override fun OnItemClick(
                holder: CtAdapter.CtViewHolder,
                view: View,
                data: Cocktail,
                position: Int
            ) {
                Toast.makeText(this@MainActivity,data.toString(),Toast.LENGTH_SHORT).show()
                // Detail activity로 이동.
                val intent=Intent(this@MainActivity,DetailActivity::class.java)
                intent.putExtra("selectedCocktail",data)
                startActivity(intent)
            }

        }
    }

    //adapter에 아이템 추가
    private fun manage_cocktail() {
        //TODO("data 추가")
        for (i in 1..30)
            adapter.items.add(Cocktail("img1",R.drawable.cocktail_img2))
    }
}