package com.example.cocktailproject

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.example.cocktailproject.databinding.ActivityIntroducingBinding

class IntroducingActivity : AppCompatActivity() {
    private lateinit var selectedCocktail: Cocktail
    private lateinit var binding: ActivityIntroducingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding= ActivityIntroducingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        selectedCocktail=intent.getSerializableExtra("selectedCocktail") as Cocktail

        init()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=selectedCocktail.ctName
    }

    private fun init() {
        loadSelectedCocktail()
        try {
            val introText = resources.getString(
                resources.getIdentifier(
                    selectedCocktail.ctName.replace(" ",""),
                    "string",
                    this.packageName
                )
            )
            binding.introTextView.text = introText
        } catch (e: Exception) {
            binding.introTextView.text = "hi"
        }
    }

    private fun loadSelectedCocktail() {
        val cBmp= BitmapFactory.decodeStream(assets.open(selectedCocktail.ctPhoto))
        binding.ctImgIntro.setImageBitmap(cBmp)
        binding.ctNameIntro.text=selectedCocktail.ctName
    }
    //뒤로가기 --> 상세정보화면으로 되돌아감
    override fun onBackPressed() {
        super.onBackPressed()
        /*val i = Intent(this@IntroducingActivity, DetailActivity::class.java)
        i.putExtra("selectedCocktail",selectedCocktail)
        startActivity(i)
        finish()*/
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}