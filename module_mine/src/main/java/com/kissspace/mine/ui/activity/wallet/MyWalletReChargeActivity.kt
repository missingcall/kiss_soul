package com.kissspace.mine.ui.activity.wallet

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.didi.drouter.api.DRouter
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.*
import com.kissspace.common.config.Constants
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.ext.setMarginStatusBar
import com.kissspace.common.http.getSelectPayChannelList
import com.kissspace.common.model.wallet.PayProductResponses
import com.kissspace.common.model.wallet.WalletRechargeList
import com.kissspace.common.provider.IPayProvider
import com.kissspace.common.util.format.Format
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.mine.viewmodel.WalletViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityWalletRechargeBinding
import com.kissspace.util.dp
import com.kissspace.util.immersiveStatusBar
import com.kissspace.util.isNotEmpty
import com.kissspace.util.logE
import com.kissspace.util.orZero


/**
 * @Author gaohangbo
 * @Date 2022/12/29 16:17.
 * @Describe 充值页面
 */
@Router(uri = RouterPath.PATH_USER_WALLET_GOLD_RECHARGE)
class MyWalletReChargeActivity : BaseActivity(R.layout.mine_activity_wallet_recharge) {
    private val mBinding by viewBinding<MineActivityWalletRechargeBinding>()

    private val mViewModel by viewModels<WalletViewModel>()

    //当前选择的支付方式
    private var currentWalletRechargeList: WalletRechargeList? = null

    //当前选择的充值产品
    private var currentPayProductResponses: PayProductResponses? = null

    //当前选择的人民币
    private var currentSelectRmb: Double? = null

    override fun initView(savedInstanceState: Bundle?) {
        immersiveStatusBar(true)
        mBinding.m = mViewModel
        mBinding.lifecycleOwner = this
        mBinding.titleBar.setMarginStatusBar()
        setTitleBarListener(mBinding.titleBar)

        mViewModel.walletRechargeHint.value = String.format(
            resources.getString(R.string.mine_wallet_charge_hint),
            MMKVProvider.wechatPublicAccount
        )
        mBinding.rvPayType.isNestedScrollingEnabled = false
        mBinding.rvPayType.linear().setup {
            addType<WalletRechargeList>(com.kissspace.module_common.R.layout.common_item_pay_type)
            singleMode = true
            onBind {
                val model = getModel<WalletRechargeList>()
                val image =
                    this.findView<ImageView>(com.kissspace.module_common.R.id.iv_icon)
                when(model.payChannelType){
                    Constants.WECHAT_PAY->{
                        image.setBackgroundResource(com.kissspace.module_common.R.mipmap.common_icon_pay_wechat)
                    }
                    Constants.ALI_PAY ->{
                        image.setBackgroundResource(com.kissspace.module_common.R.mipmap.common_icon_pay_alipay)
                    }
                    Constants.SAND_PAY ->{
                        image.setBackgroundResource(com.kissspace.module_common.R.mipmap.common_icon_pay_card)
                    }
                    Constants.SAND_ALI_PAY ->{
                        image.setBackgroundResource(com.kissspace.module_common.R.mipmap.common_icon_pay_alipay)
                    }
                    Constants.SAND_WECHAT_PAY ->{
                        image.setBackgroundResource(com.kissspace.module_common.R.mipmap.common_icon_pay_wechat)
                    }
                }
            }
            onChecked { position, isChecked, _ ->
                val model = getModel<WalletRechargeList>(position)
                model.isSelected = isChecked
                model.notifyChange()
            }
            onFastClick(com.kissspace.module_common.R.id.cl_pay_type) {
                //checkedSwitch(modelPosition)
                checkedAll(false)
                setChecked(modelPosition, true)
                val model = getModel<WalletRechargeList>(modelPosition)
                currentWalletRechargeList = model
                changePayTypeList(model.payProductListResponses)
            }
        }.models = mutableListOf()

        mBinding.rvWalletType.isNestedScrollingEnabled = false

        mBinding.rvWalletType.grid(3).setup {
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
                mViewModel.mRmb.value = "¥" +
                        Format.E.format(getModel<PayProductResponses>().payProductCash)
            }
        }.models = mutableListOf()


        //支付类型
        getSelectPayChannelList { list ->
            list.filter { it.isSelected }
            list.let { walletRechargeList ->
                mBinding.rvPayType.bindingAdapter.models=walletRechargeList
                val position =
                    walletRechargeList.indexOfFirst { it.payChannelName == Constants.WECHAT_PAY_NAME }
                logE("position__$position")
                position.let {
                    if (position != -1) {
                        mBinding.rvPayType.bindingAdapter.setChecked(position.orZero(), true)
                        //设置默认选中方式类型
                        currentWalletRechargeList = walletRechargeList[position.orZero()]
                        //默认设置选中
                        changePayTypeList(walletRechargeList[position.orZero()].payProductListResponses)
                    } else {
                        //默认选中第一个
                        if (walletRechargeList.isNotEmpty()) {
                            mBinding.rvPayType.bindingAdapter.setChecked(0, true)
                            //设置默认选中方式类型
                            currentWalletRechargeList = walletRechargeList[0]
                            //默认设置选中
                            changePayTypeList(walletRechargeList[0].payProductListResponses)
                        }
                    }
                }
            }
        }

        mBinding.scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = mBinding.scrollView.scrollY
            if (scrollY <= 0) {
                mBinding.titleBar.leftIcon.setTint(Color.WHITE)
                mBinding.layoutStatusBar.setBackgroundColor(Color.TRANSPARENT)
            } else if (scrollY <= 100.dp) {
                mBinding.titleBar.leftIcon.setTint(Color.WHITE)
                mBinding.layoutStatusBar.setBackgroundColor(Color.WHITE)
                val rate = (scrollY.toFloat() / 100.dp)
                mBinding.layoutStatusBar.background.alpha = (rate * 255).toInt()
            } else {
                mBinding.titleBar.leftIcon.setTint(Color.BLACK)
                mBinding.layoutStatusBar.setBackgroundColor(Color.WHITE)
                mBinding.layoutStatusBar.background.alpha = 255
            }
        }
        mBinding.clConfirm.safeClick {
            currentWalletRechargeList.let {
                if (currentPayProductResponses?.payChannelType == Constants.SAND_ALI_PAY) {
                    currentPayProductResponses?.payProductId?.let { it1 ->
                        mViewModel.sandPay("02020002", Constants.SAND_ALI_PAY, it1) {
                            //跳转到杉德支付
                            jump(RouterPath.PATH_H5WebActivity, "url" to it)
                        }
                    }
                } else if (currentPayProductResponses?.payChannelType == Constants.SAND_PAY) {
                    currentPayProductResponses?.payProductId?.let { it1 ->
                        mViewModel.sandPay("05030001", Constants.SAND_PAY, it1) {
                            //跳转到杉德支付
                            jump(RouterPath.PATH_H5WebActivity, "url" to it)
                        }
                    }
                }else if (currentPayProductResponses?.payChannelType == Constants.SAND_WECHAT_PAY) {
                    currentPayProductResponses?.payProductId?.let { it1 ->
//                        mViewModel.sandWechatPay("02010005", Constants.SAND_WECHAT_PAY, it1) {
//                            val api = WXAPIFactory.createWXAPI(this@MyWalletReChargeActivity,"wx969c9e7b79bc0503")
//                            api.registerApp("wx969c9e7b79bc0503")
//                            val req = WXLaunchMiniProgram.Req()
//                            req.userName = "gh_99ba4bfcf8b3"
//                            req.path = "pages/zf/index?"
//                            req.miniprogramType = 2
//                            api.sendReq(req)
//                        }
                    }
                } else {
                    //var result = "{\"merchant_uuid\":\"b1e67bf02558613ce1f146032949ace5\",\"merchant_name\":\"北京趣声科技有限公司-支付宝\",\"pay_result\":\"alipay_sdk=alipay-sdk-PHP-4.11.14.ALL&app_id=2021003173696405&biz_content=%7B%22body%22%3A%22aliPay%22%2C%22subject%22%3A%22aliPay%22%2C%22out_trade_no%22%3A%22230113164857323000018031%22%2C%22total_amount%22%3A%22648.00%22%2C%22passback_params%22%3A%22%22%2C%22time_expire%22%3A%222023-01-13+17%3A18%3A57%22%2C%22product_code%22%3A%22QUICK_MSECURITY_PAY%22%7D&charset=UTF-8&format=json&method=alipay.trade.app.pay&notify_url=http%3A%2F%2Ftest-financial.tianmcloud.top%2Fnotify%2Falipay&sign_type=RSA2&timestamp=2023-01-13+16%3A48%3A57&version=1.0&sign=N6F3Y6eecvx8Y1YRZ8h0Kug4HF3WJoDhfdWtlv2SaZhM9mi7L9RFkVP7entwO%2FDdN1cuZ%2FuJQt8POgMhgt1XGdFAmbMmbddrZnIxgRObElcneAyS4ccC14idZtQyEFFeSDReaLeTW7QlS0lqm0bUZfaAiX9EprThkvehi1HACax%2FCgc5cDz4ZHEUjOiNWRtqnQdh%2Fbl%2BebZxROAYexe3pBSgB0JH2nGOpqORiRKIhyqku9fXXF4ycvxOVSQ3RNkCdWCxKks12ONjLwM9TvkjYgeOdR5198Rhk9uqZEGgkKnm%2Bnf7lTxX3R5v0TCzA3O6k6yuzkJgtKDxH6juhArfjQ%3D%3D\",\"orderNo\":\"230113164857323000018031\"}"
                    DRouter.build(IPayProvider::class.java).getService().pay(
                        currentWalletRechargeList?.payChannelType.orEmpty(),
                        currentPayProductResponses?.payProductId.orEmpty(),
                        MyWalletGoldExChargeActivity@ this
                    ) { result ->
                        if (result) {
                            mViewModel.getMyMoneyBag {
                                mViewModel.mGoldNumber.value = Format.E_EE.format(it?.coin.orZero())
                            }
                        }
                    }
                }
            }
        }
    }


    private fun changePayTypeList(listPayProductList: List<PayProductResponses>?) {
        mBinding.rvWalletType.bindingAdapter.models=listPayProductList
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
                        mViewModel.mRmb.value = "¥" +
                                Format.E.format(listPayProductList?.get(0)?.payProductCash)
                        list[0].isSelected = true
                        list[0].notifyChange()
                    }
                }
            } else {
                mBinding.rvWalletType.bindingAdapter.checkedAll(false)
                if (position != null) {
                    mBinding.rvWalletType.bindingAdapter.setChecked(position, true)
                    list[position].isSelected = true
                    mViewModel.mRmb.value =
                        "¥" + Format.E.format(list[position].payProductCash)
                    list[position].notifyChange()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mViewModel.getMyMoneyBag {
            mViewModel.mGoldNumber.value = Format.E_EE.format(it?.coin.orZero())
        }
    }
}