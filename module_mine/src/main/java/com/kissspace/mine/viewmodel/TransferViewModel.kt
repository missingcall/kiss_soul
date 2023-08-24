package com.kissspace.mine.viewmodel

import android.graphics.drawable.Drawable
import androidx.lifecycle.MutableLiveData
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.Constants
import com.kissspace.common.model.UserInfoBean
import com.kissspace.common.model.wallet.WalletModel
import com.kissspace.common.util.DJSLiveData
import com.kissspace.common.util.customToast
import com.kissspace.common.util.setApplicationValue
import com.kissspace.mine.http.MineApi
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.util.isNotEmpty
import com.kissspace.util.logE
import com.kissspace.util.toast
import org.json.JSONArray

/**
 * @Author gaohangbo
 * @Date 2023/4/13 13:33.
 * @Describe
 */
class TransferViewModel : BaseViewModel() {
    //转账类型
    val transferType = MutableLiveData<String>()

    val transferTitle = MutableLiveData<String>()

    val transferTitleBg = MutableLiveData<Drawable>()

    //转账标题提示
    val transferTitleHint = MutableLiveData<String>()

    //转账的金币
    val transferUserNumber = MutableLiveData<Double>()

    //是否多人转账
    val isTransferMultiple = MutableLiveData<Boolean>()

    //多人转账
    val multipleTransferText = MutableLiveData<CharSequence>()

    val transferImage = MutableLiveData<Int>()

//    //是否显示实际转账金额
//    val isShowTransferTrueMoney = MutableLiveData<Boolean>()
//
//    //金币实付金额
//    val coinTransferUserNumberString = MutableLiveData<String>()

    var mGoldNumber = MutableLiveData<String>()

    //转账底部提示
    val transferWalletHint = MutableLiveData<String>()

    //转账的用户id
    val transferUserId = MutableLiveData<String>()

    //实付金额
    val isTransferCoinEnable = DJSLiveData<Boolean>().apply {
        addSources(transferUserId, transferUserNumber) {
            if (isTransferMultiple.value == true) {
                setValue(transferUserNumber.value != 0.0)
            } else {
                setValue(transferUserNumber.value != 0.0 && transferUserId.value.isNotEmpty())
            }

        }
    }


    //根据displayid获取用户信息
    fun loadUserByDisplayId(displayId: String, onSuccess: ((UserInfoBean?) -> Unit)?) {
        val param = mutableMapOf<String, Any?>()
        param["displayId"] = displayId
        request<UserInfoBean>(
            MineApi.API_QueryUserByDisplayIdResponse, Method.GET, param, onSuccess =
            {
                onSuccess?.invoke(it)
            }, onError = {
                customToast(it.message)
            })
    }

    //金币转账
    fun transferCoin(coin: Double, targetDisplayId: String, block: ((Boolean?) -> Unit)?) {
        val param = mutableMapOf<String, Any?>()
        //coin
        param["coin"] = coin
        //	收款用户
        param["targetDisplayId"] = targetDisplayId
        setApplicationValue(
            type = Constants.TypeFaceRecognition,
            value = Constants.FaceRecognitionType.BLIND_BOX.type.toString()
        )
        request<Boolean>(MineApi.API_TRANSFER_COIN, Method.POST, param, onSuccess =
        {
            block?.invoke(it)
        }, onError = {
            logE(it.message)
            customToast(it.message)
        })
    }

    //钻石转账
    fun transferDiamond(diamond: Double, targetDisplayId: String, block: ((Boolean?) -> Unit)?) {
        val param = mutableMapOf<String, Any?>()
        //coin
        param["diamond"] = diamond
        //	收款用户
        param["targetDisplayId"] = targetDisplayId
        setApplicationValue(
            type = Constants.TypeFaceRecognition,
            value = Constants.FaceRecognitionType.BLIND_BOX.type.toString()
        )
        request<Boolean>(MineApi.API_TRANSFER_DIAMOND, Method.POST, param, onSuccess =
        {
            block?.invoke(it)
        }, onError = {
            customToast(it.message)
        })
    }

    fun getMyMoneyBag(block: ((WalletModel?) -> Unit)?) {
        val param = mutableMapOf<String, Any?>()
        request<WalletModel>(MineApi.API_MY_WALLET, Method.GET, param, onSuccess =
        {
            block?.invoke(it)
        })
    }

    fun transferMultipleDiamondValue(
        diamond: Double,
        targetUserIds: List<String?>,
        block: ((Boolean?) -> Unit)?
    ) {
        val param = mutableMapOf<String, Any?>()
        param["targetUserIds"] = JSONArray(targetUserIds)
        param["diamond"] = diamond
        request<Boolean>(
            MineApi.API_BATCH_TRANSFER_DIAMOND,
            Method.POST,
            param,
            onSuccess = {
                block?.invoke(it)
            },
            onError = {
                toast(it.message)
            }
        )
    }

}