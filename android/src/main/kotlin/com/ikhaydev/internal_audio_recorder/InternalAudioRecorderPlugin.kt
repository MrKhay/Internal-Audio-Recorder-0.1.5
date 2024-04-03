package com.ikhaydev.internal_audio_recorder

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.projection.MediaProjectionManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ikhaydev.internal_audio_recorder.AudioCaptureService.Companion.EXTRA_ENCODING
import com.ikhaydev.internal_audio_recorder.AudioCaptureService.Companion.EXTRA_OUTPUT_PATH
import com.ikhaydev.internal_audio_recorder.AudioCaptureService.Companion.EXTRA_RESULT_DATA
import com.ikhaydev.internal_audio_recorder.AudioCaptureService.Companion.EXTRA_SAMPLE_RATE
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.concurrent.TimeUnit

/** InternalAudioRecorderPlugin */
class InternalAudioRecorderPlugin : FlutterPlugin, MethodCallHandler,
    PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener,
    ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity

    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private lateinit var result: MethodChannel.Result
    private lateinit var activity: Activity  // Change: Make it nullable
    private var outputPath = ""
    private var encoding = 0
    private var sampleRate = 0
    private var delay = 0


    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {

        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "internal_audio_recorder")
        channel.setMethodCallHandler(this)

    }

    // Create a BroadcastReceiver to receive audio chunks
    private val audioChunkReceiver = object : BroadcastReceiver() {
        val integers = listOf(1, 2, 3, 4, 5)
        override fun onReceive(context: Context?, intent: Intent?) {
            // Extract audio chunk from intent and process it
            val audioChunk: ByteArray? = intent?.getByteArrayExtra(AudioCaptureService.EXTRA_AUDIO_CHUNK)
            // Process the received audio chunk as needed

            channel.invokeMethod("onData",audioChunk);
        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.activity = binding.activity
        binding.addActivityResultListener(this)
        binding.addRequestPermissionsResultListener(this)

     // Register the BroadcastReceiver
        context.registerReceiver(audioChunkReceiver, IntentFilter(AudioCaptureService.ACTION_AUDIO_CHUNK))


    }

    override fun onDetachedFromActivityForConfigChanges() {
//        this.activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        this.activity = binding.activity as FlutterActivity
        binding.addActivityResultListener(this)
        binding.addRequestPermissionsResultListener(this)
    }

    override fun onDetachedFromActivity() {
//        this.activity = null
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        context.unregisterReceiver(audioChunkReceiver)
    }

    private fun startCapturing(

    ): String? {
              Log.d(
                        AudioCaptureService.LOG_TAG,
                        "OMO 1"
                    )
        if (!File(outputPath).exists()) return "File not exist"
             Log.d(
                        AudioCaptureService.LOG_TAG,
                        "OMO 2"
                    )
        if (!isRecordAudioPermissionGranted()) {
                         Log.d(
                        AudioCaptureService.LOG_TAG,
                        "OMO 3"
                    )
            requestRecordAudioPermission()
                         Log.d(
                        AudioCaptureService.LOG_TAG,
                        "OMO 4"
                    )
        } else {
                         Log.d(
                        AudioCaptureService.LOG_TAG,
                        "OMO 5"
                    )
            startMediaProjectionRequest()
                         Log.d(
                        AudioCaptureService.LOG_TAG,
                        "OMO 6"
                    )
        }

        return null

    }


    private fun stopCapturing() {

        activity.startService(Intent(context, AudioCaptureService::class.java).apply {
            action = AudioCaptureService.ACTION_STOP
        })

    }


    fun convertPCMTOM4AFile(
        inputPath: String, outputPath: String, sampleRate: Int
    ): String? {
        TODO("Not yet implemented")
    }

    override fun onMethodCall(call: MethodCall, result: Result) {

        when (call.method) {

            "stopCapturing" -> {
                try {
                    stopCapturing()
                    result.success(null)
                } catch (e: Exception) {
                    result.success(e.toString())
                }
            }
            "startCapturing" -> {

                // Initialize result
                this.result = result
                try {
                    // Assign the activity context to the 'activity' property
                    outputPath = call.argument<String?>("outputPath") ?: ""
                    encoding = call.argument<Int?>("encoding") ?: AudioFormat.ENCODING_PCM_16BIT
                    sampleRate = call.argument<Int?>("sampleRate") ?: 44100
                    delay = call.argument<Int?>("delay") ?: 0

                    Log.d(
                        AudioCaptureService.LOG_TAG,
                        "Encoding: $encoding  : SampleRate: $sampleRate : Path $outputPath Delay:$delay"
                    )
                    startCapturing()

                } catch (e: Exception) {
                    result.success(e.toString())
                }

            }
            else -> result.notImplemented()
        }
    }


    // Modify isRecordAudioPermissionGranted to accept the activity context
    private fun isRecordAudioPermissionGranted(): Boolean {

        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

// Modify requestRecordAudioPermission to accept the activity context

    private fun requestRecordAudioPermission() {
             Log.d(
                        AudioCaptureService.LOG_TAG,
                        "OMO 33"
                    )
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ): Boolean {
             Log.d(
                        AudioCaptureService.LOG_TAG,
                        "OMO 333"
                    )
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
             Log.d(
                        AudioCaptureService.LOG_TAG,
                        "OMO 3333"
                    )
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(
                    context,
                    "Permissions to capture audio granted. Click the button once again.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context, "Permissions to capture audio denied.", Toast.LENGTH_SHORT
                ).show()
            }
        }

                     Log.d(
                        AudioCaptureService.LOG_TAG,
                        "OMO 33333"
                    )
        return true
    }

    /**
     * Before a capture session can be started, the capturing app must
     * call MediaProjectionManager.createScreenCaptureIntent().
     * This will display a dialog to the user, who must tap "Start now" in order for a
     * capturing session to be started. This will allow both video and audio to be captured.
     * Encoding format can be passed, default is [AudioFormat.ENCODING_PCM_16BIT]
     * Sample Rate can be passed, default is [44100]
     */
    private fun startMediaProjectionRequest(
    ) {
                     Log.d(
                        AudioCaptureService.LOG_TAG,
                        "OMO 44"
                    )
        mediaProjectionManager =
            context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
             Log.d(
                        AudioCaptureService.LOG_TAG,
                        "OMO 444"
                    )
        val screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent()

        Log.d(
            AudioCaptureService.LOG_TAG,
            "Encoding: $encoding  : SampleRate: $sampleRate : Path $outputPath 4"
        )



        Log.d(AudioCaptureService.LOG_TAG, "Called 4")
        activity.startActivityForResult(
            screenCaptureIntent,
            MEDIA_PROJECTION_REQUEST_CODE,
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        Log.d(AudioCaptureService.LOG_TAG, "Called 5 $resultCode")
        if (requestCode == MEDIA_PROJECTION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(AudioCaptureService.LOG_TAG, "Called 7")
                Toast.makeText(
                    context,
                    "MediaProjection permission obtained.",
                    Toast.LENGTH_SHORT
                ).show()



                Log.d(
                    AudioCaptureService.LOG_TAG,
                    "Encoding: $encoding  : SampleRate: $sampleRate : Path $outputPath  3"
                )

                val audioCaptureIntent = Intent(context, AudioCaptureService::class.java).apply {
                    action = AudioCaptureService.ACTION_START
                    // Pass encoding and sample rate values to startAudioCapture
                    putExtra(EXTRA_ENCODING, encoding)
                    putExtra(EXTRA_SAMPLE_RATE, sampleRate)
                    putExtra(EXTRA_OUTPUT_PATH, outputPath)

                    // Pass audio data
                    putExtra(EXTRA_RESULT_DATA, data!!)
                }


                // Delay
                runBlocking {
                    // DELAY SPECIFIED TIME
                    delay(TimeUnit.SECONDS.toMillis(delay.toLong()))

                    // START FOREGROUND SERVICE
                    activity.startForegroundService(audioCaptureIntent)

                }

                // RETURN RESULT
                result.success(null)

                return true

            } else {

                Log.d(AudioCaptureService.LOG_TAG, "Called 10")
                Toast.makeText(
                    context, "Request to obtain MediaProjection denied.", Toast.LENGTH_SHORT
                ).show()
            }
        }
        return true
    }


    companion object {
        private const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 42
        private const val MEDIA_PROJECTION_REQUEST_CODE = 13
    }


}
