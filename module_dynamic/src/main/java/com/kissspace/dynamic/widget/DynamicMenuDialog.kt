package com.kissspace.dynamic.widget

import by.kirich1409.viewbindingdelegate.viewBinding
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.widget.BaseBottomSheetDialogFragment
import com.kissspace.common.widget.BaseBottomSheetDialogFragmentV2
import com.kissspace.module_dynamic.R
import com.kissspace.module_dynamic.databinding.DynamicDialogMenuBinding

class DynamicMenuDialog(private val items:Array<String>,private val onItemClick:(DynamicMenuDialog,String)->Unit):BaseBottomSheetDialogFragmentV2(R.layout.dynamic_dialog_menu) {
    private val mBinding by viewBinding<DynamicDialogMenuBinding>()
    override fun initView() {
        mBinding.recyclerView.linear().setup {
            addType<String>(R.layout.dynamic_dialog_menu_item)
            onClick(R.id.root){
                onItemClick(this@DynamicMenuDialog,getModel())
            }
        }.mutable = items.toMutableList()

    }
}