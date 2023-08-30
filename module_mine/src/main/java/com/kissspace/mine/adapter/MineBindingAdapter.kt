package com.kissspace.mine.adapter

import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintAttribute
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ResourceUtils
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.util.dp
import com.noober.background.drawable.DrawableCreator
import com.kissspace.common.config.Constants
import com.kissspace.common.model.PersonCar
import com.kissspace.common.model.GoodsListBean
import com.kissspace.common.model.UserInfoBean
import com.kissspace.common.model.UserProfileBean
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.util.resToColor
import com.kissspace.common.widget.UserLevelIconView
import com.kissspace.module_mine.R
import com.kissspace.util.loadImage
import com.kissspace.util.loadImageCircle
import com.kissspace.util.orZero


object MineBindingAdapter {

    @JvmStatic
    @BindingAdapter("sexImage", requireAll = false)
    fun sexImage(imageView: ImageView, sex: String?) {
        imageView.setImageResource(if (sex == Constants.SEX_MALE) R.mipmap.mine_icon_sex_male else R.mipmap.mine_sex_female)
    }

    @JvmStatic
    @BindingAdapter("visitorNewCount", requireAll = false)
    fun visitorNewCount(textView: TextView, count: Int? = 0) {
        if (count == null || count == 0) {
            textView.visibility = View.GONE
        } else {
            textView.visibility =
                if (count - MMKVProvider.currentVisitorCount > 0) View.VISIBLE else View.GONE
            textView.text = "+" + (count - MMKVProvider.currentVisitorCount)
        }
    }

    @JvmStatic
    @BindingAdapter("fansNewCount", requireAll = false)
    fun fansNewCount(textView: TextView, count: Int? = 0) {
        if (count == null || count == 0) {
            textView.visibility = View.GONE
        } else {
            textView.visibility =
                if (count - MMKVProvider.currentFansCount > 0) View.VISIBLE else View.GONE
            textView.text = "+" + (count - MMKVProvider.currentFansCount)
        }
    }

    @JvmStatic
    @BindingAdapter("userProfileEditBtnVisibility", requireAll = false)
    fun userProfileEditBtnVisibility(textView: TextView, userId: String?) {
        textView.visibility = if (MMKVProvider.userId == userId) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("userProfileFollowBtnState", requireAll = false)
    fun userProfileFollowBtnState(textView: TextView, userId: String?) {
        textView.visibility = if (MMKVProvider.userId == userId) View.GONE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter("userProfileFollowText", requireAll = false)
    fun userProfileFollowText(textView: TextView, isFollow: Boolean?) {
        textView.text = if (isFollow == true) "取消关注" else "关注"
    }

    @JvmStatic
    @BindingAdapter("noCarText", requireAll = false)
    fun noCarText(textView: TextView, userInfo: UserProfileBean?) {
        textView.visibility = if (userInfo?.car?.isEmpty() == true) View.VISIBLE else View.GONE
        textView.text =
            if (userInfo?.userId?.equals(MMKVProvider.userId) == true) "您还没有坐骑，快去购买一辆吧" else "该用户还未拥有坐骑"
    }

    @JvmStatic
    @BindingAdapter("userGiftWallCount", requireAll = false)
    fun userGiftWallCount(textView: TextView, count: Int?) {
        if (count == null || count == 0) {
            textView.visibility = View.INVISIBLE
        } else {
            textView.text = "x${count}"
            textView.visibility = View.VISIBLE
        }
    }

    @JvmStatic
    @BindingAdapter("jumpStoreVisibility", requireAll = false)
    fun jumpStoreVisibility(textView: TextView, userInfo: UserProfileBean?) {
        textView.visibility =
            if (userInfo?.car?.isEmpty() == true && userInfo.userId == MMKVProvider.userId) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("dressUpBtnState", requireAll = false)
    fun dressUpBtnState(imageView: ImageView, state: String?) {
        if (state == "001") {
            imageView.setImageResource(R.mipmap.mine_icon_dress_up_cancel_wear)
        } else {
            imageView.setImageResource(R.mipmap.mine_dress_up_wear)
        }
    }

    @JvmStatic
    @BindingAdapter("followBtnState", requireAll = false)
    fun followBtnState(imageView: ImageView, isFollow: Boolean) {
        imageView.setImageResource(if (isFollow) R.mipmap.mine_icon_btn_unfollow else R.mipmap.mine_icon_btn_follow)
    }


    @JvmStatic
    @BindingAdapter("userCarRecyclerData", requireAll = false)
    fun userCarRecyclerData(recyclerView: RecyclerView, data: List<PersonCar>?) {
        data?.let {
            recyclerView.linear(RecyclerView.HORIZONTAL).setup {
                addType<PersonCar> { R.layout.mine_layout_user_profile_car_item }
            }.models = it
        }
    }

    @JvmStatic
    @BindingAdapter("showUserEditAvatar", requireAll = false)
    fun showUserEditAvatar(imageView: ImageView, bean: UserProfileBean?) {
        imageView.loadImageCircle(bean?.auditingProfilePath?.ifEmpty { bean.profilePath })
    }

    @JvmStatic
    @BindingAdapter("avatarVerifyVisibility", requireAll = false)
    fun avatarVerifyVisibility(imageView: ImageView, bean: UserProfileBean?) {
        imageView.visibility =
            if (bean?.auditingProfilePath?.isNotEmpty() == true) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("userProfileTitle", requireAll = false)
    fun userProfileTitle(textView: TextView, userId: String?) {
        textView.text = if (MMKVProvider.userId == userId) "关于我" else "关于TA"

    }

    @JvmStatic
    @BindingAdapter("followRoomIcon", requireAll = false)
    fun followRoomIcon(imageView: ImageView, model: UserProfileBean?) {
        if (model != null && model.followRoomId.isNotEmpty() && model.userId != MMKVProvider.userId) {
            imageView.setBackgroundResource(R.drawable.mine_anim_follow_room)
            val drawable = imageView.background as AnimationDrawable
            drawable.start()
        }
    }

    @JvmStatic
    @BindingAdapter("followRoomVisible", requireAll = false)
    fun followRoomVisible(layout: ConstraintLayout, model: UserProfileBean?) {
        layout.visibility =
            if (model != null && model.followRoomId.isNotEmpty() && model.userId != MMKVProvider.userId) View.VISIBLE else View.GONE
    }


    @JvmStatic
    @BindingAdapter("mineLevelCount", requireAll = false)
    fun mineLevelCount(textView: UserLevelIconView, count: Int) {
        textView.setLeveCount(count)
    }

    @JvmStatic
    @BindingAdapter("storeCarPreviewVisible", requireAll = false)
    fun storeCarPreviewVisible(textView: TextView, id: String) {
        textView.visibility = if (id.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter("storeCarBuyVisible", requireAll = false)
    fun storeCarBuyVisible(textView: TextView, id: String) {
        textView.visibility = if (id.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter("storeCarSellOutVisible", requireAll = false)
    fun storeCarSellOutVisible(textView: TextView, id: String) {
        textView.visibility = if (id.isNullOrEmpty()) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("carImage", requireAll = false)
    fun carImage(imageView: ImageView, model: GoodsListBean) {
        if (model.commodityId.isNullOrEmpty()) {
            imageView.setImageResource(R.mipmap.mine_icon_store_car_default)
        } else {
            imageView.loadImage(model.icon)
        }
    }


    @JvmStatic
    @BindingAdapter("userIdText", requireAll = false)
    fun userIdText(textView: TextView, userInfo: UserInfoBean?) {
        userInfo?.let {
            textView.text = "ID:  " + it.beautifulId.ifEmpty { it.displayId }
        }
    }

    @JvmStatic
    @BindingAdapter("profileNickname", requireAll = false)
    fun profileNickname(textView: TextView, userInfo: UserProfileBean?) {
        userInfo?.let {
            textView.text = it.auditingNickname.ifEmpty { it.nickname }
        }
    }

    @JvmStatic
    @BindingAdapter("profileSignInfo", requireAll = false)
    fun profileSignInfo(textView: TextView, userInfo: UserProfileBean?) {
        userInfo?.let {
            textView.text = it.auditingPersonalSignature.ifEmpty { it.personalSignature }
        }
    }

    @JvmStatic
    @BindingAdapter("storeCarDrawableStart", requireAll = false)
    fun storeCarDrawableStart(textView: TextView, id: String) {
        val drawable =
            if (id.isNullOrEmpty()) ResourceUtils.getDrawable(R.mipmap.mine_icon_integral_store) else ResourceUtils.getDrawable(
                R.mipmap.mine_icon_store_coin
            )
        drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
        textView.setCompoundDrawables(drawable, null, null, null)
    }


    @JvmStatic
    @BindingAdapter("goodsInvalidDaysVisible", requireAll = false)
    fun goodsInvalidDaysVisible(textView: TextView, attribute: String) {
        textView.visibility = if (attribute == "001") View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("loadGoodsBackground", requireAll = false)
    fun loadGoodsBackground(imageView: ImageView, url: String) {
        imageView.loadImage(url, topLeftRadius = 12f, topRightRadius = 12f)
    }

    @JvmStatic
    @BindingAdapter("familyMemberManagerText", requireAll = false)
    fun familyMemberManagerText(textView: TextView, role: String) {
        textView.text = if (role == "003") "取消管理" else "设置管理"
    }

    @JvmStatic
    @BindingAdapter("isShowMemberManager", requireAll = false)
    fun isShowMemberManager(textView: TextView, role: String) {
        textView.visibility = if (role == "003" || role == "004") View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("memberRoleText", requireAll = false)
    fun memberRoleText(textView: TextView, role: String) {
        textView.text = if (role == "003") "管理" else "族长"
    }



    @JvmStatic
    @BindingAdapter("showUserMedal")
    fun showUserMedal(layout: com.nex3z.flowlayout.FlowLayout, model: UserProfileBean?) {
        model?.let {
            layout.removeAllViews()
            if (it.consumeLevel.orZero() > 0) {
                val wealthLevel = UserLevelIconView(layout.context)
                wealthLevel.setLeveCount(UserLevelIconView.LEVEL_TYPE_EXPEND, it.consumeLevel)
                layout.addView(wealthLevel)
            }
            if (it.charmLevel.orZero() > 0) {
                val charmLevel = UserLevelIconView(layout.context)
                charmLevel.setLeveCount(UserLevelIconView.LEVEL_TYPE_INCOME, it.charmLevel!!)
                layout.addView(charmLevel)
            }
            val lp =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 19.dp.toInt())
            it.medalList?.forEach { that ->
                val image = ImageView(layout.context)
                image.adjustViewBounds = true
                image.layoutParams = lp
                image.loadImage(that.url)
                layout.addView(image)
            }
        }
    }
}