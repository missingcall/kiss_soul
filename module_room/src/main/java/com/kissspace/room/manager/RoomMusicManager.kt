package com.kissspace.room.manager

import com.blankj.utilcode.util.GsonUtils
import com.kissspace.common.config.Constants
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.model.MusicListModel
import com.kissspace.common.model.MusicResourceModel
import com.kissspace.common.model.MusicSongsInfoModel
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.customToast
import com.kissspace.common.util.randomRange
import com.kissspace.room.interfaces.MusicPlayListener
import im.zego.zegoexpress.ZegoCopyrightedMusic
import im.zego.zegoexpress.ZegoExpressEngine
import im.zego.zegoexpress.ZegoMediaPlayer
import im.zego.zegoexpress.callback.IZegoMediaPlayerEventHandler
import im.zego.zegoexpress.constants.ZegoCopyrightedMusicBillingMode
import im.zego.zegoexpress.constants.ZegoCopyrightedMusicResourceType
import im.zego.zegoexpress.constants.ZegoCopyrightedMusicVendorID
import im.zego.zegoexpress.constants.ZegoMediaPlayerState
import im.zego.zegoexpress.entity.ZegoCopyrightedMusicConfig
import im.zego.zegoexpress.entity.ZegoCopyrightedMusicRequestConfig
import im.zego.zegoexpress.entity.ZegoUser
import org.json.JSONObject

/**
 *@author: adan
 *@date: 2023/3/22
 *@Description:房间音乐管理类
 */
class RoomMusicManager private constructor(){

    companion object {
        val instance: RoomMusicManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RoomMusicManager()
        }
    }

    private var mCopyrightedMusic : ZegoCopyrightedMusic? = null
    private var mMediaPlayer : ZegoMediaPlayer? = null

    /**
     * 当前播放歌曲信息
     */
    var musicInfo: MusicSongsInfoModel? = null
    /**
     * 是否在播放歌曲中
     */
    var isMusicPlay = false

    /**
     * 当前播放的模式
     * 0 列表循环 1 单曲循环 2 随机播放
     */
    var playMode = Constants.MusicPlayMode.ORDER_PLAY_MODE

    /**
     * 当前播放列表
     */
    private var currentMusicList = mutableListOf<MusicSongsInfoModel>()

    /**
     * 收藏
     */
    private var collectMusicList = mutableListOf<MusicSongsInfoModel>()

    /**
     * 默认播放列表,精选
     */
    private var concentrationMusicList = mutableListOf<MusicSongsInfoModel>()

    /**
     * 当前播放音频对象在对应播放列表的角标
     */
    private var currentIndex = 0

    /**
     * 当前播放列表类型
     */
    var playListType = Constants.MusicPlayMode.CONCENTRATION_PLAY_LIST

    private var musicCallback: MusicPlayListener? = null

    /**
     * 播放器播放进度回调
     */
    private val mediaPlayerEventHandler = object : IZegoMediaPlayerEventHandler() {
        override fun onMediaPlayerPlayingProgress(
            mediaPlayer: ZegoMediaPlayer?, millisecond: Long
        ) {
            super.onMediaPlayerPlayingProgress(mediaPlayer, millisecond)
            musicCallback?.onProgressUpdate(millisecond.toInt())
        }

        override fun onMediaPlayerStateUpdate(
            mediaPlayer: ZegoMediaPlayer?, state: ZegoMediaPlayerState?, errorCode: Int
        ) {
            super.onMediaPlayerStateUpdate(mediaPlayer, state, errorCode)
            if (state == ZegoMediaPlayerState.PLAY_ENDED) {
                //播放结束
                currentMusicList.clear()
                if (playListType == Constants.MusicPlayMode.COLLECT_PLAY_LIST) {
                    currentMusicList.addAll(collectMusicList)
                } else {
                    currentMusicList.addAll(concentrationMusicList)
                }
                currentIndex = currentMusicList.indexOfFirst {
                    musicInfo?.getSongsId() == it.getSongsId()
                }
                nextAudio()
            }
        }
    }


    fun init(engine: ZegoExpressEngine?){
        mCopyrightedMusic = engine?.createCopyrightedMusic()
        val musicConfig = ZegoCopyrightedMusicConfig().apply {
            user = ZegoUser(MMKVProvider.userId)
        }
        mCopyrightedMusic?.initCopyrightedMusic(musicConfig) {}
        mMediaPlayer = engine?.createMediaPlayer()
        mMediaPlayer?.setEventHandler(mediaPlayerEventHandler)
    }

    /**
     * 查询音乐歌曲列表
     */
    fun requestMusicList(
        isSearch: Boolean = false,
        keyword: String = "",
        page: Int,
        block: (List<MusicSongsInfoModel>) -> Unit
    ) {
        val json = JSONObject()
        json.put("page", page)
        if (isSearch) {
            json.put("keyword", keyword)
        } else {
            json.put("tag_id", "571")
        }
        json.put("vendor_id", 2)
        mCopyrightedMusic?.sendExtendedRequest(
            if (isSearch) "/search/song" else "/tag/song", json.toString()
        ) { code, _, result ->
            if (code == 0) {
                val mMusicListModel = GsonUtils.fromJson(result, MusicListModel::class.java)
                var musicList = mMusicListModel.data.songs
                if (musicList.isNotEmpty() && musicList.size > 20) {
                    musicList = musicList.subList(0, 20)
                }
                concentrationMusicList.clear()
                concentrationMusicList.addAll(musicList)
//                if (isPlayConcentrationList()){
//                    currentMusicList.clear()
//                    currentMusicList.addAll(concentrationMusicList)
//                }
                block(musicList)
            }else{
                customToast("获取列表失败,请稍后重试")
            }
        }
    }

    /**
     * 获取歌曲资源
     */
    private fun requestMusicResource(musicModel: MusicSongsInfoModel) {
        musicInfo = musicModel
        musicCallback?.onPlayMusicUpdate(musicModel)
        val config = ZegoCopyrightedMusicRequestConfig()
        config.songID = musicModel.getSongsId()
        config.mode = ZegoCopyrightedMusicBillingMode.getZegoCopyrightedMusicBillingMode(0)
        config.vendorID = ZegoCopyrightedMusicVendorID.ZEGO_COPYRIGHTED_MUSIC_VENDOR2
        val resourceType = ZegoCopyrightedMusicResourceType.ZEGO_COPYRIGHTED_MUSIC_RESOURCE_SONG
        mCopyrightedMusic?.requestResource(config, resourceType) { i, s ->
            if (i == 0) {
                val mMusicResourceModel = GsonUtils.fromJson(s, MusicResourceModel::class.java)
                downLoadMusic(mMusicResourceModel.data.resources[0].resource_id)
            } else {
                customToast("播放失败,请重试")
            }
        }
    }

    /**
     * 下载资源
     */
    private fun downLoadMusic(resourceID: String) {
        mCopyrightedMusic?.download(resourceID) {
            if (it == 0) {
                //歌曲下载成功
                startPlayMusic(resourceID)
            } else {
                customToast("播放失败,请重试")
            }
        }
    }

    private fun startPlayMusic(resourceID: String) {
        if (isMusicPlay) {
            mMediaPlayer?.stop()
        }
        mMediaPlayer?.loadCopyrightedMusicResourceWithPosition(resourceID, 0) {
            isMusicPlay = it == 0
        }
        mMediaPlayer?.start()
        mMediaPlayer?.enableAux(true)
    }

    /**
     * 播放暂停
     */
    fun resumePlayMusic(isResume: Boolean) {
        if (isResume) {
            mMediaPlayer?.resume()
        } else {
            mMediaPlayer?.pause()
        }
    }

    /**
     * 更新当前播放音乐index
     */
    fun updateCurrentIndex(index: Int) {
        currentIndex = index
    }

    /**
     * 设置音量
     */
    fun setVolumeNumber(volume: Int) {
        mMediaPlayer?.setVolume(volume)
    }

    /**
     * 获取当前推流音量
     */
    fun getVolumeNumber(): Int {
        return if (mMediaPlayer != null) {
            mMediaPlayer?.publishVolume!!
        } else {
            0
        }
    }

    /**
     * 当前播放的是否为精选列表
     */
    fun isPlayConcentrationList():Boolean{
        return isMusicPlay && playListType == Constants.MusicPlayMode.CONCENTRATION_PLAY_LIST
    }

    /**
     * 当前播放的是否为收藏列表
     */
    fun isPlayCollectList():Boolean{
        return isMusicPlay && playListType == Constants.MusicPlayMode.COLLECT_PLAY_LIST
    }

    /**
     * 是否是当前播放的歌曲
     */
    fun isPlayMusicInfo(songId:String):Boolean{
        return songId == musicInfo?.getSongsId()
    }

    /**
     * 设置歌曲进度 信息回调
     */
    fun setMediaPalyerEventHandler(mediaPlayerEventHandler: MusicPlayListener) {
        musicCallback = mediaPlayerEventHandler
    }

    fun setCollectList(model: List<MusicSongsInfoModel>) {
        collectMusicList.clear()
        collectMusicList.addAll(model)
        if (isPlayCollectList()){
            currentMusicList.clear()
            currentMusicList.addAll(collectMusicList)
        }
    }

    /**
     * 切换播放模式
     */
    fun changePlayMode() {
        when (playMode) {
            Constants.MusicPlayMode.ORDER_PLAY_MODE -> {
                playMode = Constants.MusicPlayMode.SINGLE_PLAY_MODE
            }

            Constants.MusicPlayMode.SINGLE_PLAY_MODE -> {
                playMode = Constants.MusicPlayMode.RANDOM_PLAY_MODE
            }

            Constants.MusicPlayMode.RANDOM_PLAY_MODE -> {
                playMode = Constants.MusicPlayMode.ORDER_PLAY_MODE
            }
        }
    }

    /**
     * 切换播放列表,并播放歌曲
     */
    fun switchPlayList(type: Int, index: Int) {
        currentMusicList.clear()
        if (type == Constants.MusicPlayMode.CONCENTRATION_PLAY_LIST) {
            currentMusicList.addAll(concentrationMusicList)
        } else {
            currentMusicList.addAll(collectMusicList)
        }
        currentIndex = index
        val musicSongsInfoModel = currentMusicList[index]
        //点击播放同一首歌不处理
        if (type == playMode) {
            if (isMusicPlay && musicSongsInfoModel.getSongsName() == musicInfo?.getSongsName()) {
                return
            }
        } else {
            playListType = type
            FlowBus.post(Event.MusicListChangeEvent)
        }
        requestMusicResource(musicSongsInfoModel)
    }

    fun nextAudio() {
        if (!currentMusicList.isNullOrEmpty()) {
            when (playMode) {
                //顺序
                Constants.MusicPlayMode.ORDER_PLAY_MODE -> {
                    currentIndex = if (currentIndex < currentMusicList.size - 1) {
                        currentIndex + 1
                    } else {
                        0
                    }
                }
                //单曲(不做处理)
                Constants.MusicPlayMode.SINGLE_PLAY_MODE -> {
                }
                //随机
                Constants.MusicPlayMode.RANDOM_PLAY_MODE -> {
                    currentIndex = randomRange(0, currentMusicList.size - 1)
                }
            }
            if (currentIndex < 0){
                currentIndex = 0
            }
            val musicSongsInfoModel = currentMusicList[currentIndex]
            requestMusicResource(musicSongsInfoModel)
            resetMusicListStatus()
        }
    }

    private fun resetMusicListStatus() {
        if (playListType == Constants.MusicPlayMode.CONCENTRATION_PLAY_LIST) {
            FlowBus.post(
                Event.ChangePlayMusicList(
                    currentIndex
                ))
        } else {
            FlowBus.post(
                Event.ChangePlayMusicCollect(
                    currentIndex
                ))
        }
    }

    fun previousAudio() {
        if (!currentMusicList.isNullOrEmpty()) {
            when (playMode) {
                //顺序
                Constants.MusicPlayMode.ORDER_PLAY_MODE -> {
                    currentIndex = if (currentIndex > 0) {
                        currentIndex - 1
                    } else {
                        currentMusicList.size - 1
                    }
                }
                //单曲(不做处理)
                Constants.MusicPlayMode.SINGLE_PLAY_MODE -> {
                }
                //随机
                Constants.MusicPlayMode.RANDOM_PLAY_MODE -> {
                    currentIndex = randomRange(0, currentMusicList.size - 1)
                }
            }
            if (currentIndex < 0){
                currentIndex = 0
            }
            val musicSongsInfoModel = currentMusicList[currentIndex]
            requestMusicResource(musicSongsInfoModel)
            resetMusicListStatus()
        }
    }

    fun getMediaPlayStatus(): Boolean {
        return mMediaPlayer?.currentState == ZegoMediaPlayerState.PLAYING
    }


    fun release(){
        mCopyrightedMusic = null
        mMediaPlayer = null
        currentMusicList.clear()
        collectMusicList.clear()
        concentrationMusicList.clear()
        musicInfo = null
        musicCallback = null
    }
}