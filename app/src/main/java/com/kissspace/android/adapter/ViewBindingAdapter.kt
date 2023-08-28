package com.kissspace.android.adapter

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ResourceUtils
import com.blankj.utilcode.util.SizeUtils
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.util.dp
import com.kissspace.common.model.RoomListBannerBean
import com.kissspace.common.model.RoomListBean
import com.kissspace.common.util.format.Format
import com.kissspace.common.util.handleSchema
import com.kissspace.module_room.R
import com.kissspace.room.widget.CountItem
import com.kissspace.util.loadImage
import com.kissspace.util.loadImageCircle

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
        banner: RecyclerView,
        data: MutableList<RoomListBannerBean>
    ) {
        banner.linear(orientation = HORIZONTAL).setup {
            addType<RoomListBannerBean> { com.kissspace.android.R.layout.app_item_party_banner_list }
            onBind {
                val model = getModel<RoomListBannerBean>()
                val imageView = findView<ImageView>(com.kissspace.android.R.id.iv_banner)
                imageView.setImageResource(com.kissspace.android.R.mipmap.app_party_top_room_rank_bg)
            }
            onFastClick(R.id.root) {
                val model = getModel<RoomListBannerBean>()
                handleSchema(model.schema)
            }
        }.models = data
    }

    @JvmStatic
    @BindingAdapter("roomListMicMembers", requireAll = false)
    fun roomListMicMembers(layout: LinearLayout, room: RoomListBean) {
        layout.removeAllViews()
        val padding = SizeUtils.dp2px(1f)
        val margin = SizeUtils.dp2px(-3f)
        val size = SizeUtils.dp2px(21f)
        var userList =
            room.wheatPositionList.filter { it.wheatPositionId.isNotEmpty() }
        if (userList.isNotEmpty() && userList.size > 4) {
            userList = userList.subList(0, 4)
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