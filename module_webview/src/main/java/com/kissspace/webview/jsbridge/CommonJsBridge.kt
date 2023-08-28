package com.kissspace.webview.jsbridge

import android.webkit.JavascriptInterface
import com.kissspace.util.logE

class CommonJsBridge(private val block: (String, Any, Any?) -> Unit) {

    @JavascriptInterface
    fun closeWeb() {
        logE("closeWeb")
        block(JSName.JSNAME_CLOSE_WEB,"", null)
    }

    @JavascriptInterface
    fun closeController() {
        block(JSName.JSNAME_FINISH, "",null)
    }

    @JavascriptInterface
    fun closeEffect(isShowWater: Boolean) {
        block(JSName.JSNAME_IS_WATER, "",isShowWater)
    }

    @JavascriptInterface
    fun getCoin() {
        block(JSName.JSNAME_GET_COIN,"", null)
    }

    //聊天
    @JavascriptInterface
    fun goConversation(userId: String) {
        block(JSName.JSNAME_GoConversation,"", userId)
    }

    @JavascriptInterface
    fun goHomePage(userId: String) {
        block(JSName.JSNAME_GoHomePage, userId,"")
    }

    @JavascriptInterface
    fun followToRoom(param: String) {
        logE("param--userId" + param)
        block(JSName.JSNAME_followToRoom, param,"")
    }

    @JavascriptInterface
    fun jumpH5UserIdentity(param: String) {
        block(JSName.JSNAME_jumpH5UserIdentity, param,"")
    }

    @JavascriptInterface
    fun showPayDialogFragment() {
        block(JSName.JSNAME_showPayDialogFragment, "",null)
    }

    @JavascriptInterface
    fun goWearHeaddress() {
        block(JSName.JSNAME_goWearHeaddress, "",null)
    }

    @JavascriptInterface
    fun showGiftPopup() {
        block(JSName.JSNAME_showGiftPopup,"", null)
    }

    @JavascriptInterface
    fun sendDynamic() {
        block(JSName.JSNAME_sendDynamic, "",null)
    }

    @JavascriptInterface
    fun audioPlay(path:String) {
        block(JSName.JSNAME_audioPlay, path,"")
    }

    @JavascriptInterface
    fun reportAFeed(dynamicId:String,userId:String) {
        block(JSName.JSNAME_reportAFeed, dynamicId,userId)
    }

    @JavascriptInterface
    fun openPreviewImage(current:Int,urls:Array<String>) {
        block(JSName.JSNAME_openPreviewImage,current.toString(),urls)
    }

    @JavascriptInterface
    fun jumpDynamicDetail(dynamicId:String) {
        block(JSName.JSNAME_jumpDynamicDetail,dynamicId,null)
    }

    @JavascriptInterface
    fun jumpInteractiveMessages() {
        block(JSName.JSNAME_jumpInteractiveMessages,"",null)
    }
    @JavascriptInterface
    fun navigateBackPage() {
        block(JSName.JSNAME_navigateBackPage,"",null)
    }
}

object JSName {
    const val JSNAME_CLOSE_WEB = "closeWeb"

    const val JSNAME_IS_WATER = "closeEffect"

    const val JSNAME_FINISH = "closeController"

    const val JSNAME_GET_COIN = "getCoin"

    const val JSNAME_GoHomePage = "goHomePage"

    const val JSNAME_GoConversation = "goConversation"

    const val JSNAME_followToRoom = "followToRoom"

    const val JSNAME_jumpH5UserIdentity = "jumpH5UserIdentity"

    const val JSNAME_showPayDialogFragment = "showPayDialogFragment"

    //佩戴头像
    const val JSNAME_goWearHeaddress = "goWearHeaddress"

    //显示礼物
    const val JSNAME_showGiftPopup = "showGiftPopup"

    //发送动态
    const val JSNAME_sendDynamic = "sendDynamic"

    //音频播放
    const val JSNAME_audioPlay = "audioPlay"

    //预览动态图片
    const val JSNAME_openPreviewImage = "openPreviewImage"

    //举报用户
    const val JSNAME_reportAFeed = "reportAFeed"

    //跳转到动态详情
    const val JSNAME_jumpDynamicDetail = "jumpDynamicDetail"

    //跳转到互动消息页
    const val JSNAME_jumpInteractiveMessages = "jumpInteractiveMessages"

    //返回到上一个页面
    const val JSNAME_navigateBackPage = "navigateBackPage"


}