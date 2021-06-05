package com.example.cocktailproject

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.graphics.set
import com.example.cocktailproject.databinding.ActivityAr2Binding
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
//import com.example.cocktailproject.ml.LiteModelDeeplabv3Mobilenetv2Ade20k1Default2
import com.example.cocktailproject.ml.MyModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.nio.ByteBuffer
import kotlin.math.min

//permission은 라이브러리에 내장되어있음.
class AR2 : AppCompatActivity() {

    private lateinit var binding: ActivityAr2Binding
    private lateinit var camera:CameraView
    private lateinit var selectedCocktail:Cocktail
    private lateinit var selectedCocktailDetail: CocktailDetail
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityAr2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        //intent 정보 받기
        selectedCocktail= intent.getSerializableExtra("selectedCocktail") as Cocktail
        selectedCocktailDetail= intent.getSerializableExtra("selectedCocktailDetail") as CocktailDetail

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
        camera=findViewById(R.id.camera)
        //LifeCycleOwner 이 액티비티로 연결해줘야함.
        camera.setLifecycleOwner(this)
        //사진 찍은거 event listener
        camera.addCameraListener(object:CameraListener(){
            override fun onPictureTaken(result: PictureResult) {
                //Toast.makeText(applicationContext,"take snapshot "+result.format.toString(),Toast.LENGTH_SHORT).show() //JPEG
                result.toBitmap(513,513){
                    //잘 찍히느지 확인용 코드 . TODO:삭제요망
                    //binding.sample.setImageBitmap(it)
                    if (it != null) {
                        imageProcess(it)
                    }

                }
                result.toBitmap {
//bitmap 생성되었음. TODO:변환 후 모델에 넣고 돌리면 됨.

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
                mainHandler.postDelayed(this,2*1000)
            }
        })

    }

    private val imageSizeX=512
    private val imageSizeY=512
    private val channelSize = 3
    private val IMAGE_MEAN = 127.5f
    private val IMAGE_STD = 127.5f
    private val PROBABILITY_MEAN = 0.0f
    private val PROBABILITY_STD = 1.0f
    private var isfirst=true
    //process image DeepLabLite Model
    private fun imageProcess(result: Bitmap) {

        // Creates inputs for reference.
        //bitmap to tensorimage
        val predictor=Predictor(this)
        var preprocessedImage=TensorImage()
        //preprocessedImage=predictor.preprocessImage(result,0,preprocessedImage)!!

        //run model
        //var output=predictor.runModel(preprocessedImage)
        //val image = TensorImage.fromBitmap(result)
        //val output=predictor.runModel(image)
        val maskedBitmap=predictor.runModel(result)

        //if(isfirst)
            binding.sample.setImageBitmap(maskedBitmap)
        binding.overlayimage.setImageBitmap(maskedBitmap)
        isfirst=false
        //if (output.isEmpty())
        //    throw Exception("output is empty!")

        //TODO: output처리
        //Log.i("output check",output.toString())

    }

}