package com.kissspace.mine.ui.activity.wallet

import android.os.Bundle
import android.text.SpannableString
import android.util.Log
import androidx.activity.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.Constants
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.parseIntent
import com.kissspace.common.util.format.Format
import com.kissspace.mine.viewmodel.WalletViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityWalletExchangeSuccessBinding

/**
 * @Author gaohangbo
 * @Date 2023/1/1 11:05.
 * @Describe 操作成功页面
 */
@Router(path = RouterPath.PATH_USER_WALLET_OPERATE_SUCCESS)
class MyWalletOperateSuccessActivity : BaseActivity(R.layout.mine_activity_wallet_exchange_success) {
    private val mBinding by viewBinding<MineActivityWalletExchangeSuccessBinding>()
    //金额
    private var number by parseIntent<Double>()
    private var walletType by parseIntent<String>()
    private var successType by parseIntent<String>()
    private var userId by parseIntent<String>()
    private var transferSuccessText by parseIntent<CharSequence>()
    private val mViewModel by viewModels<WalletViewModel>()

    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)

        mBinding.tvConfirm.safeClick {
            finish()
        }
        mBinding.lifecycleOwner = this
        mBinding.m = mViewModel
        when (successType) {
            Constants.SuccessType.EXCHANGE.type -> {
                mViewModel.isShowWithDrawStatus.value = false
                mViewModel.isShowSuccessOperate.value = false
                mViewModel.isShowWhiteBackground.value = true
                mViewModel.successMessage.value = "金币兑换成功"
                mViewModel.successNumber.value = "+${Format.E.format(number)}"
            }
            Constants.SuccessType.TRANSFER.type -> {
                mViewModel.isShowWithDrawStatus.value = false
                mViewModel.isShowSuccessOperate.value = true
                mViewModel.isShowWhiteBackground.value = false
                if(userId.isNotEmpty()){
                    mViewModel.transferSuccessText.value = SpannableString("ID:${userId}")
                }else{
                    mViewModel.transferSuccessText.value = transferSuccessText
                }
                when (walletType) {
                    Constants.WalletType.EARNS.type -> {
                        mViewModel.successMessage.value = "收益转账成功"
                    }
                    Constants.WalletType.DIAMOND.type -> {
                        mViewModel.successMessage.value = "钻石转账成功"
                    }
                    Constants.WalletType.COIN.type -> {
                        mViewModel.successMessage.value = "金币转账成功"
                    }
                    else ->{

                    }
                }
                mViewModel.successNumber.value = "-${Format.E.format(number)}"
            }
            Constants.SuccessType.WITHDRAW.type -> {
                mViewModel.isShowWithDrawStatus.value = true
                mViewModel.isShowSuccessOperate.value = false
                mViewModel.isShowWhiteBackground.value = true
                when (walletType) {
                    Constants.WalletType.EARNS.type -> {
                        mViewModel.successMessage.value = "收益提现"
                    }
                    Constants.WalletType.DIAMOND.type -> {
                        mViewModel.successMessage.value = "钻石提现"
                    }
                    else ->{

                    }
                }
                mViewModel.successNumber.value = "-${Format.E.format(number)}"
            }
            else -> {}

        }

    }

}