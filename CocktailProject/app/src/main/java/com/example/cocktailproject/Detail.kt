package com.example.cocktailproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cocktailproject.databinding.ActivityDetailBinding

class Detail : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding= ActivityDetailBinding.inflate(layoutInflater)
        val view= binding.root
        setContentView(view)
        show_detail()
    }

    private fun show_detail() {
        binding.cocktailIngLayout.layoutManager=LinearLayoutManager(this)
        //adapter 생성
    }
}