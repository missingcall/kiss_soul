package com.kissspace.room.adapter

import com.blankj.utilcode.util.LogUtils
import com.drake.brv.BindingAdapter
import com.drake.brv.item.ItemAttached
import org.libpag.PAGView


class RoomChatRecyclerAdapter : BindingAdapter(), ItemAttached {

    override fun onViewDetachedFromWindow(holder: BindingViewHolder) {
        super.onViewDetachedFromWindow(holder)
    }

    override fun onViewAttachedToWindow(holder: BindingViewHolder) {
        super.onViewAttachedToWindow(holder)
    }
}