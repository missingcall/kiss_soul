package com.kissspace.mine.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.activity.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.ToastUtils
import com.didi.drouter.annotation.Router
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.Constants
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.http.uploadPicture
import com.kissspace.common.model.ReportTypeListModel
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.openPictureSelector
import com.kissspace.common.widget.DeleteImageView
import com.kissspace.common.widget.PreviewImageView
import com.kissspace.mine.viewmodel.ReportViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityReportBinding
import com.kissspace.util.loadImage
import com.kissspace.util.orZero
import java.io.File

/**
 * @Author gaohangbo
 * @Date 2023/1/5 20:27.
 * @Describe 举报页面 (举报用户和举报房间)
 */
@Router(path = RouterPath.PATH_REPORT)
class ReportActivity : BaseActivity(R.layout.mine_activity_report) {

    private var reportType :String?=null


    private val mBinding by viewBinding<MineActivityReportBinding>()


    private val mViewModel by viewModels<ReportViewModel>()

    private val listPair = mutableListOf<Pair<Int, TextView>>()

    private var listImagesParams: MutableList<String> = mutableListOf()

    private var filePathList: MutableList<String> = mutableListOf()

    private var reportTypeList: MutableList<ReportTypeListModel>? = null

    //用户id
    private var userId:String?=null

    //房间id
    private var roomId:String?=null

    //房主Id
    private var roomOwnerId:String?=null

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.lifecycleOwner = this
        mBinding.m = mViewModel
        mViewModel.selectImageCount.value = filePathList.size
        reportType= intent.getStringExtra("reportType")
        setTitleBarListener(mBinding.titleBar)
        userId=intent.getStringExtra("userId")
        roomId=intent.getStringExtra("roomId")
        roomOwnerId=intent.getStringExtra("roomOwnerId")
        if(reportType== Constants.ReportType.ROOM.type){
            mBinding.titleBar.title = "举报房间"
        }else if(reportType==Constants.ReportType.USER.type){
            mBinding.titleBar.title = "举报用户"
        }

        mViewModel.queryReportInformantType {
            reportTypeList = it?.toMutableList()
            val layout = mBinding.flowlayout
            for (i in it.orEmpty().indices) {
                val textView = LayoutInflater.from(layout.context)
                    .inflate(R.layout.mine_item_report_layout, layout, false) as TextView
                textView.id = i
                textView.text = it?.get(i)?.informantTypeName.orEmpty()
                listPair.add(i to textView)
                textView.setOnClickListener { view ->
                    for (pair in listPair) {
                        pair.second.isSelected = false
                    }
                    mViewModel.reportType.value = reportTypeList?.get(view.id)?.informantTypeId
                    listPair[view.id].second.isSelected = true
                }
                layout.addView(textView, 0)
            }
        }

        mBinding.mPreviewImageView.setPictureList(
            filePathList,
            object : PreviewImageView.OnLoadPhotoListener {
                override fun onPhotoLoading(
                    pos: Int,
                    data: String,
                    imageView: DeleteImageView
                ) {
                    imageView.loadImage(File(data), radius = 8f)
                }
            },
            9
        )
        mBinding.mPreviewImageView.setOnAddPhotoClickListener {
            openPictureSelector(
                this,
                mViewModel.selectMaxImageCount - mBinding.mPreviewImageView.currentFileList.size.orZero()
            ) {
                filePathList.addAll(it.orEmpty())
                mBinding.mPreviewImageView.addData(it.orEmpty())
                mViewModel.selectImageCount.value =
                    mBinding.mPreviewImageView.selectedCount.orZero()
            }
        }

        mBinding.mPreviewImageView.onDeleteListener = object : PreviewImageView.OnDeleteListener {
            override fun onDelete(url: String?) {
                if (filePathList.contains(url)) {
                    filePathList.remove(url);
                }
                mViewModel.selectImageCount.value = filePathList.size
            }
        }

        mBinding.tvConfirm.safeClick {
            if (filePathList.isNotEmpty()) {
                val fileList: MutableList<File> = mutableListOf()
                filePathList.forEach { path ->
                    fileList.add(File(path))
                }
                uploadPicture(fileList) { result ->
                    listImagesParams = result.toMutableList()
                    if (reportType == Constants.ReportType.USER.type) {
                        mViewModel.reportUser(
                            mViewModel.reportType.value.orEmpty(),
                            mBinding.etInput.contentText.orEmpty(), userId.orEmpty(),listImagesParams
                        ) {
                            ToastUtils.showShort("举报成功,我们将对您的反馈第一时间审核")
                            finish()
                        }
                    }else{
                        mViewModel.reportLiveRoom(roomId.orEmpty(),roomOwnerId.orEmpty(),
                            mViewModel.reportType.value.orEmpty(),
                            mBinding.etInput.contentText.orEmpty(), listImagesParams
                        ) {
                            ToastUtils.showShort("举报成功,我们将对您的反馈第一时间审核")
                            finish()
                        }
                    }
                }
            } else {
                if (reportType == Constants.ReportType.USER.type) {
                    mViewModel.reportUser(
                        mViewModel.reportType.value.orEmpty(),
                        mBinding.etInput.contentText.orEmpty(), userId.orEmpty(),listImagesParams
                    ) {
                        ToastUtils.showShort("举报成功,我们将对您的反馈第一时间审核")
                        finish()
                    }
                }else {
                    mViewModel.reportLiveRoom(
                        roomId.orEmpty(),roomOwnerId.orEmpty(),
                        mViewModel.reportType.value.orEmpty(),
                        mBinding.etInput.contentText.orEmpty(),listImagesParams
                    ) {
                        ToastUtils.showShort("举报成功,我们将对您的反馈第一时间审核")
                        finish()
                    }
                }
            }
        }
    }
}