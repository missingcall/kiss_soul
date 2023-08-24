package com.kissspace.common.util

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.blankj.utilcode.util.ColorUtils
import com.hjq.bar.TitleBar
import com.noober.background.drawable.DrawableCreator
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.kissspace.common.util.format.DateFormat
import com.kissspace.common.widget.HeadWearView
import com.kissspace.common.widget.UserLevelIconView
import com.kissspace.module_common.R
import com.kissspace.util.*
import java.io.File
import java.net.URL

object CommonBindingAdapter {
    @JvmStatic
    @BindingAdapter("titleText", requireAll = false)
    fun titleText(title: TitleBar, text: String?) {
        title.title = text
    }


    @JvmStatic
    @BindingAdapter(value = ["imageUrl", "imageRadius"])
    fun loadImage(imageView: ImageView, imageUrl: String?, imageRadius: Float = 0f) {
        imageView.loadImage(imageUrl, radius = imageRadius)
    }


    @JvmStatic
    @BindingAdapter("loadImage", requireAll = false)
    fun loadImage(imageView: ImageView, imageUrl: String?) {
        imageView.loadImage(imageUrl)
    }

    @JvmStatic
    @BindingAdapter("loadImageOrGone", requireAll = false)
    fun loadImageOrGone(imageView: ImageView, imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            imageView.visibility = View.GONE
        } else {
            imageView.visibility = View.VISIBLE
            imageView.loadImage(imageUrl)
        }
    }

    @JvmStatic
    @BindingAdapter("goneIf")
    fun viewVisibleIf(
        view: View, gone: Boolean?
    ) {
        view.visibility = if (gone == true) View.GONE else View.VISIBLE
    }


    @JvmStatic
    @BindingAdapter("loadChatImage")
    fun loadChatImage(imageView: ImageView, url: String) {
        if (url.startsWith("http") || url.startsWith("https")) {
            imageView.loadImage(url)
        } else {
            imageView.loadImage(File(url))
        }
    }

    @JvmStatic
    @BindingAdapter("goneUnless")
    fun viewGoneUnless(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("clIsSelected")
    fun clIsSelected(constraintLayout: ConstraintLayout, isSelected: Boolean) {
        constraintLayout.isSelected = isSelected
    }


    @JvmStatic
    @BindingAdapter("setImageBackground")
    fun setImageBackground(imageView: ImageView, resource: Int) {
        imageView.setBackgroundResource(resource)
    }

    @JvmStatic
    @BindingAdapter("loadImageResource")
    fun loadImageResource(imageView: ImageView, resource: Int) {
        imageView.setImageResource(resource)
    }


    @JvmStatic
    @BindingAdapter("commonBtnState", requireAll = false)
    fun commonBtnState(textView: TextView, enable: Boolean = false) {
        if (enable) {
            textView.setBackgroundResource(R.mipmap.common_button_bg)
            textView.setTextColor(ColorUtils.getColor(R.color.color_FFFD62))
        } else {
            textView.setBackgroundResource(R.mipmap.common_button_bg_no_check)
            textView.setTextColor(ColorUtils.getColor(R.color.color_50FFFD62))
        }
        textView.isEnabled = enable
    }

    @JvmStatic
    @BindingAdapter("loadCircleImage", requireAll = false)
    fun loadCircleImage(imageView: ImageView, imageUrl: String?) {
        imageView.loadImageCircle(imageUrl)
    }

    @JvmStatic
    @BindingAdapter("userLevelCount", requireAll = false)
    fun userLevelCount(textView: UserLevelIconView, count: Int) {
        textView.setLeveCount(count)
    }

    @JvmStatic
    @BindingAdapter("imageResource", requireAll = false)
    fun imageResource(imageView: ImageView, resource: Int) {
        imageView.setImageResource(resource)
    }

    @JvmStatic
    @BindingAdapter("longToTime", requireAll = false)
    fun longToTime(textView: TextView, time: Long) {
        textView.text = time.formatDate(DateFormat.YYYY_MM_DD_HH_MM_SS)
    }

    @BindingAdapter("setRightDrawable")
    fun setRightDrawable(textView: TextView, drawable: Drawable?) {
        if (drawable == null) {
            textView.setCompoundDrawables(null, null, null, null)
        } else {
            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
            textView.setCompoundDrawables(null, null, drawable, null)
        }
    }

    @BindingAdapter("setLeftDrawable")
    fun setLeftDrawable(textView: TextView, drawable: Drawable?) {
        if (drawable == null) {
            textView.setCompoundDrawables(null, null, null, null)
        } else {
            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
            textView.setCompoundDrawables(drawable, null, null, null)
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["headPagUrl", "headStaticUrl"])
    fun loadHeadWear(headWearView: HeadWearView, headPagUrl: String?, headStaticUrl: String?) {
        if (headPagUrl.isNullOrEmpty()) {
            headWearView.setHeadWearUrl(headPagUrl, headStaticUrl)
        } else {
            getPagPath(headPagUrl) {
                headWearView.setHeadWearUrl(it, headStaticUrl)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("imageViewSelected", requireAll = false)
    fun imageViewSelected(imageView: ImageView, imageViewSelected: Boolean) {
        imageView.isSelected=imageViewSelected
    }

    @JvmStatic
    @BindingAdapter("urlChecked", requireAll = false)
    fun urlChecked(imageView: ImageView, isChecked: Boolean) {
        imageView.setImageResource(if (isChecked) com.kissspace.module_common.R.mipmap.common_icon_checkbox_selected else com.kissspace.module_common.R.mipmap.common_icon_check_normal)
    }

    @JvmStatic
    @BindingAdapter("showWalletType")
    fun showWalletType(textView: TextView, text: String?) {
        textView.text = text
    }

}