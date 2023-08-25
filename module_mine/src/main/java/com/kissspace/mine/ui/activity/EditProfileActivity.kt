package com.kissspace.mine.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.ToastUtils
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.*
import com.github.gzuliyujiang.wheelpicker.DatePicker
import com.github.gzuliyujiang.wheelpicker.entity.DateEntity
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.http.uploadPicture
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.getMutable
import com.kissspace.common.util.hideLoading
import com.kissspace.common.util.openPictureSelector
import com.kissspace.util.resToColor
import com.kissspace.common.util.showLoading
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.mine.http.MineApi
import com.kissspace.mine.viewmodel.EditProfileViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityEditProfileBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import org.json.JSONArray
import java.io.File
import java.util.Calendar

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/31 13:35
 * @Description: 编辑个人资料页面
 *
 */
@Router(uri = RouterPath.PATH_EDIT_PROFILE)
class EditProfileActivity : com.kissspace.common.base.BaseActivity(R.layout.mine_activity_edit_profile) {
    private val mBinding by viewBinding<MineActivityEditProfileBinding>()
    private val mViewModel by viewModels<EditProfileViewModel>()

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.vm = mViewModel
        initRecyclerView()
        initEvent()
    }

    private fun initRecyclerView() {
        mBinding.recyclerView.linear(RecyclerView.HORIZONTAL).setup {
            addType<String> { R.layout.mine_layout_add_picture_item }
            onFastClick(R.id.iv_delete) {
                CommonConfirmDialog(this@EditProfileActivity, "确定要删除照片吗?") {
                    if (this) {
                        val model = getModel<String>()
                        removePicture(model)
                    }
                }.show()
            }
        }.models = mutableListOf()
    }

    private fun initEvent() {
        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                finish()
            }
        })
        mBinding.addPicture.setOnClickListener {
            openPictureSelector(this,4 - mBinding.recyclerView.mutable.size) {
                val file = mutableListOf<File>()
                it?.forEach { path ->
                    file.add(File(path))
                }
                uploadPicture(file) { result ->
                    result.addAll(0, mViewModel.userInfo.get()!!.getAllPicture())
                    uploadBgPath(result)
                }
            }
        }

        mBinding.layoutEditNickname.safeClick {
            mViewModel.userInfo.get()?.let {
                jump(RouterPath.PATH_EDIT_NICKNAME, "nickname" to it.nickname)
            }
        }

        mBinding.layoutEditSign.safeClick {
            mViewModel.userInfo.get()?.let {
                jump(RouterPath.PATH_EDIT_SIGN, "sign" to it.getActualPersonalSignature())
            }
        }

        mBinding.layoutEditBirthday.setOnClickListener {
            val picker = DatePicker(this).apply {
                setTitle("")
                setBodyHeight(311)
                okView.setTextColor(com.kissspace.module_common.R.color.color_FFFD62.resToColor())
                cancelView.setTextColor(com.kongzue.dialogx.R.color.white50.resToColor())
                setBackgroundColor(com.kissspace.module_common.R.color.black.resToColor())
                wheelLayout.setIndicatorColor(com.kissspace.module_common.R.color.color_494B70.resToColor())
                topLineView.visibility = View.GONE
                wheelLayout.apply {
                    val str = mViewModel.userInfo.get()?.birthday?.split("-")
                    val defaultDate= DateEntity.target(str?.get(0)?.toInt()?:2000, str?.get(1)?.toInt()?:1, str?.get(2)?.toInt()?:1)
                    setRange(
                        DateEntity.target(1950, 1, 1),
                        DateEntity.target(
                            Calendar.getInstance().get(Calendar.YEAR),
                            Calendar.getInstance().get(Calendar.MONTH) + 1,
                            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                        ),
                        defaultDate
                    )
                    setSelectedTextColor(com.kissspace.module_common.R.color.color_FFFD62.resToColor())
                    setTextColor(com.kongzue.dialogx.R.color.white50.resToColor())
                }
                setOnDatePickedListener { year, month, day ->
                    val monthStr = if (month < 10) "0${month}" else month
                    val dayStr = if (day < 10) "0${day}" else day
                    mViewModel.editBirthday("${year}-${monthStr}-${dayStr}")
                }
            }
            picker.show()
        }

        mBinding.layoutAvatar.setOnClickListener {
            openPictureSelector(this,isCrop = true) {
                uploadPicture(mutableListOf(File(it!![0]))) { pic ->
                    mViewModel.editAvatar(pic[0])
                }
            }
        }
    }

    private fun uploadBgPath(pictures: List<String>?) {
        showLoading()
        val jsonArray = JSONArray()
        pictures?.forEach {
            jsonArray.put(it)
        }
        val param = mutableMapOf<String, Any?>("bgPath" to jsonArray)
        request<Int>(MineApi.API_EDIT_PROFILE, Method.POST, param, onSuccess = {
            hideLoading()
            mViewModel.requestUserInfo()
        }, onError = { _ ->
            ToastUtils.showShort("照片上传失败,请重试")
        })
    }

    private fun removePicture(deletePic: String) {
        showLoading()
        val newList = mutableListOf<String>()
        newList.addAll(mBinding.recyclerView.getMutable())
        newList.remove(deletePic)
        val jsonArray = JSONArray()
        newList.forEach {
            jsonArray.put(it)
        }
        val param = mutableMapOf<String, Any?>("bgPath" to jsonArray)
        request<Int>(MineApi.API_EDIT_PROFILE, Method.POST, param, onSuccess = {
            hideLoading()
            mViewModel.requestUserInfo()
        }, onError = { _ ->
            ToastUtils.showShort("照片删除失败,请重试")
        })
    }

    override fun onResume() {
        super.onResume()
        mViewModel.requestUserInfo()
    }

    @SuppressLint("SetTextI18n")
    override fun createDataObserver() {
        super.createDataObserver()
        lifecycleScope.launchWhenResumed {
            mViewModel.userInfoEvent.collect {
                mBinding.recyclerView.mutable.clear()
                mBinding.recyclerView.addModels(it.bgPath + it.auditingBgPath)
                mBinding.addPicture.visibility =
                    if (it.bgPath.size == 4) View.GONE else View.VISIBLE
                mBinding.tvCountPicture.text = "(${mBinding.recyclerView.mutable.size}/4)"
            }
        }
    }

}