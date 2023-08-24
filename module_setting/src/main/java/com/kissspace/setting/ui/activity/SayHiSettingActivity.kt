package com.kissspace.setting.ui.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.commit
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.github.gzuliyujiang.wheelpicker.OptionPicker
import com.github.gzuliyujiang.wheelpicker.contract.OnOptionPickedListener
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.router.RouterPath
import com.kissspace.util.resToColor
import com.kissspace.module_setting.R
import com.kissspace.module_setting.databinding.SettingActivitySayHiBinding
import com.kissspace.setting.ui.fragment.SayHiAudioFragment
import com.kissspace.setting.ui.fragment.SayHiPictureFragment
import com.kissspace.setting.ui.fragment.SayHiTextFragment
import com.kissspace.setting.viewmodel.SayHiViewModel

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/28 15:23
 * @Description: 打招呼设置页面
 *
 */
@Router(path = RouterPath.PATH_SAY_HI_SETTING)
class SayHiSettingActivity : com.kissspace.common.base.BaseActivity(R.layout.setting_activity_say_hi),
    OnOptionPickedListener {
    private val mBinding by viewBinding<SettingActivitySayHiBinding>()
    private val mViewModel by viewModels<SayHiViewModel>()
    private var picker: OptionPicker? = null
    override fun initView(savedInstanceState: Bundle?) {

        setTitleBarListener(mBinding.titleBar)
        initPicker()
        mBinding.rltChooseType.setOnClickListener {
            picker?.show()
        }
    }

    private fun initPicker() {
        val list = mutableListOf("文本消息", "语音消息", "图片消息")
        picker = OptionPicker(this).apply {
            setTitle("")
            setBodyHeight(235)
            setData(list)
            okView.setTextColor(com.kissspace.module_common.R.color.color_6C74FB.resToColor())
            topLineView.visibility = View.GONE
            wheelLayout.apply {
                setSelectedTextColor(com.kissspace.module_common.R.color.color_6C74FB.resToColor())
                setTextColor(com.kissspace.module_common.R.color.color_949499.resToColor())
            }
            setOnOptionPickedListener(this@SayHiSettingActivity)
        }
    }

    override fun onOptionPicked(position: Int, item: Any?) {
        mBinding.tvCurrentType.text = item?.toString()
        mViewModel.submitEnable.set(false)
        when (position) {
            0 -> {
                supportFragmentManager.commit {
                    replace(R.id.container, SayHiTextFragment())
                }
            }

            1 -> {
                supportFragmentManager.commit {
                    replace(R.id.container, SayHiAudioFragment())
                }
            }

            2 -> {
                supportFragmentManager.commit {
                    replace(R.id.container, SayHiPictureFragment())
                }
            }
        }
    }

}