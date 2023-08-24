package com.kissspace.mine.ui.activity

import android.os.Bundle
import android.text.InputFilter.LengthFilter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.router.parseIntent
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.router.RouterPath
import com.kissspace.util.addAfterTextChanged
import com.kissspace.common.util.customToast
import com.kissspace.mine.http.MineApi
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityEditNicknameBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/31 14:47
 * @Description: 编辑昵称页面
 *
 */
@Router(path = RouterPath.PATH_EDIT_NICKNAME)
class EditNicknameActivity : com.kissspace.common.base.BaseActivity(R.layout.mine_activity_edit_nickname) {
    private val nickname: String by parseIntent<String>()
    private val mBinding by viewBinding<MineActivityEditNicknameBinding>()
    override fun initView(savedInstanceState: Bundle?) {


        mBinding.editNickname.apply {
//            filters = arrayOf( EditFilterUtil.getInputFilterEmoji(),
//                    LengthFilter(10))
            filters = arrayOf(LengthFilter(10))
            setText(nickname)
        }

        mBinding.titleBar.setTitleBarListener(
            onLeftClick = {
                finish()
            },
            onRightClick = {
                if (mBinding.editNickname.text.toString().trim().isNotEmpty()) {
                    val param = mutableMapOf<String, Any?>()
                    param["nickname"] = mBinding.editNickname.text.toString().trim()
                    request<Int>(MineApi.API_EDIT_PROFILE, Method.POST, param, onSuccess = {
                        customToast("保存成功，审核中")
                        finish()
                    }, onError = {
                        customToast(it.message)
                    })
                }
            },
        )
        mBinding.editNickname.addAfterTextChanged {
            if (it.isNullOrBlank()) {
                mBinding.titleBar.setRightTitleColor(resources.getColor(com.kissspace.module_common.R.color.color_949499))
            } else {
                mBinding.titleBar.setRightTitleColor(resources.getColor(com.kissspace.module_common.R.color.color_6C74FB))
            }
        }

        mBinding.ivClean.setOnClickListener {
            mBinding.editNickname.setText("")
        }

    }
}