package com.kissspace.room.provider

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.GsonUtils
import com.didi.drouter.annotation.Service
import com.kissspace.util.topActivity
import com.kissspace.common.config.CommonApi
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.model.CheckRoomInfoModel
import com.kissspace.common.model.RoomInfoBean
import com.kissspace.common.model.RoomListBean
import com.kissspace.common.provider.IRoomProvider
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.customToast
import com.kissspace.common.widget.InputRoomPwdDialog
import com.kissspace.common.widget.LoadingDialog
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.room.http.RoomApi
import com.kissspace.room.manager.RoomServiceManager
import com.kissspace.room.ui.activity.BaseLiveActivity
import com.kissspace.room.widget.GiftDialog
import com.kissspace.util.logE


@Service(function = [IRoomProvider::class])
class RoomServiceProvider : IRoomProvider {
    private var loading: LoadingDialog? = null
    override fun jumpRoom(
        crId: String?,
        roomType: String?,
        stochastic: String?,
        userId: String?,
        roomList: MutableList<RoomListBean>
    ) {
        RoomServiceManager.roomList.clear()
        roomList.removeAll { it.userId == MMKVProvider.userId }
        RoomServiceManager.roomList.addAll(roomList)
        val roomInfo = RoomServiceManager.roomInfo
        when {
            stochastic == "001" -> {
                //随机进房间
                showLoading()
                if (roomInfo != null) {
                    RoomServiceManager.release()
                }
                joinRoom(roomType = roomType, stochastic = stochastic) {
                    doJump(it.crId, stochastic, userId)
                }
            }

            crId.isNullOrEmpty() && !roomType.isNullOrEmpty() || (roomInfo != null && roomInfo.roomTagCategory == roomType && roomInfo.houseOwnerId == MMKVProvider.userId && crId.isNullOrEmpty()) -> {
                //进自己房间 不需要校验任何信息
                if (roomInfo == null) {
                    showLoading()
                    createRoom(roomType!!) {
                        doJump(it.crId, stochastic, userId)
                    }
                } else if (roomInfo != null && roomInfo.roomTagCategory == roomType && roomInfo.houseOwnerId == MMKVProvider.userId) {
//                    doJump(roomInfo.crId, stochastic, userId, roomInfo)
                    refreshRoom(crId = roomInfo!!.crId) {
                        doJump(it.crId, stochastic, userId, it)
                    }
                } else {
                    showLoading()
                    RoomServiceManager.release()
                    createRoom(roomType = roomType!!) {
                        doJump(it.crId, stochastic, userId)
                    }
                }
            }

            else -> {
                //进别人房间
                if (crId == roomInfo?.crId) {
                    //刷新房间，不用校验
//                    doJump(roomInfo!!.crId, stochastic, userId, roomInfo!!)
                    refreshRoom(crId!!) {
                        doJump(it.crId, stochastic, userId, it)
                    }
                } else {
                    RoomServiceManager.release()
                    checkRoom(crId!!, stochastic, userId)
                }
            }
        }
    }

    private fun showLoading() {
        if (loading == null) {
            loading = LoadingDialog("正在进入房间")
        }
        if (topActivity is AppCompatActivity) {
            loading?.show((topActivity as AppCompatActivity).supportFragmentManager)
        }
    }

    private fun dismissLoading() {
        if (loading != null && loading?.isAdded == true) {
            loading?.dismiss()
        }
    }

    private fun checkRoom(crId: String, stochastic: String?, userId: String?) {
        checkRoomInfo(crId) {
            when {
                it.roomPwd.isNotEmpty() && it.role == Constants.ROOM_USER_TYPE_NORMAL -> {
                    //有密码
                    val dialog = InputRoomPwdDialog()
                    dialog.initData(it.roomPwd) { pwd ->
                        showLoading()
                        joinRoom(crId, pwd, it.roomTagCategory) { info ->
                            doJump(info.crId, stochastic, userId)
                        }
                    }
                    dialog.show((topActivity as AppCompatActivity).supportFragmentManager)
                }

                it.kickOutOfTheRoom -> {
                    customToast("您被踢出房间未满15分钟")
                }

                else -> {
                    showLoading()
                    joinRoom(crId, roomType = it.roomTagCategory) { info ->
                        doJump(info.crId, stochastic, userId)
                    }
                }
            }

        }

    }

    override fun getRoomId(): String? = RoomServiceManager.roomInfo?.crId

    override fun showGiftDialog(
        fm: FragmentManager,
        crId: String,
        targetUserId: String,
        isOpenPointsBox: Boolean,
        from: Int,
        isChooseLuck: Boolean
    ) {
        GiftDialog.newInstance(
            crId, targetUserId, isOpenPointsBox, from, isChooseLuck
        ).show(fm)
    }

    override fun loginOutRoom(block: (() -> Unit)?) {
        RoomServiceManager.release(block)
    }


    private fun checkRoomInfo(crId: String, onSuccess: (CheckRoomInfoModel) -> Unit) {
        val params = mutableMapOf<String, Any?>("chatRoomId" to crId)
        request<CheckRoomInfoModel>(CommonApi.API_QUERY_ROOM_PASSWORD,
            Method.GET,
            params,
            onSuccess = {
                onSuccess(it)
            },
            onError = { customToast(it.message) })
    }

    private fun doJump(
        crId: String?,
        stochastic: String?,
        userId: String?,
        roomInfoBean: RoomInfoBean? = null
    ) {
        dismissLoading()
        logE("roomInfoBean111======${roomInfoBean}")
        jump(
            RouterPath.PATH_LIVE_AUDIO,
            "crId" to crId.orEmpty(),
            "stochastic" to stochastic.orEmpty(),
            "userId" to userId.orEmpty(),
            "roomInfo" to GsonUtils.toJson(roomInfoBean).orEmpty()
        )
        if (topActivity is BaseLiveActivity) {
            (topActivity as BaseLiveActivity).finish()
        }
    }

    private fun createRoom(roomType: String, onSuccess: (RoomInfoBean) -> Unit) {
        val param = mutableMapOf<String, Any?>()
        param["roomTagCategory"] = roomType
        request<RoomInfoBean>(RoomApi.API_CREATE_ROOM, Method.POST, param, onSuccess = {
            onSuccess(it)
        }, onError = {
            dismissLoading()
            customToast(it.errorMsg)
        })
    }

    private fun joinRoom(
        crId: String? = null,
        password: String? = null,
        roomType: String? = null,
        stochastic: String? = null,
        onSuccess: (RoomInfoBean) -> Unit
    ) {
        val param = mutableMapOf<String, Any?>()
        param["crId"] = crId
        param["roomPwd"] = password
        param["roomType"] = roomType
        param["stochastic"] = stochastic
        request<RoomInfoBean>(RoomApi.API_JOIN_ROOM, Method.POST, param, onSuccess = {
            onSuccess(it)
        }, onError = {
            dismissLoading()
            customToast(it.errorMsg)
        })
    }

    private fun refreshRoom(crId: String, onSuccess: (RoomInfoBean) -> Unit) {
        val param = mutableMapOf<String, Any?>()
        param["crId"] = crId
        param["userId"] = MMKVProvider.userId
        request<RoomInfoBean>(RoomApi.API_REFRESH_ROOM, Method.POST, param, onSuccess = {
            onSuccess(it)
        }, onError = {
            dismissLoading()
            customToast(it.message)
        })
    }
}



