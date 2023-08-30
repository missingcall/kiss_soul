package com.kissspace.setting.ui.fragment

import android.Manifest
import android.animation.ValueAnimator
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.TimeUtils
import com.netease.nimlib.sdk.media.player.AudioPlayer
import com.netease.nimlib.sdk.media.player.OnPlayListener
import com.netease.nimlib.sdk.media.record.AudioRecorder
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback
import com.netease.nimlib.sdk.media.record.RecordType
import com.permissionx.guolindev.PermissionX
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.ext.safeClick
import com.kissspace.common.http.uploadAudio
import com.kissspace.common.util.countDown
import com.kissspace.common.util.hideLoading
import com.kissspace.common.util.showLoading
import com.kissspace.common.util.customToast
import com.kissspace.common.util.getAudioPath
import com.kissspace.module_setting.R
import com.kissspace.module_setting.databinding.SettingFragmentSayHiAudioBinding
import com.kissspace.network.result.collectData
import com.kissspace.setting.viewmodel.SayHiViewModel
import kotlinx.coroutines.Job
import org.json.JSONObject
import java.io.File

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/28 15:45
 * @Description: 打招呼文本设置
 *
 */
class SayHiAudioFragment : BaseFragment(R.layout.setting_fragment_say_hi_audio),
    IAudioRecordCallback, OnPlayListener {
    private val mBinding by viewBinding<SettingFragmentSayHiAudioBinding>()
    private val mViewModel by activityViewModels<SayHiViewModel>()
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var audioPlayer: AudioPlayer
    private var animator: ValueAnimator? = null
    private var audioFile: File? = null
    private var audioLength: Long = 0
    private var job: Job? = null
    private var recordTime = 0L
    override fun initView(savedInstanceState: Bundle?) {
        mBinding.vm = mViewModel
        mViewModel.getSayHiAudioInfo()
        mBinding.progressBar.setTotal(6000)
        audioPlayer = AudioPlayer(requireContext())
        audioPlayer.onPlayListener = this
        audioRecorder = AudioRecorder(requireContext(), RecordType.AAC, 60, this)
        initEvent()
    }

    private fun initEvent() {
        mBinding.ivStartRecord.setOnClickListener {
            if (audioRecorder.isRecording) {
                finishRecord()
                return@setOnClickListener
            }
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
                mBinding.ivStartRecord.setImageResource(R.mipmap.setting_icon_audio_record_finish)
                mBinding.progressBar.visibility = View.INVISIBLE
                animator?.cancel()
                animator = null
                return@setOnClickListener
            }
            if (audioFile != null) {
                audioPlayer.setDataSource(audioFile!!.path)
                audioPlayer.start(AudioManager.STREAM_MUSIC)
                mBinding.ivStartRecord.setImageResource(R.mipmap.setting_icon_audio_record_playing)
                mBinding.progressBar.visibility = View.VISIBLE
                startProgressAnimation(audioLength.toInt())
                return@setOnClickListener
            }
            if (audioFile == null) {
                PermissionX.init(this).permissions(Manifest.permission.RECORD_AUDIO)
                    .onExplainRequestReason { scope, deniedList ->
                        val message =
                            "为了您能正常体验【录制语音打招呼】功能，2098社交需向你申请麦克风权限"
                        scope.showRequestReasonDialog(deniedList, message, "确定", "取消")
                    }
                    .explainReasonBeforeRequest()
                    .request { allGranted, _, _ ->
                        if (allGranted) {
                            startRecord()
                            recordTime = System.currentTimeMillis()
                        } else {
                            customToast("请打开麦克风权限")
                        }
                    }
                return@setOnClickListener
            }
        }

        mBinding.ivDeleteAudio.safeClick {
            audioFile = null
            audioPlayer.stop()
            mBinding.ivStartRecord.setImageResource(R.mipmap.setting_icon_audio_record)
            mBinding.tvTips.text = "点击录制"
            mBinding.ivDeleteAudio.visibility = View.GONE
            mViewModel.submitEnable.set(false)
            mBinding.tvSubmit.visibility = View.VISIBLE
        }

        mBinding.tvSubmit.safeClick {
            if (audioFile != null) {
                showLoading()
                uploadAudio(audioFile!!, onSuccess = {
                    mViewModel.submitAudioInfo(audioLength, it)
                }, onError = {
                    hideLoading()
                })
            }
        }
    }

    private fun startRecord() {
        mBinding.ivStartRecord.setImageResource(R.mipmap.setting_icon_audio_start_record)
        mBinding.progressBar.visibility = View.VISIBLE
        startProgressAnimation(6000)
        audioRecorder.startRecord()
        job = countDown(60, reverse = true, scope = lifecycleScope, onTick = {
            mBinding.tvTips.text = "0:${it}"
        })
    }

    private fun finishRecord() {
        mBinding.progressBar.clearAnimation()
        mBinding.progressBar.visibility = View.INVISIBLE
        job?.cancel()
        audioRecorder.completeRecord(false)
    }

    private fun startProgressAnimation(max: Int) {
        animator = ValueAnimator.ofInt(max).apply {
            duration = max.toLong() * 10
            addUpdateListener {
                mBinding.progressBar.setProgress(it.animatedValue.toString().toInt())
            }
        }
        animator?.start()
    }


    override fun onRecordReady() {
    }

    override fun onRecordStart(audioFile: File?, recordType: RecordType?) {
    }

    override fun onRecordSuccess(audioFile: File?, audioLength: Long, recordType: RecordType?) {
        if (audioLength < 3000) {
            mBinding.ivStartRecord.setImageResource(R.mipmap.setting_icon_audio_record)
            mBinding.ivDeleteAudio.visibility = View.GONE
            mBinding.tvTips.text = "点击录制"
            customToast("请最少录制3秒")
        } else {
            this.audioFile = audioFile
            this.audioLength = audioLength
            mBinding.ivStartRecord.setImageResource(R.mipmap.setting_icon_audio_record_finish)
            mBinding.ivDeleteAudio.visibility = View.VISIBLE
            mViewModel.submitEnable.set(true)
        }
    }

    override fun onRecordFail() {
    }

    override fun onRecordCancel() {
    }

    override fun onRecordReachedMaxTime(maxTime: Int) {
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.submitEvent, onSuccess = {
            hideLoading()
            customToast("保存成功")
            activity?.finish()
        })

        collectData(mViewModel.sayHiAudioInfo, onSuccess = {
            mBinding.tvSubmit.visibility = View.GONE
            mBinding.ivStartRecord.setImageResource(R.mipmap.setting_icon_audio_record_finish)
            mBinding.ivDeleteAudio.visibility = View.VISIBLE
            mBinding.tvTips.text = "点击删除可重新录制"
            val json =
                if (it.examineMessageContent.isNotEmpty()) JSONObject(it.examineMessageContent) else JSONObject(
                    it.messageContent
                )
            getAudioPath(json.getString("url")) { path ->
                audioFile = File(path)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        audioFile = null
        animator?.cancel()
        if (audioPlayer.isPlaying) {
            audioPlayer.stop()
        }
        if (audioRecorder.isRecording) {
            audioRecorder.completeRecord(true)
        }
    }

    override fun onPrepared() {
    }

    override fun onCompletion() {
        mBinding.ivStartRecord.setImageResource(R.mipmap.setting_icon_audio_record_finish)
        mBinding.progressBar.visibility = View.INVISIBLE
        job?.cancel()
        job = null
    }

    override fun onInterrupt() {
    }

    override fun onError(error: String?) {
    }

    override fun onPlaying(curPosition: Long) {
    }
}