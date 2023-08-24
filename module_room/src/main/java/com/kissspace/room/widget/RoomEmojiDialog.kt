package com.kissspace.room.widget

import androidx.fragment.app.FragmentManager
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.kissspace.common.config.CommonApi
import com.kissspace.common.model.ChatEmojiListBean
import com.kissspace.common.util.EmojiGameUtil
import com.kissspace.common.widget.BaseBottomSheetDialogFragment
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogEmojiBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/31 11:41
 * @Description: 房间表情弹窗
 *
 */
class RoomEmojiDialog : BaseBottomSheetDialogFragment<RoomDialogEmojiBinding>() {
    private var callBack: (ChatEmojiListBean) -> Unit = {}

    override fun getViewBinding() = RoomDialogEmojiBinding.inflate(layoutInflater)

    fun setCallBack(block: (ChatEmojiListBean) -> Unit) {
        this.callBack = block
    }

    override fun initView() {
        request<List<ChatEmojiListBean>>(CommonApi.API_QUERY_EMOJI_LIST, Method.GET, onSuccess = {
            mBinding.recyclerView.grid(5).setup {
                addType<ChatEmojiListBean> { R.layout.room_layout_emoji_item }
                onFastClick(R.id.root) {
                    val model = getModel<ChatEmojiListBean>()
                    if (EmojiGameUtil.isEmojiGame(model.name)){
                        val emojiIndex = EmojiGameUtil.getEmojiIndex(model.name)
                        model.dynamicImage = EmojiGameUtil.getEmojiPath(model.name,emojiIndex)
                        model.emojiGameImage = EmojiGameUtil.getEmojiImagePath(model.name,emojiIndex)
                        model.emojiGameIndex = emojiIndex
                        model.isEmojiLoop = false
                    }
                    callBack(model)
                    dismiss()
                }
            }.models = it
        })
    }


}