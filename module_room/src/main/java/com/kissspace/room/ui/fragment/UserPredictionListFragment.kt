package com.kissspace.room.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.LogUtils
import com.drake.brv.utils.*
import com.kissspace.util.dp
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
import com.kissspace.common.model.immessage.PredictionDeleteMessage
import com.kissspace.common.model.immessage.PredictionResultMessage
import com.kissspace.common.util.*
import com.kissspace.common.util.format.Format
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomFragmentUserPredictionListBinding
import com.kissspace.network.result.collectData
import com.kissspace.room.viewmodel.PredictionViewModel
import com.kissspace.room.widget.GiftDialog
import com.kissspace.room.widget.PredictionMenuPop
import com.kissspace.room.widget.RoomPredictionDialog
import com.kissspace.util.loadImageCircle
import kotlinx.coroutines.Job
import org.json.JSONObject

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/29 19:17
 * @Description: 观众积分竞猜列表
 *
 */
class UserPredictionListFragment : BaseFragment(R.layout.room_fragment_user_prediction_list) {
    private val mBinding by viewBinding<RoomFragmentUserPredictionListBinding>()
    private val mViewModel by viewModels<PredictionViewModel>()
    private lateinit var crId: String
    private lateinit var roomType: String
    private var job: Job? = null
    private var menuPop: PredictionMenuPop? = null

    companion object {
        fun newInstance(crId: String, roomType: String) = UserPredictionListFragment().apply {
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

                    Constants.IMMessageType.MSG_DELETE_BET -> {//删除竞猜消息
                        val data = parseCustomMessage<PredictionDeleteMessage>(attachment.data)
                        onDeleteBetMessage(data)
                    }

                    Constants.IMMessageType.MSG_PREDICTION_CREATE -> {
                        mViewModel.getPredictionList(crId)
                    }
                }
            } catch (e: java.lang.Exception) {
                LogUtils.e("消息格式错误！${e.message}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mViewModel.getPredictionList(crId)
        mViewModel.getRankingList(crId)
        mViewModel.getUserInfo()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crId = arguments?.getString("crId")!!
        roomType = arguments?.getString("roomType")!!
        //注册消息监听
        NIMClient.getService(ChatRoomServiceObserver::class.java)
            .observeReceiveMessage(customMessageObservable, true)
    }


    override fun initView(savedInstanceState: Bundle?) {
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
        mBinding.layoutBottom.setOnClickListener {
            (parentFragment as RoomPredictionDialog).addFragment(
                RoomPredictionRankingFragment.newInstance(crId)
            )
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
            addType<PredictionListBean> { R.layout.room_user_prediciton_list_item }
            onFastClick(R.id.layout_question_left) {
                val model = getModel<PredictionListBean>()
                when (model.state) {
                    "001" -> {
                        if (model.userBetOption == "002") {
                            customToast("不能反向下注", true)
                        } else {
                            (parentFragment as RoomPredictionDialog).addFragment(
                                RoomJoinPredictionFragment.newInstance(model, true)
                            )
                        }
                    }

                    "002" -> customToast("等待主播结算中...", true)
                    "003", "004" -> customToast("该场预言已截止", true)
                }
            }
            onFastClick(R.id.layout_question_right) {
                val model = getModel<PredictionListBean>()
                when (model.state) {
                    "001" -> {
                        if (model.userBetOption == "001") {
                            customToast("不能反向下注", true)
                        } else {
                            (parentFragment as RoomPredictionDialog).addFragment(
                                RoomJoinPredictionFragment.newInstance(model, false)
                            )
                        }
                    }

                    "002" -> customToast("等待主播结算中...", true)
                    "003", "004" -> customToast("该场预言已截止", true)
                }

            }
        }.models = mutableListOf()
    }


    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.rankingEvent, onSuccess = {
            mBinding.layoutBottom.visibility = if (it.rankingList.isNullOrEmpty()) View.GONE else View.VISIBLE
            mBinding.layoutRankingWrapper.removeAllViews()
            val list =
                if (it!!.rankingList.size > 3) it.rankingList.subList(0, 3) else it.rankingList
            list.forEach { rank ->
                val imageView = ImageView(context)
                val lp = LinearLayout.LayoutParams(30.dp.toInt(), 30.dp.toInt())
                lp.setMargins(6.dp.toInt(), 0, 0, 0)
                imageView.layoutParams = lp
                imageView.loadImageCircle(rank.profilePath)
                mBinding.layoutRankingWrapper.addView(imageView)
            }
        }, onEmpty = {
            mBinding.layoutBottom.visibility = View.GONE
        })
        collectData(mViewModel.predictionListEvent, onSuccess = { result ->
            mBinding.tvEmpty.visibility = View.GONE
            mBinding.recyclerView.mutable.clear()
            mBinding.recyclerView.addModels(result)
            parseTitle()
            job?.cancel()
            if (result.find { it.state == "001" } != null) {
                job = countDown(
                    Long.MAX_VALUE,
                    reverse = true,
                    scope = lifecycleScope,
                    onTick = { _ ->
                        result.forEach {
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
        }, onEmpty = {
            mBinding.tvEmpty.visibility = View.VISIBLE
        })
        collectData(mViewModel.getUserInfoEvent, onSuccess = {
            mBinding.tvBalance.text = Format.E_UP.format(it.integral)
        })


        FlowBus.observerEvent<Event.RefreshIntegralEvent>(this) {
            if (!isAdded) {
                return@observerEvent
            }
            mViewModel.getUserInfo()
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

    private fun onUserBetMessage(message: PredictionBetMessage) {
        val list = mBinding.recyclerView.bindingAdapter.mutable as MutableList<PredictionListBean>
        val data = list.find { that -> that.integralGuessId == message.integralGuessId }
        if (data != null) {
            data.leftBetAmount += message.leftBetAmount
            data.rightBetAmount += message.rightBetAmount
            data.leftBetNum = message.leftBetNum
            data.rightBetNum = message.rightBetNum
            data.notifyChange()
        }
    }

    private fun onFinishBetMessage(message: FinishBetMessage) {
        val list = mBinding.recyclerView.bindingAdapter.mutable as MutableList<PredictionListBean>
        val data = list.find { that -> that.integralGuessId == message.integralGuessId }
        if (data != null) {
            data.state = "002"
            data.notifyChange()
        }
    }

    private fun onBetResultMessage(message: PredictionResultMessage) {
        val list = mBinding.recyclerView.bindingAdapter.mutable as MutableList<PredictionListBean>
        val data = list.find { that -> that.integralGuessId == message.integralGuessId }
        if (data != null) {
            data.state = "003"
            data.whichWin = message.option
            data.notifyChange()
        }
    }

    private fun onDeleteBetMessage(message: PredictionDeleteMessage) {
        val list = mBinding.recyclerView.getMutable<PredictionBetMessage>()
        val data = list.find { that -> that.integralGuessId == message.integralGuessId }
        if (data != null) {
            mBinding.recyclerView.mutable.remove(data)
            mBinding.recyclerView.bindingAdapter.notifyItemRemoved(list.indexOf(data))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NIMClient.getService(ChatRoomServiceObserver::class.java)
            .observeReceiveMessage(customMessageObservable, false)
        if (menuPop?.isShowing == true) {
            menuPop?.dismiss()
        }
    }
}


