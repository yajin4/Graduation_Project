package com.example.cocktailproject

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cocktailproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var adapter: CtAdapter
    private val cocktailNum=20
    private var recyclerViewState: Parcelable? = null
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
                saveRecyclerViewState()
                // Detail activity로 이동.
                val intent=Intent(this@MainActivity,DetailActivity::class.java)
                intent.putExtra("selectedCocktail",data)
                startActivity(intent)
                //finish()
            }

        }

        binding.ctRecyclerView.adapter=adapter
    }

    //adapter에 아이템 추가
    private fun manage_cocktail() {
        //TODO("data 추가")
        //assets string array이용
        //각 cocktail 정보 array name을 c1,c2 .. 식으로 저장해놓음.
        for (j in 1 .. cocktailNum) { //저장해놓은 cocktail 개수
            val rname= "c$j"
            val carr = resources.getStringArray(resources.getIdentifier(rname,"array",this.packageName))
            val tempCocktailDetail=ArrayList<CocktailDetail>()
            // 0번째는 cocktail name, 1번째는 photo경로, 그 다음부터 3개씩 Details 정보들
            for (i in 2 until carr.size step 3){
                tempCocktailDetail.add(CocktailDetail(carr[i],carr[i+1],carr[i+2].toDouble()))
            }
            adapter.items.add(Cocktail(carr[0], carr[1], tempCocktailDetail))
        }

    }

    private fun saveRecyclerViewState(){
        // layoutmanager 이용하여 현재 상태를 저장
        recyclerViewState = binding.ctRecyclerView.layoutManager!!.onSaveInstanceState()
    }

    private fun setRecyclerViewState(){
        binding.ctRecyclerView.layoutManager!!.onRestoreInstanceState(recyclerViewState)
    }

    override fun onResume() {
        super.onResume()
        if(recyclerViewState!=null){
            setRecyclerViewState()
        }
    }
}