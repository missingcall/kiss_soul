package com.kissspace.message.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.ext.safeClick
import com.kissspace.common.model.LoveWallListBean
import com.kissspace.common.model.LoveWallResponse
import com.kissspace.common.util.jumpRoom
import com.kissspace.message.http.MessageApi
import com.kissspace.module_message.R
import com.kissspace.module_message.databinding.MessageFragmentLoveWallBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.util.loadImage

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/5 20:02
 * @Description: 真爱墙frament
 *
 */
class LoveWallFragment : BaseFragment(R.layout.message_fragment_love_wall) {
    private val mBinding by viewBinding<MessageFragmentLoveWallBinding>()
    private lateinit var type: String

    companion object {
        fun newInstance(type: String) = LoveWallFragment().apply {
            arguments = bundleOf("type" to type)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getString("type")!!
    }

    override fun initView(savedInstanceState: Bundle?) {
        initRecyclerView()
        mBinding.pageRefreshLayout.apply {
            onRefresh {
                initData(true)
            }
            onLoadMore {
                initData(false)
            }
        }
        initData(true)
    }

    private fun initRecyclerView() {
        mBinding.recyclerView.linear().setup {
            addType<LoveWallListBean> { R.layout.message_layout_love_wall_item }
            onBind {
                val model = getModel<LoveWallListBean>()
                val root = findView<ConstraintLayout>(R.id.root)
                val resource = when (modelPosition) {
                    0 -> R.mipmap.message_bg_love_wall_blue
                    1 -> R.mipmap.message_bg_love_wall_second
                    2 -> R.mipmap.message_bg_love_wall_third
                    else -> when (modelPosition % 3) {
                        0 -> R.mipmap.message_bg_love_wall_second
                        1 -> R.mipmap.message_bg_love_wall_third
                        else -> R.mipmap.message_bg_love_wall_four
                    }
                }
                root.setBackgroundResource(resource)
                val bottom = findView<ConstraintLayout>(R.id.layout_bottom)
                bottom.visibility = if (model.chatRoomId.isNotEmpty()) View.VISIBLE else View.GONE
                bottom.safeClick {
                    val model = getModel<LoveWallListBean>()
                    if (model.chatRoomId.isNotEmpty()) {
                        jumpRoom(model.chatRoomId)
                    }
                }
            }
            onFastClick(R.id.iv_gift) {
                val model = getModel<LoveWallListBean>()
                showGiftDialog(model)

            }
        }.models = mutableListOf()
    }

    private fun initData(isRefresh: Boolean) {
        val param = mutableMapOf<String, Any?>()
        param["recodeType"] = type
        param["pageNum"] = mBinding.pageRefreshLayout.index
        param["pageSize"] = 10
        request<LoveWallResponse>(MessageApi.API_LOVE_WALL_LIST, Method.GET, param, onSuccess = {
            if (isRefresh) {
                mBinding.recyclerView.bindingAdapter.mutable.clear()
            }
            mBinding.pageRefreshLayout.addData(it.giveGiftRecordList, hasMore = {
                it.giveGiftRecordList.size == 10
            }, isEmpty = {
                it.giveGiftRecordList.isEmpty()
            })
        })
    }

    private fun showGiftDialog(model: LoveWallListBean) {
        CustomDialog.build().apply {
            maskColor = Color.parseColor("#4D000000")
            setCustomView(object :
                OnBindView<CustomDialog>(R.layout.message_dialog_love_wall_gift) {
                override fun onBind(dialog: CustomDialog?, v: View?) {
                    v?.findViewById<ImageView>(R.id.iv_close)
                        ?.setOnClickListener {
                            dialog?.dismiss()
                        }
                    v?.findViewById<TextView>(R.id.tv_title)?.text = model.giftName
                    v?.findViewById<ImageView>(R.id.iv_gift)?.loadImage(model.url)
                    v?.findViewById<TextView>(R.id.tv_price)?.text =
                        StringUtils.getString(R.string.message_love_wall_price, model.price)
                }
            })
        }.show()
    }
}