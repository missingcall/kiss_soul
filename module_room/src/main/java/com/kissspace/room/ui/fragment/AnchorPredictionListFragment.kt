package com.kissspace.room.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.LogUtils
import com.drake.brv.utils.*
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.Constants
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.model.PredictionListBean
import com.kissspace.common.model.immessage.BaseAttachment
import com.kissspace.common.model.immessage.FinishBetMessage
import com.kissspace.common.model.immessage.PredictionBetMessage
import com.kissspace.common.model.immessage.PredictionResultMessage
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.countDown
import com.kissspace.common.util.formatNum
import com.kissspace.common.util.getMutable
import com.kissspace.common.util.parseCustomMessage
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomFragmentAnchorPredictionListBinding
import com.kissspace.network.result.collectData
import com.kissspace.room.viewmodel.PredictionViewModel
import com.kissspace.room.widget.GiftDialog
import com.kissspace.room.widget.PredictionMenuPop
import com.kissspace.room.widget.RoomPredictionDialog
import kotlinx.coroutines.Job
import org.json.JSONObject
import java.lang.Exception

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/29 19:17
 * @Description: 主播积分竞猜列表
 *
 */
class AnchorPredictionListFragment : BaseFragment(R.layout.room_fragment_anchor_prediction_list) {
    private val mBinding by viewBinding<RoomFragmentAnchorPredictionListBinding>()
    private val mViewModel by viewModels<PredictionViewModel>()
    private lateinit var crId: String
    private lateinit var roomType: String
    private var job: Job? = null
    private var menuPop: PredictionMenuPop? = null

    companion object {
        fun newInstance(crId: String, roomType: String) =
            AnchorPredictionListFragment().apply {
                arguments = bundleOf("crId" to crId, "roomType" to roomType)
            }
    }

    private val customMessageObservable = Observer<List<ChatRoomMessage>> {
        it.forEach {
            try {
                val json = JSONObject(it.attachStr)
                val type = json.getString("type")
                val attachment = BaseAttachment(type, json.get("data"))
                when (attachment.type) {
                    Constants.IMMessageType.MSG_PREDICTION_BET -> { //用户投注
                        val data = parseCustomMessage<PredictionBetMessage>(attachment.data)
                        onUserBetMessage(data)
                    }

                    Constants.IMMessageType.MSG_FINISH_BET -> {//终止投注
                        val data = parseCustomMessage<FinishBetMessage>(attachment.data)
                        onFinishBetMessage(data)
                    }

                    Constants.IMMessageType.MSG_PREDICTION_RESULT -> {//下注结果
                        val data = parseCustomMessage<PredictionResultMessage>(attachment.data)
                        onBetResultMessage(data)
                        mViewModel.getUserInfo()
                        parseTitle()
                    }
                }
            } catch (e: Exception) {
                LogUtils.e("消息格式错误！${e.message}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crId = arguments?.getString("crId")!!
        roomType = arguments?.getString("roomType")!!
        //注册消息监听
        NIMClient.getService(ChatRoomServiceObserver::class.java)
            .observeReceiveMessage(customMessageObservable, true)
    }

    override fun onResume() {
        super.onResume()
        mViewModel.getPredictionList(crId)
        mViewModel.getUserInfo()
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.layoutAdd.setOnClickListener {
            (parentFragment as RoomPredictionDialog).addFragment(
                RoomAddPredictionFragment.newInstance(crId, null)
            )
        }
        mBinding.ivMenu.setOnClickListener {
            menuPop = PredictionMenuPop(requireContext()) {
                when (this) {
                    PredictionMenuPop.ClickType.MENU_TIPS -> {
                        (parentFragment as RoomPredictionDialog).addFragment(
                            PredictionRuleFragment()
                        )
                    }

                    PredictionMenuPop.ClickType.MENU_MINE -> {
                        (parentFragment as RoomPredictionDialog).addFragment(
                            RoomMinePredictionFragment.newInstance(crId)
                        )
                    }

                    PredictionMenuPop.ClickType.MENU_SQUARE -> {
                        (parentFragment as RoomPredictionDialog).addFragment(
                            RoomPredictionSquareFragment.newInstance(crId)
                        )
                    }
                }
            }
            menuPop?.show(mBinding.ivMenu)
        }

        mBinding.tvBalance.setOnClickListener {
            val from =
                if (roomType == Constants.ROOM_TYPE_PARTY) Constants.GiftDialogFrom.FROM_PARTY else Constants.GiftDialogFrom.FROM_CHAOBO
            GiftDialog.newInstance(crId, MMKVProvider.userId, true, from)
                .show(childFragmentManager)
        }
        initRecyclerView()
    }

    private fun initRecyclerView() {
        mBinding.recyclerView.linear().setup {
            addType<PredictionListBean> { R.layout.room_anchor_prediciton_list_item }
            onFastClick(R.id.tv_delete) {
                val model = getModel<PredictionListBean>()
                mViewModel.deletePrediction(model.integralGuessId) {
                    mutable.removeAt(modelPosition)
                    bindingAdapter?.notifyItemRemoved(modelPosition)
                    mBinding.tvEmpty.visibility = if (mutable.isEmpty()) View.VISIBLE else View.GONE
                    switchAddButton()
                }
            }
            onFastClick(R.id.tv_again) {
                val model = getModel<PredictionListBean>()
                (parentFragment as RoomPredictionDialog).addFragment(
                    RoomAddPredictionFragment.newInstance(crId, model)
                )
            }
            onFastClick(R.id.tv_finish_bet) {
                val model = getModel<PredictionListBean>()
                mViewModel.stopBet(model.integralGuessId, modelPosition)
            }
            onFastClick(R.id.tv_settle_invalid) {
                val model = getModel<PredictionListBean>()
                mViewModel.settleBet(model.integralGuessId, "003")
            }
            onFastClick(R.id.tv_settle_left_win) {
                val model = getModel<PredictionListBean>()
                mViewModel.settleBet(model.integralGuessId, "001")
            }
            onFastClick(R.id.tv_settle_right_win) {
                val model = getModel<PredictionListBean>()
                mViewModel.settleBet(model.integralGuessId, "002")
            }
        }.models = mutableListOf()
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.predictionListEvent, onSuccess = { result ->
            mBinding.tvEmpty.visibility = View.GONE
            mBinding.recyclerView.mutable.clear()
            mBinding.recyclerView.addModels(result)
            parseTitle()
            switchAddButton()
            startCountDown(result)
        }, onEmpty = {
            mBinding.tvEmpty.visibility = View.VISIBLE
        })

        collectData(mViewModel.betEvent, onSuccess = {
            switchAddButton()
        })

        collectData(mViewModel.stopBetEvent, onSuccess = {
            val data = mBinding.recyclerView.models?.get(it) as PredictionListBean
            data.state = "002"
            data.notifyChange()
        })

        collectData(mViewModel.getUserInfoEvent, onSuccess = {
            mBinding.tvBalance.text = formatNum(it.integral)
        })




        FlowBus.observerEvent<Event.RefreshIntegralEvent>(this) {
            if (!isAdded) {
                return@observerEvent
            }
            mViewModel.getUserInfo()
        }

    }

    private fun startCountDown(list: List<PredictionListBean>) {
        job?.cancel()
        if (list.find { it.state == "001" } != null) {
            job = countDown(Long.MAX_VALUE, reverse = true, scope = lifecycleScope, onTick = { _ ->
                list.forEach {
                    if (it.state == "001") {
                        if (it.leftTime <= 0) {
                            it.state = "002"
                        } else {
                            it.leftTime -= 1000
                        }
                        it.notifyChange()
                    }
                }
            })
        }
    }

    private fun parseTitle() {
        val result = mBinding.recyclerView.getMutable<PredictionListBean>()
        val firstProcessingItem = result.find { it.state == "001" || it.state == "002" }
        val processingItems = result.filter { it.state == "001" || it.state == "002" }
        firstProcessingItem?.isShowTitle = true
        firstProcessingItem?.title = "进行中(${processingItems.size}/3)"

        val firstHistoryItem = result.find { it.state == "003" || it.state == "004" }
        firstHistoryItem?.isShowTitle = true
        firstHistoryItem?.title = "历史预言"
    }

    private fun switchAddButton() {
        val list = mBinding.recyclerView.mutable as MutableList<PredictionListBean>
        if (list.filter { it.state == "001" || it.state == "002" }.size >= 3) {
            mBinding.layoutAddWrapper.visibility = View.GONE
        } else {
            mBinding.layoutAddWrapper.visibility = View.VISIBLE
        }
    }

    private fun onUserBetMessage(message: PredictionBetMessage) {
        val list =
            mBinding.recyclerView.bindingAdapter.mutable as MutableList<PredictionListBean>
        val data = list.find { that -> that.integralGuessId == message.integralGuessId }
        if (data != null) {
            data.leftBetAmount += message.leftBetAmount
            data.rightBetAmount += message.rightBetAmount
            data.leftBetNum = message.leftBetNum
            data.rightBetNum = message.rightBetNum
            data.leftTimes = message.leftTimes
            data.rightTimes = message.rightTimes
            data.notifyChange()
        }
    }

    private fun onFinishBetMessage(message: FinishBetMessage) {
        val list =
            mBinding.recyclerView.bindingAdapter.mutable as MutableList<PredictionListBean>
        val data = list.find { that -> that.integralGuessId == message.integralGuessId }
        if (data != null) {
            data.state = "002"
            data.notifyChange()
        }
    }

    private fun onBetResultMessage(message: PredictionResultMessage) {
        val list =
            mBinding.recyclerView.bindingAdapter.mutable as MutableList<PredictionListBean>
        val data = list.find { that -> that.integralGuessId == message.integralGuessId }
        if (data != null) {
            data.state = "003"
            data.whichWin = message.option
            data.notifyChange()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //注册消息监听
        NIMClient.getService(ChatRoomServiceObserver::class.java)
            .observeReceiveMessage(customMessageObservable, false)
        if (menuPop?.isShowing == true) {
            menuPop?.dismiss()
        }
    }
}