package com.kissspace.room.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.LogUtils
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.Constants
import com.kissspace.common.config.ConstantsKey
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.scrollToBottom
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.http.getUserInfo
import com.kissspace.common.model.immessage.BaseAttachment
import com.kissspace.common.model.immessage.RoomChatMessageModel
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.parseIntent
import com.kissspace.common.util.customToast
import com.kissspace.common.util.parseCustomMessage
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomActivityGameBinding
import com.kissspace.room.game.QuickStartGameViewModel
import com.kissspace.room.game.RoomGameListener
import com.kissspace.room.manager.RoomServiceManager
import com.kissspace.room.util.ViewUtils
import com.kissspace.util.hideKeyboard
import com.kissspace.util.setStatusBarColor
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage
import org.json.JSONObject


@Router(path = RouterPath.PATH_ROOM_GAME)
class GameActivity : BaseActivity(R.layout.room_activity_game) ,RoomGameListener{
    private val mBinding by viewBinding<RoomActivityGameBinding>()
    private val gameId by parseIntent<Long>()
    private val roomId by parseIntent<String>()

    private val gameViewModel = QuickStartGameViewModel()

    private val onBackCallBack = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            handleBack()
        }
    }

    /**
     * 房间消息观察者
     */
    private val observer = Observer<List<ChatRoomMessage>> {
        it?.forEach { msg ->
            try {
                val json = JSONObject(msg.attachStr)
                val type = json.getString("type")
                val data = BaseAttachment(type, json.get("data"))
                if (data.type == Constants.IMMessageType.MSG_TYPE_ROOM_CHAT_GAME) {
                    val message = parseCustomMessage<RoomChatMessageModel>(data.data)
                    onGameMessage(message.nickname+": "+message.content!!)
                }
            } catch (e: java.lang.Exception) {
                LogUtils.e("消息格式错误！$it")
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        val titleIcon = when(gameId){
            ConstantsKey.GAME_ID_BILLIARD-> "台球"
            ConstantsKey.GAME_ID_FLIGHT_CHESS-> "飞行棋"
            else->"你画我猜"
        }
        mBinding.titleBar.title = titleIcon
        onBackPressedDispatcher.addCallback(onBackCallBack)
        mBinding.titleBar.setTitleBarListener(onLeftClick = {
            handleBack()
        })

        gameViewModel.gameViewLiveData.observe(this) { view ->
            if (view == null) { // 在关闭游戏时，把游戏View给移除
                mBinding.gameContainer.removeAllViews();
            } else { // 把游戏View添加到容器内
                mBinding.gameContainer.addView(
                    view,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
        }

        gameViewModel.switchGame(this, RoomServiceManager.roomInfo?.crId, gameId)

        initMessage()
    }

    private fun initMessage() {
        if (gameId == ConstantsKey.GAME_ID_DRAW_AND_GUESS){
            getUserInfo({
                gameViewModel.name = it.nickname
            })
            NIMClient.getService(ChatRoomServiceObserver::class.java)
                .observeReceiveMessage(observer, true)

            KeyboardUtils.registerSoftInputChangedListener(
                this
            ) { height ->
                mBinding.roomInputMsgView.onSoftInputChanged(height)
                if (height == 0) {
                    setStatusBarColor(Color.TRANSPARENT, false)
                }
            }

            mBinding.tvInput.safeClick {
                ViewUtils.logSoftInputMode(window.attributes.softInputMode);
                mBinding.roomInputMsgView.show();
            }

            mBinding.roomInputMsgView.setSendMsgListener {
                gameViewModel.sendTextMessage(roomId,it.toString())
                onGameMessage(gameViewModel.name+": "+it)
                mBinding.roomInputMsgView.hide()
                mBinding.roomInputMsgView.clearInput()
            }

            mBinding.recycler.linear().setup {
                addType<String> {
                    R.layout.room_game_message_item
                }
                onBind {
                    val findView = findView<TextView>(R.id.tv_content)
                    findView.text =  getModel<String>()
                }
            }.models = mutableListOf()

            gameViewModel.setOnRoomGameListener(this)
        }
    }

    override fun onResume() {
        super.onResume()
        gameViewModel.onResume()
    }

    override fun onPause() {
        super.onPause()
        gameViewModel.onPause()
    }


    override fun onDestroy() {
        super.onDestroy()
        gameViewModel.onDestroy()
        NIMClient.getService(ChatRoomServiceObserver::class.java)
            .observeReceiveMessage(observer, false)
    }

    private fun handleBack(){
        CommonConfirmDialog(this,"确定要退出游戏吗？"){
            if (this){
                finish()
            }
        }.show()
    }

    override fun onGameState(isStart: Boolean) {
        if (gameId == ConstantsKey.GAME_ID_DRAW_AND_GUESS){
            if (isStart){
                mBinding.conGameMessage.visibility = View.VISIBLE
                mBinding.recycler.mutable.clear()
                mBinding.recycler.bindingAdapter.notifyDataSetChanged()
            }else{
                mBinding.conGameMessage.visibility = View.INVISIBLE
            }
        }
    }

    override fun onGameMessage(message: String) {
        if (gameId == ConstantsKey.GAME_ID_DRAW_AND_GUESS){
            mBinding.recycler.mutable.add(message)
            mBinding.recycler.bindingAdapter.notifyDataSetChanged()
            mBinding.recycler.scrollToBottom()
        }
    }

    override fun onKeyWordInputState(isShow: Boolean) {
        mBinding.tvInput.visibility = if (isShow) View.VISIBLE else View.INVISIBLE
    }
}