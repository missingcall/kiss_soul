package com.kissspace.common.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

/**
 * @Author gaohangbo
 * @Date 2023/1/13 13:47.
 * @Describe
 */
open class DJSLiveData<T> : MediatorLiveData<T>() {

    companion object {
        internal fun <T> unsafeDJSLiveData() = DJSLiveData<T>()
    }


    /**
     * 添加多个LiveData监测，注意观察者里不包含源数据
     */
    fun addSources(vararg sources: LiveData<*>?, onChanged: () -> Unit) {
        sources.forEach { item ->
            item?.let {
                addSource(it) { onChanged.invoke() }
            }
        }
    }
}