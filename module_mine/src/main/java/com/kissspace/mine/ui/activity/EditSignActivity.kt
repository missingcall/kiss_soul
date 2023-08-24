package com.kissspace.mine.ui.activity

import android.os.Bundle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.router.parseIntent
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.customToast
import com.kissspace.mine.http.MineApi
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityEditSignBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/31 14:47
 * @Description: 编辑昵称页面
 *
 */
@Router(path = RouterPath.PATH_EDIT_SIGN)
class EditSignActivity : com.kissspace.common.base.BaseActivity(R.layout.mine_activity_edit_sign) {
    private val sign by parseIntent<String>()
    private val mBinding by viewBinding<MineActivityEditSignBinding>()

    override fun initView(savedInstanceState: Bundle?) {

        if (sign != "这个人很懒，什么都没留下") {
            mBinding.editSign.editText?.setText(sign)
        }
        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                finish()
            }

            override fun onRightClick(titleBar: TitleBar?) {
                val text = mBinding.editSign.contentText.toString().trim()
                val param = mutableMapOf<String, Any?>()
                param["personalSignature"] = text.ifEmpty { "这个人很懒，什么都没留下" }
                request<Int>(MineApi.API_EDIT_PROFILE, Method.POST, param, onSuccess = {
                    customToast("保存成功，审核中")
                    finish()
                })
            }
        })

    }
}