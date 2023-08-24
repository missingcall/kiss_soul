package com.kissspace.pay.ui
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.grid
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.config.Constants
import com.kissspace.common.ext.safeClick
import com.kissspace.common.http.getUserInfo
import com.kissspace.common.model.wallet.PayProductResponses
import com.kissspace.common.model.wallet.WalletRechargeList
import com.kissspace.common.util.format.Format
import com.kissspace.common.widget.BaseDialogFragment
import com.kissspace.module_pay.R
import com.kissspace.module_pay.databinding.PayDialogPayBinding
import com.kissspace.pay.utils.PayUtils
import com.kissspace.pay.viewmodel.PayViewModel
import com.kissspace.util.logE

/**
 * @Author gaohangbo
 * @Date 2023/2/5 21:29.
 * @Describe 充值弹窗
 */
class PayDialogFragment : BaseDialogFragment<PayDialogPayBinding>(Gravity.BOTTOM) {

    //当前选择的人民币
    private var currentSelectRmb: Double? = null

    private val mViewModel by viewModels<PayViewModel>()

    //当前选择的支付方式
    private var currentWalletRechargeList: WalletRechargeList? = null

    //当前选择的充值产品
    private var currentPayProductResponses: PayProductResponses? = null

    var onPaySuccess: (() -> Unit)? = null

    private var walletRechargeList:ArrayList<WalletRechargeList>?=null


    companion object {
        fun newInstance(walletRechargeList:ArrayList<WalletRechargeList>) = PayDialogFragment().apply {
            arguments = bundleOf("walletRechargeList" to walletRechargeList)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.pay_dialog_pay
    }

    override fun initView() {
        mBinding.m = mViewModel
        mBinding.lifecycleOwner = this
        getUserInfo(onSuccess = {
            mViewModel.mGoldNumber.value = Format.E.format(it.coin)
        })
        walletRechargeList=arguments?.getParcelableArrayList("walletRechargeList")
        //支付类型
        mBinding.rvPayType.isNestedScrollingEnabled = false

        mBinding.rvPayType.linear().setup {
            addType<WalletRechargeList>(com.kissspace.module_common.R.layout.common_item_pay_type)
            singleMode = true
            onBind {
                val model = getModel<WalletRechargeList>()
                val image =
                    this.findView<ImageView>(com.kissspace.module_common.R.id.iv_icon)
                if (model.payChannelType == Constants.WECHAT_PAY) {
                    image.setBackgroundResource(com.kissspace.module_common.R.mipmap.common_icon_pay_wechat)
                } else if (model.payChannelType == Constants.ALI_PAY) {
                    image.setBackgroundResource(com.kissspace.module_common.R.mipmap.common_icon_pay_alipay)
                } else if (model.payChannelType == Constants.SAND_PAY) {
                    image.setBackgroundResource(com.kissspace.module_common.R.mipmap.common_icon_pay_card)
                } else if (model.payChannelType == Constants.SAND_ALI_PAY) {
                    image.setBackgroundResource(com.kissspace.module_common.R.mipmap.common_icon_pay_alipay)
                }
            }
            onChecked { position, isChecked, _ ->
                val model = getModel<WalletRechargeList>(position)
                model.isSelected = isChecked
                model.notifyChange()
            }
            onClick(com.kissspace.module_common.R.id.cl_pay_type) {
                checkedAll(false)
                setChecked(modelPosition, true)
                val model = getModel<WalletRechargeList>(modelPosition)
                currentWalletRechargeList = model
                changePayTypeList(model.payProductListResponses)
            }
        }.models = walletRechargeList

        val position =
            walletRechargeList?.indexOfFirst { it.payChannelName == Constants.WECHAT_PAY_NAME }
        logE("position111$position")
        position?.let {
            if (it != -1) {
                mBinding.rvPayType.bindingAdapter.setChecked(it, true)
                //设置默认选中方式类型
                currentWalletRechargeList = walletRechargeList?.get(it)
                //默认设置选中
                changePayTypeList(walletRechargeList?.get(it)?.payProductListResponses)
            }
        }
        mBinding.tvConfirm.safeClick {
            currentWalletRechargeList?.let { item ->
                //var result = "{\"merchant_uuid\":\"b1e67bf02558613ce1f146032949ace5\",\"merchant_name\":\"北京趣声科技有限公司-支付宝\",\"pay_result\":\"alipay_sdk=alipay-sdk-PHP-4.11.14.ALL&app_id=2021003173696405&biz_content=%7B%22body%22%3A%22aliPay%22%2C%22subject%22%3A%22aliPay%22%2C%22out_trade_no%22%3A%22230113164857323000018031%22%2C%22total_amount%22%3A%22648.00%22%2C%22passback_params%22%3A%22%22%2C%22time_expire%22%3A%222023-01-13+17%3A18%3A57%22%2C%22product_code%22%3A%22QUICK_MSECURITY_PAY%22%7D&charset=UTF-8&format=json&method=alipay.trade.app.pay&notify_url=http%3A%2F%2Ftest-financial.tianmcloud.top%2Fnotify%2Falipay&sign_type=RSA2&timestamp=2023-01-13+16%3A48%3A57&version=1.0&sign=N6F3Y6eecvx8Y1YRZ8h0Kug4HF3WJoDhfdWtlv2SaZhM9mi7L9RFkVP7entwO%2FDdN1cuZ%2FuJQt8POgMhgt1XGdFAmbMmbddrZnIxgRObElcneAyS4ccC14idZtQyEFFeSDReaLeTW7QlS0lqm0bUZfaAiX9EprThkvehi1HACax%2FCgc5cDz4ZHEUjOiNWRtqnQdh%2Fbl%2BebZxROAYexe3pBSgB0JH2nGOpqORiRKIhyqku9fXXF4ycvxOVSQ3RNkCdWCxKks12ONjLwM9TvkjYgeOdR5198Rhk9uqZEGgkKnm%2Bnf7lTxX3R5v0TCzA3O6k6yuzkJgtKDxH6juhArfjQ%3D%3D\",\"orderNo\":\"230113164857323000018031\"}"
                activity?.let {
                    PayUtils.pay(
                        item.payChannelType,
                        currentPayProductResponses?.payProductId.orEmpty(),
                        it, onSuccess = {
                            onPaySuccess?.invoke()
                        })
                        dismiss()
                    }
            }
        }
    }

    private fun changePayTypeList(listPayProductList: List<PayProductResponses>?) {
        mBinding.rvWalletType.isNestedScrollingEnabled = false
        mBinding.rvWalletType.grid(3)
            .setup {
                addType<PayProductResponses>(com.kissspace.module_common.R.layout.common_item_pay_recharge)
                singleMode = true
                onChecked { position, isChecked, _ ->
                    currentPayProductResponses = getModel<PayProductResponses>(position)
                    currentSelectRmb = getModel<PayProductResponses>(position).payProductCash
                    currentPayProductResponses?.isSelected = isChecked
                    currentPayProductResponses?.notifyChange()
                }
                onFastClick(R.id.cl_charge) {
                    checkedAll(false)
                    setChecked(modelPosition, true)
                    listPayProductList?.get(modelPosition)?.let {
                        mViewModel.mSelectGold.value =
                            Format.E.format(listPayProductList[modelPosition].payProductGoldCoin)
                        mViewModel.mSelectRmb.value =
                            listPayProductList[modelPosition].payProductCash.toString()
                    }
                }
            }.models = listPayProductList

        mBinding.rvWalletType.bindingAdapter.checkedAll(false)

        listPayProductList?.let {
            if (listPayProductList.isNotEmpty()) {
                changePayTypeSelect(listPayProductList)
            }
        }
    }

    private fun changePayTypeSelect(listPayProductList: List<PayProductResponses>?) {
        listPayProductList.apply {
            this?.map { it.isSelected = false }
        }.also { list ->
            val position = list?.indexOfFirst {
                it.payProductCash == currentSelectRmb
            }
            if (position == -1) {
                list.size.let {
                    if (it > 0) {
                        mBinding.rvWalletType.bindingAdapter.checkedAll(false)
                        //设置默认选中第一个
                        mBinding.rvWalletType.bindingAdapter.setChecked(0, true)
                        list[0].isSelected = true
                        list[0].notifyChange()
                    }
                }
                mViewModel.mSelectGold.value =
                    Format.E.format(list[0].payProductGoldCoin)
                mViewModel.mSelectRmb.value = list[0].payProductCash.toString()
            } else {
                mBinding.rvWalletType.bindingAdapter.checkedAll(false)
                //设置默认选中第一个
                if (position != null) {
                    mBinding.rvWalletType.bindingAdapter.setChecked(position, true)
                    list[position].isSelected = true
                    list[position].notifyChange()
                    mViewModel.mSelectGold.value =
                        Format.E.format(list[position].payProductGoldCoin)
                    mViewModel.mSelectRmb.value = list[position].payProductCash.toString()
                }
            }
        }

    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val attr = attributes
            attr.width = WindowManager.LayoutParams.MATCH_PARENT
            attr.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes = attr
        }
    }

}