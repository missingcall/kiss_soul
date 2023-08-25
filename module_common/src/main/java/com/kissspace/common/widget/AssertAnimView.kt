package com.kissspace.common.widget

import android.content.Context
import android.util.AttributeSet
import com.kissspace.module_common.R
import com.kissspace.util.withStyledAttributes
import com.tencent.qgame.animplayer.AnimView
import com.tencent.qgame.animplayer.util.ScaleType

/**
 *@author: adan
 *@date: 2023/8/3
 *@Description:
 */
class AssertAnimView : AnimView{

    private var mPagUrl: String? = null

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        setScaleType(ScaleType.FIT_XY)
        withStyledAttributes(attributeSet, R.styleable.AssertAnimView) {
            mPagUrl = getString(R.styleable.AssertAnimView_url)
        }
        play()
    }

    private fun playAnimView() {
        setLoop(Int.MAX_VALUE)
        val assets = this.context.assets
        mPagUrl?.let {
            this.startPlay(assets,it)
        }
    }

    private fun play(){
        postDelayed({
            playAnimView()
        },500)
    }

    fun startPlay(){
        playAnimView()
    }
}