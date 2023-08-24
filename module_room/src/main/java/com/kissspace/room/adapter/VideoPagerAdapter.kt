package com.kissspace.room.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class VideoPagerAdapter(layoutId: Int, data: MutableList<MutableList<String>>) :
    BaseQuickAdapter<MutableList<String>, BaseViewHolder>(layoutId, data) {

    override fun convert(holder: BaseViewHolder, item: MutableList<String>) {

    }
}