package com.kissspace.room.ui.fragment

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.Constants.MusicPlayMode.Companion.COLLECT_PLAY_LIST
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.model.MusicSongsInfoModel
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomFragmentMusicCollectListBinding
import com.kissspace.network.result.collectData
import com.kissspace.room.manager.RoomMusicManager
import com.kissspace.room.manager.RoomServiceManager
import com.kissspace.room.viewmodel.MusicViewModel
import com.kissspace.util.toastLong


/**
 * @CreateDate: 2023-03-16 10:56:49
 * @Description: 音乐收藏列表
 */
class MusicCollectListFragment : BaseFragment(R.layout.room_fragment_music_collect_list) {

    private val mBinding by viewBinding<RoomFragmentMusicCollectListBinding>()
    private val mViewModel by viewModels<MusicViewModel>()

    var removePosition = 0
    var removeModel :MusicSongsInfoModel?  = null

    private var currentModel:MusicSongsInfoModel? =null

    companion object {
        fun newInstance() =
            MusicCollectListFragment()
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.refreshLayout.apply {
            onRefresh {
                initData()
            }
            onLoadMore {
                initData()
            }
        }
        mBinding.recyclerView.linear().setup {
            addType<MusicSongsInfoModel> {
                R.layout.room_dialog_music_collect_recycler_item
            }
            onClick(R.id.root){
                refreshModel(false)
                currentModel = getModel<MusicSongsInfoModel>()
                refreshModel(true)
                startPlayMusicSongs(modelPosition)
            }
            onClick(R.id.iv_collect){
                removePosition = modelPosition
                removeModel = getModel<MusicSongsInfoModel>()
                mViewModel.collectMusic(removeModel!!.getSongsId(),removeModel!!.songName,removeModel!!.getSingersName(), isCollect = false)
            }
            onToggle { position, _, _ ->
                val model = getModel<MusicSongsInfoModel>(position)
                model.visibility = toggleMode
                model.notifyChange()
            }
        }.models = mutableListOf()
    }

    override fun onResume() {
        super.onResume()
       mBinding.refreshLayout.refresh()
    }


    fun initData(){
        mViewModel.queryCollectMusicList(mBinding.refreshLayout.index)
    }

    private fun startPlayMusicSongs(position:Int) {
        RoomMusicManager.instance.switchPlayList(COLLECT_PLAY_LIST,position)
    }



    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.collectListEvent, onSuccess = {
            if (RoomMusicManager.instance.isPlayCollectList()){
                it.records.forEachIndexed { index, it ->
                    if (RoomMusicManager.instance.isPlayMusicInfo(it.getSongsId())){
                        it.checked = true
                        currentModel = it
                        RoomMusicManager.instance.updateCurrentIndex(index)
                    }
                }
            }
            mBinding.refreshLayout.addData(it.records, hasMore = {
                mBinding.refreshLayout.index * 20 < it.total
            }, isEmpty = {
                it.records.isEmpty()
            })
            RoomMusicManager.instance.setCollectList(mBinding.recyclerView.bindingAdapter.models as List<MusicSongsInfoModel>)
        }, onError = {

        })

        collectData(mViewModel.collectEvent, onSuccess = {
            mBinding.recyclerView.bindingAdapter.mutable.remove(removeModel)
            mBinding.recyclerView.bindingAdapter.notifyItemRemoved(removePosition)
            if (mBinding.recyclerView.bindingAdapter.models?.size == 0){
                mBinding.refreshLayout.showEmpty()
            }
            RoomMusicManager.instance.setCollectList(mBinding.recyclerView.bindingAdapter.models as List<MusicSongsInfoModel>)
            toastLong("取消收藏成功")
        }, onError = {
            toastLong("取消收藏失败")
        })

        FlowBus.observerEvent<Event.ChangeEditCollectList>(this){
            mBinding.recyclerView.bindingAdapter.toggle()
        }
        FlowBus.observerEvent<Event.ChangePlayMusicCollect>(this){
            refreshModel(false)
            val models = mBinding.recyclerView.models
            if (!models.isNullOrEmpty() && it.currentIndex < models.size){
                currentModel = models[it.currentIndex] as MusicSongsInfoModel
                refreshModel(true)
            }
        }

        FlowBus.observerEvent<Event.MusicListChangeEvent>(this){
            if (RoomMusicManager.instance.playListType != COLLECT_PLAY_LIST){
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