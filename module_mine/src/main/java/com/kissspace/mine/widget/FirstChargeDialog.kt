package com.kissspace.mine.widget

import android.content.Context
import android.view.View
import android.widget.LinearLayout.HORIZONTAL
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.ext.safeClick
import com.kissspace.common.model.firstRecharge.CommonGiftModel
import com.kissspace.common.model.wallet.FirstRechargePayProductListResponses
import com.kissspace.common.util.format.Format
import com.kissspace.common.util.getFirstChargeAllGiftValue
import com.kissspace.util.orZero
import com.kissspace.common.widget.BaseDialog
import com.kissspace.module_common.databinding.CommonItemFirstChargeBinding
import com.kissspace.module_common.R
import com.kissspace.module_common.databinding.CommonDialogFirstRechargeBinding
import com.kissspace.util.loadImage

/**
 * @Author gaohangbo
 * @Date 2023/2/6 17:51.
 * @Describe 首充弹窗
 */
class FirstChargeDialog(context: Context) :
    BaseDialog<CommonDialogFirstRechargeBinding>(context) {

    var callBack: ((String, String) -> Unit?)? = null

    var listWechatRecharge: List<FirstRechargePayProductListResponses>? = null

    var listAliRecharge: List<FirstRechargePayProductListResponses>? = null

    init {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    override fun getLayoutId(): Int {
        return R.layout.common_dialog_first_recharge
    }

    override fun initView() {
        mBinding.rvGift.linear(HORIZONTAL).divider(R.drawable.common_divider_first_recharge_list)
            .setup {
                addType<CommonGiftModel> { R.layout.common_item_first_charge }
                onBind {
                    val model = getModel<CommonGiftModel>()
                    val mBinding = getBinding<CommonItemFirstChargeBinding>()
                    if(model.icon is String){
                        mBinding.ivFirstCharge.loadImage(model.icon as String)
                    }else if(model.icon is Int){
                        mBinding.ivFirstCharge.setImageResource(model.icon as Int)
                    }
                    if (model.day != null) {
                        mBinding.tvDay.visibility = View.VISIBLE
                        mBinding.tvDay.text = "(${model.day}天)"
                    } else {
                        mBinding.tvDay.visibility = View.GONE
                    }
                    if (model.number != null) {
                        mBinding.tvNumber.visibility = View.VISIBLE
                        mBinding.tvNumber.text = "(${model.number}个)"
                    } else {
                        mBinding.tvNumber.visibility = View.GONE
                    }
                }
            }.models = mutableListOf()

        mBinding.ivClose.safeClick {
            dismiss()
        }
        mBinding.clWechat.safeClick {
            if (listWechatRecharge?.size.orZero() > 0) {
                callBack?.invoke(
                    listWechatRecharge?.get(0)?.payChannelType.orEmpty(),
                    listWechatRecharge?.get(0)?.payProductId.orEmpty()
                )
            }
        }
        mBinding.clAli.safeClick {
            if (listAliRecharge?.size.orZero() > 0) {
                callBack?.invoke(
                    listAliRecharge?.get(0)?.payChannelType.orEmpty(),
                    listAliRecharge?.get(0)?.payProductId.orEmpty()
                )
            }
        }
    }

    fun setData(listWechatRecharge: List<FirstRechargePayProductListResponses>,listAliRecharge:List<FirstRechargePayProductListResponses>) {
        this.listWechatRecharge = listWechatRecharge
        this.listAliRecharge = listAliRecharge
        if (listWechatRecharge.isNotEmpty()) {
            val listAllCommonGiftModel: MutableList<CommonGiftModel> = ArrayList()
            val commonGiftModel=CommonGiftModel(com.kissspace.module_common.R.mipmap.common_icon_firstcharge_coin,"金币${Format.E.format(listWechatRecharge[0].payProductGoldCoin)}")
            listAllCommonGiftModel.add(commonGiftModel)
            val listCommonGiftModel = getFirstChargeAllGiftValue(listWechatRecharge[0].payProductGiftCoin)
            if (listCommonGiftModel != null) {
                listAllCommonGiftModel.addAll(listCommonGiftModel)
            }
            mBinding.rvGift.bindingAdapter.models = listAllCommonGiftModel
        }
        mBinding.tvMoney.text = Format.E.format(listWechatRecharge[0].payProductCash)+"元"
    }
}