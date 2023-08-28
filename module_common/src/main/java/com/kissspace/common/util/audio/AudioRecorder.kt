package com.kissspace.common.util.audio

import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.kissspace.util.logE
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @Author gaohangbo
 * @Date 2023/7/31 17:52.
 * @Describe
 */
object AudioRecorder {

    private var recorder: MediaRecorder? = null

    private var recordFile: File? = null

    //更新时间
    private var updateTime: Long = 0


    private var durationMills: Long = 0


    val isRecording = AtomicBoolean(false)

    private val handler = Handler(Looper.getMainLooper())

    var recorderCallback: RecorderCallback? = null


    private const val RECORDING_INTERVAL = 1000L


    private const val MAX_RECORDE_DURATION = 60000L


    val lifecycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            logE("messageOnDestroy")
            stopRecordingTimer()
        }
    }

    fun startRecording(
        outputFile: String?
    ) {
        recordFile = File(outputFile)
        recorder = MediaRecorder()
        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        recorder?.setAudioChannels(2)
//        recorder?.setAudioSamplingRate(Constants.RECORD_SAMPLE_RATE_44100)
//        recorder?.setAudioEncodingBitRate(Constants.RECORD_ENCODING_BITRATE_96000)
        //最长2分钟
        recorder?.setMaxDuration(120000) //Duration unlimited or use RECORD_MAX_DURATION
        recorder?.setOutputFile(recordFile?.absolutePath)
        try {
            recorder?.prepare()
            recorder?.start()
            updateTime = System.currentTimeMillis()
            isRecording.set(true)
            //更新时间
            scheduleRecordingTimeUpdate()
            recorderCallback?.onStartRecord()

        } catch (e: IllegalStateException) {
            logE("prepare() failed")
            recorderCallback?.onError(e.message.toString())
        }

    }

    private fun scheduleRecordingTimeUpdate() {
        handler.postDelayed({
            if (recorderCallback != null && recorder != null) {
                try {
                    val curTime = System.currentTimeMillis()
                    durationMills += curTime - updateTime
                    updateTime = curTime
                    logE("更新时间$durationMills")
                    logE("更新时间之后${durationMills / 1000}")
                    recorderCallback?.onRecordProgress(durationMills / 1000)
                } catch (e: IllegalStateException) {
                    recorderCallback?.onError(e.message.toString())
                }
                if (durationMills >= MAX_RECORDE_DURATION) {
                    //到达最长时长就停止录音
                    stopRecording()
                } else {
                    scheduleRecordingTimeUpdate()
                }

            }
        }, RECORDING_INTERVAL)
    }

    fun stopRecording() {
        if (isRecording.get()) {
            stopRecordingTimer()
            try {
                recorder?.stop()
            } catch (e: RuntimeException) {
                LogUtils.e(e, "stopRecording() problems")
            }
            recorder?.release()
            if (durationMills < 4000) {
                recorderCallback?.onError("至少录音3秒以上")
                //删除文件
                FileUtils.delete(recordFile)
            } else {
                recorderCallback?.onStopRecord(recordFile)
            }
            durationMills = 0
            recordFile = null
            isRecording.set(false)
            recorder = null
        } else {
            logE("Recording has already stopped or hasn't started")
        }
    }


    fun cancelRecord() {
        stopRecordingTimer()
        try {
            recorder?.stop()
        } catch (e: RuntimeException) {
            LogUtils.e(e, "stopRecording() problems")
        }
        recorder?.release()
        recorderCallback?.onError(null)
        if (isRecording.get()) {
            durationMills = 0
            recordFile = null
            isRecording.set(false)
            recorder = null
        } else {
            logE("Recording has already stopped or hasn't started")
        }
    }


    private fun stopRecordingTimer() {
        handler.removeCallbacksAndMessages(null)
        updateTime = 0
    }

}