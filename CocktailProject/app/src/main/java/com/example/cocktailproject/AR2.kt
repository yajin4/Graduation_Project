package com.example.cocktailproject

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.cocktailproject.databinding.ActivityAr2Binding
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


//permission은 라이브러리에 내장되어있음.
class AR2 : AppCompatActivity() {

    private lateinit var binding: ActivityAr2Binding
    private lateinit var camera:CameraView
    private lateinit var selectedCocktail:Cocktail
    private lateinit var selectedCocktailDetail: ArrayList<CocktailDetail>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityAr2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        //intent 정보 받기
        selectedCocktail= intent.getSerializableExtra("selectedCocktail") as Cocktail
        selectedCocktailDetail= intent.getSerializableExtra("selectedCocktailDetail") as ArrayList<CocktailDetail>

        //서버 연결
        // connectServer()

        //카메라 기능 초기설정
        cameraInit()

        //btn click event등록
        btnInit()
    }

    //TODO: cameraOrientation확인

    private fun btnInit() {
        binding.arBackBtn.setOnClickListener {
            val i=Intent(this@AR2,DetailActivity::class.java)
            i.putExtra("selectedCocktail",selectedCocktail)
            startActivity(i)
            finish()
        }
        
        binding.nextLineBtn.setOnClickListener { 
            //TODO : 다음 한계선 출력 구현
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val i=Intent(this@AR2,DetailActivity::class.java)
        i.putExtra("selectedCocktail",selectedCocktail)
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
        finish()

    }

    private fun cameraInit() {
        //camera component
        camera=binding.camera
        //LifeCycleOwner 이 액티비티로 연결해줘야함.
        camera.setLifecycleOwner(this)
        //사진 찍은거 event listener
        camera.addCameraListener(object:CameraListener(){
            override fun onPictureTaken(result: PictureResult) {
                //Toast.makeText(applicationContext,"take snapshot "+result.format.toString(),Toast.LENGTH_SHORT).show() //JPEG
                result.toBitmap(513,513){
                    //513으로 변환해서 input주기
                    //binding.sample.setImageBitmap(it)
                    if (it != null) {
                        //imageProcess(it)
                        var tempfile=File(filesDir.toString()+"current.jpg")
                        val os= FileOutputStream(tempfile)
                        it.compress(Bitmap.CompressFormat.JPEG,100,os)
                        os.flush()
                        os.close()
                        connectServer(tempfile)
                    }
                }

//                var file=File(filesDir.toString()+"current.jpg")
//                result.toFile(file){
//                    if (it!=null){
//                        connectServer(it)
//                        Log.i("connect tag","connected!!!!"+file.isFile.toString())
//                    }
//                }
            }
        })
        //2초마다 한번씩 불리게 쓰레드 반복실행
        val mainHandler= Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable{
            override fun run() {
                //더 작은 용량의 snapshot으로 운영 TODO:잘 안될 경우 바꾸기
                camera.takePictureSnapshot()
                //camera.takePicture()
                mainHandler.postDelayed(this,10000)
            }
        })

    }

    //process image DeepLab TensorflowLite Model
    private fun imageProcess(result: Bitmap) {
        //predictor 객체 생성
        val predictor=Predictor(this)
        //runmodel : masked bitmap반환
        val maskedBitmap=predictor.runModel(result)
        //set ovelay image
        binding.sample.setImageBitmap(maskedBitmap)
        binding.overlayimage.setImageBitmap(maskedBitmap)
    }

    // connect server
    private fun connectServer(currentShot:File) {
        val ipv4Address="118.223.16.156"
        val portNum="5000"
        val postUrl = "http://"+ipv4Address+":"+portNum+"/"

        // bitmap arg로 받기
        val postBodyText="hello"
        //val requestBody=postBodyText.toRequestBody()
        //experiment
        val requestBody=currentShot.asRequestBody("image/*".toMediaTypeOrNull())
        val postBodyImage=MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image","inf.jpg",requestBody)
            .build()
        postRequest(postUrl, postBodyImage)

    }

    private fun postRequest(postUrl: String, requestBody: RequestBody) {
        val client= OkHttpClient()

        val request= Request.Builder().url(postUrl).post(requestBody).build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                //cancel the post on failure
                call.cancel()
                Log.i("connect tag","failed!!!!"+e.toString())
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                // ui thread == main thread
//                runOnUiThread {
//                    val responseText = findViewById<TextView>(R.id.responseText)
//                    responseText.text = "Failed to Connect to Server"
//                }
            }

            override fun onResponse(call: Call, response: Response) {
                // TODO("Not yet implemented")
                Log.i("connect tag","success!"+ response.body?.string())
            }
        })
    }
}