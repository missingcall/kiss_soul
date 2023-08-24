package com.kissspace.room.ui.fragment

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.databinding.BaseObservable
import androidx.fragment.app.viewModels
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.binding.dataBinding
import com.kissspace.common.ext.safeClick
import com.kissspace.common.model.PredictionListBean
import com.kissspace.common.util.customToast
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomFragmentAddPredictionBinding
import com.kissspace.network.result.collectData
import com.kissspace.room.viewmodel.AddPredictionViewModel

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/13 17:54
 * @Description: 创建竞猜fragment
 *
 */
class RoomAddPredictionFragment : BaseFragment(R.layout.room_fragment_add_prediction) {
    private val mainBinding by dataBinding<RoomFragmentAddPredictionBinding>()
    private val mViewModel by viewModels<AddPredictionViewModel>()
    private lateinit var crId: String

    companion object {
        fun newInstance(crId: String, predictionListBean: PredictionListBean?) =
            RoomAddPredictionFragment().apply {
                arguments = bundleOf("crId" to crId, "predictionListBean" to predictionListBean)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crId = arguments?.getString("crId")!!
    }

    override fun initView(savedInstanceState: Bundle?) {
        mainBinding.vm = mViewModel
        initRecyclerView()
        initEvents()
        initData()
    }

    private fun initData() {
        val predictionListBean = arguments?.getParcelable<PredictionListBean>("predictionListBean")
        mViewModel.validTime
        predictionListBean?.let {
            mViewModel.title.set(it.integralGuessTitle)
            mViewModel.leftOption.set(it.leftOption)
            mViewModel.rightOption.set(it.rightOption)
            mViewModel.validTime.set(300)
            mainBinding.recyclerTime.bindingAdapter.setChecked(2, true)
            mViewModel.buttonEnable.set(true)
        }
    }

    private fun initRecyclerView() {
        val data = mutableListOf<TimeItem>()
        data.add(TimeItem(30, "30秒", false))
        data.add(TimeItem(60, "1分钟", false))
        data.add(TimeItem(120, "2分钟", false))
        data.add(TimeItem(180, "3分钟", false))
        data.add(TimeItem(240, "4分钟", false))
        data.add(TimeItem(300, "5分钟", false))
        mainBinding.recyclerTime.grid(3).setup {
            addType<TimeItem> { R.layout.room_layout_add_prediction_time_item }
            singleMode = true
            onChecked { position, isChecked, _ ->
                val model = getModel<TimeItem>(position)
                model.checked = isChecked
                model.notifyChange()
                if (!isChecked) {
                    mViewModel.validTime.set(0)
                } else {
                    mViewModel.validTime.set(model.time)
                    mainBinding.editOptionRight
                }
                mViewModel.checkInfo()
            }
            onFastClick(R.id.text) {
                checkedSwitch(modelPosition)
            }
        }.models = data
        mainBinding.recyclerTime.bindingAdapter.setChecked(2, true)

    }

    private fun initEvents() {
        mainBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                parentFragmentManager.popBackStack()
            }
        })
        mainBinding.tvSubmit.safeClick {
            mViewModel.submit(crId)
        }
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.submitEvent, onSuccess = {
            customToast("创建成功", true)
            parentFragmentManager.popBackStack()
        }, onError = {
            customToast(it.message, true)
        })
    }
}

data class TimeItem(var time: Long, var text: String, var checked: Boolean) : BaseObservable()