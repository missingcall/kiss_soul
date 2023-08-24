package com.kissspace.login.widget

import androidx.databinding.BaseObservable
import androidx.fragment.app.FragmentManager
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.kissspace.common.config.AppConfigKey
import com.kissspace.common.http.getAppConfigByKey
import com.kissspace.common.widget.BaseBottomSheetDialogFragment
import com.kissspace.module_login.R
import com.kissspace.module_login.databinding.LoginDialogChangeAvatarBinding

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/14 17:40
 * @Description: 更换头像弹窗
 *
 */
class ChangeAvatarDialog :
    BaseBottomSheetDialogFragment<LoginDialogChangeAvatarBinding>() {
    private var block: ((Int, String?) -> Unit)? = null
    override fun getViewBinding() = LoginDialogChangeAvatarBinding.inflate(layoutInflater)

    fun setCallBack(block: (Int, String?) -> Unit) {
        this.block = block
    }

    override fun initView() {
        getAppConfigByKey<List<String>>(AppConfigKey.KEY_DEFAULT_AVATAR) {
            mBinding.recyclerView.grid(4).setup {
                addType<AvatarBean> { R.layout.login_layout_default_avatar_item }
                singleMode = true
                onFastClick(R.id.image) {
                    val model = getModel<AvatarBean>()
                    block?.invoke(0, model.url)
                    dismiss()
                }
            }.models = it.map { url -> AvatarBean(url) }
        }

        mBinding.tvTakePhoto.setOnClickListener {
            block?.invoke(0, null)
            dismiss()
        }
        mBinding.tvOpenAlbum.setOnClickListener {
            block?.invoke(1, null)
            dismiss()
        }
    }


    data class AvatarBean(var url: String, var checked: Boolean = false) : BaseObservable()
}