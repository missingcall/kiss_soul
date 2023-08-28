package com.kissspace.common.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.blankj.utilcode.util.SizeUtils
import com.kissspace.module_common.R

 /**
 * @Author gaohangbo
 * @Date 2023/2/7 10:46.
 * @Describe
 */
abstract class BaseDialog<VB : ViewDataBinding>(context: Context,private val isMatchParent:Boolean = false) :
    Dialog(context, R.style.Theme_CustomDialog) {
    lateinit var mBinding: VB
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), getLayoutId(), null, false)
        setContentView(mBinding.root)
        initView()
    }

    abstract fun getLayoutId(): Int

    abstract fun initView()


    override fun onStart() {
        super.onStart()
        this.window?.apply {
            val attr = attributes
            if (isMatchParent){
                attr.width = context.resources.displayMetrics.widthPixels
            }else{
                attr.width = context.resources.displayMetrics.widthPixels - SizeUtils.dp2px(64f)
            }
            attr.height = ViewGroup.LayoutParams.WRAP_CONTENT
            attributes = attr
        }
    }


}