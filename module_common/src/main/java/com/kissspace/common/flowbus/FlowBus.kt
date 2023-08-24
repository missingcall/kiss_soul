package com.kissspace.common.flowbus

import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/14 14:59
 * @Description: 利用全局sharedFlow实现一个简易的EventBus
 *
 */
object FlowBus {
    private val flowEvents = ConcurrentHashMap<String, MutableSharedFlow<Event?>>()

    fun getFlow(key: String): MutableSharedFlow<Event?> {
        return flowEvents[key] ?: MutableSharedFlow<Event?>().also { flowEvents[key] = it }
    }

    fun CoroutineScope.post(event: Event, delay: Long = 0) {
        launch {
            delay(delay)
            getFlow(event.javaClass.simpleName).emit(event)
        }
    }

    fun post(event: Event, delay: Long = 0) {
        MainScope().launch {
            delay(delay)
            getFlow(event.javaClass.simpleName).emit(event)
        }
    }

    //做了一点改造，加了Lifecycle.State参数可以更精细地将控制接受到事件时的执行时机
    inline fun <reified T : Event?> observerEvent(
        lifecycleOwner: LifecycleOwner,
        minState: Lifecycle.State = Lifecycle.State.CREATED,
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
        crossinline onReceived: (T) -> Unit
    ) = lifecycleOwner.lifecycleScope.launch(dispatcher) {
        getFlow(T::class.java.simpleName).collect {
            it.let { event ->
                lifecycleOwner.lifecycle.whenStateAtLeast(minState) {
                    if (event is T) onReceived(event)
                }
            }

        }
    }

    inline fun <reified T : Event?> observerGlobalEvent(
        crossinline onReceived: (T) -> Unit
    ) = GlobalScope.launch(Dispatchers.Main) {
        getFlow(T::class.java.simpleName).collect {
            it.let { event ->
                if (event is T) onReceived(event)
            }
        }
    }

}