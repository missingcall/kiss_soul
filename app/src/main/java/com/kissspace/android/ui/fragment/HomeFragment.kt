package com.kissspace.android.ui.fragment

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.LogUtils
import com.kissspace.android.R
import com.kissspace.android.adapter.ViewTagsAdapter
import com.kissspace.android.databinding.FragmentMainHomeBinding
import com.kissspace.android.viewmodel.HomeViewModel
import com.kissspace.android.widget.HomeUserInfoDialog
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.model.RoomScreenMessageModel
import com.kissspace.common.model.immessage.BaseAttachment
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.*
import com.kissspace.network.result.collectData
import com.kissspace.util.runOnUi
import com.kissspace.webview.init.WebViewCacheHolder
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage
import org.json.JSONObject


/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/3
 * @Description: 首页fragment
 */
class HomeFragment : BaseFragment(R.layout.fragment_main_home) , WebViewUtil.JsCall {

    private val mBinding by viewBinding<FragmentMainHomeBinding>()
    private val mViewModel by viewModels<HomeViewModel>()
    private lateinit var mWebView: WebView
    private lateinit var mAdapter: ViewTagsAdapter
    private var isFirst = true
    private var angleNumber = 1
    private var mRoomCircleAngleLast  = 0
    private var mRankCircleAngleLast  = 0
    private var mRoomAngleList = arrayListOf(30,335,210)
    private var mRankAngleList = arrayListOf(210,145,30)
    private val customMessageObservable = Observer<List<ChatRoomMessage>> {
        it.forEach { that ->
            try {
                val json = JSONObject(that.attachStr)
                val type = json.getString("type")
                val attachment = BaseAttachment(type, json.get("data"))
                when (attachment.type) {
                    Constants.IMMessageType.MSG_TYPE_ROOM_NEW_BROADCAST_MESSAGE -> {
                        val data = parseCustomMessage<RoomScreenMessageModel>(attachment.data)
                        mBinding.conMessage.visibility = View.VISIBLE
                        mBinding.tvMessage.text = data.messageContent
                        mBinding.tvMessage.isSelected = true
                    }
                    Constants.IMMessageType.MSG_TYPE_ROOM_NEW_BROADCAST_END -> {
                        mBinding.conMessage.visibility = View.GONE
                        mBinding.tvMessage.text = ""
                    }
                }
            } catch (e: Exception) {
                LogUtils.e("消息格式错误！${e.message}")
            }
        }
    }

    private var animRunnable = Runnable {
        cardMoveAnim()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NIMClient.getService(ChatRoomServiceObserver::class.java)
            .observeReceiveMessage(customMessageObservable, true)
    }

    override fun initView(savedInstanceState: Bundle?) {
        mViewModel.getCurrentMessage()
        initEvents()
        initWebView()
    }

    override fun onResume() {
        super.onResume()
        if (!isFirst){
            mViewModel.getHomeUserList()
        }
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.roomList, onSuccess = {
            mAdapter = ViewTagsAdapter(it)
            mBinding.tagCloud.setAdapter(mAdapter)
        })

        collectData(mViewModel.homeMessage, onSuccess = {
            if (it != null){
                mBinding.conMessage.visibility = View.VISIBLE
                mBinding.tvMessage.text = it.messageContent
                mBinding.tvMessage.isSelected = true
            }
        })
    }

    private fun initEvents() {
        mBinding.flRoom.safeClick {
            jumpRoom(roomType = Constants.ROOM_TYPE_PARTY)
        }
        mBinding.flRank.safeClick {
            val url =
                getH5Url(
                    Constants.H5.roomRankUrl,
                    true
                ) + "&fixedHeight=${BarUtils.getStatusBarHeight()}"
            jump(RouterPath.PATH_WEBVIEW, "url" to url)
        }
        mBinding.conHomeSearch.safeClick {
            jump(RouterPath.PATH_SEARCH)
        }
        mBinding.tagCloud.setOnTagClickListener { _, _, position ->
            HomeUserInfoDialog.newInstance(mAdapter.mList,position).show(requireActivity().supportFragmentManager)
        }
    }

    private fun initWebView() {
        mWebView = WebViewCacheHolder.acquireHomeWebViewInternal(requireContext())
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        mWebView.alpha = 0f
        WebViewUtil.init(mWebView,this)
        showLoading()
        WebViewUtil.loadUrl(mWebView, "https://h5.zjtmwlkj.com/index/jubarindex/map.html")
        mBinding.container.addView(mWebView, layoutParams)
    }



    private fun cardMoveAnim(){
        angleNumber += 1
        if (angleNumber > 2){
            angleNumber = 0
        }
        val valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
        valueAnimator.duration = 1300
        valueAnimator.interpolator = LinearInterpolator()
        val roomLayoutParams = mBinding.flRoom.layoutParams as ConstraintLayout.LayoutParams
        val rankLayoutParams = mBinding.flRank.layoutParams as ConstraintLayout.LayoutParams
        mRoomCircleAngleLast = roomLayoutParams.circleAngle.toInt()
        mRankCircleAngleLast = rankLayoutParams.circleAngle.toInt()
        valueAnimator.addUpdateListener {
            if (isAdded){
                val animatedValue = (it.animatedValue as Float).toFloat()
                var roomNextAngle  = mRoomAngleList[angleNumber]
                roomLayoutParams.circleAngle =  mRoomCircleAngleLast + (roomNextAngle - mRoomCircleAngleLast) * animatedValue
                mBinding.flRoom.layoutParams = roomLayoutParams

                val rankNextAngle = mRankAngleList[angleNumber]
                rankLayoutParams.circleAngle =  mRankCircleAngleLast - (mRankCircleAngleLast - rankNextAngle) * animatedValue
                mBinding.flRank.layoutParams = rankLayoutParams
            }
        }
        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {
            }

            override fun onAnimationEnd(p0: Animator) {
                if (isAdded){
                    mBinding.center.postDelayed(animRunnable,5000)
                }
            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationRepeat(p0: Animator) {
            }
        })
        valueAnimator.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        //注销注册
        NIMClient.getService(ChatRoomServiceObserver::class.java)
            .observeReceiveMessage(customMessageObservable, false)
        mBinding.center.removeCallbacks(animRunnable)
    }

    override fun getName(): String = "sgg2077"

    @JavascriptInterface
    override fun loadsuc() {
        runOnUi {
            if (mWebView.alpha == 0f){
                mWebView.visibility = View.VISIBLE
                ViewCompat.animate(mWebView).alpha(1f).setDuration(666).start()
                mViewModel.getHomeUserList()
                isFirst = false
                mBinding.flRank.alpha = 1f
                mBinding.flRoom.alpha = 1f
                cardMoveAnim()
            }
        }
    }
}