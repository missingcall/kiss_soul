package com.kissspace.room.viewmodel

import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.model.RoomMusicCollectListBean
import com.kissspace.common.model.RoomOnLineUserListBean
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import com.kissspace.room.http.RoomApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MusicViewModel : BaseViewModel() {

    private val _collectEvent = MutableSharedFlow<ResultState<Int>>()
    val collectEvent = _collectEvent.asSharedFlow()

    private val _collectListEvent = MutableSharedFlow<ResultState<RoomMusicCollectListBean>>()
    val collectListEvent = _collectListEvent.asSharedFlow()

    fun collectMusic(songId:String,songName: String ="",singerName:String="",duration:Int = 0,isCollect:Boolean){
        val param = mutableMapOf<String, Any?>()
        param["songId"] = songId
        param["songName"] = songName
        param["singerName"] = singerName
        param["duration"] = duration
        param["type"] = if (isCollect) "001" else "002"
        request(RoomApi.API_COLLECT_MUSIC, Method.POST, param, state = _collectEvent)
    }

    fun  queryCollectMusicList(page:Int = 1){
        val param = mutableMapOf<String, Any?>()
        param["userId"] = MMKVProvider.userId
        param["pageNum"] = page
        param["pageSize"] = 20
        request(RoomApi.API_COLLECT_MUSIC_LIST, Method.GET, param, state = _collectListEvent)
    }
}