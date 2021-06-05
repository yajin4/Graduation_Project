package com.example.cocktailproject

//import com.example.cocktailproject.ml.LiteModelDeeplabv3Mobilenetv2Ade20k1Default2
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.segmenter.ImageSegmenter
import org.tensorflow.lite.task.vision.segmenter.OutputType
import org.tensorflow.lite.task.vision.segmenter.Segmentation
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel


// Constants
private const val MAX_RESULT_DISPLAY = 3 // Maximum number of results displayed

class Predictor(val context:Context) {
    private val IMAGE_MEAN = 127.5f
    private val IMAGE_STD = 127.5f

    private val PROBABILITY_MEAN = 0.0f
    private val PROBABILITY_STD = 1.0f

    /** The TensorFlow Lite model.  */
   /* private var tfliteModel: LiteModelDeeplabv3Mobilenetv2Ade20k1Default2 =
        LiteModelDeeplabv3Mobilenetv2Ade20k1Default2.newInstance(context)*/


    /** Image size along the x axis.  */
    private val imageSizeX = 513

    /** Image size along the y axis.  */
    private val imageSizeY = 513

    private val channelSize = 3

    private val numOfClasses=151
    /** An instance of the driver class to run model inference with Tensorflow Lite.  *//*
    private val tflite: Interpreter = PredictorMeta.loadInterpreter(context)

    *//** Options for configuring the Interpreter.  *//*
    private val tfliteOptions = Interpreter.Options()


    *//** Output probability TensorBuffer.  *//*
    private val outputProbabilityBuffer:TensorBuffer = TensorBuffer.createFixedSize(
        tflite.getOutputTensor(0).shape(),
        tflite.getOutputTensor(0).dataType())

    *//** Processer to apply post processing of the output probability.  *//*
    private val probabilityProcessor: TensorProcessor = TensorProcessor.Builder().add(NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD)).build()

    private val labelGender = listOf("gender","age")*/


    /** Loads input image, and applies preprocessing.  */
/*    fun preprocessImage(bitmap: Bitmap, sensorOrientation: Int, inputImageBuffer:TensorImage): TensorImage? {
        // Loads bitmap into a TensorImage.

        inputImageBuffer.load(bitmap)

        // Creates processor for the TensorImage.
        val cropSize = min(bitmap.width, bitmap.height)

        val numRotation = sensorOrientation / 90

        val imageProcessor: ImageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(cropSize, cropSize))
            .add(ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.BILINEAR))
            .add(Rot90Op(numRotation))
            .add(NormalizeOp(IMAGE_MEAN, IMAGE_STD))
            .build()

        return imageProcessor.process(inputImageBuffer)
    }

    fun runModel(input:TensorImage): List<Category> {
        Log.i("텐서플로우",input.dataType.toString())


        //lateinit var outputs: LiteModelDeeplabv31Metadata2.Outputs
        lateinit var outputs: LiteModelDeeplabv3Mobilenetv2Ade20k1Default2.Outputs
        try {
            // Runs model inference and gets result.
            outputs = tfliteModel.process(input)
            Log.i("텐서플로우outputs",outputs.toString())
        }
        catch (e: Exception){
            Log.e("텐서플로우model process error",e.toString())
        }
        lateinit var segmentationMasks:List<Category>
        try{
            segmentationMasks = outputs.segmentationMasksAsCategoryList
                .apply {
                    sortByDescending { it.score } // Sort with highest confidence first
                }.take(MAX_RESULT_DISPLAY)

           *//* segmentationMasks = outputs.probabilityAsCategoryList
                .apply {
                    sortByDescending { it.score } // Sort with highest confidence first
                }.take(MAX_RESULT_DISPLAY)*//*
            Log.i("텐서플로우segmentationMasks",outputs.segmentationMasksAsCategoryList.toString())
            //return segmentationMasks
        }
        catch (e:Exception){
            Log.e("텐서플로우segmentationmaskerror",e.toString())
        }
//        for (output in segmentationMasks){
//            Log.i("텐서플로우output",output.label.toString()+output.score.toString())
//        }

        // Releases model resources if no longer used.
        //model.close()
        return mutableListOf()
    }*/

    fun runModel(inputbitmap:Bitmap):Bitmap{

        //TODO:실험구문 입니다 > task library https://www.tensorflow.org/lite/inference_with_metadata/task_library/image_segmenter
        val options=ImageSegmenter.ImageSegmenterOptions.builder().setOutputType(OutputType.CONFIDENCE_MASK).build()
        val imageSegmenter=ImageSegmenter.createFromFile(context,"lite-model_deeplabv3-mobilenetv2_dm05-int8_1_default_2.tflite") //adk20은 mask값 오류가 나서 안되고 dm05 int8은 잘됩니다.
        val inputTensorImage=TensorImage()
        inputTensorImage.load(inputbitmap)
        val results = imageSegmenter.segment(inputTensorImage)
        val (maskBitmap, itemsFound) = createMaskBitmapAndLabels(
            results.get(0), inputbitmap.getWidth(),
            inputbitmap.getHeight()
        )
        for (item in itemsFound){
            Log.i("아이템",item.key.toString()+" : "+item.value.red.toString()+","+item.value.green.toString()+","+item.value.blue.toString())
        }
        return maskBitmap

        //interpreter 생성
        val fileDescriptor: AssetFileDescriptor = context.assets.openFd("lite-model_deeplabv3-mobilenetv2-ade20k_1_default_2.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.getChannel()
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val buff =  fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
      //val file=File("")
        val interpreter=Interpreter(buff)
        //input buffer생성
        val bitmap = Bitmap.createScaledBitmap(inputbitmap, imageSizeX, imageSizeY, true)
        val input = ByteBuffer.allocateDirect(imageSizeX*imageSizeY*channelSize*4).order(ByteOrder.nativeOrder()) //4는 float32니까 byte*4
        for (y in 0 until imageSizeY) {
            for (x in 0 until imageSizeX) {
                val px = bitmap.getPixel(x, y)

                // Get channel values from the pixel value.
                val r = Color.red(px)
                val g = Color.green(px)
                val b = Color.blue(px)

                // Normalize channel values to [-1.0, 1.0]. This requirement depends on the model.
                // For example, some models might require values to be normalized to the range
                // [0.0, 1.0] instead.
                val rf = (r - 127) / 255f
                val gf = (g - 127) / 255f
                val bf = (b - 127) / 255f

                input.putFloat(rf)
                input.putFloat(gf)
                input.putFloat(bf)
            }
        }
        //output buffer
        val bufferSize = imageSizeX*imageSizeY*numOfClasses * java.lang.Float.SIZE / java.lang.Byte.SIZE
        val modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder())
        //run
        interpreter.run(input,modelOutput)

        modelOutput.rewind()
        val probabilities = modelOutput.asFloatBuffer()
        try {
            val reader = BufferedReader(
                InputStreamReader(context.assets.open("labels.txt"),"UTF-8")
            )

            for (i in 0..150) {
                val label: String = reader.readLine()
                val probability = probabilities.get(i)
                Log.i("label레이블",label+" : "+probability.toString())
                //println("$label: $probability")
            }
        } catch (e: IOException) {
            // File not found?
            Log.e("label파일없음",e.toString())
        }

    }

    /** Runs inference and returns the classification results.  */
   /* fun recognizeImage(bitmap: Bitmap,sensorOrientation: Int): Map<String, Float> {

        //Trace.beginSection("recognizeImage")
        //Trace.beginSection("loadImage")
        val startTimeForLoadImage = SystemClock.uptimeMillis()
        val initialInputImageBuffer = TensorImage(tflite.getInputTensor(0).dataType())
        val inputImageBuffer = loadImage(bitmap, sensorOrientation, initialInputImageBuffer)
        val endTimeForLoadImage = SystemClock.uptimeMillis()
        //Trace.endSection()
        log("Timecost to load the image: " + (endTimeForLoadImage - startTimeForLoadImage))

        //Trace.beginSection("runInference")
        val startTimeForReference = SystemClock.uptimeMillis()
        tflite.run(inputImageBuffer?.buffer, outputProbabilityBuffer.buffer.rewind())
        val endTimeForReference = SystemClock.uptimeMillis()
        //Trace.endSection()
        log("Timecost to run model inference: " + (endTimeForReference - startTimeForReference))

        val labeledProbability: Map<String, Float> =
            TensorLabel(
                labelGender,
                probabilityProcessor.process(outputProbabilityBuffer)
            ).mapWithFloatValue

        //Trace.endSection()

        log(labeledProbability.toString())

        return labeledProbability
    }*/
    private fun createMaskBitmapAndLabels(
        result: Segmentation,
        inputWidth: Int,
        inputHeight: Int
    ): Pair<Bitmap, Map<String, Int>> {
        // For the sake of this demo, change the alpha channel from 255 (completely opaque) to 128
        // (semi-transparent), because the maskBitmap will be stacked over the original image later.
        val coloredLabels = result.getColoredLabels()
        var colors = IntArray(coloredLabels.size)
        var cnt = 0
        for (coloredLabel in coloredLabels) {
            val rgb = coloredLabel.getArgb()
            colors[cnt++] = Color.argb(128, Color.red(rgb), Color.green(rgb), Color.blue(rgb))
        }
        // Use completely transparent for the background color.
        colors[0] = Color.TRANSPARENT
        for(color in colors)
            Log.i("컬러",color.red.toString()+","+color.green.toString()+","+color.blue.toString())

        // Create the mask bitmap with colors and the set of detected labels.
        val maskTensor = result.getMasks().get(0)
        val maskArray = maskTensor.getBuffer().array()
        val pixels = IntArray(maskArray.size)
        val itemsFound = HashMap<String, Int>()
        for (i in maskArray.indices) {
            //Log.i("컬러 현재 mask",i.toString()+"번:"+maskArray[i].toInt().toString())
/*            if(maskArray[i].toInt()<0)
                maskArray[i]=(-maskArray[i].toInt()).toByte()*/
            val color = colors[maskArray[i].toInt()]
            pixels[i] = color
            itemsFound.put(coloredLabels.get(maskArray[i].toInt()).getlabel(), color)
        }
        val maskBitmap = Bitmap.createBitmap(
            pixels, maskTensor.getWidth(), maskTensor.getHeight(),
            Bitmap.Config.ARGB_8888
        )
        // Scale the maskBitmap to the same size as the input image.
        return Pair(Bitmap.createScaledBitmap(maskBitmap, inputWidth, inputHeight, true), itemsFound)
    }
}