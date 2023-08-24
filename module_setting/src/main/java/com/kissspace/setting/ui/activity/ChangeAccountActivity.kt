package com.kissspace.setting.ui.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.model.UserAccountBean
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.hideLoading
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.oldAccountExit
import com.kissspace.common.util.showAdolescentDialog
import com.kissspace.common.util.showLoading
import com.kissspace.setting.viewmodel.ChangeAccountViewModel
import com.kissspace.module_setting.R
import com.kissspace.module_setting.databinding.SettingActivityChangeAccountBinding
import com.kissspace.network.result.collectData
import com.kissspace.util.toast

/**
 *@author: adan
 *@date: 2023/4/7
 *@Description:切换账号
 */
@Router(path = RouterPath.PATH_CHANGE_ACCOUNT)
class ChangeAccountActivity : com.kissspace.common.base.BaseActivity(R.layout.setting_activity_change_account) {

    private val mBinding by viewBinding<SettingActivityChangeAccountBinding>()
    private val mViewModel by viewModels<ChangeAccountViewModel>()
    private var userPhone :String = ""
    private var isCreateAccount  = false
    private var loginAccountPosition = 0;

    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        mBinding.conAdd.safeClick {
            createAccount()
        }
        initRecyclerView()
        initData()
    }

    private fun initData() {
        userPhone = MMKVProvider.userPhone
        mViewModel.requestUserListByPhone(MMKVProvider.userPhone)
    }

    private fun initRecyclerView() {
        mBinding.recycler.linear().divider(com.kissspace.module_common.R.drawable.common_user_account_divider_item).setup {
            addType<UserAccountBean> {
                R.layout.setting_change_account_list_recycler_item
            }
            onBind {
                findView<ConstraintLayout>(R.id.root).safeClick {
                    val model = getModel<UserAccountBean>()
                    if (model.checked){
                        toast("当前账号已登录")
                        return@safeClick
                    }
                    loginAccountPosition = adapterPosition
                    showLoading("切换中...")
                    mViewModel.loginByUserId(model.userId)
                }
            }
            onChecked { position, isChecked, _ ->
                val model = getModel<UserAccountBean>(position)
                model.checked = isChecked
                model.notifyChange()
            }
            singleMode = true
            clickThrottle = 500
        }.models = mutableListOf()
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.accounts, onSuccess = {
            it.forEachIndexed { index, userAccountBean ->
                if (userAccountBean.userId == MMKVProvider.userId){
                    loginAccountPosition = index
                }
            }
            if (it.size < 10){
                mBinding.conAdd.visibility = View.VISIBLE
            }else{
                mBinding.conAdd.visibility = View.GONE
            }
            mBinding.recycler.bindingAdapter.addModels(it)
            mBinding.recycler.bindingAdapter.setChecked(loginAccountPosition,true)
        })

        collectData(mViewModel.createAccounts, onSuccess = {
            mViewModel.loginByUserId(it.userId)
        }, onError = {
            hideLoading()
            toast("创建账号失败,请稍后重试")
        })

        collectData(mViewModel.token, onSuccess = {
            //先退出之前账号的房间
            oldAccountExit {
                mViewModel.loginIm(it,onSuccess = {
                    if (!isCreateAccount){
                        isCreateAccount = false
                        changeAccountSuccess()
                    }
                })
            }
        }, onError = {
            hideLoading()
            toast("切换账号失败")
        })
    }

    private fun changeAccountSuccess() {
        FlowBus.post(Event.RefreshChangeAccountEvent)
        FlowBus.post(Event.CloseRoomFloating)
        mBinding.recycler.bindingAdapter.setChecked(loginAccountPosition,true)
        toast("切换账号成功")
    }

    private fun createAccount() {
        isCreateAccount = true
        showLoading()
        mViewModel.createAccount(userPhone)
    }
}