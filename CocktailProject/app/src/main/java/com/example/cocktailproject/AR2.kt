package com.example.cocktailproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.cocktailproject.databinding.ActivityAr2Binding
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult

//permission은 라이브러리에 내장되어있음.
class AR2 : AppCompatActivity() {

    private lateinit var binding: ActivityAr2Binding
    private lateinit var camera:CameraView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityAr2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        //카메라 기능 초기설정
        cameraInit()


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

                result.toBitmap {
                    //잘 찍히느지 확인용 코드 . TODO:삭제요망
                    binding.sample.setImageBitmap(it)

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
}