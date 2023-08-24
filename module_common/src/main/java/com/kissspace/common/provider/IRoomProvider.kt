package com.kissspace.common.provider

import androidx.fragment.app.FragmentManager
import com.kissspace.common.model.RoomListBean

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/5 17:22
 * @Description: 房间模块provider
 *
 */
interface IRoomProvider {

    /**
     *  跳转房间
     *  @param crId 房间id
     *  @param roomType 房间类型
     *  @param stochastic 是否随机跳转
     *  @param userId 跟随者userId
     */
    fun jumpRoom(
        crId: String?,
        roomType: String? = null,
        stochastic: String?,
        userId: String?,
        roomList: MutableList<RoomListBean>
    )

    /**
     * 获取当前房间ID
     * @return 房间ID
     */
    fun getRoomId(): String?


    /**
     *  显示送礼弹窗
     *  @param fm FragmentManager
     *  @param crId 房间ID
     *  @param targetUserId 接收人ID
     *  @param isOpenPointsBox 是否要开积分盲盒
     */
    fun showGiftDialog(
        fm: FragmentManager,
        crId: String,
        targetUserId: String,
        isOpenPointsBox: Boolean,
        from: Int,
        isChooseLucky: Boolean = false,
    )

    /**
     * 退出房间
     */
    fun loginOutRoom(block: (() -> Unit)? = null)
}