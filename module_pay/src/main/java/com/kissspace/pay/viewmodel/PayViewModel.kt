package com.kissspace.pay.viewmodel

import androidx.lifecycle.MutableLiveData
import com.kissspace.common.base.BaseViewModel
/**
 * @Author gaohangbo
 * @Date 2023/2/6 12:04.
 * @Describe
 */
class PayViewModel : BaseViewModel() {

    //我的剩余金币
    var mGoldNumber = MutableLiveData("")

    var mSelectGold = MutableLiveData("")

    var mSelectRmb =  MutableLiveData("")

}