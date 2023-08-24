package com.kissspace.util

import kotlin.reflect.KClass

inline fun <reified T> isArrayListOfType(anyObject: Any): Boolean {
    if (anyObject !is ArrayList<*>) {
        return false
    }
    val elementType = (T::class as KClass<*>).java
    return anyObject.all { elementType.isInstance(it) }
}