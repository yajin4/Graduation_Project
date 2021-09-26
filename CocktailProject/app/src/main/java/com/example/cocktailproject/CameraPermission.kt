package com.example.cocktailproject

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cocktailproject.databinding.ActivityCameraPermissionBinding

class CameraPermission : AppCompatActivity() {

    lateinit var binding:ActivityCameraPermissionBinding
    private lateinit var selectedCocktail:Cocktail
    private lateinit var selectedCocktailDetail: List<CocktailDetail>
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityCameraPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //intent 받기
        selectedCocktail= intent.getSerializableExtra("selectedCocktail") as Cocktail
        selectedCocktailDetail= selectedCocktail.ctDetail
        //btn event설정
        btnInit()
        //권한 요청
        //checkCameraPerms()
        //액션바 설정
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //
        prefs = getSharedPreferences("Pref", MODE_PRIVATE)
    }

    private fun btnInit() {

        binding.requestPermsBtn.setOnClickListener {
            checkCameraPerms()

        }
    }
    //기본뒤로가기클릭
    override fun onBackPressed() {
        super.onBackPressed()
        val i=Intent(this@CameraPermission,DetailActivity::class.java)
        i.putExtra("selectedCocktail",selectedCocktail)
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
        finish()
    }

    private fun checkCameraPerms(){
        // Request camera permissions
        if (allPermissionsGranted()) {
            //permission 허용되어있을 시 카메라 시작
            //처음 어플 실행 시에만 가이드 창으로 이동.
            if(checkFirstRun()){
                val i2 = Intent(this@CameraPermission, GuideActivity::class.java)
                i2.putExtra("selectedCocktail",selectedCocktail)
                i2.putExtra("isFromDetail",true)
                startActivity(i2)
                finish()
            }
            else{
                val i2 = Intent(this@CameraPermission, MakingActivity::class.java)
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
                if(checkFirstRun()){
                    val i2 = Intent(this@CameraPermission, GuideActivity::class.java)
                    i2.putExtra("selectedCocktail",selectedCocktail)
                    i2.putExtra("isFromDetail",true)
                    startActivity(i2)
                    finish()
                }
                else{
                    val i2 = Intent(this@CameraPermission, MakingActivity::class.java)
                    i2.putExtra("selectedCocktail",selectedCocktail)
                    startActivity(i2)
                    finish()
                }
            } else {

                //api30(안드11)부터 퍼미션 거절 시 다시 묻지 않음 상태가 되어 사용자가 직접해야됨
                val i2 = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+packageName))
                i2.addCategory(Intent.CATEGORY_DEFAULT)
                i2.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(i2)

            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->{
                val i=Intent(this@CameraPermission,DetailActivity::class.java)
                i.putExtra("selectedCocktail",selectedCocktail)
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(i)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
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
}