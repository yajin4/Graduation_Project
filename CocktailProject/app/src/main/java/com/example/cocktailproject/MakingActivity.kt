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
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.cocktailproject.databinding.ActivityAr2Binding
import com.example.cocktailproject.dialogFragments.MakingGuideDialogFragment
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


//permission은 라이브러리에 내장되어있음.
class MakingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAr2Binding //ui component 접근
    private lateinit var camera:CameraView // 
    private lateinit var selectedCocktail:Cocktail //현재 선택된 cocktail
    private lateinit var selectedCocktailDetail: ArrayList<CocktailDetail> // 선택된 cocktail의 재료 정보

    private var ingIndex = 0 // 현재 넣어야하는 재료 index (selectedCocktailDetail의 index)
    private var ingTotal = 0.0 // 비율 계산용으로 전체 비율의 합
    private lateinit var ingBool:BooleanArray // 각 detail 재료들이 채워졌는지 여부 값 저장
    private lateinit var ingSum:DoubleArray // 각 재료의 누적비율(ratio 전달용)을 index로 접근할 수 있도록 선언

    private val classNum=6 // 사용하는 color 배열 크기
    private val color=IntArray(classNum) // color 값 저장

    private var colorIndex=3 //한계선 색 3:일반 4:good 5:over

    //segment 저장
    private lateinit var arr:ArrayList<ArrayList<Int>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityAr2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        //intent 정보 받기
        selectedCocktail= intent.getSerializableExtra("selectedCocktail") as Cocktail
        selectedCocktailDetail= intent.getSerializableExtra("selectedCocktailDetail") as ArrayList<CocktailDetail>
        //액션바 설정
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=selectedCocktail.ctName

        init()
    }

    private fun init(){
        //재료 관련 변수 초기화
        //ingSize = selectedCocktailDetail.size //총 재료 개수
        ingTotal = selectedCocktail.ctDetail.sumOf { it.Ing_amount } // 총 재료 양
        ingSum= DoubleArray(selectedCocktailDetail.size){0.0}
        var ratioSum=0.0
        for(i in 0 until selectedCocktailDetail.size){
            ingSum[i]=ratioSum+ selectedCocktailDetail[i].Ing_amount/ ingTotal
            ratioSum += selectedCocktailDetail[i].Ing_amount/ ingTotal
        }
        ingBool = BooleanArray(selectedCocktailDetail.size){false}
        
        // TODO : 안내문 dialog를 activity등으로 변경할 지 고민..
        val newFragment = ARGuideDialogFragment()
        newFragment.show(supportFragmentManager,"guide fragment show")

        //카메라 기능 초기설정
        cameraInit()

        //btn click event등록
        btnInit()

        // color값 초기화
        color[0]= Color.TRANSPARENT
        // alpha : 128 == 반투명 / 1=cup 2=fluid 3=한계선(기본) 4=한계선(통과) 5=한계선(초과)
        color[1]=Color.argb(128,128,128,128)
        color[2]=Color.argb(128,0,255,0)
        color[3]=Color.argb(255,0,0,0) //required fluid line
        color[4]=Color.argb(255,0,0,255)
        color[5]=Color.argb(255,255,0,0)

        binding.instruction.text = selectedCocktailDetail[ingIndex].Ing_name

    }

    //TODO: cameraOrientation확인

    private fun btnInit() {
        binding.nextLineBtn.setOnClickListener { 
            //TODO : 다음 한계선 출력 구현 (이전 단계 이동도 있으면 좋을듯)
            if(ingIndex == ingBool.size-1){ //끝
                binding.status.text = "칵테일이 완성되었습니다!"
            }
            else {
                if (ingBool[ingIndex]) {
                    ingIndex += 1
                    binding.status.text = "다음 단계로 진행합니다."
                    binding.instruction.text = selectedCocktailDetail[ingIndex].Ing_name
                    colorIndex = 3
                } else {
                    binding.status.text = "아직 적절한 양이 아닙니다."
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val i=Intent(this@AR2,DetailActivity::class.java)
        i.putExtra("selectedCocktail",selectedCocktail)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
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

                /*result.toBitmap(513,513){
                    val bmpFile = File(filesDir.toString()+"currentTemp.jpg")
                    lateinit var out:OutputStream
                    try {
                        bmpFile.createNewFile() //file 생성
                        out = FileOutputStream(bmpFile) //outputStream 생성
                        it?.compress(Bitmap.CompressFormat.JPEG,100,out) //file에 bitmap 저장
                    }catch (e:Exception){
                        e.printStackTrace()
                    }finally {
                        try {
                            out.close()
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }

                    connectServer(bmpFile)
                }*/
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
                mainHandler.postDelayed(this,1500)

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
        val ipv4Address="118.223.16.156"
        val portNum="8081"
        val postUrl = "http://$ipv4Address:$portNum/"

        // request msg 생성. 파일을 첨부한 body를 생성.
        val uniqueID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val requestBody=currentShot.asRequestBody("image/*".toMediaTypeOrNull())
        val postBodyImage=MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "inf$uniqueID.jpg",requestBody)
            .addFormDataPart("ratio",(ingSum[ingIndex]*100).toInt().toString())
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
                    binding.status.text = "서버 연결 실패"
                    binding.overlayimage.setImageResource(android.R.color.transparent) //clear overlay image
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Log.i("connect tag","success! during time(ms) : "+(response.receivedResponseAtMillis-response.sentRequestAtMillis))

                //response의 segmap key의 2차원 배열 값을 arr에 저장함
                try{
                    val json=JSONObject(response.body!!.string())
                    val isSuccess=json.getString("success")
                    val returnMsg=json.getString("msg")
                    runOnUiThread {
                        binding.status.text = returnMsg
                        binding.overlayimage.setImageResource(android.R.color.transparent) // clear overlay
                    }
                    if (isSuccess=="false") // label 후처리 실패
                        return
                    val jsonArr=json.getJSONArray("segmap")
                    // 2차원 배열 저장할 변수
                    arr = ArrayList<ArrayList<Int>>()

                    // call you method from here or add any other statements
                    for (i in 0 until jsonArr.length()){
                        arr.add(ArrayList())

                        for (j in 0 until jsonArr.getJSONArray(i).length()){
                            try {
                                arr[i].add(jsonArr.getJSONArray(i).getInt(j))
                            }
                            catch(e:Exception){
                                Log.i("data i error",i.toString())
                                Log.i("data j error",j.toString())
                            }
                        }
                    }


                    // ratio 조사
                    val ratio = json.getString("ratio")
                    val ratioMsg = json.getString("ratioMsg")
                    val ratioStatus = json.getString("ratioStatus")
                    runOnUiThread {
                        binding.status.text = ratioMsg
                    }
                    if (ratio == "true") {
                        ingBool[ingIndex] = true
                        if (ingIndex == ingBool.size-1){ //끝 //TODO: 마지막 단계에서 초과시..?
                            binding.status.text = "칵테일이 완성되었습니다!"
                        }
                        when(ratioStatus){
                            "good" -> {
                                colorIndex=4 //good
                            }
                            "over"->{
                                colorIndex=5 //over
                            }
                        }
                    }
                    else{
                        colorIndex=3
                        when(ratioStatus){
                            "no" -> {

                            }
                            "under"->{

                            }
                        }
                    }

                    printSegmap()
                }
                catch (e:Exception){
                    e.printStackTrace()
                    runOnUiThread {
                        binding.status.text = "오류 발생"
                    }
                }

            }
        })
    }

    private fun printSegmap() {
        // seg bitmap 만들기 위해 각 class값에 맞는 color를 pixel 배열에 저장 (1차원 배열 버전). pixel color값으로 bitmap을 생성하고 화면에 출력 
        val width=arr[0].size
        val height=arr.size
        val pixels=IntArray(width * height)
        for (i in 0 until height){
            for (j in 0 until width){
                if (arr[i][j] == 3){ //한계선
                    // 초과 // 통과 // 일반
                    pixels[i*(height)+j]=color[colorIndex]
                }
                else
                    pixels[i*(height)+j]=color[arr[i][j]]
            }
        }
        val maskBitmap = Bitmap.createBitmap(
            pixels, width, height,
            Bitmap.Config.ARGB_8888
        )
        val scaledBitmap=Bitmap.createScaledBitmap(maskBitmap, width, height, true)
        val scaledBitmap=Bitmap.createScaledBitmap(maskBitmap, WIDTH, HEIGHT, true)//val scaledBitmap=Bitmap.createScaledBitmap(maskBitmap, width, height, true)
        // CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
        // In order to access the TextView,ImageVuew(etc..) inside the UI thread, the code is executed inside runOnUiThread()
        runOnUiThread {
            binding.overlayimage.setImageBitmap(scaledBitmap)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                val i=Intent(this@AR2,DetailActivity::class.java)
                i.putExtra("selectedCocktail",selectedCocktail)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(i)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}