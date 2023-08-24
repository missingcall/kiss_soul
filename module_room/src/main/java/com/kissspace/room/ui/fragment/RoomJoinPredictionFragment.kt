package com.kissspace.room.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.databinding.BaseObservable
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.CommonApi
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.model.PredictionListBean
import com.kissspace.common.model.UserInfoBean
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.EditInputFilter
import com.kissspace.util.addAfterTextChanged
import com.kissspace.common.util.countDown
import com.kissspace.common.util.getMutable
import com.kissspace.common.util.parse2CountDown
import com.kissspace.common.util.customToast
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomFragmentJoinPredictionBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.room.http.RoomApi
import com.kissspace.util.toast
import java.math.BigDecimal
import kotlin.math.round

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/4
 * @Description: 参与积分预言fragment
 *
 */
class RoomJoinPredictionFragment : BaseFragment(R.layout.room_fragment_join_prediction) {
    private val mBinding by viewBinding<RoomFragmentJoinPredictionBinding>()
    private lateinit var model: PredictionListBean
    private var isLeft = true
    private var userInfo: UserInfoBean? = null

    companion object {
        fun newInstance(model: PredictionListBean, isLeft: Boolean) =
            RoomJoinPredictionFragment().apply {
                arguments = bundleOf("model" to model, "isLeft" to isLeft)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = arguments?.getParcelable("model")!!
        isLeft = arguments?.getBoolean("isLeft")!!
    }

    override fun initView(savedInstanceState: Bundle?) {
        initEvent()
        initRecyclerView()
        initData()
    }

    private fun initData() {
        mBinding.tvTitle.text = model.integralGuessTitle
        mBinding.tvResult.text =
            if (isLeft) "猜测：" + model.leftOption else "猜测：" + model.rightOption
        startCountDown()
        request<UserInfoBean>(CommonApi.API_GET_USER_INFO, Method.GET, onSuccess = {
            userInfo = it
            mBinding.tvBalance.text = it.getIntegralLong().toString()
            mBinding.editAmount.filters = arrayOf(
                EditInputFilter(
                    it.integral,
                    0
                )
            )
        })
    }

    private fun updateReward() {
        val text = mBinding.editAmount.text.toString().trim()
        val checkedCount = if (text.isNullOrEmpty()) 0 else text.toLong()
        if (checkedCount == 0L) {
            mBinding.tvReward.text = "0"
        } else {
            val otherAmount =
                (if (isLeft) model.rightBetAmount else model.leftBetAmount).toDouble()
            val mineAmount =
                (if (isLeft) model.leftBetAmount else model.rightBetAmount) + checkedCount
            val ratio = BigDecimal(otherAmount * 0.8f.div(mineAmount) + 1).setScale(
                2,
                BigDecimal.ROUND_HALF_UP
            ).toDouble()
            mBinding.tvReward.text = round((ratio * checkedCount)).toString()
        }

    }

    private fun startCountDown() {
        var leftTime = model.leftTime
        countDown(Long.MAX_VALUE,
            reverse = true,
            scope = lifecycleScope,
            onTick = { _ ->
                leftTime -= 1000
                mBinding.tvCountDown.text = leftTime.parse2CountDown() + "截止"
            })
    }

    private fun initEvent() {
        mBinding.editAmount.addAfterTextChanged { updateReward() }

        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                super.onLeftClick(titleBar)
                parentFragmentManager.popBackStack()
            }

            override fun onRightClick(titleBar: TitleBar?) {
                super.onRightClick(titleBar)
                CommonConfirmDialog(
                    requireContext(),
                    "提交后平台会根据回放确认情况，如果做实有违规情况，本场积分将全部退回。",
                    negativeString = "取消"
                ) {
                    if (this) {
                        jump(
                            RouterPath.PATH_REPORT,
                            "reportType" to Constants.ReportType.USER.type,
                            "userId" to userInfo?.userId.orEmpty()
                        )
                    }
                }.show()
            }
        })

        mBinding.editAmount.setOnFocusChangeListener { _, b ->
            if (b) {
                mBinding.recyclerView.getMutable<BetItem>().forEach {
                    it.isChecked = false
                    it.notifyChange()
                }
            }
        }

        mBinding.tvSubmit.safeClick {
            val param = mutableMapOf<String, Any?>()
            var betAmount = 0L
            val checkedList = mBinding.recyclerView.bindingAdapter.getCheckedModels<BetItem>()
            if (mBinding.editAmount.text.isNotEmpty()) {
                betAmount = mBinding.editAmount.text.toString().toLong()
            } else if (checkedList.isNotEmpty()) {
                val model = checkedList[0]
                betAmount = if (model.amount == 0L) userInfo?.getIntegralLong()!! else model.amount
            } else {
                customToast("请选择下注积分", true)
                return@safeClick
            }
            if (betAmount <= 0) {
                customToast("请选择下注积分", true)
                return@safeClick
            }
            if (betAmount < 10) {
                customToast("最少投注10积分", true)
                return@safeClick
            }
            if (betAmount > 3000000) {
                customToast("最多可投注3000000积分", true)
                return@safeClick
            }
            param["betAmount"] = betAmount
            param["integralGuessId"] = model.integralGuessId
            param["option"] = if (isLeft) "001" else "002"
            request<Int>(RoomApi.API_PREDICTION_BET, Method.POST, param, onSuccess = {
                customToast("投注成功", true)
                parentFragmentManager.popBackStack()
            }, onError = {
                toast(it.message)
            })
        }
    }

    private fun initRecyclerView() {
        val data = mutableListOf<BetItem>()
        data.add(BetItem(10, true))
        data.add(BetItem(100, false))
        data.add(BetItem(200, false))
        data.add(BetItem(500, false))
        data.add(BetItem(1000, false))
        data.add(BetItem(0, false))
        mBinding.recyclerView.grid(3).setup {
            addType<BetItem> { R.layout.room_dialog_prediction_join_bet_item }
            onBind {
                findView<LinearLayout>(R.id.root_join_prediction).setOnClickListener {
                    val model = getModel<BetItem>()
                    val list = mutable as MutableList<BetItem>
                    list.forEachIndexed { index, betItem ->
                        betItem.isChecked = index == modelPosition
                        betItem.notifyChange()
                    }
                    if (model.amount == 0L) {
                        mBinding.editAmount.setText(userInfo?.getIntegralLong().toString())
                    } else {
                        mBinding.editAmount.setText(model.amount.toString())
                    }
                    mBinding.editAmount.clearFocus()
                }
                findView<ImageView>(R.id.iv_logo).visibility =
                    if (getModel<BetItem>().amount > 0) View.VISIBLE else View.GONE
            }
        }.models = data
        mBinding.editAmount.setText("10")
    }
}

data class BetItem(var amount: Long, var isChecked: Boolean = false) : BaseObservable()