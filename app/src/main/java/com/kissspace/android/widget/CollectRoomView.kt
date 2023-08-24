package com.kissspace.android.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.blankj.utilcode.util.StringUtils
import com.kissspace.android.R
import com.kissspace.util.loadImageCircle

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/13 17:58
 * @Description:
 *
 */
class CollectRoomView : FrameLayout {
    private val imageView: ImageView
    private val collectCount:TextView

    constructor(context: Context) : super(context, null) {
    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
    }

    init {
        View.inflate(context, R.layout.app_widget_home_collect, this)
        imageView = findViewById(R.id.iv_collect)
        collectCount = findViewById(R.id.tv_collect_count)

    }

     fun initData(url:String,count:Int) {
        imageView.loadImageCircle(url)
        collectCount.text = StringUtils.getString(R.string.app_collect_room_count,count)
    }

}