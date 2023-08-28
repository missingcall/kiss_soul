package com.kissspace.common.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.kissspace.common.util.getAssertMP4Path
import com.kissspace.module_common.R
import com.kissspace.util.withStyledAttributes
import com.tencent.qgame.animplayer.AnimView
import com.tencent.qgame.animplayer.util.ScaleType
import java.io.File

/**
 *@author: adan
 *@date: 2023/8/3
 *@Description:
 */
class LifeCycleAnimView : AnimView, LifecycleEventObserver {

    private var mPagUrl: String? = null

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        setScaleType(ScaleType.FIT_XY)
        withStyledAttributes(attributeSet, R.styleable.AssertAnimView) {
            mPagUrl = getString(R.styleable.AssertAnimView_url)
        }
        bindLife()
    }

    private fun bindLife() {
        if (context  is AppCompatActivity){
            val appCompatActivity = context as AppCompatActivity
            appCompatActivity.lifecycle.addObserver(this)
        }
    }

    private fun playAnimView() {
        setLoop(Int.MAX_VALUE)
        mPagUrl?.let {
            getAssertMP4Path(it){path,isAssets->
                if (isAssets){
                    val assets = this.context.assets
                    this.startPlay(assets,it)
                }else{
                    this.startPlay(File(path))
                }
            }
        }
    }

    private fun startPlay(){
        playAnimView()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when(event){
            Lifecycle.Event.ON_RESUME -> {
               if (!isRunning()){
                   startPlay()
               }
            }
            Lifecycle.Event.ON_PAUSE -> {
//                stopPlay()
            }
            else -> {

            }
        }
    }

}