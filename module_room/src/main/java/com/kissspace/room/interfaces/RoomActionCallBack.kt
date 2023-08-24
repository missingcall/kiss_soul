package com.kissspace.room.interfaces

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/13 17:54
 * @Description: 房间action bar 点击事件回调
 *
 */
interface RoomActionCallBack {
    //收藏
    fun collect()

    //公告
    fun showNotice()

    //分享
    fun share()

    //退出房间
    fun quit()

    //显示名片
    fun showProfile()

    //退出房间
    fun back()
}