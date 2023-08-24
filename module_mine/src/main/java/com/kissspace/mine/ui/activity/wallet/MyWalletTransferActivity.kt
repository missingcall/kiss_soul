package com.kissspace.mine.ui.activity.wallet

import android.os.Build
import android.os.Bundle
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import androidx.activity.viewModels
import com.didi.drouter.annotation.Router
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.binding.dataBinding
import com.kissspace.common.callback.ActivityResult
import com.kissspace.common.callback.ActivityResult.TransferAccount
import com.kissspace.common.config.Constants
import com.kissspace.common.config.Constants.TypeSendSms
import com.kissspace.common.ext.*
import com.kissspace.common.http.getUserInfo
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.RouterPath.PATH_USER_WALLET_OPERATE_SUCCESS
import com.kissspace.common.util.*
import com.kissspace.common.util.format.Format
import com.kissspace.mine.widget.TransferConfirmDialog
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityWalletTransferBinding
import com.kissspace.common.model.family.FamilyMemberRecord
import com.kissspace.common.router.jump
import com.kissspace.common.router.parseIntent
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.mine.viewmodel.TransferViewModel
import com.kissspace.util.*
import com.kissspace.util.activityresult.registerForStartActivityResult
import okhttp3.internal.filterList

/**
 * @Author gaohangbo
 * @Date 2023/1/1 12:06.
 * @Describe 转账页面
 */
@Router(path = RouterPath.PATH_USER_WALLET_TRANSFER)
class MyWalletTransferActivity : BaseActivity(R.layout.mine_activity_wallet_transfer) {

    private val mBinding by dataBinding<MineActivityWalletTransferBinding>()

    private val mViewModel by viewModels<TransferViewModel>()

    private var transferNumber: Double? = null

    private var walletType by parseIntent<String>()

    private var transferUserId by parseIntent<String>()

    private var transferFamilyMemberList: ArrayList<FamilyMemberRecord>? = null

    private val startActivityLauncher = registerForStartActivityResult { result ->
        if (result.resultCode == RESULT_OK) {
            mViewModel.transferCoin(
                mViewModel.transferUserNumber.value.orZero(),
                mViewModel.transferUserId.value.orEmpty()
            ) { isTransferSuccess ->
                customToast("转账成功")
                isTransferSuccess?.let {
                    jump(
                        PATH_USER_WALLET_OPERATE_SUCCESS,
                        "type" to Constants.SuccessType.TRANSFER.type,
                        "number" to mViewModel.transferUserNumber.value.orZero(),
                        "userId" to mViewModel.transferUserId.value.orEmpty(),
                        "walletType" to Constants.WalletType.COIN.type,
                        "successType" to Constants.SuccessType.TRANSFER.type
                    )
                }
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        immersiveStatusBar(false)
        mBinding.ivBack.safeClick {
            finish()
        }
        mBinding.viewStatusBar.setMarginStatusBar()
        mBinding.lifecycleOwner = this
        mBinding.m = mViewModel

        transferFamilyMemberList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(
                "transferFamilyMemberList",
                FamilyMemberRecord::class.java
            )
        } else {
            intent.getParcelableArrayListExtra("transferFamilyMemberList")
        }

        mViewModel.isTransferMultiple.value = transferFamilyMemberList?.size.orZero() > 1

        when (walletType) {
            Constants.WalletType.COIN.type -> {
                mViewModel.transferTitle.value = "金币转账"
                mViewModel.transferTitleBg.value =
                    resources.getDrawable(R.mipmap.mine_icon_wallet_coin_transfer_text)
                mViewModel.transferType.value = Constants.WalletType.COIN.type
                mViewModel.transferImage.value = R.mipmap.mine_wallet_gold
//                mViewModel.isShowTransferTrueMoney.value = false
                mViewModel.transferWalletHint.value =
                    resources.getString(R.string.mine_wallet_transfer_hint)
                        .replaceFirst("%s", MMKVProvider.wechatPublicAccount)
            }

            Constants.WalletType.DIAMOND.type -> {
                mViewModel.transferTitle.value = "钻石转账"
                mViewModel.transferTitleBg.value =
                    resources.getDrawable(R.mipmap.mine_icon_wallet_diamond_transfer_text)
                mViewModel.transferType.value = Constants.WalletType.DIAMOND.type
                mViewModel.transferImage.value = R.mipmap.mine_wallet_diamond_withdraw
//                mViewModel.isShowTransferTrueMoney.value = false
                mViewModel.transferWalletHint.value =
                    resources.getString(R.string.mine_wallet_transfer_diamond_hint)
                        .replaceFirst("%s", MMKVProvider.wechatPublicAccount)
                if (transferUserId.isNotEmptyBlank()) {
                    mBinding.etUserId.setText(transferUserId)
                    mBinding.etUserId.isEnabled = false
                    mViewModel.transferUserId.value = transferUserId
                }
            }

            else -> {

            }
        }
        val spannableStringBuilder = SpannableStringBuilder("转账给用户")
        spannableStringBuilder.append(getSpannableString())
        mViewModel.multipleTransferText.value = spannableStringBuilder

        val transferTitleHint: SpannableString = if (transferFamilyMemberList != null) {
            SpannableString("(批量转账每人最低为100)")
        } else {
            SpannableString("(${mViewModel.transferType.value}最低转账为100)")
        }

        mViewModel.transferTitleHint.value = transferTitleHint.toString()

        mBinding.etUserId.addAfterTextChanged {
            mViewModel.transferUserId.value = it.trimString()
        }
        val spannableHint: SpannableString = if (transferFamilyMemberList != null) {
            SpannableString("钻石每人最低转账${minTransferValue}")
        } else {
            SpannableString("(${mViewModel.transferType.value}最低转账为${minTransferValue})")
        }
        spannableHint.setSpan(
            AbsoluteSizeSpan(15, true),
            0,
            spannableHint.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        mBinding.etGold.hint = spannableHint

        mBinding.etGold.onAfterTextChanged = {
            if (it.isNotEmptyBlank()) {
                mViewModel.transferUserNumber.value =
                    it.trimString().toDouble()
                if (walletType == Constants.WalletType.COIN.type) {
                    //只有金币才显示对应的手续费
                    // 大于100显示金币金额
//                    if (mViewModel.transferUserNumber.value.orZero() < 100) {
//                        mViewModel.coinTransferUserNumberString.value = ""
//                    } else {
//                        logE("mViewModel.transferUserNumber.value")
//                        //四舍五入
//                        mViewModel.coinTransferUserNumberString.value =
//                            Format.O_OO.format(mViewModel.transferUserNumber.value.orZero())
//                    }
                }
            } else {
                mViewModel.transferUserNumber.value = 0.0
            }
        }

        mBinding.tvConfirm.safeClick {
            when (walletType) {
                Constants.WalletType.COIN.type -> {
                    if (mViewModel.transferUserNumber.value.orZero() == 0.0) {
                        customToast("请输入转账金币")
                        return@safeClick
                    } else if (mViewModel.transferUserNumber.value.orZero() < 100) {
                        customToast("金币转账最小为100")
                        return@safeClick
                    }
                    mViewModel.loadUserByDisplayId(
                        mViewModel.transferUserId.value.orEmpty(),
                        onSuccess = {
                            val transferConfirmDialog = TransferConfirmDialog.newInstance(it)
                            transferConfirmDialog.show(supportFragmentManager)
                            transferConfirmDialog.setCallBack {
                                mViewModel.transferCoin(
                                    mViewModel.transferUserNumber.value.orZero(),
                                    mViewModel.transferUserId.value.orEmpty()
                                ) { isTransferSuccess ->
                                    customToast("转账成功")
                                    isTransferSuccess?.let {
                                        jump(
                                            PATH_USER_WALLET_OPERATE_SUCCESS,
                                            "type" to Constants.SuccessType.TRANSFER.type,
                                            "number" to mViewModel.transferUserNumber.value.orZero(),
                                            "userId" to mViewModel.transferUserId.value.orEmpty(),
                                            "walletType" to Constants.WalletType.COIN.type,
                                            "successType" to Constants.SuccessType.TRANSFER.type
                                        )
                                    }
                                }
//                                logE("转账数目" + mViewModel.transferUserNumber.value.orZero())
//                                jump(
//                                    RouterPath.PATH_SEND_SMS_CODE,
//                                    "type" to TransferAccount,
//                                    activity = this,
//                                    resultLauncher = startActivityLauncher
//                                )
                            }
                        })
                }

                Constants.WalletType.DIAMOND.type -> {
                    if (mViewModel.transferUserNumber.value.orZero() == 0.0) {
                        customToast("请输入钻石金币")
                        return@safeClick
                    } else if (mViewModel.transferUserNumber.value.orZero() < 100) {
                        customToast("钻石转账最小为${minTransferValue}")
                        return@safeClick
                    }
                    //如果是家族长
                    setApplicationValue(
                        TypeSendSms,
                        Constants.WalletType.DIAMOND.type,
                        ActivityResult.TransferAccount
                    )
                    if (transferFamilyMemberList != null && transferFamilyMemberList?.size.orZero() > 1) {
                        CommonConfirmDialog(
                            context,
                            title = "",
                            titleSpannableString = SpannableStringBuilder("确定向").append(
                                getSpannableString()
                            ),
                            negativeString = "取消"
                        ) {
                            if (this) {
                                val listId: List<String?>? =
                                    transferFamilyMemberList?.filterList { this.isSelected }
                                        ?.map { item ->
                                            item.userId.orEmpty()
                                        }
                                mViewModel.transferMultipleDiamondValue(
                                    mViewModel.transferUserNumber.value.orZero(),
                                    listId.orEmpty()
                                ) { isTransferSuccess ->
                                    customToast("转账成功")
                                    isTransferSuccess?.let {
                                        jump(
                                            PATH_USER_WALLET_OPERATE_SUCCESS,
                                            "type" to Constants.SuccessType.TRANSFER.type,
                                            "number" to mViewModel.transferUserNumber.value.orZero() * listId?.size.orZero(),
                                            "transferSuccessText" to getSpannableString(),
                                            "walletType" to Constants.WalletType.DIAMOND.type,
                                            "successType" to Constants.SuccessType.TRANSFER.type
                                        )
                                    }
                                }
                            }
                        }.show()
                    } else {
                        mViewModel.loadUserByDisplayId(
                            mViewModel.transferUserId.value.orEmpty(),
                            onSuccess = {
                                val transferConfirmDialog =
                                    TransferConfirmDialog.newInstance(it)
                                transferConfirmDialog.show(supportFragmentManager)
                                transferConfirmDialog.setCallBack {
                                    mViewModel.transferDiamond(
                                        mViewModel.transferUserNumber.value.orZero(),
                                        mViewModel.transferUserId.value.orEmpty()
                                    ) { isTransferSuccess ->
                                        customToast("转账成功")
                                        isTransferSuccess?.let {
                                            jump(
                                                PATH_USER_WALLET_OPERATE_SUCCESS,
                                                "type" to Constants.SuccessType.TRANSFER.type,
                                                "number" to mViewModel.transferUserNumber.value.orZero(),
                                                "userId" to mViewModel.transferUserId.value.orEmpty(),
                                                "walletType" to Constants.WalletType.DIAMOND.type,
                                                "successType" to Constants.SuccessType.TRANSFER.type
                                            )
                                        }
                                    }
                                }

                            })
                    }

                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        mViewModel.getMyMoneyBag {
            it?.let {
                when (walletType) {
                    Constants.WalletType.COIN.type -> {
                        transferNumber = it.coin.orZero()
                        mViewModel.mGoldNumber.value = Format.E_EE.format(transferNumber.orZero())
                    }

                    Constants.WalletType.DIAMOND.type -> {
                        transferNumber = it.diamond.orZero()
                        mViewModel.mGoldNumber.value = Format.E_EE.format(transferNumber.orZero())
                    }

                    else -> {

                    }

                }

            }
        }
    }

    private fun getSpannableString(): CharSequence {
        if (transferFamilyMemberList != null && transferFamilyMemberList?.size.orZero() > 1) {
            val transferMemberText =
                "${transferFamilyMemberList?.get(0)?.nickname?.ellipsizeString(3)}/${
                    transferFamilyMemberList?.get(1)?.nickname?.ellipsizeString(3)
                }"
            val spannableStringBuilder = SpannableStringBuilder()
            val colorSpan =
                ForegroundColorSpan(resources.getColor(com.kissspace.module_common.R.color.color_FFFD62))
            val spannableString = SpannableString(transferMemberText)
            spannableString.setSpan(
                colorSpan,
                0,
                transferMemberText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableStringBuilder.append(spannableString)

            spannableStringBuilder.append("等")
            spannableStringBuilder.append(transferFamilyMemberList?.size.orZero().toString())
            spannableStringBuilder.append("位用户转账")

            return spannableStringBuilder
        }
        return SpannableStringBuilder()
    }

    companion object {
        const val minTransferValue = 100
    }

}