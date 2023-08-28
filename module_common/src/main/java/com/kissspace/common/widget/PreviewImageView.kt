package com.kissspace.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.AttrRes
import androidx.core.content.withStyledAttributes
import androidx.core.view.setMargins
import com.blankj.utilcode.util.ConvertUtils
import com.kissspace.module_common.R
import com.kissspace.util.logE
import org.apmem.tools.layouts.FlowLayout

class PreviewImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : FlowLayout(context, attrs, defStyleAttr) {

    var maxPhotoCount = 9

    var currentFileList: MutableList<String> = ArrayList()

    private var addImageView: ImageView? = null

    private var mImageSize = 0

    private var fillViewInMeasure = true

    var onLoadPhotoListener: OnLoadPhotoListener? = null

    var onPhotoClickListener: OnPhotoClickListener? = null

    private var mOnAddImageClickListener: OnClickListener? = null

    private var imagePadding :Int =0

    private fun initView(context: Context, attrs: AttributeSet?) {
        orientation = HORIZONTAL
        layoutDirection = View.LAYOUT_DIRECTION_LTR
        context.withStyledAttributes(attrs, R.styleable.PreviewImageView) {
            imagePadding = getDimensionPixelSize(R.styleable.PreviewImageView_image_padding,
                DEFAULT_PADDING
            )
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mImageSize == 0) {
            mImageSize =
                (measuredWidth - paddingLeft - paddingRight - DEFAULT_PADDING * 6) / 3
            logE("paddingLeft$paddingLeft---+paddingRight$paddingRight+---DEFAULT_PADDING$DEFAULT_PADDING")
            logE("mImageSize$mImageSize")
        }
        //获取view的宽度
        if (addImageView == null) {
            addImageView = ImageView(context)
            if (maxPhotoCount > 3) {
                addImageView?.setImageResource(R.mipmap.common_icon_add_picture)
            } else {
                addImageView?.setImageResource(R.mipmap.common_icon_add_picture_white)
            }
            addImageView?.id = ADD_IMAGE_ID
            addImageView?.setOnClickListener { v ->
                if (mOnAddImageClickListener != null) {
                    mOnAddImageClickListener!!.onClick(v)
                }
            }
            addView(addImageView, generateDefaultImageSizeLayoutParams())
        }
        if (fillViewInMeasure) {
            fillViewInMeasure = false
            callToUpdateData()
        }

    }

    fun setPictureList(
        pictureList: List<String>,
        onLoadPhotoListener: OnLoadPhotoListener,
        count: Int
    ) {
        maxPhotoCount = count
        //显示一张图片
        this.currentFileList.clear()
        this.currentFileList.addAll(pictureList)
        this.onLoadPhotoListener = onLoadPhotoListener
        //调用onMeasure 测量布局
        requestLayout()

    }

    fun clear() {
        currentFileList.clear()
        callToUpdateData()
    }

    fun deleteData(pos: Int) {
        if (pos < 0 || pos > currentFileList.size) return
        currentFileList.removeAt(pos)
        callToUpdateData()
    }

    fun setNewData(list: List<String>) {
        currentFileList.clear()
        for (data in list) {
            addData(data, false)
        }
        callToUpdateData()
    }

    fun addData(list: List<String>) {
        if (list.isEmpty()) return
        val hasRest = maxPhotoCount - this.currentFileList.size > 0
        if (!hasRest) return
        for (data in list) {
            addData(data, false)
        }
        callToUpdateData()
    }

    fun addData(data: String) {
        addData(data, true)
    }

    private fun addData(data: String, needUpdataView: Boolean) {
        currentFileList.add(data)
        if (needUpdataView) {
            callToUpdateData()
        }
    }

    val selectedCount: Int
        get() = currentFileList.size

    private fun callToUpdateData() {
        if (isListEmpty(currentFileList)) {
            clearViews()
            if (checkAddActionImageView()) {
                addView(
                    getImageViewWithOutParent(addImageView),
                    generateDefaultImageSizeLayoutParams()
                )
            }
        } else {
            fillView()
        }
    }

    private fun fillView() {
        if (onLoadPhotoListener == null) {
            throw NullPointerException("OnLoadPhotoListener must not be null,please check")
        }
        //重新清除了，就不需要显示了
        removeAllViewsInLayout()
        var addImageViewPos = -1
        for (i in currentFileList.indices) {
            val iv = DeleteImageView(context, mImageSize)
            iv.scaleType = ImageView.ScaleType.CENTER_CROP
            iv.onDeleteClickListener = object : DeleteImageView.OnDeleteClickListener {
                override fun onDelete() {
                    if (onDeleteListener != null) {
                        onDeleteListener!!.onDelete(currentFileList[i])
                        currentFileList.removeAt(i)
                        callToUpdateData()
                    }
                }
            }
            iv.setOnClickListener {
                if (onPhotoClickListener != null) {
                    onPhotoClickListener!!.onPhotoClickListener(i, currentFileList[i], iv)
                }
            }
            val data = currentFileList[i]
            onLoadPhotoListener!!.onPhotoLoading(i, data, iv)
            addViewInLayout(iv, i, generateDefaultImageSizeLayoutParams(), true)
            addImageViewPos = i
        }
        if (checkAddActionImageView()) {
            //可以添加imageView
            addViewInLayout(
                addImageView,
                addImageViewPos + 1,
                generateDefaultImageSizeLayoutParams(),
                true
            )
        }
    }

    private fun generateDefaultImageSizeLayoutParams(): LayoutParams {
        val params = LayoutParams(mImageSize, mImageSize)
        params.setMargins(DEFAULT_PADDING)
        return params
    }

    private fun clearViews() {
        removeAllViewsInLayout()
        getImageViewWithOutParent(addImageView)
        requestLayout()
    }


    private fun getImageViewWithOutParent(imageView: ImageView?): ImageView? {
        if (imageView == null) return null
        if (imageView.parent != null) {
            (imageView.parent as ViewGroup).removeView(imageView)
        }
        return imageView
    }

    private fun isListEmpty(list: List<String>?): Boolean {
        return list.isNullOrEmpty()
    }

    private fun checkAddActionImageView(): Boolean {
        return isListEmpty(currentFileList) || currentFileList.size < maxPhotoCount
    }


    interface OnLoadPhotoListener {
        fun onPhotoLoading(pos: Int, data: String, imageView: DeleteImageView)
    }

    interface OnPhotoClickListener {
        fun onPhotoClickListener(pos: Int, data: String, imageView: DeleteImageView)
    }

    fun setOnAddPhotoClickListener(onAddPhotoClickListener: OnClickListener?) {
        mOnAddImageClickListener = onAddPhotoClickListener
    }

    interface OnDeleteListener {
        fun onDelete(url: String?)
    }

    var onDeleteListener: OnDeleteListener? = null

    init {
        initView(context,attrs)
    }

    companion object {
        private const val TAG = "PreviewImageView"
        val ADD_IMAGE_ID = generateViewId()
        private val DEFAULT_PADDING = ConvertUtils.dp2px(7f)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }
}