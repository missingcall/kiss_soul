package com.kissspace.login.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.viewModels
import com.didi.drouter.annotation.Router
import com.kissspace.util.finishActivity
import com.kissspace.util.toast
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.binding.dataBinding
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.http.uploadPicture
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.hideLoading
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.openCamera
import com.kissspace.common.util.openPictureSelector
import com.kissspace.common.util.showLoading
import com.kissspace.login.viewmodel.EditProfileViewModel
import com.kissspace.login.widget.ChangeAvatarDialog
import com.kissspace.module_login.R
import com.kissspace.module_login.databinding.LoginActivityEditProfileBinding
import com.kissspace.util.hideKeyboard
import com.kissspace.util.isClickThisArea
import java.io.File

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/18
 * @Description:  完善个人信息
 *
 */
@Router(path = RouterPath.PATH_LOGIN_EDIT_PROFILE)
class LoginEditProfileActivity : BaseActivity(R.layout.login_activity_edit_profile) {
    private val mBinding by dataBinding<LoginActivityEditProfileBinding>()
    private val mViewModel by viewModels<EditProfileViewModel>()
    override fun initView(savedInstanceState: Bundle?) {
        mBinding.vm = mViewModel
        mViewModel.requestUserInfo()
        mBinding.lltSexMale.setOnClickListener {
            mViewModel.userInfoBean.get()?.let {
                it.sex = Constants.SEX_MALE
            }
            mViewModel.userInfoBean.notifyChange()
        }
        mBinding.lltSexFemale.setOnClickListener {
            mViewModel.userInfoBean.get()?.let {
                it.sex = Constants.SEX_FEMALE
            }
            mViewModel.userInfoBean.notifyChange()
        }
        mBinding.textSubmit.safeClick {
            mViewModel.userInfoBean.get()?.let {
                val nickname = it.nickname
                if (nickname.isEmpty()) {
                    toast("请填写昵称")
                    return@safeClick
                }
                if (nickname.contains(" ")) {
                    toast("昵称请不要输入空格")
                    return@safeClick
                }
                if (nickname.length < 2) {
                    toast("昵称最少填写2个字符")
                    return@safeClick
                }
                if (nickname.length > 15) {
                    toast("昵称最多可填写15个字符")
                    return@safeClick
                }
                showLoading()
                mViewModel.editUserInfo({
                    hideLoading()
                    MMKVProvider.isEditProfile = true
                    jump(RouterPath.PATH_MAIN, "index" to 0)
                    finishActivity<LoginEditProfileActivity>()
                },{
                    hideLoading()
                })
            }
        }

        mBinding.tvChoosePicture.setOnClickListener {
            val dialog = ChangeAvatarDialog()
            dialog.setCallBack { index, url ->
                if (url != null) {
                    mViewModel.userInfoBean.get()?.let {
                        it.profilePath = url
                    }
                    mViewModel.userInfoBean.notifyChange()
                } else {
                    if (index == 0) {
                        openCamera { path ->
                            path?.let { upload(path) }
                        }
                    } else {
                        openPictureSelector(isCrop = true) {
                            it?.let { upload(it[0]) }
                        }
                    }
                }
            }
            dialog.show(supportFragmentManager)
        }
    }

    private fun upload(url: String) {
        uploadPicture(mutableListOf(File(url))) { urls ->
            mViewModel.userInfoBean.get()!!.profilePath = urls[0]
            mViewModel.userInfoBean.notifyChange()
        }
    }


    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            true
        } else super.dispatchKeyEvent(event)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {  //把操作放在用户点击的时候
            currentFocus?.let {
                if (!isClickThisArea(it, ev)) { //判断用户点击的是否是输入框以外的区域
                    hideKeyboard()//收起键盘
                } else {
                    mBinding.editNickname.isCursorVisible = true // 再次点击显示光标  编辑框获取触摸监听
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}