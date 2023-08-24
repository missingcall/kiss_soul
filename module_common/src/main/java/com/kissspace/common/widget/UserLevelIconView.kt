package com.kissspace.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.kissspace.util.withStyledAttributes
import com.kissspace.module_common.R

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/13 17:58
 * @Description: 用户等级标签view
 *
 */
class UserLevelIconView : FrameLayout {
    private var mImageView: ImageView
    private var mTextView: TextView
    private var mLevelCount = 0
    private var mLevelType = LEVEL_TYPE_EXPEND

    companion object {
        //财富
        const val LEVEL_TYPE_EXPEND = 10000

        //魅力
        const val LEVEL_TYPE_INCOME = 10001
    }

    constructor(context: Context) : super(context, null) {
        initView()
    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        withStyledAttributes(attributeSet, R.styleable.UserLevelIconView) {
            mLevelCount = getInteger(R.styleable.UserLevelIconView_level_count, 0)
            mLevelType = getInt(R.styleable.UserLevelIconView_level_type, LEVEL_TYPE_EXPEND)
        }
        initView()
    }

    init {
        View.inflate(context, R.layout.common_view_user_level_icon, this)
        mImageView = findViewById(R.id.image_view)
        mTextView = findViewById(R.id.text_view)
    }

    private fun initView() {
        mImageView.setImageResource(getIconByLevelCount(mLevelType, mLevelCount))
        mTextView.text = mLevelCount.toString()
    }

    fun setLeveCount(level: Int) {
        visibility = if (level == 0) View.GONE else View.VISIBLE
        mTextView.text = level.toString()
        mImageView.setImageResource(getIconByLevelCount(mLevelType, level))
    }

    fun setLeveCount(type:Int,level: Int) {
        visibility = if (level == 0) View.GONE else View.VISIBLE
        mTextView.text = level.toString()
        mImageView.setImageResource(getIconByLevelCount(type, level))
    }

    private fun getIconByLevelCount(levelType: Int, level: Int): Int {
        return when (levelType) {
            LEVEL_TYPE_INCOME -> {
                when (level) {
                    0 -> R.mipmap.common_icon_user_level_income_zero
                    in 1..9 -> R.mipmap.common_icon_user_level_income_one
                    in 10..19 -> R.mipmap.common_icon_user_level_income_two
                    in 20..29 -> R.mipmap.common_icon_user_level_income_three
                    in 30..34 -> R.mipmap.common_icon_user_level_income_four
                    in 35..39 -> R.mipmap.common_icon_user_level_income_five
                    in 40..44 -> R.mipmap.common_icon_user_level_income_six
                    in 45..49 -> R.mipmap.common_icon_user_level_income_seven
                    else -> R.mipmap.common_icon_user_level_income_eight
                }
            }
            else -> {
                when (level) {
                    0 -> R.mipmap.common_icon_user_level_expend_zero
                    in 1..9 -> R.mipmap.common_icon_user_level_expend_one
                    in 10..19 -> R.mipmap.common_icon_user_level_expend_two
                    in 20..29 -> R.mipmap.common_icon_user_level_expend_three
                    in 30..34 -> R.mipmap.common_icon_user_level_expend_four
                    else -> R.mipmap.common_icon_user_level_expend_five
                }
            }
        }
    }
}