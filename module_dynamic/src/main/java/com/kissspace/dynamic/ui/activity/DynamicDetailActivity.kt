package com.kissspace.dynamic.ui.activity

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.didi.drouter.utils.RouterLogger
import com.drake.brv.utils.addModels
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.grid
import com.drake.brv.utils.linear
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.drake.softinput.hideSoftInput
import com.drake.softinput.setWindowSoftInput
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.Constants
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setDrawable
import com.kissspace.common.http.checkUserPermission
import com.kissspace.common.model.dynamic.DynamicDetailCommentInfo
import com.kissspace.common.model.dynamic.DynamicDetailLikeInfo
import com.kissspace.common.model.dynamic.DynamicInfoRecord
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.router.parseIntent
import com.kissspace.common.util.customToast
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.previewPicture
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.dynamic.http.DynamicApi
import com.kissspace.dynamic.widget.DynamicMenuDialog
import com.kissspace.module_dynamic.R
import com.kissspace.module_dynamic.databinding.DynamicActivityDetailBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.util.resToColor
import com.kissspace.util.toast
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.interfaces.OnMenuItemClickListener
import com.tencent.mmkv.MMKV


@Router(path = RouterPath.PATH_DYNAMIC_DETAIL)
class DynamicDetailActivity : BaseActivity(R.layout.dynamic_activity_detail) {
    private val mBinding by viewBinding<DynamicActivityDetailBinding>()
    private val dynamicDetail by parseIntent<DynamicInfoRecord>()
    override fun initView(savedInstanceState: Bundle?) {
        mBinding.m = dynamicDetail
        mBinding.tvLike.text = dynamicDetail.numberOfLikes.toString()
        mBinding.tvComment.text = dynamicDetail.dynamicCommentsList.size.toString()
        mBinding.tvLike.setDrawable(
            if (dynamicDetail.likeStatus) R.mipmap.dynamic_like else R.mipmap.dynamic_unlike,
            Gravity.START
        )

        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                super.onLeftClick(titleBar)
                finish()
            }

            override fun onRightClick(titleBar: TitleBar?) {
                super.onRightClick(titleBar)

                checkUserPermission(Constants.UserPermission.PERMISSION_DELETE_DYNAMIC){has->
                    val items = if (dynamicDetail.userId == MMKVProvider.userId) {
                        arrayOf("删除", "取消")
                    } else {
                        if (has) arrayOf("举报", "删除","取消") else arrayOf("举报", "取消")
                    }
                    DynamicMenuDialog(items) { dialog, text ->
                        when (text) {
                            "取消" -> dialog.dismiss()
                            "删除" -> deleteDynamic()
                            "举报" -> {
                                jump(
                                    RouterPath.PATH_REPORT,
                                    "reportType" to Constants.ReportType.USER.type,
                                    "userId" to dynamicDetail.userId
                                )
                            }
                        }
                    }.showDialog(supportFragmentManager)
                }
            }
        })
        mBinding.recyclerPicture.grid(3).setup {
            addType<String>(R.layout.dynamic_picture_item)
            onClick(R.id.root) {
                previewPicture(
                    this@DynamicDetailActivity,
                    modelPosition,
                    findView<ImageView>(R.id.root),
                    dynamicDetail.pictureDynamicContent.toMutableList()
                )
            }
        }.models = dynamicDetail.pictureDynamicContent

        mBinding.tabLayout.configTabLayoutConfig {
            onSelectItemView = { _, index, selected, _ ->
                if (selected) {
                    if (index == 0) {
                        mBinding.tvAmount.text = "${dynamicDetail.commentAmount}条评论"
                        requestComment()

                    } else {
                        mBinding.tvAmount.text = "${dynamicDetail.numberOfLikes}点赞"
                        requestLike(true)
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

        setWindowSoftInput(float = mBinding.layoutComment, transition = mBinding.layoutComment)


        mBinding.tvLike.safeClick {
            val param = mutableMapOf<String, Any?>()
            param["dynamicId"] = dynamicDetail.dynamicId
            request<Boolean>(
                url = DynamicApi.API_LIKE_DYNAMICS,
                method = Method.POST,
                param = param,
                onSuccess = {
                    dynamicDetail.likeStatus = !dynamicDetail.likeStatus
                    mBinding.tvLike.setDrawable(
                        if (dynamicDetail.likeStatus) R.mipmap.dynamic_like else R.mipmap.dynamic_unlike,
                        Gravity.START
                    )
                    requestLike(mBinding.tabLayout.currentItemIndex==1)
                })
        }

        mBinding.tvSend.safeClick {
            val text = mBinding.editComment.text.toString().trim()
            if (text.isEmpty()) {
                toast("请输入评论内容")
                return@safeClick
            }
            val param = mutableMapOf<String, Any?>(
                "commentContent" to text,
                "dynamicId" to dynamicDetail.dynamicId
            )
            request<Boolean>(DynamicApi.API_ADD_COMMENT, method = Method.POST, param, onSuccess = {
                hideSoftInput()
                mBinding.editComment.setText("")
                requestComment(mBinding.tabLayout.currentItemIndex == 0)
            })


        }
    }

    private fun requestComment(isRefreshRecycler:Boolean = true) {
        val params = mutableMapOf<String, Any?>("dynamicId" to dynamicDetail.dynamicId)
        request<List<DynamicDetailCommentInfo>>(
            DynamicApi.API_GET_COMMENT_DETAIL,
            method = Method.GET,
            param = params,
            onSuccess = {
                if (isRefreshRecycler){
                    mBinding.recyclerView.mutable.clear()
                    mBinding.recyclerView.bindingAdapter.notifyDataSetChanged()
                    mBinding.recyclerView.addModels(it)
                    mBinding.emptyLayout.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                }
                mBinding.tvComment.text = it.size.toString()
            })
    }

    private fun deleteDynamic() {
        CommonConfirmDialog(this, title = "您确定要删除此动态吗") {
            if (this) {
                val param = mutableMapOf<String, Any?>("dynamicId" to dynamicDetail.dynamicId)
                request<Boolean>(DynamicApi.API_DELETE_DYNAMIC, Method.POST, param, onSuccess = {
                    customToast("删除成功")
                    finish()
                })
            }
        }.show()
    }

    private fun requestLike(isRefreshRecycler: Boolean) {
        val params = mutableMapOf<String, Any?>("dynamicId" to dynamicDetail.dynamicId)
        request<List<DynamicDetailLikeInfo>>(
            DynamicApi.API_GET_DYNAMIC_LILES,
            method = Method.GET,
            param = params,
            onSuccess = {
                if (isRefreshRecycler){
                    mBinding.recyclerView.mutable.clear()
                    mBinding.recyclerView.bindingAdapter.notifyDataSetChanged()
                    mBinding.recyclerView.addModels(it)
                    mBinding.emptyLayout.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                }
                mBinding.tvLike.text = it.size.toString()
            })
    }
}

