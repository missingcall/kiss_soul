package com.kissspace.mine.ui.activity.wallet

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import androidx.activity.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.ToastUtils
import com.didi.drouter.annotation.Router
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.Constants
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setMarginStatusBar
import com.kissspace.common.http.getUserInfo
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.RouterPath.PATH_USER_WALLET_OPERATE_SUCCESS
import com.kissspace.common.router.RouterPath.PATH_USER_WITHDRAW_RECODE
import com.kissspace.common.router.jump
import com.kissspace.common.router.parseIntent
import com.kissspace.common.util.customToast
import com.kissspace.common.util.format.Format
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.mine.viewmodel.WalletViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityWalletWithdrawBinding
import com.kissspace.util.immersiveStatusBar
import com.kissspace.util.isNotEmptyBlank
import com.kissspace.util.orZero


/**
 * @Author gaohangbo
 * @Date 2023/1/3 09:48.
 * @Describe 收益提现
 */
@Router(path = RouterPath.PATH_USER_WALLET_WITHDRAW)
class MyWalletWithDrawActivity : BaseActivity(R.layout.mine_activity_wallet_withdraw) {

    private val mBinding by viewBinding<MineActivityWalletWithdrawBinding>()

    private val mViewModel by viewModels<WalletViewModel>()

    private var walletType by parseIntent<String>()

    override fun initView(savedInstanceState: Bundle?) {
        immersiveStatusBar(false)
        mBinding.ivBack.safeClick {
            finish()
        }
        mBinding.viewStatusBar.setMarginStatusBar()
        mBinding.m = mViewModel
        mBinding.lifecycleOwner = this

        mBinding.tvAllWithDraw.safeClick {
            if (mViewModel.withDrawBalance.value.orZero() > withDrawMaxNumber) {
                mBinding.etWithDraw.setText(withDrawMaxNumber.toString())
            } else {
                mBinding.etWithDraw.setText(Format.E.format(mViewModel.withDrawBalance.value.orZero()))
            }
        }

        mBinding.etWithDraw.onAfterTextChanged = {
            if (it.isNotEmptyBlank()) {
                mViewModel.withDrawNumberContent.value = it
            } else {
                mViewModel.withDrawNumberContent.value = null
            }
        }

        when (walletType) {
            Constants.WalletType.EARNS.type -> {
                mViewModel.withDrawImage.value = R.mipmap.mine_wallet_earns_withdraw
                mViewModel.withDrawType.value = Constants.WalletType.EARNS.type
                mViewModel.withDrawTypeTitle.value = "收益提现"

                mViewModel.withDrawTextHint.value =
                    resources.getString(R.string.mine_wallet_withdraw_earns_hint)
                        .replaceFirst("%s", MMKVProvider.wechatPublicAccount)
            }

            Constants.WalletType.DIAMOND.type -> {
                mViewModel.withDrawImage.value = R.mipmap.mine_wallet_diamond_withdraw
                mViewModel.withDrawType.value = Constants.WalletType.DIAMOND.type
                mViewModel.withDrawTypeTitle.value = "钻石提现"
                mViewModel.withDrawTextHint.value =
                    resources.getString(R.string.mine_wallet_withdraw_diamond_hint)
                        .replaceFirst("%s", MMKVProvider.wechatPublicAccount)
            }

            else -> {
            }
        }

        val hint = SpannableString("${mViewModel.withDrawType.value}最低提现10000")

        hint.setSpan(AbsoluteSizeSpan(18, true), 0, hint.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        mBinding.etWithDraw.hint = hint

        mBinding.tvConfirm.safeClick {
            mViewModel.withDrawNumber.value = mViewModel.withDrawNumberContent.value?.toDouble()
            var withdrawType: Int? = null
            if (walletType == Constants.WalletType.EARNS.type) {
                withdrawType = 1
            } else if (walletType == Constants.WalletType.DIAMOND.type) {
                withdrawType = 2
            }
            if (mViewModel.withDrawNumber.value != null) {
                if (mViewModel.withDrawNumber.value.orZero() > mViewModel.withDrawBalance.value.orZero()) {
                    ToastUtils.showShort("提现${walletType}余额不足")
                    return@safeClick
                }
                if (mViewModel.withDrawNumber.value.orZero() < withDrawMinNumber) {
                    ToastUtils.showShort(
                        "提现${walletType}收益不能小于${
                            Format.E.format(
                                withDrawMinNumber
                            )
                        }"
                    )
                    return@safeClick
                }
                if (mViewModel.withDrawNumber.value.orZero() > withDrawMaxNumber) {
                    ToastUtils.showShort(
                        "提现${walletType}不能大于${
                            Format.E.format(
                                withDrawMaxNumber
                            )
                        }"
                    )
                    return@safeClick
                }

                if (mViewModel.withDrawNumber.value.orZero() != 0.0) {
                    //是家族成员才能提现
                    getUserInfo(onSuccess = {
                        if (it.family) {
                            mViewModel.withDrawNumber(
                                mViewModel.withDrawNumber.value.orZero(),
                                withdrawType.orZero()
                            ) { isWithdrawSuccess ->
                                if (isWithdrawSuccess == true) {
                                    ToastUtils.showShort("申请提现成功")
                                    jump(
                                        RouterPath.PATH_USER_WALLET_OPERATE_SUCCESS,
                                        "number" to mViewModel.withDrawNumber.value.orZero(),
                                        "successType" to Constants.SuccessType.WITHDRAW.type,
                                        "walletType" to walletType
                                    )
                                    //设置内容清空
                                    mBinding.etWithDraw.setText("")
                                }
                            }
                        } else {
                            customToast("不是家族成员")
                        }
                    })
                } else {
                    ToastUtils.showShort("请输入提现金额")
                }
            }
        }

        mBinding.tvWithDrawRecode.safeClick {
            jump(
                PATH_USER_WITHDRAW_RECODE,
                "walletType" to walletType
            )
        }
    }


    override fun onResume() {
        super.onResume()
        mViewModel.getMyMoneyBag {
            it?.let {
                mViewModel.walletModel.value = it
                when (walletType) {
                    Constants.WalletType.EARNS.type -> {
                        mViewModel.withDrawBalance.value = it.accountBalance.orZero()
                    }

                    Constants.WalletType.DIAMOND.type -> {
                        mViewModel.withDrawBalance.value = it.diamond.orZero()
                    }

                    else -> {

                    }
                }
            }
        }
    }

    companion object {
        const val withDrawMaxNumber: Int = 9000000
        const val withDrawMinNumber: Int = 10000
    }

}