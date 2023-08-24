package com.kissspace.common.widget

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kissspace.util.djsUniqueTag
import com.kissspace.util.logE

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/13 17:51
 * @Description: BottomSheetDialogFragment基类
 *
 */
abstract class BaseBottomSheetDialogFragment<VB : ViewBinding>(private val softInputMode: Int? = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) :
    BottomSheetDialogFragment() {

    lateinit var mBinding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, com.kissspace.module_common.R.style.Theme_CustomBottomSheetDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mBinding = getViewBinding()
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            setSoftInputMode(softInputMode!!)
        }
        dialog.setContentView(mBinding.root)
        initView()
        observerData()
        return dialog
    }


    abstract fun getViewBinding(): VB

    abstract fun initView()

    open fun show(manager: FragmentManager) {
        if (!isAdded && !manager.isDestroyed && !manager.isStateSaved) {
            try {
                this.show(manager, djsUniqueTag)
            } catch (e: IllegalStateException) {
                logE(e)
            }
        }
    }

    open fun observerData() {

    }
}