package com.kissspace.room.widget

import android.os.Bundle
import android.view.Gravity
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.kissspace.common.http.uploadPicture
import com.kissspace.common.util.CoilEngine
import com.kissspace.common.util.UCropEngine
import com.kissspace.common.util.hideLoading
import com.kissspace.common.util.showLoading
import com.kissspace.common.util.customToast
import com.kissspace.common.widget.BaseDialogFragment
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogRoomInfoBinding
import com.kissspace.network.result.collectData
import com.kissspace.room.viewmodel.RoomInfoViewModel
import java.io.File

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/12
 * @Description: 房间信息弹窗
 *
 */
class RoomInfoDialog : BaseDialogFragment<RoomDialogRoomInfoBinding>(Gravity.CENTER) {
    private lateinit var mCrId: String
    private val mViewModel by viewModels<RoomInfoViewModel>()

    companion object {
        fun newInstance(crId: String) = RoomInfoDialog().apply {
            arguments = bundleOf("crId" to crId)
        }
    }

    override fun getLayoutId() = R.layout.room_dialog_room_info

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mCrId = it.getString("crId")!!
        }
    }

    override fun initView() {
        mBinding.vm = mViewModel
        mViewModel.requestRoomInfo(mCrId)
        mBinding.ivPoster.setOnClickListener {
            PictureSelector.create(this).openGallery(SelectMimeType.TYPE_IMAGE)
                .isDisplayCamera(true).setMaxSelectNum(1).setImageEngine(CoilEngine()).isGif(false)
                .setCropEngine(UCropEngine(false))
                .forResult(object : OnResultCallbackListener<LocalMedia> {
                    override fun onResult(result: ArrayList<LocalMedia>?) {
                        val media = result?.get(0)
                        val file = File(media?.cutPath)
                        uploadPicture(mutableListOf(file)) {
                            mViewModel.cover.set(it[0])
                        }
                    }

                    override fun onCancel() {

                    }
                })
        }
        mBinding.submit.setOnClickListener {
            if (mViewModel.roomInfo.get()?.roomTitle.isNullOrEmpty()) {
                customToast("请填写房间名")
                return@setOnClickListener
            }
            showLoading()
            mViewModel.submitInfo()
        }
    }

    override fun observerData() {
        super.observerData()
        collectData(mViewModel.submitEvent, onSuccess = {
            hideLoading()
            customToast("房间信息修改成功")
            dismiss()
        }, onError = {
            hideLoading()
            customToast(it.message)
        })
    }


}