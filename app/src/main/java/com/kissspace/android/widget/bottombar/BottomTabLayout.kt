package com.kissspace.android.widget.bottombar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.blankj.utilcode.util.SizeUtils
import com.kissspace.util.dp
import com.kissspace.android.R
import com.kissspace.util.resToColor

class BottomTabLayout(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private val mTabList = mutableListOf<BottomTabBean>()
    private var mCurrentIndex = 0
    private var mRootView: ViewGroup
    private var mItemParam: LayoutParams


    init {
        View.inflate(context, R.layout.custom_layout_bottom_tab, this)
        mRootView = findViewById(R.id.root)
        clipChildren = false
        mItemParam = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        mItemParam.setMargins(0, SizeUtils.dp2px(-10f), 0, 0)
        mItemParam.weight = 1f
    }


    fun addTabItem(item: BottomTabBean) {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.custom_layout_bottom_item, null, false)
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

    private var mTabChangeLister: ((Int) -> Unit)? = null
    fun setOnTabChangedListener(block: (Int) -> Unit) {
        this.mTabChangeLister = block
    }

    private fun initTab(itemView: View, tabBean: BottomTabBean) {
        itemView.layoutParams = mItemParam
        val textView = itemView.findViewById<TextView>(R.id.text)
        val imageView = itemView.findViewById<ImageView>(R.id.image)
        textView.text = tabBean.text
        textView.setTextColor(if (tabBean.isSelected) tabBean.selectedTextColor.resToColor() else tabBean.normalTextColor.resToColor())
        imageView.setImageResource(if (tabBean.isSelected) tabBean.selectDrawable else tabBean.normalDrawable)
        val lp = imageView.layoutParams as ConstraintLayout.LayoutParams
        var marginBottom = if (tabBean.isSelected) 0f else 5.dp
        lp.setMargins(0, 0, 0, marginBottom.toInt())
        imageView.layoutParams = lp
    }

    private fun resetAllItems() {
        for (i in 0 until mRootView.childCount) {
            val itemView = mRootView.getChildAt(i)
            val textView = itemView.findViewById<TextView>(R.id.text)
            val imageView = itemView.findViewById<ImageView>(R.id.image)
            val unReadCount = itemView.findViewById<TextView>(R.id.tv_notify_count)
            textView.setTextColor(mTabList[i].normalTextColor.resToColor())
            imageView.setImageResource(mTabList[i].normalDrawable)
            val lp = imageView.layoutParams as ConstraintLayout.LayoutParams
            lp.setMargins(0, 0, 0, 5.dp.toInt())
            imageView.layoutParams = lp

            val unReadLayoutParam = unReadCount.layoutParams as ConstraintLayout.LayoutParams
            unReadLayoutParam.setMargins(0, 0, -(10.dp.toInt()), 0)
            unReadCount.layoutParams = unReadLayoutParam
        }
    }

    private fun clickItem() {
        val itemView = mRootView.getChildAt(mCurrentIndex)
        val textView = itemView.findViewById<TextView>(R.id.text)
        val imageView = itemView.findViewById<ImageView>(R.id.image)
        val unReadCount = itemView.findViewById<TextView>(R.id.tv_notify_count)
        textView.setTextColor(mTabList[mCurrentIndex].selectedTextColor.resToColor())
        imageView.setImageResource(mTabList[mCurrentIndex].selectDrawable)
        val lp = imageView.layoutParams as ConstraintLayout.LayoutParams
        lp.setMargins(0, 0, 0, 0)
        imageView.layoutParams = lp

        val unReadLayoutParam = unReadCount.layoutParams as ConstraintLayout.LayoutParams
        unReadLayoutParam.setMargins(0, 0, -(10.dp.toInt()), 0)
        unReadCount.layoutParams = unReadLayoutParam
    }

    fun setDefaultItem(index: Int) {
        mCurrentIndex = index
        resetAllItems()
        clickItem()
    }

    fun updateMessageUnReadCount(count: Int) {
        val textView = mRootView.getChildAt(1).findViewById<TextView>(R.id.tv_notify_count)
        textView.visibility = if (count > 0) View.VISIBLE else View.GONE
        textView.text = if (count > 99) "99+" else count.toString()
    }

}