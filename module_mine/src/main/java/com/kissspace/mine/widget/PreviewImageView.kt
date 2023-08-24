package com.kissspace.mine.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.OnHierarchyChangeListener
import com.kissspace.mine.widget.DeleteImageView.OnDeleteClickListener
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.AttrRes
import com.blankj.utilcode.util.ConvertUtils
import com.kissspace.module_mine.R
import org.apmem.tools.layouts.FlowLayout
import com.kissspace.util.logE
import java.lang.NullPointerException
import java.util.ArrayList

class PreviewImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : FlowLayout(context, attrs, defStyleAttr), OnHierarchyChangeListener {
    private var DEFAULT_MAX_PHOTO_COUNT = 1

    private var ivPool //缓存删除的view
            : SimpleObjectPool<DeleteImageView>? = null

    var datas: MutableList<String> = ArrayList()

    private var addImageView: ImageView? = null

    private var mImageSize = 0


    private var fillViewInMeasure = true

    var onLoadPhotoListener: OnLoadPhotoListener? = null

    var onPhotoClickListener: OnPhotoClickListener? = null

    private var mOnAddImageClickListener: OnClickListener? = null


    private fun initView(context: Context) {
        orientation = HORIZONTAL
        layoutDirection = View.LAYOUT_DIRECTION_LTR
        setOnHierarchyChangeListener(this)
        ivPool = SimpleObjectPool(DeleteImageView::class.java, DEFAULT_MAX_PHOTO_COUNT)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mImageSize == 0) {
            mImageSize =
                (measuredWidth - paddingLeft - paddingRight - DEFAULT_PADDING * DEFAULT_MAX_PHOTO_COUNT * 2) / 3
            logE("mImageSize$mImageSize")
        }
        //获取view的宽度
        if (addImageView == null) {
            initAddImageView()
            addView(addImageView, generateDefaultImageSizeLayoutParams())
        }
        if (fillViewInMeasure) {
            fillViewInMeasure = false
            callToUpdateData()
        }

    }

    fun setDatas(datas: List<String>, onLoadPhotoListener: OnLoadPhotoListener, count: Int) {
        DEFAULT_MAX_PHOTO_COUNT = count
        //显示一张图片
        ivPool = SimpleObjectPool(DeleteImageView::class.java, count)
        this.datas.clear()
        this.datas.addAll(datas)
        this.onLoadPhotoListener = onLoadPhotoListener
        //callToUpdateData()
        //调用onMeasure 测量布局
        requestLayout()

    }

    fun clear() {
        datas.clear()
        callToUpdateData()
    }

    fun deleteData(pos: Int) {
        if (pos < 0 || pos > datas.size) return
        datas.removeAt(pos)
        callToUpdateData()
    }

    fun setNewData(datas1: List<String>) {
        datas.clear()
        for (data in datas1) {
            addData(data, false)
        }
        callToUpdateData()
    }

    fun addData(datas: List<String>) {
        if (datas.isEmpty()) return
        val hasRest = DEFAULT_MAX_PHOTO_COUNT - this.datas.size > 0
        if (!hasRest) return
        for (data in datas) {
            addData(data, false)
        }
        callToUpdateData()
    }

    fun addData(data: String) {
        addData(data, true)
    }

    private fun addData(data: String, needUpdataView: Boolean) {
        datas.add(data)
        if (needUpdataView) {
            callToUpdateData()
        }
    }

    val selectedCount: Int
        get() = datas.size
    val restPhotoCount: Int
        get() = DEFAULT_MAX_PHOTO_COUNT - datas.size

    private fun callToUpdateData() {
        if (isListEmpty(datas)) {
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
        removeAllViewsInLayout()
        var addImageViewPos = -1
        for (i in datas.indices) {
            var iv = ivPool!!.get()
            if (iv == null) {
                iv = DeleteImageView(context, mImageSize)
                iv.scaleType = ImageView.ScaleType.CENTER_CROP
                iv.onDeleteClickListener = object : OnDeleteClickListener {
                    override fun onDelete() {
                        if (onDeleteListener != null) {
                            onDeleteListener!!.onDelete(datas[i])
                            datas.removeAt(i)
                            callToUpdateData()
                        }
                    }
                }
            }
            val targetImageView = iv
            val data = datas[i]
            onLoadPhotoListener!!.onPhotoLoading(i, data, targetImageView)
            addViewInLayout(targetImageView, i, generateDefaultImageSizeLayoutParams(), true)
            addImageViewPos = i
            iv.setOnClickListener(OnClickListener {
                if (onPhotoClickListener != null) {
                    onPhotoClickListener!!.onPhotoClickListener(i, data, targetImageView)
                }
            })
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
        params.leftMargin = DEFAULT_PADDING
        params.rightMargin = DEFAULT_PADDING
        params.topMargin = DEFAULT_PADDING
        return params
    }

    fun clearViews() {
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

    private fun isListEmpty(datas: List<String>?): Boolean {
        return datas == null || datas.isEmpty()
    }

    private fun checkAddActionImageView(): Boolean {
        initAddImageView()
        return isListEmpty(datas) || datas.size < DEFAULT_MAX_PHOTO_COUNT
    }

    private fun initAddImageView() {
        if (addImageView == null) {
            addImageView = ImageView(context)
            //addImageView.setBackgroundColor(Color.RED);
            if (DEFAULT_MAX_PHOTO_COUNT > 3) {
                addImageView?.setImageResource(R.mipmap.mine_icon_add_picture)
            } else {
                addImageView?.setImageResource(R.mipmap.mine_icon_add_picture_white)
            }
            addImageView?.id = ADD_IMAGE_ID
            addImageView?.setOnClickListener { v ->
                if (mOnAddImageClickListener != null) {
                    mOnAddImageClickListener!!.onClick(v)
                }
            }
        }
    }


    override fun onChildViewAdded(parent: View, child: View) {

    }

    override fun onChildViewRemoved(parent: View, child: View) {
        if (child is DeleteImageView) {
            ivPool?.put(child)
        }
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
        initView(context)
    }

    companion object {
        private const val TAG = "PreviewImageView"
        val ADD_IMAGE_ID = generateViewId()
        private val DEFAULT_PADDING = ConvertUtils.dp2px(7f)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        ivPool?.clearPool()
    }
}