@file:JvmName("FragmentDataBinding")

package com.kissspace.common.binding

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.kissspace.module_common.R
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T : ViewDataBinding> Fragment.dataBinding(): ReadOnlyProperty<Fragment, T> {
    return object : ReadOnlyProperty<Fragment, T> {
        override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
            (requireView().getTag(R.id.data_binding_tag) as? T)?.let { return it }
            return bind<T>(requireView()).also {
                it.lifecycleOwner = thisRef.viewLifecycleOwner
                it.root.setTag(R.id.data_binding_tag, it)
            }
        }

        private fun <T : ViewDataBinding> bind(view: View): T = DataBindingUtil.bind(view)!!
    }
}



