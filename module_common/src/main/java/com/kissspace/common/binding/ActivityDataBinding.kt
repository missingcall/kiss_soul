@file:JvmName("ActivityDataBinding")

package com.kissspace.common.binding

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity

fun <T : ViewDataBinding> AppCompatActivity.dataBinding(): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    bind<T>(getContentView()).also {
        it.lifecycleOwner = this@dataBinding
    }
}

fun <T : ViewDataBinding> AppCompatActivity.withDataBinding(withBinding: (binding: T) -> Unit) {
    val binding = bind<T>(getContentView()).also {
        it.lifecycleOwner = this@withDataBinding
    }
    withBinding(binding)
}

private fun <T : ViewDataBinding> bind(view: View): T = DataBindingUtil.bind(view)!!
private fun AppCompatActivity.getContentView(): View {
    return checkNotNull(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)) {
        "Call setContentView or Use Activity's secondary constructor passing layout res id."
    }
}
