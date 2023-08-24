package com.kissspace.webview.jsbridge

import android.webkit.JavascriptInterface
import com.kissspace.util.logE

  class CommonJsBridge(private val block: (String,Any?) -> Unit) {

    @JavascriptInterface
    fun closeWeb() {
        logE("closeWeb")
        block(JSName.JSNAME_CLOSE_WEB, null)
    }

    @JavascriptInterface
    fun closeController() {
        block(JSName.JSNAME_FINISH, null)
    }

    @JavascriptInterface
    fun closeEffect(isShowWater:Boolean) {
        block(JSName.JSNAME_IS_WATER, isShowWater)
    }
    @JavascriptInterface
    fun getCoin() {
        block(JSName.JSNAME_GET_COIN,null)
    }

    //聊天
    @JavascriptInterface
    fun goConversation(userId:String) {
        block(JSName.JSNAME_GoConversation,userId)
    }

    @JavascriptInterface
    fun goHomePage(userId:String) {
        block(JSName.JSNAME_GoHomePage,userId)
    }
    @JavascriptInterface
    fun followToRoom(param:String) {
        logE("param--userId"+param)
        block(JSName.JSNAME_followToRoom,param)
    }

    @JavascriptInterface
    fun jumpH5UserIdentity(param:String) {
        block(JSName.JSNAME_jumpH5UserIdentity,param)
    }

    @JavascriptInterface
    fun showPayDialogFragment() {
        block(JSName.JSNAME_showPayDialogFragment,null)
    }

      @JavascriptInterface
      fun goWearHeaddress() {
          block(JSName.JSNAME_goWearHeaddress,null)
      }

      @JavascriptInterface
      fun showGiftPopup() {
          block(JSName.JSNAME_showGiftPopup,null)
      }
}

object JSName {
    const val JSNAME_CLOSE_WEB = "closeWeb"

    const val JSNAME_IS_WATER = "closeEffect"

    const val JSNAME_FINISH = "closeController"

    const val JSNAME_GET_COIN = "getCoin"

    const val JSNAME_GoHomePage= "goHomePage"

    const val JSNAME_GoConversation= "goConversation"

    const val JSNAME_followToRoom= "followToRoom"

    const val JSNAME_jumpH5UserIdentity= "jumpH5UserIdentity"

    const val JSNAME_showPayDialogFragment= "showPayDialogFragment"

    //佩戴头像
    const val JSNAME_goWearHeaddress= "goWearHeaddress"

    //显示礼物
    const val JSNAME_showGiftPopup= "showGiftPopup"


}