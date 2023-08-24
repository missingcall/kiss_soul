package com.kissspace.room.widget

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.util.orZero
import com.kissspace.common.widget.BaseDialogFragment
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogGuideRootBinding
import com.kissspace.room.ui.fragment.RoomGuideFragment

/**
 * @Author gaohangbo
 * @Date 2023/3/8 17:19.
 * @Describe 显示房间引导弹窗
 */
class RoomGuideDialogFragment : BaseDialogFragment<RoomDialogGuideRootBinding>(Gravity.CENTER) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme)
    }

    override fun getLayoutId(): Int {
        return R.layout.room_dialog_guide_root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.attributes?.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            dimAmount = 0F
            flags = flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }



    override fun initView() {
        mBinding.viewPager.apply {
            adapter = object : FragmentStateAdapter(requireActivity()) {
                override fun getItemCount(): Int = 4
                override fun createFragment(position: Int): Fragment =
                    RoomGuideFragment.newInstance(position)

                override fun getItemId(position: Int): Long = position.toLong()

                override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
                    super.onAttachedToRecyclerView(recyclerView)
                    recyclerView.setItemViewCacheSize(0)
                }
            }
        }
        mBinding.viewPager.isUserInputEnabled = false

        FlowBus.observerEvent<Event.RoomGuideEvent>(this){
            if(it.isJump){
                MMKVProvider.isShowRoomGuide=true
                dismiss()
            }else{
                setCurrentItem(it.position.orZero())
            }
        }
    }
    private fun setCurrentItem(position: Int) {
        if (mBinding.viewPager.adapter?.itemCount.orZero() > position) {
            mBinding.viewPager.currentItem = position
        }
    }


    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val window = dialog.window
            if (window != null) {
                val width = ViewGroup.LayoutParams.MATCH_PARENT
                val height = ViewGroup.LayoutParams.MATCH_PARENT
                window.setLayout(width, height)
            }
        }
    }


}