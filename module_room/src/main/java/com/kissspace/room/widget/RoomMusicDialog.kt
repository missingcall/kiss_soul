package com.kissspace.room.widget

import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.angcyo.tablayout.DslTabLayout
import com.kissspace.util.dp
import com.kissspace.util.toast
import com.luck.picture.lib.utils.DateUtils
import com.qmuiteam.qmui.kotlin.onClick
import com.kissspace.common.config.Constants
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setUpViewPager2
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.model.MusicSongsInfoModel
import com.kissspace.common.widget.BaseDialogFragment
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogMusicBinding
import com.kissspace.room.interfaces.MusicPlayListener
import com.kissspace.room.manager.RoomMusicManager
import com.kissspace.room.ui.fragment.MusicCollectListFragment
import com.kissspace.room.ui.fragment.MusicListFragment


/**
 * @CreateDate: 2023-03-15 17:25:57
 * @Description: 房间点歌弹窗
 *
 */
class RoomMusicDialog : BaseDialogFragment<RoomDialogMusicBinding>(Gravity.BOTTOM) {


    private val  eventListener = object: MusicPlayListener {
        override fun onProgressUpdate(millisecond: Int) {
            mBinding.seekBar.progress = millisecond
            mBinding.tvMusicTime.text = DateUtils.formatDurationTime(millisecond.toLong())
        }

        override fun onPlayMusicUpdate(model: MusicSongsInfoModel) {
            mBinding.tvMusicName.text = model.getSongsName()
            mBinding.tvSingerName.text = model.getSingersName()
            mBinding.tvMusicEndTime.text = DateUtils.formatDurationTime(model.duration.toLong())
            mBinding.ivMusicPlay.isSelected = true
            mBinding.seekBar.max = model.duration
            isPlay = true
        }

    }

    /**
     * 音乐是否在播放中
     */
    var isPlay = false

    override fun getLayoutId(): Int = R.layout.room_dialog_music

    override fun initView() {
        initTab()
        changeModeImage()
        loadPlayMusicInfo()

        RoomMusicManager.instance.setMediaPalyerEventHandler(eventListener)
        mBinding.seekbarVolume.progress = RoomMusicManager.instance.getVolumeNumber()
        mBinding.ivMusicPlay.onClick {
            if (RoomMusicManager.instance.isMusicPlay){
                RoomMusicManager.instance.resumePlayMusic(!isPlay)
                isPlay = !isPlay
                mBinding.ivMusicPlay.isSelected = isPlay
            }else{
                toast("请先选择歌曲列表播放")
            }
        }
        mBinding.ivMusicPrevious.safeClick {
            RoomMusicManager.instance.previousAudio()
        }
        mBinding.ivMusicNext.safeClick {
            RoomMusicManager.instance.nextAudio()
        }
        mBinding.ivStartStyle.onClick {
            RoomMusicManager.instance.changePlayMode()
            changeModeImage()
        }
        mBinding.tvEdit.onClick {
            val isEdit = mBinding.tvEdit.text.equals("编辑")
            if (isEdit){
                mBinding.tvEdit.text = "完成"
                mBinding.tvEdit.setTextColor(resources.getColor(com.kissspace.module_common.R.color.color_FFFD62))
            }else{
                mBinding.tvEdit.text = "编辑"
                mBinding.tvEdit.setTextColor(resources.getColor(com.kissspace.module_common.R.color.common_white))
            }
            FlowBus.post(Event.ChangeEditCollectList(isEdit))
        }
        mBinding.ivMusicVolume.onClick {
            mBinding.rlSeekBar.visibility = if (mBinding.rlSeekBar.isVisible) View.GONE else View.VISIBLE
        }
        mBinding.seekbarVolume.setOnSeekBarChangeListener(object :OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar) {
            }

            override fun onStopTrackingTouch(seekbar: SeekBar) {
                RoomMusicManager.instance.setVolumeNumber(seekbar.progress)
            }

        })
        mBinding.seekBar.isEnabled = false
    }

    private fun loadPlayMusicInfo() {
        if (RoomMusicManager.instance.isMusicPlay){
            RoomMusicManager.instance.musicInfo?.let {
                mBinding.tvMusicName.text = it.getSongsName()
                mBinding.tvSingerName.text = it.getSingersName()
                mBinding.tvMusicEndTime.text = DateUtils.formatDurationTime(it.duration.toLong())
                mBinding.ivMusicPlay.isSelected = RoomMusicManager.instance.getMediaPlayStatus()
                mBinding.seekBar.max = it.duration
            }
        }
    }

    private fun changeModeImage() {
        var modeResourceId = R.mipmap.room_bg_music_play_style_circulate
        when (RoomMusicManager.instance.playMode) {
            Constants.MusicPlayMode.ORDER_PLAY_MODE -> {
                modeResourceId = R.mipmap.room_bg_music_play_style_circulate
            }
            Constants.MusicPlayMode.SINGLE_PLAY_MODE -> {
                modeResourceId = R.mipmap.room_bg_music_play_style_single
            }
            Constants.MusicPlayMode.RANDOM_PLAY_MODE -> {
                modeResourceId = R.mipmap.room_bg_music_play_style_random
            }
        }
        mBinding.ivStartStyle.setImageResource(modeResourceId)
    }

    private fun initTab() {
        val param = DslTabLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT
        )
        param.setMargins(8.dp.toInt(), 0, 8.dp.toInt(), 0)
        val textView = TextView(requireContext())
        textView.text = "精选"
        textView.gravity = Gravity.CENTER
        textView.layoutParams = param
        mBinding.tabLayout.addView(textView)
        val textView2 = TextView(requireContext())
        textView2.text = "收藏"
        textView2.gravity = Gravity.CENTER
        textView2.layoutParams = param
        mBinding.tabLayout.addView(textView2)

        mBinding.viewPager.apply {
            adapter = object : FragmentStateAdapter(this@RoomMusicDialog) {
                override fun getItemCount() = 2
                override fun createFragment(position: Int): Fragment =
                    if (position == 0) {
                        MusicListFragment.newInstance()
                    } else {
                        MusicCollectListFragment.newInstance()
                    }
            }
        }
        mBinding.tabLayout.setUpViewPager2(mBinding.viewPager) {
            if (it == 1){
                mBinding.tvEdit.text = "编辑"
                mBinding.tvEdit.setTextColor(resources.getColor(com.kissspace.module_common.R.color.common_white))
                mBinding.tvEdit.visibility = View.VISIBLE
            }else{
                mBinding.tvEdit.visibility = View.GONE
            }
        }

    }

    override fun show(fm: FragmentManager) {
        show(fm, "RoomMusicDialog")
    }
}