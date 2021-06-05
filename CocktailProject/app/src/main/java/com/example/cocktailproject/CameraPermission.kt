package com.example.cocktailproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cocktailproject.databinding.ActivityCameraPermissionBinding

class CameraPermission : AppCompatActivity() {

    lateinit var binding:ActivityCameraPermissionBinding
    private lateinit var selectedCocktail:Cocktail
    private lateinit var selectedCocktailDetail: CocktailDetail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityCameraPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //intent 받기
        selectedCocktail= intent.getSerializableExtra("selectedCocktail") as Cocktail
        selectedCocktailDetail= intent.getSerializableExtra("selectedCocktailDetail") as CocktailDetail
        //btn event설정
        btnInit()
        //권한 요청
        //checkCameraPerms()

    }

    private fun btnInit() {
        binding.permissionBackBtn.setOnClickListener {
            val i=Intent(this@CameraPermission,DetailActivity::class.java)
            i.putExtra("selectedCocktail",selectedCocktail)
            startActivity(i)
            finish()
        }

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
            val i2 = Intent(this@CameraPermission, AR2::class.java)
            i2.putExtra("selectedCocktail",selectedCocktail)
            i2.putExtra("selectedCocktailDetail",selectedCocktailDetail)
            startActivity(i2)
            finish()
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
                val i2 = Intent(this@CameraPermission, AR2::class.java)
                i2.putExtra("selectedCocktail",selectedCocktail)
                i2.putExtra("selectedCocktailDetail",selectedCocktailDetail)
                startActivity(i2)
                finish()
            } else {

//                finish()
                //api30(안드11)부터 퍼미션 거절 시 다시 묻지 않음 상태가 되어 사용자가 직접해야됨
                                Toast.makeText(this,
                    "권한 대화상자가 표시되지 않을 시\n직접 권한을 설정해주세요.",
                    Toast.LENGTH_SHORT).show()

            }
        }
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    }
}