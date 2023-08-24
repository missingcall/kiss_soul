package com.kissspace.common.provider

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.kissspace.common.model.wallet.WalletRechargeList


/**
 * @Author gaohangbo
 * @Date 2023/3/22 15:34.
 * @Describe
 */
interface IPayProvider {

    fun pay(payChannelType:String, payProductId:String, activity : AppCompatActivity, onResult: ((Boolean) -> Unit)?=null)

    fun showPayDialogFragment(parentFragmentManager:FragmentManager,walletRechargeList:List<WalletRechargeList>,onPaySuccess: (() -> Unit)?=null)

}