package com.example.cocktailproject

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.cocktailproject.databinding.ActivityGuideBinding

class GuideActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGuideBinding
    private lateinit var pager:ViewPager2
    private lateinit var layoutIndicator:LinearLayout
    private lateinit var continueBtn:Button
    private var isFromDetail:Boolean = false
    private lateinit var gArr:Array<String>

    private lateinit var selectedCocktail: Cocktail
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pager = binding.pager
        layoutIndicator = binding.layoutIndicator
        continueBtn = binding.continueBtn
        continueBtn.setOnClickListener{
            val i= Intent(this@GuideActivity, MakingActivity::class.java)
            i.putExtra("selectedCocktail",selectedCocktail)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
            finish()
        }

        selectedCocktail = intent.getSerializableExtra("selectedCocktail") as Cocktail
        isFromDetail = intent.getBooleanExtra("isFromDetail", false)

        initGuideArr()
        initPager()
        initIndicator()
    }

    private fun initGuideArr() {
        gArr=resources.getStringArray(resources.getIdentifier("guideArr","array",this.packageName))
    }

    private fun initPager() {
        //adapter 적용될 img list data 생성
        val l = ArrayList<String>()
        l.add("guideImage/goodPos.jpg")
        l.add("guideImage/cupStandard.jpg")
        l.add("guideImage/noLineBack.jpg")
        l.add("guideImage/dontMove1.jpg")
        l.add("guideImage/noLogo.jpg")
        l.add("guideImage/waterDrop.jpg")
        l.add("guideImage/noClose.jpg")
        l.add("guideImage/noPet.jpg")
        l.add("guideImage/lightProblem.jpg")
        //pager 설정

        val pagerAdapter = SlideGuideAdapter(l)
        pager.adapter = pagerAdapter

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
                // 각 화면마다의 guide 출력
                binding.gTxt.text = gArr[position]
                // 마지막 페이지 continue 버튼 보이게 설정
                if(position==l.size-1){
                    continueBtn.visibility=Button.VISIBLE
                }
                else{
                    continueBtn.visibility=Button.INVISIBLE
                }
            }
        })
    }

    private fun initIndicator(){
        val indicators=ArrayList<ImageView>()

        val params=LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(10,8,10,8)

        for (i in 0 until GUIDE_NUM){
            indicators.add(ImageView(this))
            indicators[i].setImageDrawable(this.getDrawable(R.drawable.indicator_off))
            indicators[i].layoutParams=params
            layoutIndicator.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(position:Int){
        for (i in 0 until layoutIndicator.childCount){
            val imageView:ImageView= layoutIndicator.getChildAt(i) as ImageView
            if(i == position){
                imageView.setImageDrawable(this.getDrawable(R.drawable.indicator_on))
            }
            else{
                imageView.setImageDrawable(this.getDrawable(R.drawable.indicator_off))
            }
        }
    }

    override fun onBackPressed() {
        // super.onBackPressed()
        if(pager.currentItem == 0){
            if (isFromDetail){
                val i= Intent(this@GuideActivity, DetailActivity::class.java)
                i.putExtra("selectedCocktail",selectedCocktail)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(i)
                finish()
            }
            else{
                val i= Intent(this@GuideActivity, MakingActivity::class.java)
                i.putExtra("selectedCocktail",selectedCocktail)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(i)
                finish()
            }
        }
        else{
            pager.currentItem-=1
        }
    }

    companion object {
        private const val GUIDE_NUM = 9
    }
}



