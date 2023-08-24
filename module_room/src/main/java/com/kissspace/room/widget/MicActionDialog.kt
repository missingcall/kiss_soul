package com.kissspace.room.widget

import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.config.Constants
import com.kissspace.common.model.MicUserModel
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.widget.BaseBottomSheetDialogFragment
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogMicActionBinding

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/9
 * @Description: 点击麦位管理弹窗
 *
 */
class MicActionDialog : BaseBottomSheetDialogFragment<RoomDialogMicActionBinding>() {
    private lateinit var mModel: MicUserModel
    private var mRole: String? = Constants.ROOM_USER_TYPE_NORMAL
    private var mRoomType: String = Constants.ROOM_TYPE_PARTY
    private lateinit var mCallBack: ClickType.(MicUserModel) -> Unit
    private val items = mutableListOf<Items>()
    fun initData(
        model: MicUserModel,
        role: String? = Constants.ROOM_USER_TYPE_NORMAL,
        type: String,
        callBack: ClickType.(MicUserModel) -> Unit
    ) {
        mModel = model
        mRole = role
        mRoomType = type
        mCallBack = callBack
    }

    override fun getViewBinding() = RoomDialogMicActionBinding.inflate(layoutInflater)

    override fun initView() {
        when {
            mModel.wheatPositionId.isNullOrEmpty() -> {
                mBinding.tvTitle.text = getTitle(mModel.onMicroPhoneNumber)
                items.add(Items(ClickType.TYPE_UP_MIC, "上麦"))
                if (mRole != Constants.ROOM_USER_TYPE_NORMAL) {
                    //有身份
                    if (!mModel.lockWheat) {
                        items.add(Items(ClickType.TYPE_LOCK_MIC, "锁麦"))
                        items.add(Items(ClickType.TYPE_LOCK_ALL, "一键全锁"))
                    } else {
                        items.add(Items(ClickType.TYPE_UNLOCK_MIC, "解锁麦"))
                        items.add(Items(ClickType.TYPE_UNLOCK_ALL, "一键全开"))
                    }
                    items.add(Items(ClickType.TYPE_INVITE, "抱上麦"))
                    items.add(Items(ClickType.TYPE_CLEAN_INCOME, "清魅力"))
                }
            }

            mModel.wheatPositionId == MMKVProvider.userId -> {
                mBinding.tvTitle.text = mModel.wheatPositionIdName
                if (mRole == Constants.ROOM_USER_TYPE_NORMAL) {
                    //普通用户
                    items.add(Items(ClickType.TYPE_QUIT, "下麦"))
                    items.add(Items(ClickType.TYPE_INFO_DETAIL, "看资料"))
                } else {
                    items.add(Items(ClickType.TYPE_QUIT, "下麦"))
                    items.add(Items(ClickType.TYPE_INFO_DETAIL, "看资料"))
                    items.add(Items(ClickType.TYPE_CLEAN_INCOME, "清魅力"))
                }
            }

            else -> {
                mBinding.tvTitle.text = mModel.wheatPositionIdName
                if (mRole != Constants.ROOM_USER_TYPE_NORMAL) {
                    items.add(Items(ClickType.TYPE_KICK, "抱下麦"))
                    items.add(Items(ClickType.TYPE_INFO_DETAIL, "看资料"))
                    items.add(Items(ClickType.TYPE_CLEAN_INCOME, "清魅力"))
                } else {
                    items.add(Items(ClickType.TYPE_INFO_DETAIL, "看资料"))
                }
            }
        }
        mBinding.recyclerView.linear().divider(R.drawable.room_divider_mic_action_item).setup {
            addType<Items> { R.layout.room_dialog_mic_action_item }
            onClick(R.id.root) {
                mCallBack(getModel<Items>().type, mModel)
                dismiss()
            }
        }.models = items
    }

    private fun getTitle(index: Int): String {
        return when (index) {
            0 -> "主持"
            8 -> "老板"
            else -> StringUtils.getString(R.string.room_mic_action_title_index, index)
        }
    }

    data class Items(var type: ClickType, var text: String)
    sealed class ClickType {
        //下麦
        object TYPE_QUIT : ClickType()

        //看资料
        object TYPE_INFO_DETAIL : ClickType()

        //上麦
        object TYPE_UP_MIC : ClickType()

        //锁麦
        object TYPE_LOCK_MIC : ClickType()

        //解锁麦
        object TYPE_UNLOCK_MIC : ClickType()

        //一键全锁
        object TYPE_LOCK_ALL : ClickType()

        //一键全解锁
        object TYPE_UNLOCK_ALL : ClickType()

        //抱上麦
        object TYPE_INVITE : ClickType()

        //抱下麦
        object TYPE_KICK : ClickType()

        //清魅力
        object TYPE_CLEAN_INCOME : ClickType()
    }
}



