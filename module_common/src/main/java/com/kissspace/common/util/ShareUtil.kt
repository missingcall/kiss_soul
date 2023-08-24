package com.kissspace.common.util

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.umeng.socialize.ShareAction
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import com.umeng.socialize.media.UMImage
import com.umeng.socialize.media.UMWeb
import com.kissspace.common.config.ConstantsKey
import com.kissspace.util.logE

/**
 * @Author gaohangbo
 * @Date 2023/2/16 16:15.
 * @Describe
 */
object ShareUtil {
    private var iwxapi //微信支付api
            : IWXAPI? = null

    private val lifecycleObserver = LifecycleEventObserver { source, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            logE("messageOnDestroy")
            mShareListener = null
        }
    }
    private var mShareListener: UMShareListener? = null

    fun shareWechat(roomName:String,description:String,roomUrl:String,roomCover:String,activity: Activity) {
        iwxapi = WXAPIFactory.createWXAPI(activity, null) //初始化微信api
        iwxapi?.registerApp(ConstantsKey.WECHAT_APPID)
        if (activity is ComponentActivity) {
            activity.lifecycle.addObserver(lifecycleObserver)
        }
        if (iwxapi?.isWXAppInstalled != true) {
            customToast("微信未安装")
            return
        }
        mShareListener = CustomShareListener(activity)
        val web = UMWeb(roomUrl)
        web.title =roomName
        web.setThumb(UMImage(activity, roomCover))
        web.description = description
        ShareAction(activity).withMedia(web)
            .setPlatform(SHARE_MEDIA.WEIXIN)
            .setCallback(mShareListener).share()
    }

    fun shareWechatFriend(roomName:String,roomUrl: String,roomCover:String,activity: Activity) {
        iwxapi = WXAPIFactory.createWXAPI(activity, null) //初始化微信api
        iwxapi?.registerApp(ConstantsKey.WECHAT_APPID)
        if (activity is ComponentActivity) {
            activity.lifecycle.addObserver(lifecycleObserver)
        }
        if (iwxapi?.isWXAppInstalled != true) {
            customToast("微信未安装")
            return
        }
        mShareListener = CustomShareListener(activity)
        val web = UMWeb(roomUrl)
        web.title =roomName
        web.setThumb(UMImage(activity, roomCover))
        ShareAction(activity).withMedia(web)
            .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)
            .setCallback(mShareListener).share()
    }
    class CustomShareListener(context: Context) : UMShareListener {

        override fun onStart(platform: SHARE_MEDIA) {

        }

        override fun onResult(platform: SHARE_MEDIA) {
            if (platform.name == "WEIXIN_FAVORITE") {
//                toast("$platform 收藏成功啦")
            } else {
                if (platform != SHARE_MEDIA.MORE && platform != SHARE_MEDIA.SMS && platform != SHARE_MEDIA.EMAIL && platform != SHARE_MEDIA.FLICKR && platform != SHARE_MEDIA.FOURSQUARE && platform != SHARE_MEDIA.TUMBLR && platform != SHARE_MEDIA.POCKET && platform != SHARE_MEDIA.PINTEREST && platform != SHARE_MEDIA.INSTAGRAM && platform != SHARE_MEDIA.GOOGLEPLUS && platform != SHARE_MEDIA.YNOTE && platform != SHARE_MEDIA.EVERNOTE) {
//                 toast("$platform 分享成功啦")
                }
            }
        }

        override fun onError(platform: SHARE_MEDIA, t: Throwable) {
            if (platform != SHARE_MEDIA.MORE && platform != SHARE_MEDIA.SMS && platform != SHARE_MEDIA.EMAIL && platform != SHARE_MEDIA.FLICKR && platform != SHARE_MEDIA.FOURSQUARE && platform != SHARE_MEDIA.TUMBLR && platform != SHARE_MEDIA.POCKET && platform != SHARE_MEDIA.PINTEREST && platform != SHARE_MEDIA.INSTAGRAM && platform != SHARE_MEDIA.GOOGLEPLUS && platform != SHARE_MEDIA.YNOTE && platform != SHARE_MEDIA.EVERNOTE) {
//                toast("$platform 分享失败啦")
                logE("t"+t.message)
            }
        }

        override fun onCancel(platform: SHARE_MEDIA) {
//            toast("$platform 分享取消了")
        }
    }

}