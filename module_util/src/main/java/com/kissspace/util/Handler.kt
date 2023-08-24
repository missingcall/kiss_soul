package com.kissspace.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.ConcurrentHashMap

internal val handler = Handler(Looper.getMainLooper())
internal val map = ConcurrentHashMap<Any, ConcurrentHashMap<String, Runnable>>()

@Synchronized
fun removeRunnable(
    any: Any, f: (() -> Unit)? = null
) {
    //L.v("XmRunnable remove $any $f from $map")
    if (f == null) {
        map[any]?.forEach {
            handler.removeCallbacks(it.value)
        }
        map.remove(any)
    } else {
        map[any]?.iterator()?.let {
            while (it.hasNext()) {
                val next = it.next()
                if (next.key == f.xmUniqueTagWith(any)) {
                    handler.removeCallbacks(next.value)
                    it.remove()
                }
            }
        }
        if (map[any].isNullOrEmpty()) {
            map.remove(any)
        }
    }
}

@Synchronized
fun put(
    any: Any, delayMillis: Long? = null, f: () -> Unit
) {
    val r = Runnable {
        try {
            f.invoke()
        } finally {
            removeRunnable(any, f)
        }
    }
    map[any] = (map[any] ?: ConcurrentHashMap()).apply {
        if (delayMillis != null) handler.postDelayed(r, delayMillis) else handler.post(r)
        put(f.xmUniqueTagWith(any), r)
    }
}


/**
 * activity和fragment在destroy的时候会remove任务f，其它的需要自己remove
 */
fun Any.postRunnable(
    needRemove: Boolean = false, f: () -> Unit
) {
    if (needRemove) {
        removeRunnable(this, f)
    }
    put(this, null, f)
}

/**
 * activity和fragment在destroy的时候会remove任务f，其它的需要自己remove
 */
fun Any.postDelay(
    delayMillis: Long, needRemove: Boolean = false, f: () -> Unit
) {
    if (needRemove) {
        removeRunnable(this, f)
    }
    put(this, delayMillis, f)
}

fun (() -> Unit).xmUniqueTagWith(any: Any) = "${any.hashCode()}${this.javaClass}"

val isMainThread: Boolean get() = Looper.getMainLooper().thread == Thread.currentThread()

inline val <T : Any> T.djsUniqueTag: String
    get() = javaClass.simpleName + hashCode()


//@ExperimentalContracts
inline fun <T> T.runOnUi(crossinline block: T.() -> Unit) {
//    contract {
//        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
//    }
    return if (Thread.currentThread() == Looper.getMainLooper().thread) {
        block(this)
    } else {
        this?.postRunnable {
            block(this)
        } ?: Unit
    }
}