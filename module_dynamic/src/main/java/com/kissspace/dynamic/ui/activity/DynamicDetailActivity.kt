package com.kissspace.dynamic.ui.activity

import android.os.Bundle
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.angcyo.tablayout.delegate2.ViewPager2Delegate
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.addModels
import com.drake.brv.utils.grid
import com.drake.brv.utils.linear
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.drake.softinput.hideSoftInput
import com.drake.softinput.setWindowSoftInput
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.ext.safeClick
import com.kissspace.common.model.dynamic.DynamicDetailCommentInfo
import com.kissspace.common.model.dynamic.DynamicDetailLikeInfo
import com.kissspace.common.model.dynamic.DynamicInfoRecord
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.parseIntent
import com.kissspace.common.util.previewPicture
import com.kissspace.dynamic.http.DynamicApi
import com.kissspace.dynamic.ui.fragment.DynamicCommentFragment
import com.kissspace.module_dynamic.R
import com.kissspace.module_dynamic.databinding.DynamicActivityDetailBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.util.logE
import com.kissspace.util.toast

@Router(path = RouterPath.PATH_DYNAMIC_DETAIL)
class DynamicDetailActivity:BaseActivity(R.layout.dynamic_activity_detail) {
    private val mBinding by viewBinding<DynamicActivityDetailBinding>()
    private val dynamicDetail by parseIntent<DynamicInfoRecord>()
    override fun initView(savedInstanceState: Bundle?) {
        mBinding.m = dynamicDetail
        mBinding.titleBar.setOnTitleBarListener(object :OnTitleBarListener{
            override fun onLeftClick(titleBar: TitleBar?) {
                super.onLeftClick(titleBar)
                finish()
            }

            override fun onRightClick(titleBar: TitleBar?) {
                super.onRightClick(titleBar)
            }
        })
        mBinding.recyclerPicture.grid(3).setup {
            addType<String>(R.layout.dynamic_picture_item)
            onClick(R.id.root){
                previewPicture(this@DynamicDetailActivity,modelPosition,findView<ImageView>(R.id.root),dynamicDetail.pictureDynamicContent.toMutableList())
            }
        }.models = dynamicDetail.pictureDynamicContent

        mBinding.tabLayout.configTabLayoutConfig{
            onSelectItemView = { _, index, selected, _ ->
                if(selected){
                    if (index==1){
                        mBinding.tvAmount.text = "${dynamicDetail.commentAmount()}条评论"
                        requestLike()
                    }else{
                        mBinding.tvAmount.text = "${dynamicDetail.numberOfLikes}点赞"
                        requestComment()
                    }
                }
                false
            }
        }
        mBinding.recyclerView.linear().setup {
            addType<DynamicDetailCommentInfo>(R.layout.dynamic_detail_comment_item)
            addType<DynamicDetailLikeInfo>(R.layout.dynamic_detail_like_item)
        }.mutable = mutableListOf()
        requestComment()

        setWindowSoftInput(float = mBinding.layoutComment, transition =mBinding.layoutComment)

        mBinding.tvSend.safeClick {
            val text = mBinding.editComment.text.toString().trim()
            if (text.isEmpty()){
                toast("请输入评论内容")
                return@safeClick
            }
            val param = mutableMapOf<String,Any?>("commentContent" to text,"dynamicId" to dynamicDetail.dynamicId)
            request<Boolean>(DynamicApi.API_ADD_COMMENT, method = Method.POST,param, onSuccess = {
                hideSoftInput()
                mBinding.editComment.setText("")
                if (mBinding.tabLayout.currentItemIndex == 0){
                    requestComment()
                }
            })


        }
    }

    private fun requestComment(){
        val params = mutableMapOf<String,Any?>("dynamicId" to dynamicDetail.dynamicId)
        request<List<DynamicDetailCommentInfo>>(DynamicApi.API_GET_COMMENT_DETAIL, method = Method.GET, param = params, onSuccess = {
            mBinding.recyclerView.mutable.clear()
            mBinding.pageRefreshLayout.addData(it, isEmpty = {it.isEmpty()})
        })
    }

    private fun requestLike(){
        val params = mutableMapOf<String,Any?>("dynamicId" to dynamicDetail.dynamicId)
        request<List<DynamicDetailLikeInfo>>(DynamicApi.API_GET_DYNAMIC_LILES, method = Method.GET, param = params, onSuccess = {
            mBinding.recyclerView.mutable.clear()
            mBinding.pageRefreshLayout.addData(it, isEmpty = {it.isEmpty()})
        })
    }
}

