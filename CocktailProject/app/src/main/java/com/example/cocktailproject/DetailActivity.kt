package com.example.cocktailproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cocktailproject.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    lateinit var adapter: DtAdapter
    lateinit var selectedCocktail: Cocktail
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //intent로 전달받은 selected cocktail확인
        selectedCocktail=Intent().getSerializableExtra("selectedCocktail") as Cocktail
        //TODO("해당 cocktail detail가져오기")

        binding = ActivityDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        show_detail()
    }

    private fun show_detail() {
        binding.cocktailIngLayout.layoutManager = LinearLayoutManager(this)
        //adapter 생성
        adapter = DtAdapter(ArrayList<CocktailDetail>())

        // recyclerview에 연결
        binding.cocktailIngLayout.adapter = adapter
        //data 추가
        manage_cocktail_detail()

        //click event
        adapter.itemClickListener = object : DtAdapter.OnItemClickListener {
            override fun OnItemClick(
                holder: DtAdapter.DtViewHolder,
                view: View,
                data: CocktailDetail,
                position: Int
            ) {
                //TODO("Not yet implemented")
                Toast.makeText(this@DetailActivity, data.toString(), Toast.LENGTH_SHORT).show()
            }

        }


    }

    // adapter에 아이템 추가
    private fun manage_cocktail_detail() {
        //TODO("data 추가")
        for (i in 1..30)
            adapter.items.add(CocktailDetail( selectedCocktail.ctName, R.drawable.cocktail_img2, 50.3))
    }
}