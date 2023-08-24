package com.kissspace.mine.widget

open class SimpleObjectPool<T> @JvmOverloads constructor(type: Class<T>?, protected var size: Int = 8) {
    private var objsPool: Array<T?>
    private var curPointer = -1

    init {
        objsPool = java.lang.reflect.Array.newInstance(type, size) as Array<T?>
    }

    @Synchronized
    fun get(): T? {
        if (curPointer == -1 || curPointer > objsPool.size) return null
        val obj = objsPool[curPointer]
        objsPool[curPointer] = null
        curPointer--
        return obj
    }

    @Synchronized
    fun put(t: T): Boolean {
        if (curPointer == -1 || curPointer < objsPool.size - 1) {
            curPointer++
            objsPool[curPointer] = t
            return true
        }
        return false
    }

    fun clearPool() {
        for (i in objsPool.indices) {
            objsPool[i] = null
        }
        curPointer = -1
    }
}