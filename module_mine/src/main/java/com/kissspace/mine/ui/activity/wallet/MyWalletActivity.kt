package com.kissspace.mine.ui.activity.wallet

import android.os.Bundle
import androidx.activity.viewModels
import com.didi.drouter.annotation.Router
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.binding.dataBinding
import com.kissspace.common.config.Constants
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.http.checkUserPermission
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.showAuthenticationDialog
import com.kissspace.mine.viewmodel.WalletViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityWalletBinding
import com.kissspace.util.logE
import com.kissspace.util.toast


/**
 * @Author gaohangbo
 * @Date 2022/12/29 12:23.
 * @Describe 我的钱包页面
 */
@Router(path = RouterPath.PATH_USER_WALLET)
class MyWalletActivity : BaseActivity(R.layout.mine_activity_wallet) {

    private val mBinding by dataBinding<MineActivityWalletBinding>()

    private val mViewModel by viewModels<WalletViewModel>()

    private var isIntData = true

    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)

        mBinding.lifecycleOwner = this

        mBinding.m = mViewModel

        checkUserPermission(Constants.UserPermission.PERMISSION_TRANSFER_COIN) {
            mViewModel.exchangeCoinPermission.value = it
        }
        checkUserPermission(Constants.UserPermission.PERMISSION_TRANSFORMER_REWARD) {
            mViewModel.isTransferReward.set(it)
        }

        //获取钱包
        getMoney()

        isIntData = false

        //充值接口
        mBinding.tvGoldRecharge.safeClick {
            jump(RouterPath.PATH_USER_WALLET_GOLD_RECHARGE)
        }

        //金币转账
        mBinding.tvGoldTransfer.safeClick {
            jump(
                RouterPath.PATH_USER_WALLET_TRANSFER,
                "walletType" to Constants.WalletType.COIN.type
            )
        }

        mBinding.tvCoinDetail.safeClick {
            jump(
                RouterPath.PATH_USER_WALLET_COIN_DIAMOND_DETAIL,
                "walletType" to Constants.WalletType.COIN.type
            )
        }

        mBinding.tvDiamondDetail.safeClick {
            jump(
                RouterPath.PATH_USER_WALLET_COIN_DIAMOND_DETAIL,
                "walletType" to Constants.WalletType.DIAMOND.type
            )
        }

        mBinding.tvProfitDetail.safeClick {
            jump(
                RouterPath.PATH_USER_WALLET_COIN_DIAMOND_DETAIL,
                "walletType" to Constants.WalletType.EARNS.type
            )
        }

        mBinding.tvEarnsExchange.safeClick {
            jump(
                RouterPath.PATH_USER_WALLET_EXCHANGE,
                "walletType" to Constants.WalletType.EARNS.type
            )
        }

        mBinding.tvEarnsWithdraw.safeClick {
            bindAlipay(Constants.WalletType.EARNS.type)
        }

        //钻石兑换
        mBinding.tvDiamondExchange.safeClick {
            jump(
                RouterPath.PATH_USER_WALLET_EXCHANGE,
                "walletType" to Constants.WalletType.DIAMOND.type
            )
        }

        //提现
        mBinding.tvDiamondWithdraw.safeClick {
            bindAlipay(Constants.WalletType.DIAMOND.type)
        }

        //钻石转账
        mBinding.tvDiamondTransfer.safeClick {
            jump((RouterPath.PATH_USER_WALLET_UPDATE_FAMILY_MEMBER_LIST))
        }

        mBinding.tvTransferWithdraw.safeClick {
            jump(
                RouterPath.PATH_USER_WALLET_TRANSFER,
                "walletType" to Constants.WalletType.EARNS_TRANSFER.type
            )
        }

        FlowBus.observerEvent<Event.RefreshCoin>(this) {
            getMoney()
        }

    }

    override fun onResume() {
        super.onResume()
        if (!isIntData) {
            getMoney()
        }
    }

    private fun bindAlipay(walletType: String) {
        if (!MMKVProvider.authentication) {
            showAuthenticationDialog {
                jump(RouterPath.PATH_USER_IDENTITY_AUTH)
            }
        } else {
            if (mViewModel.walletModel.value?.isBindAliPay == false) {
                toast("请绑定支付宝账号")
                jump(RouterPath.PATH_BIND_ALIPAY)
            } else {
                logE("mViewModel.accountBalance.value" + mViewModel.accountBalance.value)
                jump(
                    RouterPath.PATH_USER_WALLET_WITHDRAW,
                    "walletType" to walletType
                )
            }
        }
    }

    private fun getMoney() {
        mViewModel.getMyMoneyBag {
            it?.let {
                mViewModel.walletModel.value = it
            }
        }
    }
}