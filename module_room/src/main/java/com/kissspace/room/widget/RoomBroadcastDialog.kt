package com.kissspace.room.widget

import android.os.Bundle
import android.view.Gravity
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.didi.drouter.api.DRouter
import com.kissspace.common.ext.safeClick
import com.kissspace.common.http.getSelectPayChannelList
import com.kissspace.common.http.getUserInfo
import com.kissspace.common.provider.IPayProvider
import com.kissspace.common.util.customToast
import com.kissspace.common.util.formatNumCoin
import com.kissspace.common.widget.BaseDialogFragment
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogRoomBroadcastBinding
import com.kissspace.network.result.collectData
import com.kissspace.room.viewmodel.RoomBroadcastViewModel
import com.kissspace.util.addAfterTextChanged
import com.kissspace.util.toast
import org.json.JSONObject

class RoomBroadcastDialog : BaseDialogFragment<RoomDialogRoomBroadcastBinding>(Gravity.CENTER) {
    private  var costCoin: Int = 0
    private  var price: Int = 0
    private  var userCoin: Double = 0.0
    private  var isStart: Boolean = true
    private val mViewModel by viewModels<RoomBroadcastViewModel>()

    companion object {
        fun newInstance(messageCoin: Int,start:Boolean) = RoomBroadcastDialog().apply {
            arguments = bundleOf("costCoin" to messageCoin,"start" to start)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        costCoin = arguments?.getInt("costCoin")!!
        isStart = arguments?.getBoolean("start")!!
        if (!isStart){
            costCoin += 500
        }
        price = costCoin
    }

    override fun getLayoutId() = R.layout.room_dialog_room_broadcast

    override fun initView() {
        getUserInfo(onSuccess = {
            userCoin = it.coin
            mBinding.tvBalance.text = formatNumCoin(userCoin)
        })
        mBinding.tvPrice.text = costCoin.toString()
        mBinding.editMessage.addAfterTextChanged {
            val message = it.toString().trim()
            if (message.isNotEmpty()){
                mBinding.tvLength.text = message.length.toString() +"/20"
            }else{
                mBinding.tvLength.text = "0/20"
            }
        }
        mBinding.tvDecrease.setOnClickListener {
            if (price == costCoin){
                if (!isStart){
                    customToast("价格不能再减降了哦")
                }else{
                    customToast("全服广播最低1000金币")
                }
            }else{
                price -= 500
                mBinding.tvPrice.text = price.toString()
            }
        }
        mBinding.tvAdd.setOnClickListener {
            price += 500
            mBinding.tvPrice.text = price.toString()
        }

        mBinding.tvSend.safeClick {
            if (userCoin < price){
                getSelectPayChannelList { list ->
                    DRouter.build(IPayProvider::class.java).getService()
                        .showPayDialogFragment(parentFragmentManager, list) {
                            getUserInfo(onSuccess = { userinfo ->
                               userCoin = userinfo.coin
                               mBinding.tvBalance.text = formatNumCoin(userCoin)
                            })
                        }
                }
                return@safeClick
            }
            sendMessage()
        }

        mBinding.tvCancel.setOnClickListener {
            dismiss()
        }

        mBinding.ivQuestion.safeClick {
            context?.let {
                RoomBroadcastTipsDialog(it).show()
            }
        }
    }

    private fun sendMessage() {
        val text = mBinding.editMessage.text.toString().trim()
        if (text.isEmpty()) {
            customToast("请输入你想说的")
        } else {
            mViewModel.send(price.toString(), text)
        }
    }

    override fun observerData() {
        super.observerData()
        collectData(mViewModel.sendMessageEvent, onSuccess = {
            dismiss()
            toast("发送成功")
        }, onError = {
            if (it.errCode == "53129"){
                customToast("当前价格发生改变，请重新发送")
                val json = JSONObject(it.message)
                val coin = json.getInt("data")
                isStart = false
                costCoin = coin + 500
                price = costCoin
                mBinding.tvPrice.text = costCoin.toString()
            }else{
                customToast(it.message)
            }
        })
    }



}