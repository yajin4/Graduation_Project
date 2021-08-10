package com.example.cocktailproject

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cocktailproject.databinding.ActivityAr2Binding
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


//permission은 라이브러리에 내장되어있음.
class AR2 : AppCompatActivity() {

    private lateinit var binding: ActivityAr2Binding
    private lateinit var camera:CameraView
    private lateinit var selectedCocktail:Cocktail
    private lateinit var selectedCocktailDetail: ArrayList<CocktailDetail>
    private val classNum=4
    private val color=IntArray(classNum)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityAr2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        //intent 정보 받기
        selectedCocktail= intent.getSerializableExtra("selectedCocktail") as Cocktail
        selectedCocktailDetail= intent.getSerializableExtra("selectedCocktailDetail") as ArrayList<CocktailDetail>


        init()
    }

    private fun init(){
        //카메라 기능 초기설정
        cameraInit()

        //btn click event등록
        btnInit()

        // color값 초기화
        color[0]= Color.TRANSPARENT
        // alpha : 128 == 반투명 / 1=cup 2=fluid
        color[1]=Color.argb(128,Color.red(255),Color.blue(0),Color.green(0))
        color[2]=Color.argb(128,Color.red(0),Color.blue(255),Color.green(0))
        color[3]=Color.argb(255,Color.red(0),Color.blue(0),Color.green(0))
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
/*                result.toBitmap(513,513){
                    //513으로 변환해서 input주기
                    //binding.sample.setImageBitmap(it)
                    if (it != null) {
                        //imageProcess(it)
                        var tempfile=File(filesDir.toString()+"current.jpg")
                        val os= FileOutputStream(tempfile)
                        it.compress(Bitmap.CompressFormat.PNG,100,os)
                        os.flush()
                        os.close()
                        connectServer(tempfile)
                    }
                }*/

                var file=File(filesDir.toString()+"current.jpg")
                result.toFile(file){
                    if (it!=null){
                        connectServer(it)
                    }
                }
            }
        })
        //2초마다 한번씩 불리게 쓰레드 반복실행
        val mainHandler= Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable{
            override fun run() {
                //더 작은 용량의 snapshot으로 운영 TODO:잘 안될 경우 바꾸기
                camera.takePictureSnapshot()
                //camera.takePicture()
                mainHandler.postDelayed(this,3000)
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
        val portNum="8081"
        val postUrl = "http://"+ipv4Address+":"+portNum+"/"

        // file arg로 받기
        Log.i("unique Andoird ID",Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))
        val uniqueID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val requestBody=currentShot.asRequestBody("image/*".toMediaTypeOrNull())
        val postBodyImage=MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image","inf"+uniqueID+".jpg",requestBody)
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
                runOnUiThread {
                    Toast.makeText(this@AR2,"서버와의 연결에 실패하였습니다.",Toast.LENGTH_SHORT)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Log.i("connect tag","success!")
                //response의 segmap key의 2차원 배열 값을 arr에 저장함
                val json=JSONObject(response.body!!.string())
                val jsonArr=json.getJSONArray("segmap")
                // 2차원 배열 저장할 변수
                var arr = ArrayList<ArrayList<Int>>()

                for (i in 0 until jsonArr.length()){
                    arr.add(ArrayList())

                    for (j in 0 until jsonArr.getJSONArray(i).length()){
                        try {
                            arr[i].add(jsonArr.getJSONArray(i).getInt(j))
                        }
                        catch(e:Exception){
                            Log.i("connect tag i : ",i.toString())
                            Log.i("connect tag j : ",j.toString())
                        }
                    }
                }

                printSegmap(arr)
            }
        })
    }

    private fun printSegmap(arr: ArrayList<ArrayList<Int>>) {
        // seg bitmap 만들기 위한 color 저장 (2차원 배열 버전) > 2차원 배열은 bitmap createBitmap의 인자로 줄 수가 없어서 1차원으로 수정
//        val pixels=ArrayList<ArrayList<Int>>()
//        for (i in 0 until arr.size){
//            pixels.add(ArrayList())
//            for (j in 0 until arr[i].size){
//                pixels[i].add(color[arr[i][j]])
//            }
//        }
        // seg bitmap 만들기 위한 color 저장 (1차원 배열 버전)
        val width=arr[0].size
        val height=arr.size
        val pixels=IntArray(width * height)
        for (i in 0 until height){
            for (j in 0 until width){
                pixels[i*(height)+j]=color[arr[i][j]]
            }
        }
        val maskBitmap = Bitmap.createBitmap(
            pixels, width, height,
            Bitmap.Config.ARGB_8888
        )
        val scaledBitmap=Bitmap.createScaledBitmap(maskBitmap, width, height, true)
        // CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
        // In order to access the TextView,ImageVuew(etc..) inside the UI thread, the code is executed inside runOnUiThread()
        runOnUiThread {
            binding.sample.setImageBitmap(scaledBitmap)
            binding.overlayimage.setImageBitmap(scaledBitmap)
        }

    }
}