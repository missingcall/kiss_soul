package com.kissspace.pay.provider

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.didi.drouter.annotation.Service
import com.didi.drouter.api.Extend
import com.kissspace.pay.ui.PayDialogFragment
import com.kissspace.common.model.wallet.WalletRechargeList
import com.kissspace.common.provider.IPayProvider
import com.kissspace.pay.utils.PayUtils


@Service(function = [IPayProvider::class], cache = Extend.Cache.SINGLETON)
class PayService : IPayProvider {
    override fun pay(
        payChannelType: String,
        payProductId: String,
        activity: AppCompatActivity,
        onResult: ((Boolean) -> Unit)?
    ) {
        PayUtils.pay(payChannelType, payProductId, activity, onSuccess = {
            onResult?.invoke(true)
        }, onFailure = {
            onResult?.invoke(false)
        })
    }

    override fun showPayDialogFragment(
        parentFragmentManager: FragmentManager,
        walletRechargeList: List<WalletRechargeList>,
        onPaySuccess: (() -> Unit)?
    ) {
        val payDialogFragment = PayDialogFragment.newInstance(walletRechargeList as ArrayList<WalletRechargeList>)
        payDialogFragment.onPaySuccess = onPaySuccess
        payDialogFragment.show(parentFragmentManager)
    }


}


