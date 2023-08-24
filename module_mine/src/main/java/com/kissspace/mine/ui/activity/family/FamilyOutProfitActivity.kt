package com.kissspace.mine.ui.activity.family

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.commit
import androidx.lifecycle.viewModelScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.Constants
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.getSearchData
import com.kissspace.mine.ui.fragment.FamilyRoomEarnsFragment
import com.kissspace.mine.viewmodel.FamilyViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineFamilyOutProfitBinding
import com.kissspace.util.logE
import com.kissspace.util.trimString

/**
 * @Author gaohangbo
 * @Date 2022/12/29 08:38.
 * @Describe 家族房间收益
 */
@Router(path = RouterPath.PATH_FAMILY_OUT_PROFIT)
class FamilyOutProfitActivity : BaseActivity(R.layout.mine_family_out_profit) {
    val mBinding by viewBinding<MineFamilyOutProfitBinding>()
    private val mViewModel by viewModels<FamilyViewModel>()
    private var familyId: String? = null
    private lateinit var  familyRoomEarnsFragment:FamilyRoomEarnsFragment
    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        familyId = intent.getStringExtra("familyId")
        getSearchData(mViewModel.viewModelScope,mBinding.commonSearchView.mBinding.etSearch){
            logE("value$it")
            searchContent(it.toString())
        }

        familyRoomEarnsFragment=FamilyRoomEarnsFragment.newInstance(Constants.FamilyEarnsType.FamilyOutToady.type,  familyId)

        supportFragmentManager.commit (true){
            replace(
                R.id.container,
                familyRoomEarnsFragment
            )
        }
        mBinding.commonSearchView.onEditorActionBlock={
            searchContent(it)
        }
    }

    private fun searchContent(content: String) {
        if(!familyRoomEarnsFragment.isDetached){
            familyRoomEarnsFragment.searchContent(content, true)
        }
    }

    fun getSearchContent(): String {
        return mBinding.commonSearchView.mBinding.etSearch.text.trimString()
    }
}