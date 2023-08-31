package com.kissspace.room.ui.activity

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.drake.softinput.setWindowSoftInput
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.parseIntent
import com.kissspace.common.util.setViewPagerTouchSlop
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomActivityAudioBinding
import com.kissspace.room.adapter.RoomPagerAdapter
import com.kissspace.room.manager.RoomServiceManager
import com.kissspace.room.ui.fragment.LiveAudioFragment
import com.kissspace.util.immersiveStatusBar
import com.kissspace.util.isNotEmpty
import com.kissspace.util.logE
import com.netease.nimlib.sdk.media.player.AudioPlayer

@Router(path = RouterPath.PATH_LIVE_AUDIO)
class LiveAudioActivity : BaseActivity(R.layout.room_activity_audio) {
    private val mBinding by viewBinding<RoomActivityAudioBinding>()
    private var crId by parseIntent<String>()
    private var stochastic by parseIntent<String>()
    private var userId by parseIntent<String>()
    private var roomInfo by parseIntent<String>()
    private var canReplaceFragment = true
    private var middlePosition = 0
    private var currentPosition = 0
    private var currentRoomId: String = ""
    private val mediaPlayer by lazy { MediaPlayer() }

    override fun initView(savedInstanceState: Bundle?) {
        immersiveStatusBar(false)
        initViewPager()
        val afd = assets.openFd("official_msg.mp3")
        mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        mediaPlayer.isLooping = true
        mediaPlayer.prepare()
        mediaPlayer.setVolume(0f, 0f)
        mediaPlayer.start()
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    private fun initViewPager() {
        mBinding.viewPager.setViewPagerTouchSlop()
        middlePosition = if (RoomServiceManager.roomList.isNotEmpty()) Int.MAX_VALUE / 2 else 0
        currentPosition = middlePosition
        currentRoomId = crId
        mBinding.viewPager.apply {
            val pagerAdapter = RoomPagerAdapter()
            pagerAdapter.setData(mutableListOf(""))
            adapter = pagerAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    canReplaceFragment = true
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                        canReplaceFragment = false
                    }
                }
            })
            setPageTransformer { page, position ->
                val viewGroup = page as ViewGroup
                if (position == 0f && canReplaceFragment) {
                    loadVideoAndChatRoom(viewGroup.findViewById(R.id.container))
                }
            }
            setCurrentItem(middlePosition, false)
        }
    }


    private fun loadVideoAndChatRoom(viewGroup: ViewGroup) {
        var index = RoomServiceManager.roomList.indexOfFirst { it.crId == currentRoomId }
        val roomId = when {
            mBinding.viewPager.currentItem > currentPosition -> {
                index++
                if (index >= RoomServiceManager.roomList.size - 1) {
                    index = 0
                }
                logE("当前房间是下一个--${index}--${RoomServiceManager.roomList[index].roomTitle}")
                RoomServiceManager.roomList[index].crId

            }

            mBinding.viewPager.currentItem < currentPosition -> {
                index--
                if (index <= 0) {
                    index = RoomServiceManager.roomList.size - 1
                }
                logE("当前房间是上你一个--${index}--${RoomServiceManager.roomList[index].roomTitle}")
                RoomServiceManager.roomList[index].crId

            }

            else -> currentRoomId
        }
        supportFragmentManager.commit(true) {
            replace(
                viewGroup.id,
                LiveAudioFragment.newInstance(roomId, stochastic, userId,roomInfo)
            )
        }
        currentPosition = mBinding.viewPager.currentItem
        currentRoomId = roomId
    }

    override fun handleBackPressed() {
        RoomServiceManager.roomInfo?.let {
            FlowBus.post(Event.ShowRoomFloating(it.crId, it.roomIcon))
        }
        super.handleBackPressed()
    }
}