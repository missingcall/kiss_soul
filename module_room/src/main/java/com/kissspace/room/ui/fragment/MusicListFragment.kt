package com.kissspace.room.ui.fragment

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.Constants.MusicPlayMode.Companion.CONCENTRATION_PLAY_LIST
import com.kissspace.common.ext.safeClick
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.model.MusicSongsInfoModel
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomFragmentMusicListBinding
import com.kissspace.network.result.collectData
import com.kissspace.room.manager.RoomMusicManager
import com.kissspace.room.viewmodel.MusicViewModel
import com.kissspace.util.toast

/**
 * @CreateDate: 2023-03-16 10:56:49
 * @Description: 音乐列表
 */
class MusicListFragment : BaseFragment(R.layout.room_fragment_music_list) {

    private val mBinding by viewBinding<RoomFragmentMusicListBinding>()
    private val mViewModel by viewModels<MusicViewModel>()

    private var collectModel: MusicSongsInfoModel? = null
    private var currentModel:MusicSongsInfoModel? =null

    companion object {
        fun newInstance() =
            MusicListFragment()
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.tvSearch.safeClick {
            searchMusic()
        }
        mBinding.etSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchMusic()
            }
            true
        }
        mBinding.recyclerView.linear().setup {
            addType<MusicSongsInfoModel> {
                R.layout.room_dialog_music_list_recycler_item
            }
            onClick(R.id.root){
                refreshModel(false)
                currentModel = getModel<MusicSongsInfoModel>()
                refreshModel(true)
                startPlayMusicSongs(modelPosition)
            }
            onClick(R.id.iv_collect){
                val model = getModel<MusicSongsInfoModel>()
                collectModel = model
                mViewModel.collectMusic(model.song_id,model.song_name,model.singer_name,model.duration,true)
            }
        }.models = mutableListOf()

        RoomMusicManager.instance.requestMusicList(page = 1){ result->
            mBinding.recyclerView.models = result
        }
    }

    private fun searchMusic(){
        var searchMessage = mBinding.etSearch.text.toString().trim()
        RoomMusicManager.instance.requestMusicList(searchMessage.isNotEmpty(),searchMessage,page = 1){result->
            if (RoomMusicManager.instance.isPlayConcentrationList()){
                result.forEachIndexed { index, it ->
                    if (RoomMusicManager.instance.isPlayMusicInfo(it.getSongsId())){
                        it.checked = true
                        currentModel = it
                        RoomMusicManager.instance.updateCurrentIndex(index)
                    }
                }
            }
            mBinding.recyclerView.models = result
            mBinding.recyclerView.scrollToPosition(0)
        }
    }

    private fun startPlayMusicSongs(position:Int) {
        RoomMusicManager.instance.switchPlayList(CONCENTRATION_PLAY_LIST,position)
    }

    override fun createDataObserver() {
        super.createDataObserver()

        collectData(mViewModel.collectEvent, onSuccess = {
            toast("收藏成功")
            collectModel?.isCollect = false
            collectModel?.notifyChange()
        }, onError = {
            toast("收藏失败")
        })

        FlowBus.observerEvent<Event.ChangePlayMusicList>(this) {
            refreshModel(false)
            val models = mBinding.recyclerView.models
            if (!models.isNullOrEmpty() && it.currentIndex < models.size){
                currentModel = models[it.currentIndex] as MusicSongsInfoModel
                refreshModel(true)
            }
        }

        FlowBus.observerEvent<Event.MusicListChangeEvent>(this){
            if (RoomMusicManager.instance.playListType != CONCENTRATION_PLAY_LIST){
                refreshModel(false)
                currentModel = null
            }
        }
    }

    /**
     * 刷新状态
     */
    private fun refreshModel(check:Boolean){
        if (currentModel != null){
            currentModel?.checked = check
            currentModel?.notifyChange()
        }
    }
}