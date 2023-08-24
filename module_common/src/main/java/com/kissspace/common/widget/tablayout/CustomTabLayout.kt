package com.kissspace.common.widget.tablayout

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.blankj.utilcode.util.SizeUtils
import com.kissspace.module_common.R
import com.kissspace.util.dp
import com.kissspace.util.logE
import com.kissspace.util.resToColor
import com.kissspace.util.withStyledAttributes

/**
 *@author: adan
 *@date: 2023/6/24
 *@Description:
 */
class CustomTabLayout : RelativeLayout {

    /**
     * item text 默认颜色
     */
    private var tabItemTextDefaultColor = Color.TRANSPARENT

    /**
     * item text 默认大小
     */
    private var tabItemTextDefaultSize: Float = 12 * Resources.getSystem().displayMetrics.density


    private var indicatorDefaultDrawable: Drawable =
        resources.getDrawable(R.drawable.common_custom_tablayout_default_indicator_bg)


    private val mTabList = mutableListOf<CustomTabLayoutBean>()
    private var mCurrentIndex = 0
    private var mRootView: ViewGroup
    private var mItemParam: LinearLayout.LayoutParams


    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        withStyledAttributes(attributeSet, R.styleable.CustomTabLayoutView) {

            tabItemTextDefaultSize = getDimensionPixelOffset(
                R.styleable.CustomTabLayoutView_tab_item_text_default_size,
                tabItemTextDefaultSize.toInt()
            ).toFloat()

            tabItemTextDefaultColor = getColor(
                R.styleable.CustomTabLayoutView_tab_item_text_default_color,
                tabItemTextDefaultColor
            )
        }

        View.inflate(context, R.layout.custom_layout_tablayout, this)
        mRootView = findViewById(R.id.root)
        clipChildren = false
        mItemParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        mItemParam.setMargins(0, SizeUtils.dp2px(-10f), 0, 0)
        mItemParam.weight = 1f
    }


    fun addTabItem(item: CustomTabLayoutBean) {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.custom_layout_tablayout_item, null, false)
        initTab(itemView, item)
        mTabList.add(item)
        mRootView.addView(itemView)
        itemView.setOnClickListener {
            mCurrentIndex = mRootView.indexOfChild(itemView)
            resetAllItems()
            clickItem()
            mTabChangeLister?.invoke(mCurrentIndex)
        }
    }


    private fun initTab(itemView: View, tabBean: CustomTabLayoutBean) {
        itemView.layoutParams = mItemParam
        val textView = itemView.findViewById<TextView>(R.id.text)
        val imageView = itemView.findViewById<ImageView>(R.id.image_default_indicator)
        val chooseView = itemView.findViewById<ConstraintLayout>(R.id.con_choose)
        val imageTitle = itemView.findViewById<ImageView>(R.id.image_choose_title)
        val textChooseTitle = itemView.findViewById<TextView>(R.id.text_choose_title)
        textView.text = tabBean.text
        textView.setTextColor(tabItemTextDefaultColor)
        imageView.setImageDrawable(indicatorDefaultDrawable)
        if (tabBean.selectDrawable > 0) {
            imageTitle.setImageResource(tabBean.selectDrawable)
            textChooseTitle.visibility = View.GONE
            imageTitle.visibility = View.VISIBLE
        } else {
            textChooseTitle.visibility = View.VISIBLE
            textChooseTitle.text = tabBean.text
            imageTitle.visibility = View.GONE
        }
        if (tabBean.isSelected) {
            chooseView.visibility = View.VISIBLE
            textView.visibility = View.GONE
            imageView.visibility = View.GONE
        } else {
            chooseView.visibility = View.GONE
            textView.visibility = View.VISIBLE
            imageView.visibility = View.VISIBLE
        }
    }


    private fun resetAllItems() {
        for (i in 0 until mRootView.childCount) {
            val itemView = mRootView.getChildAt(i)
            val textView = itemView.findViewById<TextView>(R.id.text)
            val imageView = itemView.findViewById<ImageView>(R.id.image_default_indicator)
            val chooseView = itemView.findViewById<ConstraintLayout>(R.id.con_choose)
            val imageTitle = itemView.findViewById<ImageView>(R.id.image_choose_title)
            textView.text = mTabList[i].text
            textView.setTextColor(tabItemTextDefaultColor)
            imageView.setImageDrawable(indicatorDefaultDrawable)
            imageTitle.setImageResource(mTabList[i].selectDrawable)
            chooseView.visibility = View.GONE
            textView.visibility = View.VISIBLE
            imageView.visibility = View.VISIBLE
        }
    }

    private fun clickItem() {
        val itemView = mRootView.getChildAt(mCurrentIndex)
        val textView = itemView.findViewById<TextView>(R.id.text)
        val imageView = itemView.findViewById<ImageView>(R.id.image_default_indicator)
        val chooseView = itemView.findViewById<ConstraintLayout>(R.id.con_choose)
        chooseView.visibility = View.VISIBLE
        textView.visibility = View.GONE
        imageView.visibility = View.GONE
    }

    fun onTabLayoutPageSelected(position: Int) {
        mCurrentIndex = position
        resetAllItems()
        clickItem()
    }

    private var mTabChangeLister: ((Int) -> Unit)? = null

    fun setOnTabChangedListener(block: (Int) -> Unit) {
        this.mTabChangeLister = block
    }

    fun setViewPager(viewPager2: ViewPager2) {
        viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                onTabLayoutPageSelected(position)
            }
        })
    }
}