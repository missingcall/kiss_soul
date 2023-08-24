package com.kissspace.common.provider

import androidx.fragment.app.FragmentManager
import com.kissspace.common.model.UpgradeBean

/**
 * @Author gaohangbo
 * @Date 2023/3/3 18:17.
 * @Describe
 */
interface IAppProvider {

    fun showUpgradeDialog(
        fm: FragmentManager, upgradeBean: UpgradeBean
    )


}