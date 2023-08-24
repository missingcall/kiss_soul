package com.kissspace.android.provider

import androidx.fragment.app.FragmentManager
import com.didi.drouter.annotation.Service
import com.didi.drouter.api.Extend
import com.kissspace.android.widget.UpgradeDialog
import com.kissspace.common.model.UpgradeBean

import com.kissspace.common.provider.IAppProvider


@Service(function = [IAppProvider::class], cache = Extend.Cache.SINGLETON)
class AppService : IAppProvider {

    override fun showUpgradeDialog(fm: FragmentManager, upgradeBean: UpgradeBean) {
        UpgradeDialog.newInstance(upgradeBean).show(fm)
    }


}


