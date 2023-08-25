package com.kissspace.mine.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.kissspace.module_mine.R
import com.kissspace.util.resToColor
import com.kissspace.util.withStyledAttributes

/**
 *@author: adan
 *@date: 2023/8/17
 *@Description:
 */
class MineInletView :ConstraintLayout{

    private val mTextView: TextView
    private val mImageView: ImageView
    private val mMessageView: View
    private var  labelName = ""
    private var  labelColor = 0
    private var  arrow = R.mipmap.mine_icon_arrow_gray

    init {
        View.inflate(context, R.layout.layout_mine_inlet, this)
        mImageView = findViewById(R.id.iv_arrow)
        mTextView = findViewById(R.id.tv_type_name)
        mMessageView = findViewById(R.id.v_message)
    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        withStyledAttributes(attributeSet, R.styleable.MineInletView) {
            labelName = getString(R.styleable.MineInletView_mineInletView_labelText)+""
            labelColor = getColor(R.styleable.MineInletView_mineInletView_labelColor,
                com.kissspace.module_common.R.color.white.resToColor())
            arrow = getResourceId(R.styleable.MineInletView_mineInletView_arrow,R.mipmap.mine_icon_arrow_gray)
        }
        initView()
    }

    private fun initView() {
        mTextView.text = labelName
        mTextView.setTextColor(labelColor)
        mImageView.setImageResource(arrow)
    }

    fun showMessageTips(isShow:Boolean){
        mMessageView.visibility = if (isShow) View.VISIBLE else View.GONE
    }
}