package com.kissspace.mine.ui.activity.wallet

import android.os.Bundle
import android.view.View
import android.view.View.OnFocusChangeListener
import androidx.activity.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.ToastUtils
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.Constants
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setMarginStatusBar
import com.kissspace.common.model.wallet.WalletListModel
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.mine.util.SoftKeyInputHidWidget
import com.kissspace.mine.viewmodel.WalletViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityWalletExchangeBinding
import com.kissspace.module_mine.databinding.MineItemExchangeBinding
import com.kissspace.util.formatDoubleDown
import com.kissspace.util.immersiveStatusBar
import com.kissspace.util.isMultiple
import com.kissspace.util.isNotEmptyBlank
import com.kissspace.util.logE
import com.kissspace.util.orZero


/**
 * @Author gaohangbo
 * @Date 2022/12/31 21:31.
 * @Describe 兑换页面
 */
@Router(path = RouterPath.PATH_USER_WALLET_EXCHANGE)
class MyWalletExchangeActivity : BaseActivity(R.layout.mine_activity_wallet_exchange) {

    private val mBinding by viewBinding<MineActivityWalletExchangeBinding>()

    private val mViewModel by viewModels<WalletViewModel>()

    private val walletList = mutableListOf<WalletListModel>()

    private var walletType: String? = null

    //兑换比例
    private var exchangeRate: Double = 1.0

    override fun initView(savedInstanceState: Bundle?) {
        immersiveStatusBar(false)
        mBinding.viewStatusBar.setMarginStatusBar()
        SoftKeyInputHidWidget.assistActivity(this)
        mBinding.ivBack.safeClick {
            finish()
        }
        mBinding.m = mViewModel
        mBinding.lifecycleOwner = this

        walletType = intent.getStringExtra("walletType")

        when (walletType) {
            Constants.WalletType.EARNS.type -> {
                mViewModel.exchangeType.value = Constants.WalletType.EARNS.type
                mViewModel.exchangeTypeTitleBg.value =
                    resources.getDrawable(R.mipmap.mine_wallet_exchange_earns_text)
                mBinding.tvDetail.visibility = View.GONE
                //exchangeRate = 0.9375
            }

            Constants.WalletType.DIAMOND.type -> {
                mViewModel.exchangeType.value = Constants.WalletType.DIAMOND.type
                mViewModel.exchangeTypeTitleBg.value =
                    resources.getDrawable(R.mipmap.mine_wallet_exchange_diamond)
                mBinding.tvDetail.visibility = View.VISIBLE
                // exchangeRate = 1.0
            }

            else -> {
            }
        }
        mViewModel.walletExchangeHint.value =
            resources.getString(R.string.mine_wallet_exchange_earns_hint)
                .replaceFirst("%s", MMKVProvider.wechatPublicAccount)
        //获取钱包
        mViewModel.getMyMoneyBag {
            it?.let {
                mViewModel.walletModel.value = it
                mBinding.m = mViewModel
            }
        }
        walletList.let {
            it.add(
                WalletListModel(
                    exchangeNumber = formatDoubleDown(600.0 / exchangeRate),
                    rmbNumber = 600.0,
                    isWalletSelected = false
                )
            )
            it.add(
                WalletListModel(
                    exchangeNumber = formatDoubleDown(1200.0 / exchangeRate),
                    rmbNumber = 1200.0,
                    isWalletSelected = false
                )
            )
            it.add(
                WalletListModel(
                    exchangeNumber = formatDoubleDown(1800.0 / exchangeRate),
                    rmbNumber = 1800.0,
                    isWalletSelected = false
                )
            )
            it.add(
                WalletListModel(
                    exchangeNumber = formatDoubleDown(3000.0 / exchangeRate),
                    rmbNumber = 3000.0,
                    isWalletSelected = false
                )
            )
            it.add(
                WalletListModel(
                    exchangeNumber = formatDoubleDown(6800.0 / exchangeRate),
                    rmbNumber = 6800.0,
                    isWalletSelected = false
                )
            )
            it.add(
                WalletListModel(
                    exchangeNumber = formatDoubleDown(9800.0 / exchangeRate),
                    rmbNumber = 9800.0,
                    isWalletSelected = false
                )
            )
        }

        mBinding.rvType.isNestedScrollingEnabled = false

        mBinding.rvType.grid(3).setup {
            addType<WalletListModel>(R.layout.mine_item_exchange)
            singleMode = true
            onChecked { position, isChecked, _ ->
                val model = getModel<WalletListModel>(position)
                logE("onChecked" + mViewModel.exchangeNumber.value.orZero().toString() + "---")
                model.isWalletSelected = isChecked
                model.notifyChange()
                //不加有可能没有选中的值把当前的值覆盖了
                if (isChecked) {
                    mViewModel.exchangeRmb.value = model.rmbNumber
                    mViewModel.exchangeNumber.value = model.exchangeNumber
                }
            }
            onBind {
                val viewBinding = getBinding<MineItemExchangeBinding>()
                if (walletType == Constants.WalletType.EARNS.type) {
                    viewBinding.ivType.setImageResource(R.mipmap.mine_wallet_earns_withdraw)
                } else if (walletType == Constants.WalletType.DIAMOND.type) {
                    viewBinding.ivType.setImageResource(R.mipmap.mine_wallet_diamond_withdraw)
                }
            }
            onFastClick(R.id.cl_charge) {
                //设置内容清空
                mBinding.etText.setText("")
                mBinding.etText.clearFocus()
                setChecked(modelPosition, true)
            }
        }.models = walletList

        //设置第一个选中
        mBinding.rvType.bindingAdapter.setChecked(0, true)

        mViewModel.exchangeNumber.value =
            (mBinding.rvType.bindingAdapter.getModel(0) as WalletListModel).exchangeNumber

        mBinding.etText.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                mBinding.rvType.bindingAdapter.checkedAll(false)
                mViewModel.exchangeNumber.value = null
                mViewModel.exchangeRmb.value = null
            }
        }

        mBinding.etText.onAfterTextChanged = {
            if (it.isNotEmptyBlank()) {
                mViewModel.exchangeNumber.value =
                    formatDoubleDown(mBinding.etText.text.toString().toDouble() / exchangeRate)
                mViewModel.exchangeRmb.value = mBinding.etText.text.toString().toDouble()
            } else {
                //没有选中条目
                if (mBinding.rvType.bindingAdapter.checkedCount == 0) {
                    mViewModel.exchangeNumber.value = null
                    mViewModel.exchangeRmb.value = null
                }
            }
        }

        mBinding.clExchange.safeClick {
            if (isMultiple(mViewModel.exchangeRmb.value.orZero(), 100.0)) {
                ToastUtils.showShort("请输入100的倍数")
                return@safeClick
            }
            logE("exchangeCoin${mViewModel.exchangeRmb.value}")
            logE("exchangeNumber${mViewModel.exchangeNumber.value}")
            when (walletType) {
                Constants.WalletType.EARNS.type -> {
                    CommonConfirmDialog(this, "确定用${walletType}兑换成金币吗") {
                        if (this) {
                            mViewModel.exchangeEarns(mViewModel.exchangeRmb.value.orZero()) {
                                if (it == true) {
                                    val amount = if (mBinding.etText.text.isNullOrEmpty()) {
                                        mBinding.rvType.bindingAdapter.getCheckedModels<WalletListModel>()[0].exchangeNumber
                                    } else {
                                        mBinding.etText.text.toString().toDouble()
                                    }

                                    jump(
                                        RouterPath.PATH_USER_WALLET_OPERATE_SUCCESS,
                                        "number" to amount,
                                        "walletType" to Constants.WalletType.EARNS.type,
                                        "successType" to Constants.SuccessType.EXCHANGE.type
                                    )
                                }
                            }
                        }
                    }.show()
                }

                Constants.WalletType.DIAMOND.type -> {
                    CommonConfirmDialog(this, "确定用${walletType}兑换成金币吗") {
                        if (this) {
                            mViewModel.exchangeDiamond(mViewModel.exchangeRmb.value.orZero()) {
                                if (it == true) {
                                    val amount = mViewModel.exchangeRmb.value.orZero()
                                    jump(
                                        RouterPath.PATH_USER_WALLET_OPERATE_SUCCESS,
                                        "number" to amount,
                                        "walletType" to Constants.WalletType.EARNS.type,
                                        "successType" to Constants.SuccessType.EXCHANGE.type
                                    )
                                }
                            }
                        }
                    }.show()
                }

                else -> {
                }
            }
        }
        mBinding.tvDetail.safeClick {
            jump(
                RouterPath.PATH_USER_WALLET_DIAMOND_EXCHANGE_DETAIL,
                "walletType" to Constants.WalletType.DIAMOND.type
            )
        }
    }

    override fun onResume() {
        super.onResume()
        mViewModel.getMyMoneyBag {
            it?.let {
                when (walletType) {
                    Constants.WalletType.EARNS.type -> {
                        mViewModel.mExchangeBalance.value = it.accountBalance.orZero()
                    }

                    Constants.WalletType.DIAMOND.type -> {
                        mViewModel.mExchangeBalance.value = it.diamond.orZero()
                    }

                    else -> {
                    }
                }
                mViewModel.walletModel.value = it
            }
        }
    }
}