package com.kissspace.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.kissspace.util.withStyledAttributes
import com.kissspace.module_common.R
import com.kissspace.util.loadImage

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/3/19 16:20
 * @Description: 头像框封装类
 *
 */

class HeadWearView : FrameLayout {
    private var mPagUrl: String? = null
    private var mStaticUrl: String? = null
    private val mPagView: EasyPagImageView
    private val mImageView: ImageView

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        withStyledAttributes(attributeSet, R.styleable.EasyPagView) {
            mPagUrl = getString(R.styleable.HeadWearView_pagUrl)
            mStaticUrl = getString(R.styleable.HeadWearView_staticUrl)
        }
        initView()
    }

    init {
        View.inflate(context, R.layout.common_view_user_head_wear, this)
        mImageView = findViewById(R.id.image_view)
        mPagView = findViewById(R.id.pag_view)
    }

    private fun initView() {
        if (mPagUrl.isNullOrEmpty() && mStaticUrl.isNullOrEmpty()) {
            mPagView.visibility = View.INVISIBLE
            mImageView.visibility = View.INVISIBLE
        } else if (mPagUrl.isNullOrEmpty()) {
            mPagView.visibility = View.INVISIBLE
            mImageView.visibility = View.VISIBLE
            mImageView.loadImage(mStaticUrl)
        } else {
            mPagView.visibility = View.VISIBLE
            mImageView.visibility = View.INVISIBLE
            mPagView.play(mPagUrl!!)
        }
    }

    fun setHeadWearUrl(pagUrl: String?, staticUrl: String?) {
        mPagUrl = pagUrl
        mStaticUrl = staticUrl
        initView()
    }
}