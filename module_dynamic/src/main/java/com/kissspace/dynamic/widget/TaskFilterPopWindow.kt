package com.kissspace.dynamic.widget

import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BaseObservable
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.kissspace.common.widget.BasePopWindow
import com.kissspace.module_dynamic.R
import com.kissspace.module_dynamic.databinding.PopTaskDetailBinding

/**
 * @Author gaohangbo
 * @Date 2023/8/2 13:14.
 * @Describe
 */
class TaskFilterPopWindow(val activity: AppCompatActivity) :
    BasePopWindow<PopTaskDetailBinding>(activity, R.layout.pop_task_detail) {

    init {
        //setBackgroundDrawable(AppCompatResources.getDrawable(context,R.drawable.common_cover_bg))
        width = WindowManager.LayoutParams.MATCH_PARENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
    }
    fun setFilterData() {
//        activity.getSelectPayChannelList { list ->
//            list.filter { it.isSelected }
//            list.let { walletRechargeList ->
//                mBinding.recyclerView.grid(3).setup {
//                    addType<PayProductResponses>( R.layout.item_task_detail)
//                    singleMode = true
//                    onChecked { position, isChecked, _ ->
////            currentPayProductResponses = getModel<PayProductResponses>(position)
////            currentSelectRmb = getModel<PayProductResponses>(position).payProductCash
////            currentPayProductResponses?.isSelected = isChecked
////            currentPayProductResponses?.notifyChange()
//                    }
////                    onFastClick(R.id.cl_charge) {
////                        checkedAll(false)
////                        setChecked(modelPosition, true)
//////            mViewModel.mRmb.value = "¥" +
//////                    Format.E.format(getModel<PayProductResponses>().payProductCash)
////                    }
//                }.models = walletRechargeList[0].payProductListResponses
//
//            }
//        }
        val data = mutableListOf<BetItem>()
        data.add(BetItem(10, true))
        data.add(BetItem(100, false))
        data.add(BetItem(200, false))
        data.add(BetItem(500, false))
        data.add(BetItem(1000, false))
        data.add(BetItem(0, false))
        mBinding.recyclerView.grid(3).setup {
            addType<BetItem> { R.layout.room_dialog_prediction_join_bet_item }
            onBind {
                findView<LinearLayout>(R.id.root_join_prediction).setOnClickListener {
                    val model = getModel<BetItem>()
                    val list = mutable as MutableList<BetItem>
                    list.forEachIndexed { index, betItem ->
                        betItem.isChecked = index == modelPosition
                        betItem.notifyChange()
                    }
//                    if (model.amount == 0L) {
//                       // mBinding.editAmount.setText(userInfo?.getIntegralLong().toString())
//                    } else {
//                        mBinding.editAmount.setText(model.amount.toString())
//                    }
//                    mBinding.editAmount.clearFocus()
                }
                findView<ImageView>(R.id.iv_logo).visibility =
                    if (getModel<BetItem>().amount > 0) View.VISIBLE else View.GONE
            }
        }.models = data

    }

    data class BetItem(var amount: Long, var isChecked: Boolean = false) : BaseObservable()

}