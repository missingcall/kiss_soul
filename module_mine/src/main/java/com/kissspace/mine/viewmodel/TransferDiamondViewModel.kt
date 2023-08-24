package com.kissspace.mine.viewmodel

import androidx.lifecycle.MutableLiveData
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.Constants
import com.kissspace.common.model.family.FamilyMemberModel
import com.kissspace.common.model.family.FamilyMemberRecord
import com.kissspace.mine.http.MineApi
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.util.toast
import org.json.JSONArray

/**
 * @Author gaohangbo
 * @Date 2023/4/11 18:17.
 * @Describe 钻石转账
 */
class TransferDiamondViewModel : BaseViewModel()  {

    // 是否选中
    val isSelected = MutableLiveData<Boolean>()

    //是否全选
    val isSelectedAll = MutableLiveData<Boolean>()


    val isSelectedAllText = MutableLiveData("全选")


    //分页查询所有家族成员信息
    fun getFamilyUserListByPage(keyword:String,pageNum:Int,block: ((FamilyMemberModel?) -> Unit)?) {
        val param = mutableMapOf<String, Any?>()
        param["keyword"] = keyword
        param["pageNum"] = pageNum
        param["pageSize"] = Constants.PageSize
        request<FamilyMemberModel>(
            MineApi.API_QUERY_FAMILY_USER_LIST,
            Method.GET,
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