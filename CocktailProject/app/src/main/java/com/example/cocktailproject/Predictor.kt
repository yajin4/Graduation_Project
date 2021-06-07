package com.example.cocktailproject

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.segmenter.ImageSegmenter
import org.tensorflow.lite.task.vision.segmenter.Segmentation


class Predictor(val context:Context) {

    fun runModel(inputbitmap:Bitmap):Bitmap {

        //TODO:실험구문 입니다 > task library https://www.tensorflow.org/lite/inference_with_metadata/task_library/image_segmenter
        //val options=ImageSegmenter.ImageSegmenterOptions.builder().setOutputType(OutputType.CONFIDENCE_MASK).build()
        val imageSegmenter = ImageSegmenter.createFromFile(
            context,
            "model6000_with_metadata.tflite" //nodm
        ) //adk20은 mask값 오류가 나서 안되고 dm05 int8은 잘됩니다.
        val inputTensorImage = TensorImage()
        inputTensorImage.load(inputbitmap)
        val results = imageSegmenter.segment(inputTensorImage)
        val (maskBitmap, itemsFound) = createMaskBitmapAndLabels(
            results.get(0),
            inputbitmap.width, inputbitmap.height
        )
        for (item in itemsFound) {
            Log.i(
                "아이템",
                item.key.toString() + " : " + item.value.red.toString() + "," + item.value.green.toString() + "," + item.value.blue.toString()
            )
        }
        return maskBitmap
    }

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
            val rgb = coloredLabel.argb
            colors[cnt++] = Color.argb(128, Color.red(rgb), Color.green(rgb), Color.blue(rgb))
        }
        // Use completely transparent for the background color.
        colors[0] = Color.TRANSPARENT
        //for(color in colors)
            //Log.i("컬러",color.red.toString()+","+color.green.toString()+","+color.blue.toString())

        // Create the mask bitmap with colors and the set of detected labels.
        val maskTensor = result.masks[0]
        val maskArray = maskTensor.buffer.array()
        Log.i("mask크기",maskArray.size.toString())
        val pixels = IntArray(maskArray.size)
        val itemsFound = HashMap<String, Int>()
        for (i in maskArray.indices) {
            //Log.i("컬러 현재 mask",i.toString()+"번:"+maskArray[i].toInt().toString())
/*            if(maskArray[i].toInt()<0)
                maskArray[i]=(-maskArray[i].toInt()).toByte()*/
            val color = colors[maskArray[i].toInt()]
            pixels[i] = color
            itemsFound[coloredLabels[maskArray[i].toInt()].getlabel()] = color
        }
        val maskBitmap = Bitmap.createBitmap(
            pixels, maskTensor.width, maskTensor.height,
            Bitmap.Config.ARGB_8888
        )
        // Scale the maskBitmap to the same size as the input image.
        return Pair(Bitmap.createScaledBitmap(maskBitmap, inputWidth, inputHeight, true), itemsFound)
    }
}