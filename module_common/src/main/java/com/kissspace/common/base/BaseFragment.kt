package com.kissspace.common.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/2 15:14
 * @Description:
 *
 */
abstract class BaseFragment(layoutId: Int) : Fragment(layoutId) {
    lateinit var mActivity: AppCompatActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as AppCompatActivity
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(savedInstanceState)
        createDataObserver()
    }

    /**
     * 初始化UI
     */
    abstract fun initView(savedInstanceState: Bundle?)

    /**
     * 监听数据源
     */
    open fun createDataObserver() {

    }


    override fun onDestroy() {
        super.onDestroy()
    }


}