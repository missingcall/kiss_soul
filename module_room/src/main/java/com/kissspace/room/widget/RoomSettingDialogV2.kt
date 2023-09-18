package com.kissspace.room.widget

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.databinding.BaseObservable
import com.blankj.utilcode.util.GsonUtils
import com.drake.brv.utils.addModels
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.google.gson.reflect.TypeToken
import com.kissspace.common.config.Constants
import com.kissspace.common.config.ConstantsKey
import com.kissspace.common.model.RoomInfoBean
import com.kissspace.common.model.config.RoomGameConfig
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.customToast
import com.kissspace.common.util.getBundleParcelable
import com.kissspace.common.util.getH5Url
import com.kissspace.common.util.jumpRoom
import com.kissspace.common.widget.BaseBottomSheetDialogFragment
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogSettingV2Binding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.room.config.RoomSettingClickType
import com.kissspace.util.isNotEmptyBlank
import com.kissspace.util.logE
import com.kissspace.util.toast
import com.kissspace.webview.widget.BrowserDialog
import com.tencent.mmkv.MMKV

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/3/14 16:05
 * @Description: 房间设置弹窗
 *
 */

class RoomSettingDialogV2 : BaseBottomSheetDialogFragment<RoomDialogSettingV2Binding>() {

    private lateinit var roomInfo: RoomInfoBean
    private var canBet: Boolean = false
    private var callBack: ((RoomSettingClickType) -> Unit)? = null

    override fun getViewBinding(): RoomDialogSettingV2Binding =
        RoomDialogSettingV2Binding.inflate(layoutInflater)


    companion object {
        fun newInstance(roomInfo: RoomInfoBean, canBet: Boolean) = RoomSettingDialogV2().apply {
            arguments = bundleOf("roomInfo" to roomInfo, "canBet" to canBet)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roomInfo = arguments?.getBundleParcelable("roomInfo")!!
        canBet = arguments?.getBoolean("canBet")!!
    }

    fun setCallBack(block: (RoomSettingClickType) -> Unit): RoomSettingDialogV2 {
        this.callBack = block
        return this
    }

    override fun initView() {
        mBinding.recyclerNormal.grid(4).setup {
            addType<SettingItem> { R.layout.room_setting_item }
            onBind {
                val model = getModel<SettingItem>()
                findView<ConstraintLayout>(R.id.root).visibility =
                    if (model.isSub) View.INVISIBLE else View.VISIBLE
            }
            onFastClick(R.id.root) {
                val model = getModel<SettingItem>()
                when (model.type) {
                    RoomSettingClickType.TYPE_WATER -> {
                        MMKVProvider.room_water_info_show = !MMKVProvider.room_water_info_show
                        model.isOpen = MMKVProvider.room_water_info_show
                        model.notifyChange()
                    }

                    RoomSettingClickType.TYPE_GIFT_EFFECT -> {
                        MMKVProvider.room_gift_effect_show = !MMKVProvider.room_gift_effect_show
                        model.isOpen = MMKVProvider.room_gift_effect_show
                        model.notifyChange()
                    }

                    RoomSettingClickType.TYPE_MUTE -> {
                        MMKVProvider.room_mute = !MMKVProvider.room_mute
                        model.isOpen = MMKVProvider.room_mute
                        model.notifyChange()
                    }

                    else -> {

                    }
                }
                if (model.type == RoomSettingClickType.TYPE_GET_INTEGRAL) {
                    dismiss()
                }
                callBack?.invoke(model.type)
            }
        }
        mBinding.recyclerNormal.addModels(getNormalItems())

        mBinding.tvManagerTitle.visibility =
            if (roomInfo.userRole == Constants.ROOM_USER_TYPE_NORMAL) View.GONE else View.VISIBLE
        mBinding.recyclerManager.visibility =
            if (roomInfo.userRole == Constants.ROOM_USER_TYPE_NORMAL) View.GONE else View.VISIBLE
        mBinding.recyclerManager.grid(4).setup {
            addType<SettingItem> { R.layout.room_setting_item }
            onFastClick(R.id.root) {
                val model = getModel<SettingItem>()
                if (model.type == RoomSettingClickType.TYPE_CLEAN_INCOME) {
                    CommonConfirmDialog(
                        requireContext(), "是否清除全部麦位的魅力值", "魅力值将重置为0"
                    ) {
                        if (this) {
                            customToast("设置成功")
                            callBack?.invoke(model.type)
                            dismiss()
                        }
                    }.show()
                } else {
                    callBack?.invoke(model.type)
                    dismiss()
                }
            }
        }
        mBinding.recyclerManager.addModels(getManagerItems())
        initRecyclerGame()

    }

    private fun initRecyclerGame() {
        mBinding.recyclerGame.grid(4).setup {
            addType<SettingItem> { R.layout.room_setting_item_game }
            onBind {
                val model = getModel<SettingItem>()
                findView<ConstraintLayout>(R.id.root_game).visibility =
                    if (model.isSub) View.INVISIBLE else View.VISIBLE
            }
            onFastClick(R.id.root_game) {
                val model = getModel<SettingItem>()
                when (model.type) {
                    RoomSettingClickType.TYPE_GAME1 -> {
                        jump(
                            RouterPath.PATH_ROOM_GAME,
                            "gameId" to ConstantsKey.GAME_ID_FLIGHT_CHESS,
                            "roomId" to roomInfo.neteaseChatId
                        )
                    }

                    RoomSettingClickType.TYPE_GAME2 -> {
                        jump(
                            RouterPath.PATH_ROOM_GAME,
                            "gameId" to ConstantsKey.GAME_ID_BILLIARD,
                            "roomId" to roomInfo.neteaseChatId
                        )
                    }

                    RoomSettingClickType.TYPE_GAME3 -> {
                        jump(
                            RouterPath.PATH_ROOM_GAME,
                            "gameId" to ConstantsKey.GAME_ID_DRAW_AND_GUESS,
                            "roomId" to roomInfo.neteaseChatId
                        )
                    }

                    RoomSettingClickType.TYPE_GAME -> {
                        logE("gameUrl===")
                        model.gameUrl?.let {
                            val url = "${
                                getH5Url(it, true)
                            }&chatRoomId=${roomInfo.crId}"
                            BrowserDialog.newInstance(url)
                                .show(childFragmentManager, "BrowserDialog")
                        }
                    }

                    else -> {

                    }
                }
                callBack?.invoke(model.type)
            }
        }
        mBinding.recyclerGame.addModels(getGameItems())
    }


    private fun getNormalItems(): MutableList<SettingItem> {
        val normalItem = mutableListOf<SettingItem>()

        normalItem.add(
            SettingItem(
                type = RoomSettingClickType.TYPE_GET_INTEGRAL,
                icon = R.mipmap.room_icon_setting_get_integral,
                name = "积分领取"
            )
        )
        normalItem.add(
            SettingItem(
                type = RoomSettingClickType.TYPE_PREDICTION,
                icon = R.mipmap.room_icon_setting_prediction,
                name = "积分预言",
                isSub = roomInfo.userRole == Constants.ROOM_USER_TYPE_ANCHOR && !canBet
            )
        )

        normalItem.add(
            SettingItem(
                type = RoomSettingClickType.TYPE_PK,
                icon = R.mipmap.room_icon_setting_pk,
                name = "PK",
                isSub = true
            )
        )
        normalItem.add(
            SettingItem(
                type = RoomSettingClickType.TYPE_PREDICTION,
                icon = R.mipmap.room_icon_setting_prediction,
                name = "积分预言",
                isSub = true,
            )
        )

        normalItem.add(
            SettingItem(
                type = RoomSettingClickType.TYPE_GIFT_EFFECT,
                icon = R.mipmap.room_icon_setting_gift_effect,
                name = "礼物特效",
                isShowStatus = true,
                isOpen = MMKVProvider.room_gift_effect_show
            )
        )
        normalItem.add(
            SettingItem(
                type = RoomSettingClickType.TYPE_MUTE,
                icon = R.mipmap.room_icon_setting_mute,
                name = "静音",
                isShowStatus = true,
                isOpen = MMKVProvider.room_mute
            )
        )
        normalItem.add(
            SettingItem(
                type = RoomSettingClickType.TYPE_REPORT,
                icon = R.mipmap.room_icon_setting_report,
                name = "举报",
                isShowStatus = false
            )
        )
        normalItem.add(
            SettingItem(
                type = RoomSettingClickType.TYPE_BLACKLIST,
                icon = R.mipmap.room_icon_blacklist,
                name = "房间黑名单",
                isShowStatus = false,
                isSub = roomInfo.userRole == Constants.ROOM_USER_TYPE_NORMAL
            )
        )
        return normalItem
    }

    private fun getManagerItems(): MutableList<SettingItem> {
        val normalItem = mutableListOf<SettingItem>()
        if (roomInfo.userRole != Constants.ROOM_USER_TYPE_NORMAL) {
            normalItem.add(
                SettingItem(
                    type = RoomSettingClickType.TYPE_CLEAR_SCREEN_MESSAGE,
                    icon = R.mipmap.room_icon_setting_clear_screen_message,
                    name = "清除公屏信息",
                )
            )
        }
        if (roomInfo.roomTagCategory == Constants.ROOM_TYPE_PARTY) {
            normalItem.add(
                SettingItem(
                    type = RoomSettingClickType.TYPE_INCOME,
                    icon = R.mipmap.room_icon_setting_income,
                    name = "魅力值",
                    isShowStatus = true,
                    isOpen = roomInfo.charmOnOff == "001"
                )
            )
            normalItem.add(
                SettingItem(
                    type = RoomSettingClickType.TYPE_CLEAN_INCOME,
                    icon = R.mipmap.room_icon_setting_clean_income,
                    name = "清除魅力值"
                )
            )
        }
        normalItem.add(
            SettingItem(
                type = RoomSettingClickType.TYPE_ROOM_INFO,
                icon = R.mipmap.room_icon_setting_info,
                name = "房间信息",
            )
        )
        normalItem.add(
            SettingItem(
                type = RoomSettingClickType.TYPE_PASSWORD,
                icon = R.mipmap.room_icon_setting_password,
                name = "房间密码",
            )
        )
        normalItem.add(
            SettingItem(
                type = RoomSettingClickType.TYPE_BACKGROUND,
                icon = R.mipmap.room_icon_setting_background,
                name = "房间背景",
            )
        )
        if (roomInfo.userRole != Constants.ROOM_USER_TYPE_NORMAL && roomInfo.roomTagCategory == Constants.ROOM_TYPE_PARTY) {
            normalItem.add(
                SettingItem(
                    type = RoomSettingClickType.TYPE_BACKGROUND_MUSIC,
                    icon = R.mipmap.room_icon_setting_music,
                    name = "背景音乐",
                )
            )
        }
        if (roomInfo.userRole == Constants.ROOM_USER_TYPE_ANCHOR) {
            normalItem.add(
                SettingItem(
                    type = RoomSettingClickType.TYPE_SETTING_MANAGER,
                    icon = R.mipmap.room_icon_setting_manager,
                    name = "设置管理员",
                )
            )
        }
        return normalItem
    }

    private fun getGameItems(): MutableList<SettingItem> {
        val normalItem = mutableListOf<SettingItem>()
        normalItem.add(
            SettingItem(
                type = RoomSettingClickType.TYPE_GAME1,
                icon = R.mipmap.room_icon_new_game1,
                name = "飞行棋",
            )
        )
        normalItem.add(
            SettingItem(
                type = RoomSettingClickType.TYPE_GAME2,
                icon = R.mipmap.room_icon_new_game2,
                name = "台球",
            )
        )
        normalItem.add(
            SettingItem(
                type = RoomSettingClickType.TYPE_GAME3,
                icon = R.mipmap.room_icon_new_game3,
                name = "你画我猜",
            )
        )

        if (MMKVProvider.gameConfig.isNotEmptyBlank() && MMKVProvider.isShowGame) {
            val type = object : TypeToken<List<RoomGameConfig>>() {}.type
            val games = GsonUtils.fromJson<List<RoomGameConfig>>(MMKVProvider.gameConfig, type)
            games.forEach {
                normalItem.add(
                    SettingItem(
                        type = RoomSettingClickType.TYPE_GAME,
                        icon = 0,
                        iconPath = it.game_icon,
                        name = it.game_name,
                        isSub = !MMKVProvider.isShowGame,
                        gameUrl = it.game_url
                    )
                )
            }
        }


        return normalItem
    }

    data class SettingItem(
        var type: RoomSettingClickType,
        var icon: Int,
        var name: String,
        var isShowStatus: Boolean = false,
        var isOpen: Boolean = true,
        var isSub: Boolean = false,
        var iconPath: String = "",
        var gameUrl: String = ""
    ) : BaseObservable()
}