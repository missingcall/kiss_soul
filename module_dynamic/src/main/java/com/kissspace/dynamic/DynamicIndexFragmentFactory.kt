package com.kissspace.dynamic


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.kissspace.dynamic.ui.fragment.FocusFragment
import com.kissspace.dynamic.ui.fragment.RecommendFragment


/**
 * @Author gaohangbo
 * @Date 2023/7/20 14:16.
 * @Describe
 */
class DynamicIndexFragmentFactory : FragmentFactory {
    private var data: String? = null
    private var data1: String? = null

    constructor(data: String?,data1: String?) : super() {
        this.data = data
        this.data1 = data1
    }

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return if (className == RecommendFragment::class.java.name) {
            RecommendFragment(data)
        } else super.instantiate(classLoader, className)
    }

    // 添加一个方法来获取Fragment实例
    fun getRecommendFragment(): RecommendFragment {
        return RecommendFragment(data)
    }

    fun getFocusFragment(): FocusFragment {
        return FocusFragment(data1)
    }

}