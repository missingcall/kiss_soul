package com.kissspace.mine.widget

import android.os.Bundle
import android.view.Gravity
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.kissspace.common.model.GoodsListBean
import com.kissspace.common.util.getBundleParcelable
import com.kissspace.common.widget.BaseDialogFragment
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineDialogGoodsBuyBinding

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/4/11 16:17
 * @Description: 购买装扮选择弹窗
 *
 */

class GoodsBuyDialog : BaseDialogFragment<MineDialogGoodsBuyBinding>(Gravity.CENTER) {
    private lateinit var model: GoodsListBean
    private var callBack: ((String) -> Unit)? = null
    private var payMethod = "001"

    companion object {
        fun newInstance(model: GoodsListBean) = GoodsBuyDialog().apply {
            arguments = bundleOf("model" to model)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = arguments?.getBundleParcelable("model")!!
    }

    override fun getLayoutId(): Int = R.layout.mine_dialog_goods_buy

    override fun initView() {
        mBinding.m = model
        mBinding.ivCheckCoin.setOnClickListener {
            payMethod = "001"
            mBinding.ivCheckCoin.setImageResource(com.kissspace.module_common.R.mipmap.common_icon_checkbox_selected)
            mBinding.ivCheckPoints.setImageResource(com.kissspace.module_common.R.mipmap.common_icon_check_normal)
        }
        mBinding.ivCheckPoints.setOnClickListener {
            payMethod = "002"
            mBinding.ivCheckPoints.setImageResource(com.kissspace.module_common.R.mipmap.common_icon_checkbox_selected)
            mBinding.ivCheckCoin.setImageResource(com.kissspace.module_common.R.mipmap.common_icon_check_normal)
        }

        mBinding.tvBuy.setOnClickListener {
            callBack?.invoke(payMethod)
            dismiss()
        }

    }


    fun setCallback(callBack: (String) -> Unit): GoodsBuyDialog {
        this.callBack = callBack
        return this
    }

}