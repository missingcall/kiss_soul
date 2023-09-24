package com.kissspace.dynamic.binding

import android.opengl.Visibility
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.drake.statelayout.Status
import com.kissspace.common.ext.setDrawable
import com.kissspace.common.model.dynamic.DynamicInfoRecord
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.module_dynamic.R

object DynamicBindingAdapter {

    @JvmStatic
    @BindingAdapter("dynamicSex", requireAll = false)
    fun dynamicSex(imageView: ImageView, sex: String?) {
        imageView.setImageResource(if (sex=="001") R.mipmap.dynamic_icon_sex_female else R.mipmap.dynamic_icon_sex_male)
    }

    @JvmStatic
    @BindingAdapter("dynamicLikeStatus", requireAll = false)
    fun dynamicLikeStatus(textView: TextView, status:Boolean) {
        textView.setDrawable(if (status) R.mipmap.dynamic_like else R.mipmap.dynamic_unlike,
            Gravity.START)
    }

    @JvmStatic
    @BindingAdapter("dynamicLikeAmount", requireAll = false)
    fun dynamicLikeAmount(textView: TextView, amount:Int) {
        textView.text = amount.toString()
    }

    @JvmStatic
    @BindingAdapter("dynamicFollowVisible", requireAll = false)
    fun dynamicFollowVisible(imageView: ImageView, model:DynamicInfoRecord?) {
        imageView.visibility = if (model?.followStatus==true || model?.userId==MMKVProvider.userId) View.GONE else View.VISIBLE

    }





}