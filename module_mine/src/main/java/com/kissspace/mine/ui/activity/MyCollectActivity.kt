package com.kissspace.mine.ui.activity

import android.os.Bundle
import androidx.fragment.app.commit
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.angcyo.tablayout.delegate2.ViewPager2Delegate
import com.didi.drouter.annotation.Router
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.Constants
import com.kissspace.common.router.RouterPath
import com.kissspace.mine.ui.fragment.MyCollectFragment
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityCollectBinding


/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/4 19:52
 * @Description: 我的收藏
 *
 */
@Router(path = RouterPath.PATH_MY_COLLECT)
class MyCollectActivity : BaseActivity(R.layout.mine_activity_collect) {
    private val mBinding by viewBinding<MineActivityCollectBinding>()
    override fun initView(savedInstanceState: Bundle?) {
        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                finish()
            }
        })

        supportFragmentManager.commit(true){
            replace(R.id.container, MyCollectFragment.newInstance(Constants.ROOM_TYPE_PARTY))
        }

    }
}