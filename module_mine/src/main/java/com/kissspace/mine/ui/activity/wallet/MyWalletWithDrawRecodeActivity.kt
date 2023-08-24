package com.kissspace.mine.ui.activity.wallet

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.Constants
import com.kissspace.common.router.parseIntent
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.model.wallet.WalletExchangeRecode
import com.kissspace.common.router.RouterPath
import com.kissspace.util.orZero
import com.kissspace.mine.viewmodel.WalletViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityWalletWithdrawRecodeBinding
import com.kissspace.module_mine.databinding.MineListItemWalletExchangeRecodeBinding
import com.kissspace.util.logE

/**
 * @Author gaohangbo
 * @Date 2023/1/3 09:48.
 * @Describe 提现记录页面
 */
@Router(path = RouterPath.PATH_USER_WITHDRAW_RECODE)
class MyWalletWithDrawRecodeActivity : BaseActivity(R.layout.mine_activity_wallet_withdraw_recode) {
    private val mBinding by viewBinding<MineActivityWalletWithdrawRecodeBinding>()
    private val mViewModel by viewModels<WalletViewModel>()
    private val walletType: String by parseIntent()
    private var withdrawType: Int? = null

    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        if (walletType == Constants.WalletType.EARNS.type) {
            withdrawType = 1
        } else if (walletType == Constants.WalletType.DIAMOND.type) {
            withdrawType = 2
        }
        mBinding.pageRefreshLayout.apply {
            onRefresh {
                initData(true)
            }
            onLoadMore {
                initData(false)
            }
        }
        mBinding.rvExchangeRecode.linear().setup {
            addType<WalletExchangeRecode>(R.layout.mine_list_item_wallet_exchange_recode)
            onBind {
                val model = getModel<WalletExchangeRecode>()
                when (val viewBinding = getBinding<ViewDataBinding>()) {
                    is MineListItemWalletExchangeRecodeBinding -> {
                        when (model.status) {
                            0 -> {
                                viewBinding.tvPass.text =
                                    "-" + com.kissspace.common.util.format.Format.O_OO.format(
                                        model.withdrawCoin
                                    )
                                viewBinding.tvStatus.text = "审核中"
                            }
                            1 -> {
                                viewBinding.tvPass.text =
                                    "-" + com.kissspace.common.util.format.Format.O_OO.format(
                                        model.withdrawCoin
                                    )
                                var distributionChannel=""
                                if(model.distributionChannel=="001"){
                                    distributionChannel="银行卡"
                                }else if(model.distributionChannel=="002"){
                                    distributionChannel="支付宝"
                                }
                                viewBinding.tvStatus.text = "提现成功,${distributionChannel}"
                            }
                            2 -> {
                                viewBinding.tvPass.text =
                                    "+" + com.kissspace.common.util.format.Format.O_OO.format(
                                        model.withdrawCoin
                                    )
                                viewBinding.tvStatus.text = "提现失败,${model.remark}"
                            }
                        }
                    }
                }
            }
        }.models = mutableListOf()
        initData(true)
    }

    //提现记录
    private fun initData(isRefresh: Boolean) {
        if(isRefresh){
            mBinding.pageRefreshLayout.index = 1
        }
        mViewModel.getWalletExchangeDetailRecode(
            mBinding.pageRefreshLayout.index, withdrawType.orZero()
        ) {
            if (isRefresh) {
                mBinding.rvExchangeRecode.bindingAdapter.mutable.clear()
                if (it?.walletExchangeRecodeList?.size == 0) {
                    mBinding.pageRefreshLayout.showEmpty()
                } else {
                    mBinding.pageRefreshLayout.addData(
                        it?.walletExchangeRecodeList
                    )
                    mBinding.pageRefreshLayout.showContent()
                }
                mBinding.pageRefreshLayout.finishRefresh()
            } else {
                mBinding.pageRefreshLayout.addData(
                    it?.walletExchangeRecodeList
                )
                mBinding.pageRefreshLayout.finishLoadMore()
            }
            logE("it.total" + it?.total)
            if (mBinding.rvExchangeRecode.bindingAdapter.models?.size == it?.total) {
                mBinding.pageRefreshLayout.finishLoadMoreWithNoMoreData()
            }else{
                mBinding.pageRefreshLayout.setNoMoreData(false)
            }
        }

    }


}