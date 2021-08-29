package com.example.cocktailproject

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.cocktailproject.databinding.ActivityAr2Binding
import com.example.cocktailproject.dialogFragments.ARGuideDialogFragment
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.measureTimeMillis


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
        //안내문 dialog
        val newFragment = ARGuideDialogFragment()
        newFragment.show(supportFragmentManager,"guide fragment show")

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

        binding.instruction.text= "서버 연결 중입니다."
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
                //지정한 시간마다 캡처를 하여 file 형태로 변환하고 서버에 전송함
                val file=File(filesDir.toString()+"current.jpg")
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
                mainHandler.postDelayed(this,5000)

            }
        })

    }

    //process image DeepLab TensorflowLite Model
/*    private fun imageProcess(result: Bitmap) {
        //predictor 객체 생성
        val predictor=Predictor(this)
        //runmodel : masked bitmap반환
        val maskedBitmap=predictor.runModel(result)
        //set ovelay image
        binding.sample.setImageBitmap(maskedBitmap)
        binding.overlayimage.setImageBitmap(maskedBitmap)
    }*/

    // connect server
    @SuppressLint("HardwareIds")
    private fun connectServer(currentShot:File) {
        // 서버 주소와 port 지정
        val ipv4Address=""
        val portNum="8081"
        val postUrl = "http://$ipv4Address:$portNum/"

        // request msg 생성. 파일을 첨부한 body를 생성.
        val uniqueID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val requestBody=currentShot.asRequestBody("image/*".toMediaTypeOrNull())
        val postBodyImage=MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "inf$uniqueID.jpg",requestBody)
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
                    binding.instruction.text = "서버 연결 실패"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Log.i("connect tag","success!")
                runOnUiThread {
                    binding.instruction.text = "서버 연결 성공"
                }
                //response의 segmap key의 2차원 배열 값을 arr에 저장함
                try{
                    val json=JSONObject(response.body!!.string())
                    val isSuccess=json.getString("success")
                    val returnMsg=json.getString("msg")
                    runOnUiThread {
                        binding.instruction.text = returnMsg
                    }
                    if (isSuccess=="false"){
                        return
                    }
                    val jsonArr=json.getJSONArray("segmap")
                    // 2차원 배열 저장할 변수
                    var arr = ArrayList<ArrayList<Int>>()

                    val elapsedTime= measureTimeMillis {
                        // call you method from here or add any other statements
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
                    }
                    Log.i("elapsed arr Time",elapsedTime.toString())

                    val segtime= measureTimeMillis {
                        printSegmap(arr)
                    }
                    Log.i("elapsed seg Time",segtime.toString())

                }
                catch (e:Exception){
                    runOnUiThread {
                        binding.instruction.text = "서버에서 추론 중 오류 발생"
                    }
                }

            }
        })
    }

    private fun printSegmap(arr: ArrayList<ArrayList<Int>>) {
        // seg bitmap 만들기 위해 각 class값에 맞는 color를 pixel 배열에 저장 (1차원 배열 버전). pixel color값으로 bitmap을 생성하고 화면에 출력 
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