package com.example.cocktailproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import com.example.cocktailproject.databinding.ActivityArBinding
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
typealias LumaListener = (luma: Double) -> Unit

class ARActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var binding:ActivityArBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityArBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //camera 권한 체크
        checkCamera()

    }

    private fun checkCamera(){
        // Request camera permissions
        if (allPermissionsGranted()) {
            //permission 허용되어있을 시 카메라 시작
            startCamera()
        } else {
            //권한 승인 요청
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS) //activity, permission string list, int requestCode(내가 지정가능)
        }

        // Set up the listener for take photo button
        binding.cameraCaptureButton.setOnClickListener { takePhoto() }

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()
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
                startCamera()
            } else {
                Toast.makeText(this,
                    "Camera Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable { //thread에서 실행됨
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            //Initialize your Preview object, call build on it, get a surface provider from viewfinder, and then set it on the preview.
            //카메라 화면 표시해줌
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                } //Calls the specified function block with this value as its argument and returns this value.

            //image capture 추가
            //카메라 화면 캡쳐 기능
            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            //Create a CameraSelector object and select DEFAULT_BACK_CAMERA.
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            //Create a try block. Inside that block, make sure nothing is bound to your cameraProvider, and then bind your cameraSelector and preview object to the cameraProvider.
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)
                //activity는 이미 lifecycleOwner interface가 구현되어있음.

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this)) //listener 실행할 애
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}