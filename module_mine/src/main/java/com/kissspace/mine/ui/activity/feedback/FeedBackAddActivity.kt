package com.kissspace.mine.ui.activity.feedback

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.ToastUtils
import com.didi.drouter.annotation.Router
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.router.parseIntent
import com.kissspace.common.ext.safeClick
import com.kissspace.common.http.uploadPicture
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.openPictureSelector
import com.kissspace.util.orZero
import com.kissspace.mine.viewmodel.FeedBackViewModel
import com.kissspace.mine.widget.DeleteImageView
import com.kissspace.mine.widget.PreviewImageView
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivtiyAddFeedbackBinding
import com.kissspace.util.hideKeyboard
import com.kissspace.util.isClickThisArea
import com.kissspace.util.loadImage
import java.io.File

/**
 * @Author gaohangbo
 * @Date 2023/1/7 14:27.
 * @Describe
 */
@Router(path = RouterPath.PATH_ADD_FEEDBACK)
class FeedBackAddActivity : com.kissspace.common.base.BaseActivity(R.layout.mine_activtiy_add_feedback) {
    private val mBinding by viewBinding<MineActivtiyAddFeedbackBinding>()
    private val mViewModel by viewModels<FeedBackViewModel>()

    private var listImagesParams: MutableList<String> = mutableListOf()

    private var filePathList: MutableList<String> = mutableListOf()

    private var feedBackTypeId by parseIntent<String>()

    private var feedBackTypeName: String by parseIntent<String>()

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                super.onLeftClick(titleBar)
                finish()
            }
        })

        mViewModel.feedBackTextType.set(feedBackTypeName)
        //livedata 监听生命周期
        mBinding.lifecycleOwner = this

        mBinding.m = mViewModel
        mViewModel.selectImageCount.value = filePathList.size
        mBinding.mPreviewImageView.setDatas(
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
            3
        )

        mBinding.tvUpdate.safeClick {
            if (filePathList.isNotEmpty()) {
                val fileList: MutableList<File> = mutableListOf()
                filePathList.forEach { path ->
                    fileList.add(File(path))

                }
                uploadPicture(fileList) { result ->
                    listImagesParams = result.toMutableList()
                    mViewModel.addFeedBackType(
                        feedBackTypeId,
                        mBinding.etFeedback.editText?.text.toString(),
                        listImagesParams
                    ) {
                        ToastUtils.showShort("上传成功")
                        setResult(RESULT_OK,intent.putExtra("result", FeedBackAddResult))
                        finish()
                    }
                }
            } else {
                mViewModel.addFeedBackType(
                    feedBackTypeId,
                    mBinding.etFeedback.editText?.text.toString(),
                    listImagesParams
                ) {
                    ToastUtils.showShort("操作成功")
                    finish()
                }
            }
        }

        mBinding.mPreviewImageView.setOnAddPhotoClickListener {
            openPictureSelector(mViewModel.selectMaxImageCount - mBinding.mPreviewImageView.datas.size.orZero()) {
                filePathList.addAll(it.orEmpty())
                mBinding.mPreviewImageView.addData(it.orEmpty())
                mViewModel.selectImageCount.value = mBinding.mPreviewImageView.selectedCount.orZero()
            }
        }
        mBinding.mPreviewImageView.onDeleteListener = object : PreviewImageView.OnDeleteListener {
            override fun onDelete(url: String?) {
                if (filePathList.contains(url)) {
                    filePathList.remove(url)
                }
                mViewModel.selectImageCount.value = filePathList.size
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {  //把操作放在用户点击的时候
            val v = currentFocus //得到当前页面的焦点,ps:有输入框的页面焦点一般会被输入框占据
            currentFocus?.let {
                if (!isClickThisArea(it, ev)) { //判断用户点击的是否是输入框以外的区域
                    hideKeyboard()//收起键盘
                } else {
                    mBinding.etFeedback.editText?.isCursorVisible = true // 再次点击显示光标  编辑框获取触摸监听
                }
            }

        }
        return super.dispatchTouchEvent(ev)
    }
    companion object {
        const val FeedBackAddResult="FeedBackAddResult"
    }
}

