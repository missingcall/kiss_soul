package com.kissspace.android.ui.activity
import android.os.Bundle
import androidx.activity.viewModels
import com.didi.drouter.annotation.Router
import com.kissspace.android.R
import com.kissspace.android.databinding.AppAuthConfirmBinding
import com.kissspace.android.viewmodel.IdentityAuthViewModel
import com.kissspace.common.binding.dataBinding
import com.kissspace.common.router.jump
import com.kissspace.common.router.parseIntent
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.router.RouterPath
import com.kissspace.util.addAfterTextChanged

/**
 * @Author gaohangbo
 * @Date 2023/1/2 16:42.
 * @Describe  身份认证页面
 */
@Router(uri = RouterPath.PATH_USER_IDENTITY_AUTH)
class IdentityAuthActivity : com.kissspace.common.base.BaseActivity(R.layout.app_auth_confirm){

    private val mBinding by dataBinding<AppAuthConfirmBinding>()

    private val mViewModel by viewModels<IdentityAuthViewModel>()
    private var isH5BindAlipay by parseIntent<Boolean>()
    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar){
            if(isH5BindAlipay){
                jump(RouterPath.PATH_MAIN,"index" to 0)
            }else{
                finish()
            }
        }

        mBinding.m=mViewModel
        mBinding.lifecycleOwner = this
        mBinding.etName.addAfterTextChanged {
            mViewModel.userName.value=it.toString()
        }
        mBinding.etCode.addAfterTextChanged {
            mViewModel.idNumber.value=it.toString()
        }
        mBinding.tvConfirm.safeClick {
            mViewModel.identityAuth(mBinding.etName.text.toString(),mBinding.etCode.text.toString()){
                //实名认证和活体认证进行绑定 判断返回的状态
                jump(RouterPath.PATH_USER_BIOMETRIC)
                finish()
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(isH5BindAlipay){
            jump(RouterPath.PATH_MAIN,"index" to 0)
        }

    }

}