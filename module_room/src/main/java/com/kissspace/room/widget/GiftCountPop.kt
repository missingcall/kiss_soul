package com.kissspace.room.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomPoupGiftCountBinding


/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/30 14:05
 * @Description: 礼物选择数量弹窗
 *
 */
class GiftCountPop(context: Context, private val block: Int.() -> Unit) :
    Dialog(context, com.kissspace.module_common.R.style.Theme_CustomDialog) {

    private var mBinding = RoomPoupGiftCountBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        init()
    }

    private fun init() {
        val data = mutableListOf<CountItem>()
        data.add(CountItem(1, "一心一意"))
        data.add(CountItem(10, "十全十美"))
        data.add(CountItem(38, "美丽人生"))
        data.add(CountItem(66, "一切顺利"))
        data.add(CountItem(188, "要抱抱"))
        data.add(CountItem(520, "我爱你"))
        data.add(CountItem(1314, "一生一世"))
        data.add(CountItem(3344, "生生世世"))
        data.reverse()
        mBinding.recyclerView.linear().setup {
            addType<CountItem> { R.layout.room_layout_gift_count_item }
            onBind {
                val line = findView<View>(R.id.line)
                line.visibility = if (modelPosition == mutable.size - 1) View.GONE else View.VISIBLE
            }
            onFastClick(R.id.root) {
                val model = getModel<CountItem>()
                block(model.count)
                dismiss()
            }
        }.models = data
    }

    fun showDialog() {
        window?.setGravity(Gravity.BOTTOM or Gravity.RIGHT)
        super.show()
    }


}

data class CountItem(var count: Int, var text: String)