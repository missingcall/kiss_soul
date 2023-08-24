package com.kissspace.login.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.ToastUtils
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.model.UserAccountBean
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.parseIntent
import com.kissspace.common.util.hideLoading
import com.kissspace.common.util.showLoading
import com.kissspace.login.viewmodel.LoginViewModel
import com.kissspace.module_login.R
import com.kissspace.module_login.databinding.LoginActivityChooseAccoutBinding
import com.kissspace.network.result.collectData
import com.kissspace.util.fromJson
import com.kissspace.util.toast

/**
 *@author: adan
 *@date: 2023/4/6
 *@Description: 选择账号
 */
@Router(path = RouterPath.PATH_CHOOSE_ACCOUNT)
class ChooseAccountActivity : com.kissspace.common.base.BaseActivity(R.layout.login_activity_choose_accout) {

    private val mBinding by viewBinding<LoginActivityChooseAccoutBinding>()
    private val mViewModel by viewModels<LoginViewModel>()
    var accounts by parseIntent<String>()
    var phone by parseIntent<String>()
    private var mChooseUserId = ""
    private var mTokenHead = ""
    private var mToken = ""

    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                finish()
            }

            override fun onRightClick(titleBar: TitleBar?) {
               createAccount()
            }
        })
        mBinding.tvEnter.safeClick {
            showLoading("正在登录")
            mViewModel.loginByUserId(mChooseUserId,mTokenHead,mToken)
        }
        initRecyclerView()
        initData()
    }

    private fun initData() {
        if (accounts.isNotEmpty()){
            val accountBeanList = fromJson<List<UserAccountBean>>(accounts)
            if (accountBeanList.size > 9){
                mBinding.titleBar.rightView.visibility = View.GONE
            }
            mBinding.recycler.bindingAdapter.addModels(accountBeanList)
            mBinding.recycler.bindingAdapter.setChecked(0,true)
            val userAccountBean = accountBeanList[0]
            mTokenHead = if (userAccountBean.tokenHead != null) userAccountBean.tokenHead!! else ""
            mToken = if (userAccountBean.token != null) userAccountBean.token!! else ""
        }
    }

    private fun initRecyclerView() {
        mBinding.recycler.linear().divider(com.kissspace.module_common.R.drawable.common_user_account_divider_item).setup {
            addType<UserAccountBean> {
                R.layout.login_choose_account_list_recycler_item
            }
            onClick(R.id.root){
                val checked = getModel<UserAccountBean>().checked
                if (!checked){
                    setChecked(adapterPosition, !checked)
                }
            }
            onChecked { position, isChecked, _ ->
                val model = getModel<UserAccountBean>(position)
                model.checked = isChecked
                model.notifyChange()
                mChooseUserId = model.userId
            }
            singleMode = true
        }.models = mutableListOf()
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.token, onSuccess = {
            hideLoading()
            mViewModel.loginIm(it,onSuccess = {
                finish()
            })
        }, onError = {
            hideLoading()
            ToastUtils.showLong("登录失败${it.errorMsg}")
        })

        collectData(mViewModel.createAccounts, onSuccess = {
            mViewModel.loginByUserId(it.userId,mTokenHead,mToken)
        }, onError = {
            hideLoading()
            toast("创建账号失败,请稍后重试")
        })
    }

    private fun createAccount() {
        showLoading()
        mViewModel.createAccount(phone)
    }

}