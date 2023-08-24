package com.kissspace.android.adapter

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.blankj.utilcode.util.ResourceUtils
import com.blankj.utilcode.util.SizeUtils
import com.kissspace.util.dp
import com.kissspace.common.model.RoomListBannerBean
import com.kissspace.common.model.RoomListBean
import com.kissspace.common.util.format.Format
import com.kissspace.common.util.handleSchema
import com.kissspace.util.loadImage
import com.kissspace.util.loadImageCircle
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator

object ViewBindingAdapter {
    @JvmStatic
    @BindingAdapter("isShowBeautyId")
    fun isShowBeautyId(imageView: ImageView, isShow: Boolean) {
        imageView.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("isShowLock")
    fun isShowLock(imageView: ImageView, isShow: Boolean) {
        imageView.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("onLineCount")
    fun onLineCount(textView: TextView, count: Int) {
        textView.text = count.toString()
        textView.visibility = if (count > 0) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("roomListHostVisible", requireAll = false)
    fun roomListHostVisible(root: LinearLayout, room: RoomListBean) {
        root.visibility =
            if (room.wheatPositionList.isEmpty() || room.wheatPositionList[0].wheatPositionId.isEmpty()) View.GONE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter("roomListHostImage", requireAll = false)
    fun roomListHostImage(imageView: ImageView, room: RoomListBean) {
        if (room.wheatPositionList.isNotEmpty() && room.wheatPositionList[0].wheatPositionIdHeadPortrait.isNotEmpty()) {
            imageView.loadImageCircle(room.wheatPositionList[0].wheatPositionIdHeadPortrait)
        }
    }

    @JvmStatic
    @BindingAdapter("roomListBanner", requireAll = false)
    fun roomListBanner(
        banner: Banner<RoomListBannerBean, BannerImageAdapter<RoomListBannerBean>>,
        data: MutableList<RoomListBannerBean>
    ) {
        banner.apply {
            setAdapter(object : BannerImageAdapter<RoomListBannerBean>(data) {
                override fun onBindView(
                    holder: BannerImageHolder?,
                    data: RoomListBannerBean?,
                    position: Int,
                    size: Int
                ) {
                    (holder?.itemView as ImageView).loadImage(data?.url)
                }
            })
            setOnBannerListener { data, _ ->
                handleSchema(data.schema)
            }
            indicator = CircleIndicator(banner.context)
            setIndicatorSelectedColorRes(com.kissspace.module_common.R.color.common_white)
            setIndicatorNormalColorRes(com.kissspace.module_common.R.color.color_80FFFFFF)
            setIndicatorWidth(4.dp.toInt(), 4.dp.toInt())
            setIndicatorHeight(4.dp.toInt())
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(
                        0, 0, view?.width!!, view.height, 10.dp
                    )
                }
            }
            clipToOutline = true
        }
    }

    @JvmStatic
    @BindingAdapter("roomListMicMembers", requireAll = false)
    fun roomListMicMembers(layout: LinearLayout, room: RoomListBean) {
        layout.removeAllViews()
        val padding = SizeUtils.dp2px(1f)
        val margin = SizeUtils.dp2px(-3f)
        val size = SizeUtils.dp2px(21f)
        var userList =
            room.wheatPositionList.filter { it.onMicroPhoneNumber != 0 && it.wheatPositionId.isNotEmpty() }
        if (userList.isNotEmpty() && userList.size > 6) {
            userList = userList.subList(0, 6)
        }
        userList.forEachIndexed { index, model ->
            val imageView = ImageView(layout.context)
            imageView.background =
                ResourceUtils.getDrawable(com.kissspace.android.R.drawable.bg_party_members)
            val params = LinearLayout.LayoutParams(size, size)
            if (index > 0) {
                params.setMargins(margin, 0, 0, 0)
            }
            imageView.layoutParams = params
            imageView.loadImageCircle(model.wheatPositionIdHeadPortrait)
            layout.addView(imageView)
        }
    }

    @JvmStatic
    @BindingAdapter("roomHeat", requireAll = false)
    fun roomHeat(textView: TextView, value: Long) {
        textView.visibility = if (value > 0) View.VISIBLE else View.GONE
        textView.text = when (value) {
            in 0 until 10000 -> value.toString()
            in 10000 until 100000000 -> Format.O_O_DOWN.format(value.toDouble() / 10000)
                .removeSuffix(".0") + "万"

            else -> Format.O_O_DOWN.format(value.toDouble() / 100000000).removeSuffix(".0") + "亿"
        }
    }
}