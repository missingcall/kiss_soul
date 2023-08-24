package com.kissspace.setting.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.kissspace.util.toast
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.http.getUserInfo
import com.kissspace.common.router.RouterPath
import com.kissspace.util.addAfterTextChanged
import com.kissspace.module_setting.R
import com.kissspace.module_setting.databinding.SettingActivityBindBankcardBinding
import com.kissspace.setting.viewmodel.BindBankCardViewModel

/**
 * @Author gaohangbo
 * @Date 2023/1/2 17:22.
 * @Describe 绑定银行卡页面
 */
@Router(uri = RouterPath.PATH_BIND_BANKCARD)
class BindBankCardActivity : com.kissspace.common.base.BaseActivity(R.layout.setting_activity_bind_bankcard) {
    private val mBinding by viewBinding<SettingActivityBindBankcardBinding>()
    private val mViewModel by viewModels<BindBankCardViewModel>()

    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        mBinding.m = mViewModel
        mBinding.lifecycleOwner = this
        getUserInfo(onSuccess = {
            mBinding.etBankAccount.setText(it.cardNo)
            mBinding.etBankAccount.setSelection(it.cardNo.length)
        })


        mBinding.etBankAccount.addAfterTextChanged {
            mViewModel.userBankCardNumber.value = it.toString()
        }

        mBinding.tvBind.safeClick {
            val cardNumber = mViewModel.userBankCardNumber.value.orEmpty()
            if (cardNumber.length in 15..20){
                mViewModel.bindBankCardViewModel(mViewModel.userBankCardNumber.value.orEmpty()) {
                    toast("绑定银行卡成功")
                    finish()
                }
            }else{
                toast("银行卡长度15-20")
            }
        }
    }
}