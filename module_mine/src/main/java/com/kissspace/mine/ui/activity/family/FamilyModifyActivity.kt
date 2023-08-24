package com.kissspace.mine.ui.activity.family

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.viewModels
import com.didi.drouter.annotation.Router
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.binding.dataBinding
import com.kissspace.common.router.parseIntent
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.openPictureSelector
import com.kissspace.mine.viewmodel.FamilyViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineModifyFamilyInformationBinding
import com.kissspace.network.result.collectData
import com.kissspace.util.loadImage
import com.kissspace.util.logE
import com.kissspace.util.toast
import java.io.File

/**
 * @Author gaohangbo
 * @Date 2022/12/28 18:52.
 * @Describe 家族资料
 */
@Router(path = RouterPath.PATH_MODIFY_FAMILY)
class FamilyModifyActivity : com.kissspace.common.base.BaseActivity(R.layout.mine_modify_family_information) {
    private val mBinding by dataBinding<MineModifyFamilyInformationBinding>()

    private var familyPicturePath: String? = null

    private val familyId by parseIntent<String>()

    private val familyIcon by parseIntent<String>()

    private val familyDesc by parseIntent<String>()

    private val familyNotice by parseIntent<String>()

    private val mViewModel by viewModels<FamilyViewModel>()


    override fun initView(savedInstanceState: Bundle?) {
        mBinding.titleBar.setTitleBarListener(
            onLeftClick = {
                finish()
            },
            onRightClick = {
                if (TextUtils.isEmpty(familyPicturePath)) {
                    toast("请添加家族头像")
                }else{
                    mViewModel.updateFamilyInfo(
                        familyId,
                        familyPicturePath ?: "",
                        mBinding.etDescribe.editText?.text.toString(),
                        mBinding.etNotice.editText?.text.toString()
                    )
                }

            },
        )
        mBinding.m = mViewModel
        familyPicturePath = familyIcon
        if (!TextUtils.isEmpty(familyPicturePath)) {
            mBinding.ivAddFamily.visibility = View.GONE
            mBinding.clFamily.visibility = View.VISIBLE
            mBinding.ivFamily.loadImage(familyIcon, radius = 8f)
            mBinding.ivClose.visibility = View.VISIBLE
        }
        logE("familyDesc$familyDesc")
        mViewModel.familyDesc.set(familyDesc)
        mViewModel.familyNotice.set(familyNotice)
        mBinding.ivClose.safeClick {
            familyPicturePath = null
            mBinding.ivClose.visibility = View.GONE
            mBinding.clFamily.visibility = View.GONE
            mBinding.ivAddFamily.visibility = View.VISIBLE
        }
        mBinding.ivAddFamily.safeClick {
            openPictureSelector(1) {
                mBinding.ivAddFamily.visibility = View.GONE
                mBinding.ivFamily.visibility = View.VISIBLE
                mBinding.clFamily.visibility = View.VISIBLE
                familyPicturePath = it?.get(0)
                val file = familyPicturePath?.let { it1 -> File(it1) }
                file?.let { it ->
                    mBinding.ivFamily.loadImage(it, radius = 8f)
                    mBinding.ivClose.visibility = View.VISIBLE
                    mViewModel.updateFamilyIcon(param = it) { result ->
                        toast("上传成功")
                        familyPicturePath = result
                    }
                }

            }
        }
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.updateFamilyEvent, onSuccess = {
            toast("修改成功")
            setResult(
                RESULT_OK, intent.putExtra(
                    "result",
                    FamilyModifyResult
                )
            )
            finish()
        }, onError = {
            toast(it.errorMsg)
        })
    }

    companion object {
        const val FamilyModifyResult = "FamilyModifyResult"
    }
}