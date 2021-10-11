package com.example.cocktailproject

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cocktailproject.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    lateinit var adapter: DtAdapter
    lateinit var selectedCocktail: Cocktail
    lateinit var selectedCocktailDetail: ArrayList<CocktailDetail>
    private lateinit var prefs:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //intent로 전달받은 selected cocktail확인
        selectedCocktail = intent.getSerializableExtra("selectedCocktail") as Cocktail

        binding = ActivityDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //넘어온 칵테일 이름, 사진 출력
        load_selected_cocktail()
        //detail data 출력
        show_detail()
        //btn event 설정
        btnInit()
        //액션바 설정
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=selectedCocktail.ctName

        // 처음 실행인지 검사
        prefs = getSharedPreferences("Pref", MODE_PRIVATE)
    }

    private fun btnInit() {
        //TODO("camera권한 확인 후 안되었으면 설정 창으로 되었으면 ar2 activity로 이동")
        binding.arBtn.setOnClickListener {
            checkCameraPerms()
        }
    }

    private fun load_selected_cocktail() {
        val cBmp= BitmapFactory.decodeStream(assets.open(selectedCocktail.ctPhoto))
        binding.ctImg.setImageBitmap(cBmp)
        binding.ctNameIndetail.text=selectedCocktail.ctName
    }

    private fun show_detail() {
        binding.cocktailIngLayout.layoutManager = LinearLayoutManager(this)
        //adapter 생성
        adapter = DtAdapter(ArrayList<CocktailDetail>())

        // recyclerview에 연결
        binding.cocktailIngLayout.adapter = adapter
        //data 추가
        load_selected_cocktail_detail()

        //click event
        adapter.itemClickListener = object : DtAdapter.OnItemClickListener {
            override fun OnItemClick(
                holder: DtAdapter.DtViewHolder,
                view: View,
                data: CocktailDetail,
                position: Int
            ) {
                //TODO("Not yet implemented")
                //Toast.makeText(this@DetailActivity, data.toString(), Toast.LENGTH_SHORT).show()
            }

        }


    }

    // adapter에 아이템 추가
    private fun load_selected_cocktail_detail() {
        selectedCocktailDetail=ArrayList()
        for (i in selectedCocktail.ctDetail.indices) {
            adapter.items.add(selectedCocktail.ctDetail[i])
            selectedCocktailDetail.add(selectedCocktail.ctDetail[i])
        }
    }

    private fun checkCameraPerms(){
        // Request camera permissions
        if (allPermissionsGranted()) {
            //permission 허용되어있을 시 카메라 시작
            if(checkFirstRun()){
                val i2 = Intent(this@DetailActivity, GuideActivity::class.java)
                i2.putExtra("selectedCocktail",selectedCocktail)
                i2.putExtra("isFromDetail",true)
                startActivity(i2)
                finish()
            }
            else{
                val i2 = Intent(this@DetailActivity, MakingActivity::class.java)
                i2.putExtra("selectedCocktail",selectedCocktail)
                startActivity(i2)
                finish()
            }
        } else {
            //권한 승인 요청
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            ) //activity, permission string list, int requestCode(내가 지정가능)
        }

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        // REQUIRED_PERMISSIONS의 모든 원소들(it)이 아래 식에서 true가 되면 true반환
        //ActivityCompat.checkSelfPermission(this(context), Manifest.permission.Camera) : 권한설정되어있을 시  PackageManager.PERMISSION_GRANTED
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    //사용자의 퍼미션 허용이 끝나면 자동 호출되는 함수. 허용 결과 확인
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) { //내가 지정한 code (camera에 지정해놓은 requestcode)
            if (allPermissionsGranted()) { //동의 했는지 확인
                //처음 어플 실행 시에만 가이드 창으로 이동.
                if(checkFirstRun()){
                    val i2 = Intent(this@DetailActivity, GuideActivity::class.java)
                    i2.putExtra("selectedCocktail",selectedCocktail)
                    i2.putExtra("isFromDetail",true)
                    startActivity(i2)
                    finish()
                }
                else{
                    val i2 = Intent(this@DetailActivity, MakingActivity::class.java)
                    i2.putExtra("selectedCocktail",selectedCocktail)
                    startActivity(i2)
                    finish()
                }
            } else {
//                    "Camera Permissions not granted by the user.",
                //퍼미션 거절 시 설정화면으로 이동
                val i2 = Intent(this@DetailActivity, CameraPermission::class.java)
                i2.putExtra("selectedCocktail",selectedCocktail)
                startActivity(i2)
                finish()
            }
        }
    }

    private fun checkFirstRun(): Boolean {
        val isFirstRun = prefs.getBoolean("isFirstRun",true);
        return if (isFirstRun){
            prefs.edit().putBoolean("isFirstRun",false).apply()
            true
        } else{
            false
        }
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                //val intent= Intent(this@DetailActivity, MainActivity::class.java)
                //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                //startActivity(intent)
                //finish()
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}