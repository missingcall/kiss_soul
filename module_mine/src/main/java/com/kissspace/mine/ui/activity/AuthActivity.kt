package com.kissspace.mine.ui.activity


import android.os.Bundle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.ext.safeClick
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.util.customToast
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityAuthBinding


@Router(uri = RouterPath.PATH_MINE_AUTH)
class AuthActivity : BaseActivity(R.layout.mine_activity_auth) {

    private val mBinding by viewBinding<MineActivityAuthBinding>()

    override fun initView(savedInstanceState: Bundle?) {
        initEvent()
    }

    private fun initEvent() {
        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                finish()
            }
        })
        mBinding.ivRealName.safeClick {
            if (!MMKVProvider.authentication) {
                jump(RouterPath.PATH_USER_IDENTITY_AUTH)
            }else{
                customToast("您已经实名过了")
            }
        }

        mBinding.ivSchool.safeClick {
            customToast("功能还在开发中~")
        }
        mBinding.ivCar.safeClick {
            customToast("功能还在开发中~")
        }
        mBinding.ivVideo.safeClick {
            customToast("功能还在开发中~")
        }
        mBinding.ivCareer.safeClick {
            customToast("功能还在开发中~")
        }
        mBinding.ivRoom.safeClick {
            customToast("功能还在开发中~")
        }
    }
}