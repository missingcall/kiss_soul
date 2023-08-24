package com.kissspace.room.widget

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import com.didi.drouter.api.DRouter
import com.kissspace.common.config.CommonApi
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.ext.setDrawable
import com.kissspace.common.model.UserCardModel
import com.kissspace.common.provider.IMessageProvider
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.copyClip
import com.kissspace.common.util.customToast
import com.kissspace.common.widget.BaseBottomSheetDialogFragment
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.common.widget.UserLevelIconView
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogUserProfileBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.room.http.RoomApi
import com.kissspace.util.dp
import com.kissspace.util.isNotEmptyBlank
import com.kissspace.util.loadImage
import com.kissspace.util.loadImageCircle
import com.kissspace.util.orZero

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/14 9:49
 * @Description: 用户名片弹窗
 *
 */
class RoomUserProfileDialog : BaseBottomSheetDialogFragment<RoomDialogUserProfileBinding>() {
    private lateinit var userId: String
    private lateinit var role: String
    private lateinit var crId: String
    private var userInfo: UserCardModel? = null

    companion object {
        fun newInstance(userId: String, role: String, crId: String) =
            RoomUserProfileDialog().apply {
                arguments = bundleOf("userId" to userId, "role" to role, "crId" to crId)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getString("userId")!!
        role = arguments?.getString("role")!!
        crId = arguments?.getString("crId")!!
    }

    override fun getViewBinding() = RoomDialogUserProfileBinding.inflate(layoutInflater)

    override fun initView() {
        initData()
        initEvents()
    }

    private fun initViewStatus() {
        if (userId == MMKVProvider.userId) {
            //查看自己
            mBinding.ivChat.visibility = View.VISIBLE
            mBinding.ivFollow.visibility = View.VISIBLE
            mBinding.ivSendGift.visibility = View.VISIBLE
            mBinding.lineManager.visibility = View.GONE
            mBinding.ivUnchat.visibility = View.GONE
            mBinding.ivMute.visibility = View.GONE
            mBinding.ivKickOut.visibility = View.GONE
            mBinding.ivChat.alpha = 0.5f
            mBinding.ivFollow.alpha = 0.5f
        } else {
            //查看别人
            mBinding.ivChat.visibility = View.VISIBLE
            mBinding.ivFollow.visibility = View.VISIBLE
            mBinding.ivSendGift.visibility = View.VISIBLE
            if (role == Constants.ROOM_USER_TYPE_ANCHOR || (role == Constants.ROOM_USER_TYPE_MANAGER && userInfo?.userRole == Constants.ROOM_USER_TYPE_NORMAL)) {
                mBinding.lineManager.visibility = View.VISIBLE
                mBinding.ivUnchat.visibility = View.VISIBLE
                mBinding.ivMute.visibility = View.VISIBLE
                mBinding.ivKickOut.visibility = View.VISIBLE
            }
        }
        mBinding.ivMenu.visibility = if (userId != MMKVProvider.userId) View.VISIBLE else View.GONE

    }


    @SuppressLint("SetTextI18n")
    private fun initData() {
        val params = mutableMapOf<String, Any?>("userId" to userId, "chatRoomId" to crId)
        request<UserCardModel>(RoomApi.API_GET_USER_PROFILE_INFO, Method.GET, params, onSuccess = {
            userInfo = it
            initViewStatus()
            showUserMedal(it)
            mBinding.ivAvatar.loadImageCircle(it.profilePath)
            if (it.familyName.isNotEmptyBlank()) {
                mBinding.tvFamily.text = "家族：${it.familyName}"
            }
            mBinding.tvNickname.text = it.nickname
            mBinding.tvUserId.text = "ID: " + it.displayId
            mBinding.tvSign.text = it.personalSignature
            mBinding.ivUnchat.text = if (it.isMuted) "解禁" else "禁言"
            mBinding.ivMute.text = if (it.isForbiddenMike) "开麦" else "禁麦"
            mBinding.ivMute.setDrawable(
                if (it.isForbiddenMike) R.mipmap.room_icon_user_profile_un_mute else R.mipmap.room_icon_user_profile_mute,
                Gravity.START
            )
            mBinding.ivUnchat.setDrawable(
                if (it.isMuted) R.mipmap.room_icon_user_profile_unban_talk else R.mipmap.room_icon_user_profile_ban_talk,
                Gravity.START
            )
            mBinding.ivSex.setImageResource(if (it.sex == Constants.SEX_FEMALE) R.mipmap.room_icon_sex_female else R.mipmap.room_icon_sex_male)
            updateFollow(it.isFollow)
        })
    }

    private fun initEvents() {
        mBinding.tvUserId.setOnClickListener {
            userInfo?.displayId?.let {
                copyClip(it)
            }
        }

        mBinding.ivKickOut.setOnClickListener {
            CommonConfirmDialog(requireContext(), "确定要将该用户踢出15分钟吗？") {
                if (this) {
                    kickOut()
                }
            }.show()
        }

        mBinding.ivChat.setOnClickListener {
            if (userId == MMKVProvider.userId) {
                return@setOnClickListener
            }
            userInfo?.let {
                if (it.blackoutState) {
                    customToast("您已拉黑此用户", true)
                } else {
                    val service = DRouter.build(IMessageProvider::class.java).getService()
                    service.showChatDialog(childFragmentManager, it.accid)
                }
            }
        }

        mBinding.ivSendGift.setOnClickListener {
            GiftDialog.newInstance(crId, userId, false, Constants.GiftDialogFrom.FROM_PROFILE)
                .show(childFragmentManager)
        }

        mBinding.ivMute.setOnClickListener {
            userInfo?.let {
                banMike(it.isForbiddenMike)
            }
        }

        mBinding.ivUnchat.setOnClickListener {
            userInfo?.let {
                banChat(it.isMuted)
            }
        }

        mBinding.ivAvatar.setOnClickListener {
            jump(RouterPath.PATH_USER_PROFILE, "userId" to userInfo!!.userId)
        }

        mBinding.ivFollow.setOnClickListener {
            if (userId == MMKVProvider.userId) {
                return@setOnClickListener
            }
            val url =
                if (userInfo?.isFollow == true) CommonApi.API_CANCEL_FOLLOW_USER else CommonApi.API_FOLLOW_USER
            val param = mutableMapOf<String, Any?>("userId" to userInfo?.userId)
            val method = if (userInfo?.isFollow == true) Method.GET else Method.POST
            request<Int>(url, method, param, onSuccess = {
                updateFollow(!userInfo!!.isFollow)
                userInfo?.isFollow = !userInfo!!.isFollow
            })
        }

        mBinding.ivMenu.setOnClickListener {
            UserProfileMenuPop(requireContext(), userInfo!!.blackoutState, role) {
                when (this) {
                    UserProfileMenuPop.ClickType.REPORT_USER -> {
                        jump(
                            RouterPath.PATH_REPORT,
                            "reportType" to Constants.ReportType.USER.type,
                            "userId" to userInfo?.userId.orEmpty()
                        )
                    }

                    UserProfileMenuPop.ClickType.BAN_USER -> {
                        banUser()
                    }

                    else -> {
                        banUserInRoom()
                    }

                }
            }.show(mBinding.ivMenu)
        }
    }

    private fun updateFollow(isFollow: Boolean) {
        if (isFollow) {
            mBinding.ivFollow.text = "已关注"
            mBinding.ivFollow.setCompoundDrawables(null, null, null, null)
        } else {
            mBinding.ivFollow.text = "关注"
            mBinding.ivFollow.setDrawable(R.mipmap.room_icon_user_profile_follow, Gravity.START)
        }
    }

    private fun banMike(alreadyBan: Boolean) {
        val param = mutableMapOf<String, Any?>()
        param["chatRoomId"] = crId
        param["userId"] = userId
        val url = if (alreadyBan) RoomApi.API_CANCEL_BAB_MIC else RoomApi.API_BAN_MIC
        request<Int>(url, Method.POST, param, onSuccess = {
            if (it == 1) {
                if (alreadyBan) {
                    customToast("解禁成功")
                    mBinding.ivMute.text = "禁麦"
                    mBinding.ivMute.setDrawable(R.mipmap.room_icon_user_profile_mute, Gravity.START)
                    userInfo?.isForbiddenMike = false
                } else {
                    customToast("禁麦成功")
                    mBinding.ivMute.text = "开麦"
                    mBinding.ivMute.setDrawable(
                        R.mipmap.room_icon_user_profile_un_mute, Gravity.START
                    )
                    userInfo?.isForbiddenMike = true
                }
            }
        }, onError = {
            customToast(it.message)
        })
    }

    private fun banChat(alreadyBan: Boolean) {
        val param = mutableMapOf<String, Any?>()
        param["chatRoomId"] = crId
        param["targetUserId"] = userId
        param["muteDuration"] = if (alreadyBan) 0 else 100
        request<Int>(RoomApi.API_BAN_CHAT, Method.POST, param, onSuccess = {
            customToast("操作成功")
            if (it == 1) {
                if (alreadyBan) {
                    mBinding.ivUnchat.text = "禁言"
                    mBinding.ivUnchat.setDrawable(
                        R.mipmap.room_icon_user_profile_ban_talk, Gravity.START
                    )
                    userInfo?.isMuted = false
                } else {
                    mBinding.ivUnchat.text = "解禁"
                    mBinding.ivUnchat.setDrawable(
                        R.mipmap.room_icon_user_profile_unban_talk, Gravity.START
                    )
                    userInfo?.isMuted = true
                }
            }
        }, onError = {
            customToast(it.message)
        })
    }

    private fun banUser() {
        val params = mutableMapOf<String, Any?>()
        params["userId"] = userInfo?.userId
        val url =
            if (userInfo?.blackoutState == true) CommonApi.API_CANCNEL_BAN_USER else CommonApi.API_BAN_USER
        request<Int>(url, Method.POST, params, onSuccess = {
            userInfo?.let {
                if (it.blackoutState) {
                    customToast("已取消拉黑", true)
                } else {
                    customToast("拉黑成功", true)
                }
                initData()
            }
        }, onError = {
            customToast(it.message)
        })
    }

    private fun kickOut() {
        val params = mutableMapOf<String, Any?>()
        params["chatRoomId"] = crId
        params["userId"] = userId
        request<Int>(RoomApi.API_KICK_OUT_USER, Method.GET, params, onSuccess = {
            customToast("用户已被踢出")
        }, onError = {
            customToast(it.message)
        })
    }


    private fun showUserMedal(model: UserCardModel) {
        mBinding.layoutLevel.removeAllViews()
        if (model.consumeLevel.orZero() > 0) {
            val wealthLevel = UserLevelIconView(requireContext())
            wealthLevel.setLeveCount(UserLevelIconView.LEVEL_TYPE_EXPEND, model.consumeLevel!!)
            mBinding.layoutLevel.addView(wealthLevel)
        }
        if (model.charmLevel.orZero() > 0) {
            val charmLevel = UserLevelIconView(requireContext())
            charmLevel.setLeveCount(UserLevelIconView.LEVEL_TYPE_INCOME, model.charmLevel!!)
            mBinding.layoutLevel.addView(charmLevel)
        }
        model.medalList?.forEach {
            val image = ImageView(requireContext())
            val lp =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 19.dp.toInt())
            lp.marginEnd = 2.dp.toInt()
            image.layoutParams = lp
            image.adjustViewBounds = true
            image.loadImage(it.url)
            mBinding.layoutLevel.addView(image)
        }
    }

    private fun banUserInRoom() {
        val params = mutableMapOf<String, Any?>()
        params["chatRoomId"] = crId
        params["userId"] = userInfo?.userId
        request<Int>(RoomApi.API_BAN_USER_IN_ROOM, Method.POST, params, onSuccess = {
            customToast("拉黑成功")
        }, onError = {
            customToast(it.message)
        })

    }
}
