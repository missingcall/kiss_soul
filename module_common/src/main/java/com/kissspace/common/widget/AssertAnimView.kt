package com.kissspace.common.widget

import android.content.Context
import android.util.AttributeSet
import com.kissspace.common.util.getAssertMP4Path
import com.kissspace.module_common.R
import com.kissspace.util.logE
import com.kissspace.util.withStyledAttributes
import com.tencent.qgame.animplayer.AnimView
import com.tencent.qgame.animplayer.util.ScaleType
import java.io.File

/**
 *@author: adan
 *@date: 2023/8/3
 *@Description:
 */
class AssertAnimView : AnimView{

    private var mUrl: String? = null

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        setScaleType(ScaleType.FIT_XY)
        withStyledAttributes(attributeSet, R.styleable.AssertAnimView) {
            mUrl = getString(R.styleable.AssertAnimView_url)
        }
        play()
    }

    private fun playAnimView() {
        setLoop(Int.MAX_VALUE)
        mUrl?.let {
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

    private fun play(){
        postDelayed({
            playAnimView()
        },500)
    }

    fun startPlay(){
        playAnimView()
    }
}