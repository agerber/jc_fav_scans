package edu.uchicago.gerber.favs.presentation.screens.scan

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import android.util.Size
import com.google.android.gms.tflite.client.TfLiteInitializationOptions
import com.google.android.gms.tflite.gpu.support.TfLiteGpu
import com.google.android.gms.tflite.java.TfLite
import java.io.Closeable
import java.util.PriorityQueue
import kotlin.math.min
import org.tensorflow.lite.DataType
import org.tensorflow.lite.InterpreterApi
import org.tensorflow.lite.InterpreterApi.Options.TfLiteRuntime
import org.tensorflow.lite.gpu.GpuDelegateFactory
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

/** Helper class used to communicate between our app and the TF image classification model */
class ImageClassificationHelper(
    private val context: Context,
    private val recognitionListener: DetectorListener
) : Closeable {

    /** Abstraction object that wraps a classification output in an easy to parse way */
    data class Recognition(val id: String, val title: String, val confidence: Float)

    private val preprocessNormalizeOp = NormalizeOp(IMAGE_MEAN, IMAGE_STD)
    private val postprocessNormalizeOp = NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD)
    private val labels by lazy { FileUtil.loadLabels(context, LABELS_PATH) }
    private var tfInputBuffer = TensorImage(DataType.UINT8)
    private var tfImageProcessor: ImageProcessor? = null
    var currentDelegate: Int = 0
    private var gpuSupported = false


    init {

        TfLiteGpu.isGpuDelegateAvailable(context).onSuccessTask { gpuAvailable: Boolean ->
            val optionsBuilder =
                TfLiteInitializationOptions.builder()
            if (gpuAvailable) {
                optionsBuilder.setEnableGpuDelegateSupport(true)
            }
            TfLite.initialize(
                context,
                optionsBuilder.build()
            )
        }.addOnSuccessListener {
            recognitionListener.onInitialized()
        }.addOnFailureListener {
            recognitionListener.onError(
                "TfLiteVision failed to initialize: "
                        + it.message
            )
        }
    }

    // Processor to apply post processing of the output probability
    private val probabilityProcessor = TensorProcessor.Builder()
        .add(postprocessNormalizeOp).build()

    // Use TFLite in Play Services runtime by setting the option to FROM_SYSTEM_ONLY
    private fun setUpInterpreter() {
        val interpreterOption = InterpreterApi.Options()
            .setRuntime(TfLiteRuntime.FROM_SYSTEM_ONLY)

        when (currentDelegate) {
            0 -> {
                // Default
            }

            1 -> {
                if (gpuSupported) {
                    interpreterOption.addDelegateFactory(GpuDelegateFactory())
                } else {
                    recognitionListener.onError("GPU is not supported on this device.")
                }
            }

            2 -> {
                interpreterOption.useNNAPI = true
            }
        }

        interpreter =
            InterpreterApi.create(FileUtil.loadMappedFile(context, MODEL_PATH), interpreterOption)
    }

    // Only use interpreter after initialization finished in CameraActivity
    private var interpreter: InterpreterApi? = null

    // Output probability TensorBuffer

    /** Classifies the input bitmapBuffer. */
    fun classify(bitmapBuffer: Bitmap, imageRotationDegrees: Int) {

        if (interpreter == null) {
            setUpInterpreter()
        }

        var inferenceTime = SystemClock.uptimeMillis()
        // Loads the input bitmapBuffer
        tfInputBuffer = loadImage(bitmapBuffer, imageRotationDegrees)
        val probabilityTensorIndex = 0
        val probabilityShape =
            interpreter!!.getOutputTensor(probabilityTensorIndex).shape() // {1, NUM_CLASSES}
        val probabilityDataType = interpreter!!.getOutputTensor(probabilityTensorIndex).dataType()
        val outputProbabilityBuffer =
            TensorBuffer.createFixedSize(probabilityShape, probabilityDataType)


        // Runs the inference call
        interpreter!!.run(tfInputBuffer.buffer, outputProbabilityBuffer.buffer.rewind())

        // Gets the map of label and probability
        val labeledProbability =
            TensorLabel(
                labels,
                probabilityProcessor.process(outputProbabilityBuffer)
            ).mapWithFloatValue

        inferenceTime = SystemClock.uptimeMillis() - inferenceTime

        recognitionListener.onResults(
            getTopKProbability(labeledProbability),
            inferenceTime
        )
    }

    /** Releases TFLite resources if initialized. */
    override fun close() {
        interpreter?.close()
    }

    /** Loads input image, and applies preprocessing. */
    private fun loadImage(bitmapBuffer: Bitmap, imageRotationDegrees: Int): TensorImage {
        // Initializes preprocessor if null
        return (tfImageProcessor
            ?: run {
                val inputIndex = 0
                val inputShape = interpreter!!.getInputTensor(inputIndex).shape()
                val tfInputSize =
                    Size(inputShape[2], inputShape[1]) // Order of axis is: {1, height, width, 3}

                val cropSize = minOf(bitmapBuffer.width, bitmapBuffer.height)
                ImageProcessor.Builder()
                    .add(ResizeWithCropOrPadOp(cropSize, cropSize))
                    .add(
                        ResizeOp(
                            tfInputSize.height,
                            tfInputSize.width,
                            ResizeOp.ResizeMethod.NEAREST_NEIGHBOR
                        )
                    )
                    .add(Rot90Op(-imageRotationDegrees / 90))
                    .add(preprocessNormalizeOp)
                    .build()
                    .also {
                        tfImageProcessor = it
                        Log.d(
                            TAG,
                            "tfImageProcessor initialized successfully. imageSize: $cropSize"
                        )
                    }
            })
            .process(tfInputBuffer.apply { load(bitmapBuffer) })
    }

    /** Gets the top-k results. */
    private fun getTopKProbability(labelProb: Map<String, Float>): List<Recognition> {
        // Sort the recognition by confidence from high to low.
        val pq: PriorityQueue<Recognition> =
            PriorityQueue(3, compareByDescending<Recognition> { it.confidence })
        pq += labelProb.map { (label, prob) -> Recognition(label, label, prob) }
        return List(min(3, pq.size)) { pq.poll()!! }
    }

    fun clear() {
        interpreter = null
    }

    interface DetectorListener {
        fun onInitialized()
        fun onError(error: String)
        fun onResults(
            results: List<Recognition>?,
            inferenceTime: Long
        )
    }

    companion object {
        private val TAG = ImageClassificationHelper::class.java.simpleName

        // change the file names and model name according to your model
        private const val MODEL_PATH = "tf_model.tflite"
        private const val LABELS_PATH = "labels.txt"

        // Float model does not need dequantization in the post-processing. Setting mean and std as
        // 0.0f and 1.0f, respectively, to bypass the normalization
        private const val PROBABILITY_MEAN = 0.0f
        private const val PROBABILITY_STD = 1.0f
        private const val IMAGE_MEAN = 127.0f
        private const val IMAGE_STD = 128.0f
    }
}