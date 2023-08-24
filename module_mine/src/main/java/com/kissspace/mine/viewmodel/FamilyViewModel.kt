package com.kissspace.mine.viewmodel

import androidx.databinding.ObservableField
import com.blankj.utilcode.util.ToastUtils
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.AppConfigKey
import com.kissspace.common.config.Constants
import com.kissspace.common.config.Constants.TypeFaceRecognition
import com.kissspace.common.http.getAppConfigByKey
import com.kissspace.common.http.uploadSingleFile
import com.kissspace.common.model.family.*
import com.kissspace.common.util.setApplicationValue
import com.kissspace.mine.http.MineApi
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import com.kissspace.util.toast
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File

/**
 * @Author gaohangbo
 * @Date 2022/12/26 18:29.
 * @Describe
 */
class FamilyViewModel : BaseViewModel() {

    val isNoticeEmpty = ObservableField<Boolean>()

    val familyIcon = ObservableField<String>()

    //家族公告
    val familyNotice = ObservableField<String>()

    //家族公告颜色
    val familyNoticeColor = ObservableField<Int>()

    //家族介绍
    val familyDesc = ObservableField<String>()

    val familyHint = ObservableField<String>()

    val familyCreateTime = ObservableField<String>()

//    val isShowChatRoom = ObservableField<Boolean>()

    val isHaveUserApply = ObservableField<Boolean>()

    val familyAddText = ObservableField<String>()

    //是否是游客
    val isVisitor = ObservableField<Boolean>()

    //是否是家族长
    val isFamilyHeader = ObservableField<Boolean>()

    //是否显示设置
    val isShowSetting = ObservableField<Boolean>()

    //按钮是否可用
    val isFamilyButtonEnable = ObservableField<Boolean>()

    val isHideFamilyButton = ObservableField<Boolean>()

    val familyDetailModel = ObservableField<FamilyDetailInfoModel>()

    //家族流水
    val thisWeekInLicenseRoomRevenue = ObservableField<String>()
    val thisWeekOutLicenseRoomRevenue = ObservableField<String>()
    val todayInLicenseRoomRevenue = ObservableField<String>()
    val todayOutLicenseRoomRevenue = ObservableField<String>()

    private val _getFamilyModelList = MutableSharedFlow<ResultState<List<FamilyListModels>>>()

    val getFamilyModelListEvent = _getFamilyModelList.asSharedFlow()


    private val _getFamilyMemberListModel =
        MutableSharedFlow<ResultState<FamilyMemberModel>>()

    val getFamilyMemberListModelEvent = _getFamilyMemberListModel.asSharedFlow()

    //申请家族社区
    private val _applyUserEvent = MutableSharedFlow<ResultState<Int>>()

    val applyUserEvent = _applyUserEvent.asSharedFlow()

    //解散家族
    private val _dissolveEvent = MutableSharedFlow<ResultState<Int>>()

    val dissolveFamilyEvent = _dissolveEvent.asSharedFlow()


    private val _updateFamilyEvent = MutableSharedFlow<ResultState<Int>>()

    val updateFamilyEvent = _updateFamilyEvent.asSharedFlow()

    private val _checkoutUserEvent = MutableSharedFlow<ResultState<Int>>()

    val checkoutUserEvent = _checkoutUserEvent.asSharedFlow()

    private val _moveOutEvent = MutableSharedFlow<ResultState<Int>>()

    val moveOutFamilyEvent = _moveOutEvent.asSharedFlow()


    fun getQQNumber(block: ((String?) -> Unit)?) {
        getAppConfigByKey<String>(AppConfigKey.FAMILY_QQ_NUMBER) {
            block?.invoke(it)
        }
    }

    fun getFamilyListByParameter(keyword: String) {
        val param = mutableMapOf<String, Any?>()
        param["keyword"] = keyword
        request(
            MineApi.API_GET_FAMILY_LIST_BY_PARAMETER,
            Method.GET,
            param,
            state = _getFamilyModelList
        )
    }


    fun getFamilyListByParameterByHot(keyword: String) {
        val param = mutableMapOf<String, Any?>()
        param["keyword"] = keyword
        request(
            MineApi.API_GET_FAMILY_HOT_LIST,
            Method.GET,
            param,
            state = _getFamilyModelList
        )
    }


    //查询家族列表
    fun getFamilyList() {
        request(MineApi.API_GET_FAMILY_LIST, Method.GET, state = _getFamilyModelList)
    }


    //申请家族接口
    fun applyFamilyUser(familyId: String) {
        val param = mutableMapOf<String, Any?>()
        param["familyId"] = familyId
        setApplicationValue(
            type = TypeFaceRecognition,
            value = Constants.FaceRecognitionType.OTHER.type.toString()
        )
        request(MineApi.API_GET_FAMILY_USER_APPLY, Method.POST, param, state = _applyUserEvent)
    }

    //没有加入家族的人根据Id查询家族信息
    fun getFamilyById(familyId: String, block: ((FamilyDetailInfoModel?) -> Unit)?) {
        val param = mutableMapOf<String, Any?>()
        param["id"] = familyId
        request<FamilyDetailInfoModel?>(MineApi.API_GET_FAMILY_BY_ID, Method.GET, param,
            onSuccess = { block?.invoke(it) })
    }

    //查询所有申请加入家族用户信息
    fun getFamilyUserApplyList(block: ((FamilyMemberModel?) -> Unit)?) {
        request<FamilyMemberModel>(
            MineApi.API_GET_FamilyUserApplyList,
            Method.GET,
            onSuccess = {
                block?.invoke(it)
            },
            onError = {
                toast(it.message)
            }
        )
    }

    //没有加入家族的人根据家族id查询所有家族成员信息
    fun getFamilyUserListById(
        familyId: String,
        pageSize: Int? = null,
        pageNum: Int? = null,
        keyword: String? = null
    ) {
        val param = mutableMapOf<String, Any?>()
        keyword?.let {
            param["keyword"] = keyword
        }
        param["familyId"] = familyId
        param["pageSize"] = pageSize
        param["pageNum"] = pageNum
        request(
            MineApi.API_QUERY_FAMILY_USER_LIST,
            Method.GET,
            param,
            state = _getFamilyMemberListModel
        )
    }

    //查询所有家族成员信息
    fun getFamilyUserList() {
        request(
            MineApi.API_GET_FAMILY_USER_LIST,
            Method.GET,
            state = _getFamilyMemberListModel
        )
    }

    //移出家族(退出家族传自己对id)
    fun moveOutFamily(userId: String) {
        val param = mutableMapOf<String, Any?>()
        param["userId"] = userId
        request(MineApi.API_GET_FAMILY_MOVE_OUT, Method.POST, param, state = _moveOutEvent)
    }

    //解散家族
    fun dissolveFamily(userId: String) {
        val param = mutableMapOf<String, Any?>()
        param["userId"] = userId
        request(MineApi.API_GET_FAMILY_MOVE_OUT, Method.POST, param, state = _dissolveEvent)
    }

    //更新家族资料
    fun updateFamilyInfo(
        familyId: String,
        familyIcon: String,
        familyDesc: String,
        familyNotice: String
    ) {
        val param = mutableMapOf<String, Any?>()
        param["familyId"] = familyId
        param["familyIcon"] = familyIcon
        param["familyDesc"] = familyDesc
        param["familyNotice"] = familyNotice
        request(MineApi.API_GET_UPDATE_FAMILY_INFO, Method.POST, param, state = _updateFamilyEvent)
    }


    //审核用户加入家族申请
    fun checkUserApply(applyId: String?, auditResult: String) {
        val param = mutableMapOf<String, Any?>()
//        applyId	申请人id		false
//        auditResult	审核结果，001 通过，002 拒绝
        param["applyId"] = applyId
        param["auditResult"] = auditResult
        request(
            MineApi.API_GET_FAMILY_CHECK_USER_APPLY,
            Method.POST,
            param,
            state = _checkoutUserEvent
        )
    }


    //上传家族头像
    fun updateFamilyIcon(
        param: File,
        onSuccess: ((String) -> Unit) = {}
    ) {
        uploadSingleFile(MineApi.API_GET_FAMILY_UPLOAD_ICON, "icon", param, onSuccess)
    }

    //获取家族信息
    fun getSelectFamilyInfo(block: ((FamilyDetailInfoModel?) -> Unit)?) {
        request<FamilyDetailInfoModel?>(
            MineApi.API_GET_SELECT_FAMILY_INFO,
            Method.GET,
            onSuccess = {
                block?.invoke(it)
            },
            onError = {
                ToastUtils.showLong(it.errorMsg)
            })
    }


    fun queryFamilyFlowList(
        isInRoom: Int,
        isToday: Int,
        keyword: String,
        pageNum: Int,
        block: ((FamilyFlowModel?) -> Unit)?
    ) {
        val param = mutableMapOf<String, Any?>()
        param["isInRoom"] = isInRoom
        param["isToday"] = isToday
        param["keyword"] = keyword
        param["pageNum"] = pageNum
        param["pageSize"] = Constants.PageSize
        request<FamilyFlowModel?>(
            MineApi.API_GET_SELECT_QUERY_FAMILY_FLOWLIST,
            Method.GET,
            param,
            onSuccess = {
                block?.invoke(it)
            },
            onError = {
                ToastUtils.showLong(it.errorMsg)
            })
    }


}