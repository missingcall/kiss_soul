package com.kissspace.room.ui.fragment

import android.content.Context
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.binding.dataBinding
import com.kissspace.common.ext.safeClick
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.util.orZero
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomFragmentGuideBinding
import com.kissspace.module_room.databinding.RoomFragmentGuideRootBinding
import com.kissspace.module_room.databinding.RoomFragmentGuidefirstBinding
import com.kissspace.module_room.databinding.RoomFragmentGuidesecondBinding
import com.kissspace.module_room.databinding.RoomFragmentGuidethirdBinding
import com.kissspace.room.widget.RoomGuideDialogFragment

/**
 * @Author gaohangbo
 * @Date 2023/3/8 17:38.
 * @Describe
 */
class RoomGuideFragment : BaseFragment(R.layout.room_fragment_guide_root) {
    private val mainBindingRoot by dataBinding<RoomFragmentGuideRootBinding>()
    private var mainBinding0: RoomFragmentGuideBinding? = null
    private var mainBinding1: RoomFragmentGuidefirstBinding? = null
    private var mainBinding2: RoomFragmentGuidesecondBinding? = null
    private var mainBinding3: RoomFragmentGuidethirdBinding? = null
    private var roomRoomGuideFragment: RoomGuideDialogFragment? = null

    private var position: Int = 0

    companion object {
        fun newInstance(position: Int) =
            RoomGuideFragment().apply {
                arguments = bundleOf("position" to position)
            }
    }


    override fun initView(savedInstanceState: Bundle?) {
        position = arguments?.getInt("position").orZero()
        when (position) {
            0 -> {
                mainBinding0 = RoomFragmentGuideBinding.inflate(layoutInflater)
                mainBindingRoot.clRoot.addView(mainBinding0?.root, ConstraintLayout.LayoutParams.MATCH_PARENT,ConstraintLayout.LayoutParams.MATCH_PARENT)
                mainBinding0?.tvGuideJump?.safeClick {
                    FlowBus.post(Event.RoomGuideEvent(isJump = true))
                }
                mainBinding0?.tvNext?.safeClick {
                    FlowBus.post(Event.RoomGuideEvent(position + 1,false))
                }
            }

            1 -> {
                mainBinding1 = RoomFragmentGuidefirstBinding.inflate(layoutInflater)
                mainBindingRoot.clRoot.addView(mainBinding1?.root,ConstraintLayout.LayoutParams.MATCH_PARENT,ConstraintLayout.LayoutParams.MATCH_PARENT)
                mainBinding1?.tvGuideJump?.safeClick {
                    FlowBus.post(Event.RoomGuideEvent(isJump = true))
                }
                mainBinding1?.tvNext?.safeClick {
                    FlowBus.post(Event.RoomGuideEvent(position + 1,false))
                }
            }


            2 -> {
                mainBinding2 = RoomFragmentGuidesecondBinding.inflate(layoutInflater)
                mainBindingRoot.clRoot.addView(mainBinding2?.root,ConstraintLayout.LayoutParams.MATCH_PARENT,ConstraintLayout.LayoutParams.MATCH_PARENT)
                mainBinding2?.tvGuideJump?.safeClick {
                    FlowBus.post(Event.RoomGuideEvent(isJump = true))
                }
                mainBinding2?.tvNext?.safeClick {
                    FlowBus.post(Event.RoomGuideEvent(position + 1,false))
                }
            }

            else -> {
                mainBinding3 = RoomFragmentGuidethirdBinding.inflate(layoutInflater)
                mainBindingRoot.clRoot.addView(mainBinding3?.root,ConstraintLayout.LayoutParams.MATCH_PARENT,ConstraintLayout.LayoutParams.MATCH_PARENT)
                mainBinding3?.tvGuideJump?.safeClick {
                    FlowBus.post(Event.RoomGuideEvent(isJump = true))
                }
                mainBinding3?.tvNext?.safeClick {
                    FlowBus.post(Event.RoomGuideEvent(isJump = true))
                }
            }
        }


    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        roomRoomGuideFragment = parentFragment as RoomGuideDialogFragment?
    }


}