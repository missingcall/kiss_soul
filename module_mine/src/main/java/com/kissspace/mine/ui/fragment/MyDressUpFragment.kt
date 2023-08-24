package com.kissspace.mine.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.ToastUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.model.MyDressUpListBean
import com.kissspace.common.util.customToast
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.mine.http.MineApi
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineFragmentMyDressUpBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request

class MyDressUpFragment : BaseFragment(R.layout.mine_fragment_my_dress_up) {
    private val mBinding by viewBinding<MineFragmentMyDressUpBinding>()
    private lateinit var type: String

    companion object {
        fun newInstance(type: String) = MyDressUpFragment().apply {
            arguments = bundleOf("type" to type)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getString("type")!!
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.recyclerView.grid(2).setup {
            addType<MyDressUpListBean> {
                when(type){
                    "001"->R.layout.mine_layout_my_dress_up_car_item
                    "002"->R.layout.mine_layout_my_dress_up_item
                    else->R.layout.mine_layout_my_dress_up_background_item
                }
            }
            onBind {
                val wearButton = findView<ImageView>(R.id.tv_wear)
                wearButton.visibility = if (type=="003") View.INVISIBLE else View.VISIBLE
            }
            onClick(R.id.tv_wear) {
                val model = getModel<MyDressUpListBean>()
                if (model.wearState == "001") {
                    cancelWearDress(model)
                } else {
                    if(model.getSurplusDay() == "已过期"){
                        customToast("装扮已过期")
                    }else{
                        wearDress(model)
                    }
                }
            }
        }.models = mutableListOf()

        initData()
    }

    private fun initData() {
        val params = mutableMapOf<String, Any?>("commodityType" to type)
        request<List<MyDressUpListBean>>(MineApi.API_QUERY_MY_DRESS_UP,
            Method.GET,
            params,
            onSuccess = {
                it.forEach {that->
                    that.profilePath = MMKVProvider.userAvatar
                }
                if (it.isEmpty()) {
                    mBinding.stateLayout.showEmpty()
                } else {
                    initRecyclerView(it)
                }
            })
    }

    private fun initRecyclerView(list: List<MyDressUpListBean>) {
        mBinding.recyclerView.bindingAdapter.models=list
    }

    private fun wearDress(model: MyDressUpListBean) {
        val param = mutableMapOf<String, Any?>()
        param["commodityId"] = model.commodityId
        param["commodityType"] = type
        request<String?>(MineApi.API_WEAR_DRESS_UP, Method.POST, param, onSuccess = {
//            model.wearState = if (model.wearState == "001") "002" else "001"
//            model.notifyChange()
            initData()
        })
    }

    private fun cancelWearDress(model: MyDressUpListBean) {
        val param = mutableMapOf<String, Any?>()
        param["commodityId"] = model.commodityId
        param["commodityType"] = type
        request<String?>(MineApi.API_CANCEL_WEAR_DRESS_UP, Method.POST, param, onSuccess = {
//            model.wearState = if (model.wearState == "001") "002" else "001"
//            model.notifyChange()
            initData()
        })
    }
}