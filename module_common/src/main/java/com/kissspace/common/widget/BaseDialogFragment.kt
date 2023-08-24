package com.kissspace.common.widget

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.kissspace.module_common.R
import com.kissspace.util.djsUniqueTag
import com.kissspace.util.logE

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/16 17:36
 * @Description: dialog fragment基类 封装了databinding
 *
 */
abstract class BaseDialogFragment<DB : ViewDataBinding>(
    private val gravity: Int,
    private val fullScreen: Boolean = false
) :
    DialogFragment() {
    protected lateinit var mBinding: DB


    abstract fun getLayoutId(): Int

    abstract fun initView()


    open fun observerData() {

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_CustomDialogFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        mBinding.lifecycleOwner = this
        initView()
        observerData()
        return mBinding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val attr = attributes
            attr.width = WindowManager.LayoutParams.MATCH_PARENT
            attr.height = if (fullScreen) WindowManager.LayoutParams.MATCH_PARENT else WindowManager.LayoutParams.WRAP_CONTENT
            attr.gravity = gravity
            attributes = attr
            if (gravity == Gravity.BOTTOM) {
                setWindowAnimations(R.style.DialogBottomInAnimation)
            }
        }
    }

    open fun show(manager: FragmentManager) {
        if (manager.isDestroyed || manager.isStateSaved) {
            return
        }
        show(manager, manager.djsUniqueTag)
    }
}