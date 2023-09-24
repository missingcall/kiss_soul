package com.kissspace.dynamic.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.RecyclerView.VISIBLE
import by.kirich1409.viewbindingdelegate.viewBinding
import cc.shinichi.library.ImagePreview
import com.didi.drouter.annotation.Router
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.grid
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.net.utils.scopeNetLife
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.ext.setDrawable
import com.kissspace.common.model.dynamic.DynamicCommentModel
import com.kissspace.common.model.dynamic.DynamicInfo
import com.kissspace.common.model.dynamic.DynamicInfoRecord
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.util.previewPicture
import com.kissspace.dynamic.http.DynamicApi
import com.kissspace.dynamic.ui.activity.DynamicDetailActivity
import com.kissspace.dynamic.ui.viewmodel.DynamicViewModel
import com.kissspace.module_dynamic.R
import com.kissspace.module_dynamic.databinding.DynamicFragmentListBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.collectData
import com.kissspace.util.logE
import com.qmuiteam.qmui.kotlin.onClick

class DynamicListFragment(private val position:Int = 0):BaseFragment(R.layout.dynamic_fragment_list) {
    private val mBinding by viewBinding<DynamicFragmentListBinding>()
    private val mViewModel by viewModels<DynamicViewModel>()
    override fun initView(savedInstanceState: Bundle?) {
        mBinding.pageRefreshLayout.apply {
            onRefresh {
                mViewModel.queryDynamicList(mBinding.pageRefreshLayout.index,position)
            }
            onLoadMore {
                mViewModel.queryDynamicList(mBinding.pageRefreshLayout.index,position)
            }
        }.autoRefresh()

        mBinding.recyclerView.linear().setup {
            addType<DynamicInfoRecord>(R.layout.dynamic_item)
            onBind {
                val model = getModel<DynamicInfoRecord>()
                val recyclerPicture = findView<RecyclerView>(R.id.recycler_picture)
                recyclerPicture.grid(3).setup {
                    addType<String>(R.layout.dynamic_picture_item)
                    onClick(R.id.root){
                        previewPicture(requireContext(),modelPosition,findView<ImageView>(R.id.root),model.pictureDynamicContent.toMutableList())
                    }
                }.models = model.pictureDynamicContent

                val recyclerComment = findView<RecyclerView>(R.id.recycler_comment)
                recyclerComment.linear().setup {
                    addType<DynamicCommentModel>(R.layout.dynamic_comment_item)
                }.models = if (model.dynamicCommentsList.size>2) model.dynamicCommentsList.subList(0,2) else model.dynamicCommentsList

                findView<ViewGroup>(R.id.layout_comment).visibility = if (model.dynamicCommentsList.isEmpty()) GONE else VISIBLE
                findView<TextView>(R.id.tv_check_more).visibility = if (model.dynamicCommentsList.size>2) VISIBLE else GONE

                val like = findView<TextView>(R.id.tv_like)
                like.text = model.numberOfLikes.toString()

            }
            onClick(R.id.tv_check_more){
            }
            onClick(R.id.tv_like){
                val model = getModel<DynamicInfoRecord>()
                doLike(model)
            }
            onClick(R.id.iv_follow){
                val model = getModel<DynamicInfoRecord>()
                doFollow(model.dynamicId)
            }
            onClick(R.id.root){
                val model = getModel<DynamicInfoRecord>()
                val intent = Intent(requireContext(),DynamicDetailActivity::class.java)
                intent.putExtra("dynamicDetail",model as Parcelable)
                startActivity(intent)
            }
        }.mutable = mutableListOf()

        mBinding.ivGoTop.onClick {
            mBinding.recyclerView.smoothScrollToPosition(0)
        }

        mBinding.ivAdd.onClick {
            jump(RouterPath.PATH_DYNAMIC_SEND_FRIEND)
        }
    }

    private fun doLike(model:DynamicInfoRecord){
        val param = mutableMapOf<String,Any?>()
        param["dynamicId"] = model.dynamicId
        request<Boolean>(url = DynamicApi.API_LIKE_DYNAMICS, method = Method.POST,param=param, onSuccess = {
            model.likeStatus = !model.likeStatus
            model.numberOfLikes = if (model.likeStatus) model.numberOfLikes+1 else model.numberOfLikes -1
            model.notifyChange()
        })
    }

    private fun doFollow(id:String){

    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.dynamicList, onSuccess = {
            mBinding.pageRefreshLayout.addData(it.dynamicInfoRecords, hasMore = {
                mBinding.pageRefreshLayout.index * 20 < it.total
            }, isEmpty = {
                it.dynamicInfoRecords.isEmpty()
            })
        }, onError = {
            mBinding.pageRefreshLayout.finishRefresh()
            mBinding.pageRefreshLayout.finishLoadMore()
        })
    }


}