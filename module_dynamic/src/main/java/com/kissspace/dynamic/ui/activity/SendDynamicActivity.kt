package com.kissspace.dynamic.ui.activity

import android.graphics.drawable.AnimationDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.didi.drouter.annotation.Router
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.ext.previewPicture
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.http.uploadAudio
import com.kissspace.common.http.uploadPicture
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.openPictureSelector
import com.kissspace.common.widget.DeleteImageView
import com.kissspace.common.widget.PreviewImageView
import com.kissspace.dynamic.ui.fragment.RecordVoiceFragment
import com.kissspace.dynamic.ui.viewmodel.SendDynamicViewModel
import com.kissspace.util.context
import com.kissspace.util.loadImage
import com.kissspace.util.toast
import com.kissspace.module_dynamic.R
import com.kissspace.module_dynamic.databinding.DynamicActivitySendBinding
import com.kissspace.util.SoftKeyboardFactory
import com.kissspace.util.addAfterTextChanged
import com.kissspace.util.orZero
import kotlinx.coroutines.launch
import java.io.File

/**
 * @Author gaohangbo
 * @Date 2023/7/31 17:14.
 * @Describe 发布动态页
 */
@Router(uri = RouterPath.PATH_DYNAMIC_SEND_FRIEND)
class SendDynamicActivity : BaseActivity(R.layout.dynamic_activity_send) {

    private val mBinding by viewBinding<DynamicActivitySendBinding>()

    private val mViewModel by viewModels<SendDynamicViewModel>()

    private var recordVoiceFragment: RecordVoiceFragment? = null


    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        mBinding.lifecycleOwner = this
        mBinding.m = mViewModel
        mBinding.mPreviewImageView.setPictureList(
            mViewModel.fileImageList.value,
            object : PreviewImageView.OnLoadPhotoListener {
                override fun onPhotoLoading(
                    pos: Int,
                    data: String,
                    imageView: DeleteImageView
                ) {
                    imageView.loadImage(File(data), radius = 8f)
                }
            },
            mBinding.mPreviewImageView.maxPhotoCount
        )
        mBinding.mPreviewImageView.setOnAddPhotoClickListener {
            sendPicture()
        }

        mBinding.etInput.editText?.addAfterTextChanged {
            mViewModel.sendDynamicText.value = it.toString()
        }

        mBinding.mPreviewImageView.onDeleteListener = object : PreviewImageView.OnDeleteListener {
            override fun onDelete(url: String?) {
                if (mViewModel.fileImageList.value.contains(url)) {
                    mViewModel.fileImageList.value.remove(url);
                }
                mViewModel.selectImageCount.value = mViewModel.fileImageList.value.size
            }
        }

        //预览图片
        mBinding.mPreviewImageView.onPhotoClickListener =
            object : PreviewImageView.OnPhotoClickListener {
                override fun onPhotoClickListener(
                    pos: Int,
                    data: String,
                    imageView: DeleteImageView
                ) {
                    previewPicture(
                        this@SendDynamicActivity,
                        pos,
                        null,
                        mViewModel.fileImageList.value
                    )
                }
            }

        mBinding.btSend.safeClick {
            //图片功能
            if (mViewModel.fileImageList.value.isNotEmpty()) {
                val fileList: MutableList<File> = mutableListOf()
                mViewModel.fileImageList.value.forEach { path ->
                    fileList.add(File(path))
                }
                //先上传图片再请求接口
                uploadPicture(fileList) { result ->
                    mViewModel.sendDynamic(
                        dynamicText = mBinding.etInput.contentText,
                        feedbackImageList = result.toMutableList()
                    ) {
                        finishSendDynamic()
                    }
                }
            } else if (recordVoiceFragment?.recordFile != null) {
                uploadAudio(File(recordVoiceFragment?.recordFile!!.absolutePath), onSuccess = {
                    mViewModel.sendDynamic(
                        dynamicText = mBinding.etInput.contentText, voiceDynamicContent = it, speechDuration = recordVoiceFragment?.playDuration?.toInt()
                    ) {
                        finishSendDynamic()
                    }
                }, onError = {
                    toast(it.message)
                })
            } else {
                mViewModel.sendDynamic(
                    dynamicText = mBinding.etInput.contentText
                ) {
                    finishSendDynamic()
                }
            }
        }
        SoftKeyboardFactory(this).addOnKeyboardChangedListener { keyboardVisible, height ->
            if (keyboardVisible) {
                //键盘显示的时候 停止了录音
                if(mViewModel.isOpenVoice.value){
                    recordVoiceFragment?.cancelRecord()
                }
                mViewModel.isShowVoice.value = false
                val layoutParams: ConstraintLayout.LayoutParams =
                    mBinding.clLayout.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.bottomMargin = height
                mBinding.clLayout.layoutParams = layoutParams
            } else {
                if (!mViewModel.isShowVoice.value) {
                    //如果没有开启声音 修改高度
                    if (!mViewModel.isOpenVoice.value) {
                        val layoutParams: ConstraintLayout.LayoutParams =
                            mBinding.clLayout.layoutParams as ConstraintLayout.LayoutParams
                        layoutParams.bottomMargin = height
                        mBinding.clLayout.layoutParams = layoutParams
                    } else {
                        //显示对应的声音
                        mViewModel.isShowVoice.value = true
                    }
                } else {
                    mViewModel.isShowVoice.value = true
                }
            }
        }

        mBinding.ivDeleteRecord.safeClick {
            recordVoiceFragment?.deleteRecordFile()
            mViewModel.showRecordFile.value = false
            mViewModel.finishRecord.value = false
            recordVoiceFragment?.resetRecordState()
        }


        mBinding.clVoice.safeClick {
            recordVoiceFragment?.recordFile?.path.let {
                if (mBinding.ivVoice.drawable is AnimationDrawable) {
                    (mBinding.ivVoice.drawable as AnimationDrawable).start()
                }
                val mediaPlayer = MediaPlayer()
                mediaPlayer.reset()
                mediaPlayer.setDataSource(it)
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener {
                    mediaPlayer.start()
                    mediaPlayer.seekTo(0)
                }
                mediaPlayer.setOnCompletionListener {
                    if (mBinding.ivVoice.drawable is AnimationDrawable) {
                        (mBinding.ivVoice.drawable as AnimationDrawable).stop()
                         mBinding.ivVoice.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                resources,
                                com.kissspace.module_common.R.drawable.common_anim_audio_play_left,
                                null
                            )
                        )
                    }
                }
            }
        }

        //发图片
        mBinding.ivSendPicture.safeClick {
            if (mViewModel.showRecordFile.value) {
                toast("图片和语音不能同时发布哦")
            }else{
                mViewModel.selectedMode.value = SendDynamicViewModel.SelectMode.Picture
                mViewModel.isShowVoice.value = false
                mViewModel.isOpenVoice.value = false
            }
        }

        //发语音
        mBinding.ivSendVoice.safeClick {
            if (mViewModel.selectImageCount.value != 0) {
                toast("图片和语音不能同时发布哦")
            }else{
                mViewModel.selectedMode.value = SendDynamicViewModel.SelectMode.Voice
                KeyboardUtils.hideSoftInput(mBinding.etInput)
                mViewModel.isShowVoice.value = true
                mViewModel.isOpenVoice.value = true
            }
        }

    }

    private fun finishSendDynamic() {
        ToastUtils.showShort("发布成功")
        setResult(RESULT_OK, intent.putExtra("result", SendDynamicActivityResult))
        finish()
    }

    private fun sendPicture() {
        openPictureSelector(
            context,
            mBinding.mPreviewImageView.maxPhotoCount - mBinding.mPreviewImageView.currentFileList.size.orZero()
        ) {
            mViewModel.fileImageList.value.addAll(it.orEmpty())
            mBinding.mPreviewImageView.addData(it.orEmpty())
            mViewModel.selectImageCount.value =
                mBinding.mPreviewImageView.selectedCount.orZero()
        }

    }

    override fun createDataObserver() {
        super.createDataObserver()
        lifecycleScope.launch {
            mViewModel.showRecordFile.collect { data ->
                mViewModel.showRecordFile.value = data
            }
        }
        lifecycleScope.launch {
            mViewModel.recordDuration.collect { data ->
                mViewModel.recordDuration.value = data
            }
        }
        lifecycleScope.launch {
            mViewModel.isShowVoice.collect { data ->
                showFragment(data)
            }
        }

        lifecycleScope.launch {
            mViewModel.selectedMode.collect { value ->
                mViewModel.isPictureSelected.value =
                    value == SendDynamicViewModel.SelectMode.Picture
            }
        }

    }

    private fun showFragment(isShow: Boolean) {
        if (isShow) {
            if (recordVoiceFragment == null) {
                supportFragmentManager.beginTransaction().apply {
                    recordVoiceFragment = RecordVoiceFragment()
                    recordVoiceFragment?.let {
                        replace(mBinding.flLayout.id, it)
                        commitAllowingStateLoss()
                    }
                }
            }
            mBinding.flLayout.visibility = View.VISIBLE
            val layoutParams: ConstraintLayout.LayoutParams =
                mBinding.clLayout.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.bottomMargin = 0
            mBinding.clLayout.layoutParams = layoutParams
            LogUtils.e(" mBinding.flLayout.height" + mBinding.flLayout.height)
        } else {
            mBinding.flLayout.visibility = View.GONE
        }
    }

    companion object {
        const val SendDynamicActivityResult = "SendDynamicActivityResult"
    }

    override fun onDestroy() {
        //在销毁的时候关闭录音
        recordVoiceFragment?.cancelRecord()
        super.onDestroy()
    }


}