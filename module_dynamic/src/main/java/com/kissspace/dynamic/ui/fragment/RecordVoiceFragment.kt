package com.kissspace.dynamic.ui.fragment

import android.Manifest
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Bundle
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.FileUtils
import com.kissspace.dynamic.ui.viewmodel.SendDynamicViewModel
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.ext.safeClick
import com.kissspace.common.util.audio.AudioPlayer
import com.kissspace.common.util.audio.AudioRecorder
import com.kissspace.common.util.audio.PlayerCallback
import com.kissspace.common.util.audio.PlayerState
import com.kissspace.common.util.audio.RecorderCallback
import com.kissspace.common.util.customToast
import com.kissspace.common.util.toHHMMSS
import com.kissspace.module_dynamic.R
import com.kissspace.module_dynamic.databinding.DynamicFragmentVoiceBinding
import com.kissspace.util.AppFileHelper
import com.kissspace.util.application
import com.kissspace.util.logE
import com.kissspace.util.orZero
import com.kissspace.util.toast
import com.permissionx.guolindev.PermissionX
import java.io.File
import java.io.IOException

/**
 * @Author gaohangbo
 * @Date 2023/7/31 17:23.
 * @Describe
 */
class RecordVoiceFragment : BaseFragment(R.layout.dynamic_fragment_voice) {

    private val mBinding by viewBinding<DynamicFragmentVoiceBinding>()

    private val mViewModel by activityViewModels<SendDynamicViewModel>()

    private val audioPath =
        application.getExternalFilesDir(AppFileHelper.AUDIO_PATH)?.absolutePath.toString() + "/" + System.currentTimeMillis()
            .toString() + FORMAT_AAC


    var recordFile: File? = null

    var playDuration = 0L


    override fun initView(savedInstanceState: Bundle?) {

        mBinding.m = mViewModel

        mBinding.lifecycleOwner = this

        lifecycle.addObserver(AudioPlayer)

        lifecycle.addObserver(AudioRecorder.lifecycleObserver)

        AudioRecorder.recorderCallback = object : RecorderCallback {

            override fun onStartRecord() {
                mBinding.circleProgressBar.setGapDistance(isCloseGap = false)
                mBinding.ivStartRecord.setImageResource(R.mipmap.dynamic_icon_audio_recording)
            }

            override fun onRecordProgress(mills: Long) {
                mViewModel.isRecording.value = true
                logE("$mills---")
                mBinding.tvTips.text = "录制中"
                mBinding.tvTime.text = mills.toHHMMSS(false)
                mBinding.circleProgressBar.setProgress(mills.toInt())
            }

            override fun onStopRecord(output: File?) {
                mViewModel.isRecording.value = false
                recordFile = output
                //录制完成之后计算时间
                playDuration = readRecordDuration() / 1000000
                mBinding.circleProgressBar.setGapDistance(isCloseGap = true)
                mBinding.ivStartRecord.setImageResource(R.mipmap.dynamic_icon_audio_stop)
                mBinding.tvTips.text = "录制完成"
                mViewModel.finishRecord.value = true
            }

            override fun onError(throwable: String?) {
                throwable?.let {
                    toast(it)
                }
                deleteRecordFile()
                finishRecordState(0)
            }
        }

        AudioPlayer.playerCallback = object : PlayerCallback {
            override fun onStartPlay() {
                mBinding.ivStartRecord.setImageResource(R.mipmap.dynamic_icon_audio_playing)
                logE("播放总时长" + playDuration)
                mBinding.circleProgressBar.changeCircleProgressBar(playDuration.toInt())
            }

            override fun onPlayProgress(mills: Long) {
                mViewModel.isPlaying.value = true
                mBinding.ivStartRecord.setImageResource(R.mipmap.dynamic_icon_audio_playing)
                logE("播放进度$mills")
                mBinding.tvTips.text = "点击停止"
                mBinding.tvTime.text = mills.toHHMMSS(false)
                mBinding.circleProgressBar.setProgress(mills.toInt())
            }

            override fun onStopPlay() {
                mViewModel.isPlaying.value = false
                mBinding.tvTips.text = "播放完成"
//                mBinding.tvTime.text = "00:00"
                mBinding.ivStartRecord.setImageResource(R.mipmap.dynamic_icon_audio_stop)
                mBinding.circleProgressBar.setGapDistance(isCloseGap = true)
            }

            override fun onError(throwable: String) {
                toast(throwable)
                deleteRecordFile()
                finishRecordState(60)
            }

        }

        mBinding.ivStartRecord.setOnClickListener {
            if (mViewModel.showRecordFile.value) {
                toast("一条动态只能有一条音频")
                return@setOnClickListener
            }


            if (recordFile != null) {
                if (AudioPlayer.playerState == PlayerState.PLAYING) {
                    //停止播放声音
                    AudioPlayer.stopPlayer()
                } else {
                    playRecord()
                }

            } else if (!AudioRecorder.isRecording.get()) {
                PermissionX.init(this).permissions(Manifest.permission.RECORD_AUDIO)
                    .onExplainRequestReason { scope, deniedList ->
                        val message =
                            "为了您能正常体验【录制语音打招呼】功能，独角soul需向你申请麦克风权限"
                        scope.showRequestReasonDialog(deniedList, message, "确定", "取消")
                    }
                    .explainReasonBeforeRequest()
                    .request { allGranted, _, _ ->
                        if (allGranted) {
                            startRecord()
                        } else {
                            customToast("请打开麦克风权限")
                        }
                    }
            } else {
                stopRecord()
            }
        }

        mBinding.ivDeleteAudio.safeClick {
            deleteRecordFile()
            finishRecordState(60)
        }

        mBinding.ivFinishAudio.safeClick {
            logE("播放时长$playDuration")
            mViewModel.recordDuration.value = playDuration
            mViewModel.showRecordFile.value = true
            //这次录制完成了，要重新录制
            mViewModel.finishRecord.value = false
            finishRecordState(playDuration.toInt())
        }
    }

    private fun stopRecord() {
        AudioRecorder.stopRecording()
    }


    private fun playRecord() {
        recordFile?.absolutePath?.let { it1 ->
            AudioPlayer.play(it1)
        }
    }


    private fun startRecord() {
        AudioRecorder.startRecording(audioPath)
    }

    fun cancelRecord() {
        AudioRecorder.cancelRecord()
    }


    fun deleteRecordFile() {
        kotlin.runCatching {
            FileUtils.delete(recordFile)
            recordFile = null
        }
        mViewModel.showRecordFile.value = false
        mViewModel.finishRecord.value = false
    }

    private fun finishRecordState(time: Int) {
        mBinding.tvTips.text = "点击录制"
        mBinding.tvTime.text = "00:00"
        mViewModel.isRecording.value = false
        mBinding.ivStartRecord.setImageResource(R.mipmap.dynamic_icon_audio_stop)
        if (time != 0) {
            mBinding.circleProgressBar.changeCircleProgressBar(time)
        }
    }

    fun resetRecordState() {
        mBinding.circleProgressBar.resetCircleProgressBar()
    }


    private fun readRecordDuration(): Long {
        val extractor = MediaExtractor()
        var format: MediaFormat? = null
        recordFile?.path?.let { it1 ->
            extractor.setDataSource(it1)
        }
        var j = 0
        val numTracks = extractor.trackCount
        // find and select the first audio track present in the file.
        for (i in 0 until numTracks) {
            format = extractor.getTrackFormat(i)
            j = i
            if (format.getString(MediaFormat.KEY_MIME)!!.startsWith("audio/")) {
                extractor.selectTrack(i)
                break
            }
        }
        if (j == numTracks || format == null) {
            throw IOException("No audio track found in " + recordFile.toString())
        }
        val duration: Long? = try {
            format.getLong(MediaFormat.KEY_DURATION)
        } catch (e: Exception) {
            logE(e.message)
            null
        }
        return duration.orZero()
    }


    companion object {
        const val FORMAT_AAC = ".aac"
    }

}