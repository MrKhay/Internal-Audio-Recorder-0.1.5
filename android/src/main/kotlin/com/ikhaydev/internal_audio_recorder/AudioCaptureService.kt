package com.ikhaydev.internal_audio_recorder

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.*
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.concurrent.thread
import kotlin.experimental.and


class AudioCaptureService : Service() {

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null

    private lateinit var audioCaptureThread: Thread
    private var audioRecord: AudioRecord? = null


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(
            SERVICE_ID, NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).build()
        )

        // use applicationContext to avoid memory leak on Android 10.
        // see: https://partnerissuetracker.corp.google.com/issues/139732252
        mediaProjectionManager =
            applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }


    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Audio Capture Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager = getSystemService(NotificationManager::class.java) as NotificationManager
        manager.createNotificationChannel(serviceChannel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return if (intent != null) {
            when (intent.action) {
                ACTION_START -> {
                    mediaProjection = mediaProjectionManager.getMediaProjection(
                        Activity.RESULT_OK, intent.getParcelableExtra(EXTRA_RESULT_DATA)!!
                    ) as MediaProjection

                    // Retrieve encoding and sample rate from the intent
                    val encoding =
                        intent.getIntExtra(EXTRA_ENCODING, AudioFormat.ENCODING_PCM_16BIT)
                    val sampleRate = intent.getIntExtra(EXTRA_SAMPLE_RATE, 44100)
                    val outputPath = intent.getStringExtra(EXTRA_OUTPUT_PATH) ?: ""


                    Log.d(
                        LOG_TAG,
                        "Encoding: $encoding  : SampleRate: $sampleRate : Path $outputPath 90"
                    )

                    startAudioCapture(outputPath, encoding, sampleRate)

                    Toast.makeText(
                        this,
                        "Recording started.",
                        Toast.LENGTH_SHORT
                    ).show()


                    Service.START_STICKY

                }
                ACTION_STOP -> {
                    stopAudioCapture()
                    Toast.makeText(
                        this,
                        "Recording stopped.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Service.START_NOT_STICKY
                }
                else -> throw IllegalArgumentException("Unexpected action received: ${intent.action}")
            }
        } else {
            Service.START_NOT_STICKY
        }
    }

    @SuppressLint("MissingPermission")
    private fun startAudioCapture(
        outputPath: String,
        encoding: Int,
        sampleRate: Int,

    ) {
        // Delay before starting
//        delay(TimeUnit.SECONDS.toMillis(delay.toLong()))
        val config = AudioPlaybackCaptureConfiguration.Builder(mediaProjection!!)
            .addMatchingUsage(AudioAttributes.USAGE_MEDIA)

            .build()

        /**
         * Using hardcoded values for the audio format, Mono PCM samples with a sample rate of 8000Hz
         * These can be changed according to your application's needs
         */
        val audioFormat = AudioFormat.Builder().setEncoding(encoding).setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_IN_STEREO).build()

        audioRecord = AudioRecord.Builder().setAudioFormat(audioFormat)
            .setBufferSizeInBytes(BUFFER_SIZE_IN_BYTES).setAudioPlaybackCaptureConfig(config)
            .build()

        audioRecord!!.startRecording()
        audioCaptureThread = thread(start = true) {
            val outputFile = File(outputPath)
//            Log.d(LOG_TAG, "Created file for capture target: ${outputFile.absolutePath}")
            writeAudioToFile(outputFile)
        }
    }


//    private fun createFolder(): File {
//        val audioCapturesDirectory = File(getExternalFilesDir(null), "/AudioCaptures")
//        if (!audioCapturesDirectory.exists()) {
//            audioCapturesDirectory.mkdirs()
//        }
//        return audioCapturesDirectory
//    }
//
//
//    private fun createAudioFile(): File {
//        val audioCapturesDirectory = createFolder()
//        val timestamp = SimpleDateFormat("dd-MM-yyyy-hh-mm-ss", Locale.US).format(Date())
//        val fileName = "Capture-$timestamp.pcm"
//        return File(audioCapturesDirectory.absolutePath + "/" + fileName)
//    }

    private fun writeAudioToFile(outputFile: File) {

        Log.d(
            AudioCaptureService.LOG_TAG,
            " Path ${outputFile.path}"
        )
        val fileOutputStream = FileOutputStream(outputFile)
        val capturedAudioSamples = ShortArray(NUM_SAMPLES_PER_READ)

        while (!audioCaptureThread.isInterrupted) {
            audioRecord?.read(capturedAudioSamples, 0, NUM_SAMPLES_PER_READ)

            // fileOutputStream.write(
            //     capturedAudioSamples.toByteArray(), 0, BUFFER_SIZE_IN_BYTES
            // )

            val intent = Intent(ACTION_AUDIO_CHUNK)
            intent.putExtra(EXTRA_AUDIO_CHUNK, capturedAudioSamples.toByteArray())
            sendBroadcast(intent)
            // This loop should be as fast as possible to avoid artifacts in the captured audio
            // You can uncomment the following line to see the capture samples but
            // that will incur a performance hit due to logging I/O.
//            Log.v(LOG_TAG, "Audio samples captured: ${capturedAudioSamples.toList()}")

        //    Log.d(LOG_TAG, "Captured: ${capturedAudioSamples.toByteArray()}")

            

        }


    }

    private fun stopAudioCapture() {
        requireNotNull(mediaProjection) { "Tried to stop audio capture, but there was no ongoing capture in place!" }

        audioCaptureThread.interrupt()
        audioCaptureThread.join()

        audioRecord!!.stop()
        audioRecord!!.release()
        audioRecord = null

        mediaProjection!!.stop()
        stopSelf()
    }

    override fun onBind(p0: Intent?): IBinder? = null

    private fun ShortArray.toByteArray(): ByteArray {
        // Samples get translated into bytes following little-endianness:
        // least significant byte first and the most significant byte last
        val bytes = ByteArray(size * 2)
        for (i in 0 until size) {
            bytes[i * 2] = (this[i] and 0x00FF).toByte()
            bytes[i * 2 + 1] = (this[i].toInt() shr 8).toByte()
            this[i] = 0
        }
        return bytes
    }

    companion object {
        const val LOG_TAG = "AudioCaptureService"
        private const val SERVICE_ID = 123
        private const val NOTIFICATION_CHANNEL_ID = "AudioCapture channel"

        private const val NUM_SAMPLES_PER_READ = 1024
        private const val BYTES_PER_SAMPLE = 2 // 2 bytes since we hardcoded the PCM 16-bit format
        private const val BUFFER_SIZE_IN_BYTES = NUM_SAMPLES_PER_READ * BYTES_PER_SAMPLE

        const val ACTION_START = "AudioCaptureService:Start"
        const val ACTION_STOP = "AudioCaptureService:Stop"
        const val EXTRA_RESULT_DATA = "AudioCaptureService:Extra:ResultData"
        const val EXTRA_ENCODING = "AudioCaptureService:Extra:Encoding"
        const val EXTRA_SAMPLE_RATE = "AudioCaptureService:Extra:SampleRate"
        const val EXTRA_OUTPUT_PATH = "AudioCaptureService:Extra:StorageFile"
        const val EXTRA_DELAY = "AudioCaptureService:Extra:Delay"
        const val ACTION_AUDIO_CHUNK = "ACTION_AUDIO_CHUNK"
        const val EXTRA_AUDIO_CHUNK = "EXTRA_AUDIO_CHUNK"


    }
}
