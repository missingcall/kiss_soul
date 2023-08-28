package com.kissspace.common.util

import android.Manifest
import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.constant.TimeConstants
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.didi.drouter.api.DRouter
import com.drake.brv.utils.bindingAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kongzue.dialogx.impl.ActivityLifecycleImpl
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.auth.AuthService
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.permissionx.guolindev.PermissionX
import com.kissspace.common.AdolescentTimeBean
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.BaseUrlConfig
import com.kissspace.common.config.Constants
import com.kissspace.common.config.Constants.TypeFaceRecognition
import com.kissspace.common.config.Constants.TypeSendSms
import com.kissspace.common.model.RoomListBean
import com.kissspace.common.model.UserExtensionModel
import com.kissspace.common.model.firstRecharge.BlindBoxModelItem
import com.kissspace.common.model.firstRecharge.CommonGiftModel
import com.kissspace.common.model.firstRecharge.GiftModel
import com.kissspace.common.model.firstRecharge.HeadGearModel
import com.kissspace.common.model.firstRecharge.MountModel
import com.kissspace.common.provider.IRoomProvider
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.util.format.Format
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.mmkv.clearMMKV
import com.kissspace.common.widget.LoadingDialog
import com.kissspace.common.widget.VerifyNameDialog
import com.kissspace.module_common.R
import com.kissspace.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONArray
import org.json.JSONObject
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.lang.reflect.Type
import java.math.RoundingMode
import java.net.URL
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.collections.isNotEmpty
import kotlin.system.exitProcess
import kotlin.text.isNotBlank


/**
 * 倒计时flow实现
 * @param total 总时间
 * @param scope 作用域
 * @param timeUnit 倒计时时间单位
 * @param reverse 正计时还是倒计时
 * @param onTick 每次倒计时回调
 * @param onStart 开始回调
 * @param onFinish 结束回调
 */
fun countDown(
    total: Long,
    step: Long = 1000,
    scope: CoroutineScope,
    timeUnit: TimeUnit = TimeUnit.SECONDS,
    reverse: Boolean = false,
    onTick: ((Long) -> Unit)? = null,
    onStart: (() -> Unit)? = null,
    onFinish: (() -> Unit)? = null,
): Job {
    return flow {
        if (reverse) {
            for (i in 0 until total) {
                emit(i)
                delay(step)
            }
        } else {
            for (i in total downTo 0) {
                emit(i)
                delay(step)
            }
        }
    }.flowOn(Dispatchers.Main).onStart { onStart?.invoke() }.onCompletion { onFinish?.invoke() }
        .onEach { onTick?.invoke(it) }.launchIn(scope)
}


/**
 * 判断两个集合内容是否一致
 */
fun areCollectionsDifferent(oldList: Collection<Any>, newList: Collection<Any>): Boolean {
    if (oldList.size != newList.size) {
        return true
    }
    return !oldList.containsAll(newList)
}

fun String.isJSON(): Boolean {
    if ("{}" == this || (this.length > 2 && this.startsWith("{")) && this.endsWith("}")) {
        try {
            JSONObject(this)
        } catch (e: java.lang.Exception) {
            e.printStackTrace();
            return false
        }
        return true
    }
    return false
}

fun String.isJSONArray(): Boolean {
    if ("[]" == this || (this.length > 2 && this.startsWith("[{")) && this.endsWith("}]")) {
        try {
            JSONArray(this)
        } catch (e: Exception) {
            e.printStackTrace();
            return false
        }
        return true
    }
    return false
}


fun String.isInt(): Boolean {
    try {
        this.toInt()
    } catch (e: Exception) {
        e.printStackTrace();
        return false
    }
    return true
}

fun String.isDouble(): Boolean {
    try {
        this.toDouble()
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
    return true
}

fun String.isFloat(): Boolean {
    try {
        this.toFloat()
    } catch (e: Exception) {
        e.printStackTrace();
        return false
    }
    return true
}

fun isMainProcess(context: Context?): Boolean {
    if (context == null) {
        return false
    }
    val packageName: String = context.packageName
    val processName = getProcessName(context)
    return packageName == processName
}

private fun getProcessName(context: Context): String? {
    var processName: String? = getProcessFromFile()
    if (processName == null) {
        // 如果装了xposed一类的框架，上面可能会拿不到，回到遍历迭代的方式
        processName = getProcessNameByAM(context)
    }
    return processName
}

private fun getProcessFromFile(): String? {
    var reader: BufferedReader? = null
    return try {
        val pid = Process.myPid()
        val file = "/proc/$pid/cmdline"
        reader = BufferedReader(InputStreamReader(FileInputStream(file), "iso-8859-1"))
        var c: Int
        val processName = StringBuilder()
        while (reader.read().also { c = it } > 0) {
            processName.append(c.toChar())
        }
        processName.toString()
    } catch (e: java.lang.Exception) {
        null
    } finally {
        if (reader != null) {
            try {
                reader.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

private fun getProcessNameByAM(context: Context): String? {
    var processName: String? = null
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager ?: return null
    while (true) {
        val plist = am.runningAppProcesses
        if (plist != null) {
            for (info in plist) {
                if (info.pid == Process.myPid()) {
                    processName = info.processName
                    break
                }
            }
        }
        if (!TextUtils.isEmpty(processName)) {
            return processName
        }
        try {
            Thread.sleep(100L) // take a rest and again
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
        }
    }
}

/**
 * 调用粘贴板复制
 */
fun copyClip(string: String, action: (() -> Unit)? = null) {
    try {
        val mClipData = ClipData.newPlainText("Label", string)
        (application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).run {
            setPrimaryClip(mClipData)
            customToast("复制成功")
            action?.invoke()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 解析自定义消息
 */
inline fun <reified T> parseCustomMessage(message: Any): T {
    val type = object : TypeToken<T>() {}.type
    return GsonUtils.fromJson(message.toString(), type)
}

inline fun <reified T> fromJson(json: String): T {
    val type = object : TypeToken<T>() {}.type
    return GsonUtils.fromJson(json, type)
}

inline fun parseUserExtension(data: String?): UserExtensionModel? {
    val type = object : TypeToken<UserExtensionModel>() {}.type
    return if (data.isNullOrEmpty()) null else GsonUtils.fromJson(data, type)
}

fun openPictureSelector(
    context: Context,
    max: Int = 1, isCrop: Boolean = false, block: (List<String>?) -> Unit
) {
    PictureSelector.create(context).openGallery(SelectMimeType.TYPE_IMAGE).apply {
        isDisplayCamera(false)
        setMaxSelectNum(max)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        if (isCrop) {
            setCropEngine(UCropEngine())
        }
        setImageEngine(CoilEngine()).isGif(false)
        setCompressEngine(CompressFileEngine { context, source, call ->
            Luban.with(context).load(source).ignoreBy(100)
                .setCompressListener(object : OnNewCompressListener {
                    override fun onStart() {
                    }

                    override fun onSuccess(source: String?, compressFile: File?) {
                        call?.onCallback(source, compressFile?.absolutePath)
                    }

                    override fun onError(source: String?, e: Throwable?) {
                        call?.onCallback(source, null)
                    }

                }).launch()
        }).forResult(object : OnResultCallbackListener<LocalMedia> {
            override fun onResult(result: ArrayList<LocalMedia>?) {
                val list = result?.map {
                    if (TextUtils.isEmpty(it.compressPath)) {
                        it.realPath
                    } else {
                        it.compressPath
                    }
                }
                block(list)
            }

            override fun onCancel() {
            }
        })
    }
}
/**
 * url转drawable
 */

fun openCamera(isCrop: Boolean = false, block: (String?) -> Unit) {
    checkCameraPermission {
        PictureSelector.create(topActivity as AppCompatActivity)
            .openCamera(SelectMimeType.ofImage()).apply {
                if (isCrop) {
                    setCropEngine(UCropEngine())
                }
                forResult(object : OnResultCallbackListener<LocalMedia> {
                    override fun onResult(result: java.util.ArrayList<LocalMedia>?) {
                        if (isCrop) {
                            block.invoke(result?.get(0)?.cutPath)
                        } else {
                            block.invoke(result?.get(0)?.realPath)
                        }
                    }

                    override fun onCancel() {
                    }
                })
            }
    }
}


private fun checkCameraPermission(callBack: () -> Unit) {
    PermissionX.init(topActivity as AppCompatActivity).permissions(Manifest.permission.CAMERA)
        .onExplainRequestReason { scope, deniedList ->
            val message =
                "为了您能正常体验【拍照】功能，kiss空间需向你申请摄像头权限"
            scope.showRequestReasonDialog(deniedList, message, "确定", "取消")
        }
        .explainReasonBeforeRequest()
        .request { allGranted, _, _ ->
            if (allGranted) {
                callBack()
            } else {
                ToastUtils.showShort("请打开摄像头权限")
            }
        }
}

private fun checkStoragePermission(callBack: () -> Unit) {
    PermissionX.init(topActivity as AppCompatActivity).permissions(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
        .onExplainRequestReason { scope, deniedList ->
            val message =
                "为了您能正常体验【上传图片】功能，kiss空间需向你申请访问存储权限"
            scope.showRequestReasonDialog(deniedList, message, "确定", "取消")
        }
        .explainReasonBeforeRequest()
        .request { allGranted, _, _ ->
            if (allGranted) {
                callBack()
            } else {
                ToastUtils.showShort("请打开相关权限")
            }
        }
}


fun Long.parse2CountDown(): String {
    if (this <= 0) {
        return "00:00"
    }
    return TimeUtils.millis2String(this, "mm:ss")
}

fun formatNum(value: Double, roundingMode: RoundingMode = RoundingMode.FLOOR): String {
    //向下取整数
    return if ((value >= 100000000)) {
        "${Format.E_EE.format(value / 100000000)}亿"
    } else if (value >= 10000) {
        "${Format.E_EE.format(value / 10000)}万"
    } else {
        Format.E_EE.format(value)
    }
}


fun formatNumCoin(value: Double): String {
    //向下取整数,保留2位小数
    return if ((value >= 100000000)) {
        "${Format.E_EE.format(value / 100000000)}亿"
    } else {
        Format.E_EE.format(value)
    }
}

fun formatNumIntegral(value: Double): String {
    //向上取整数（四舍五入)不保留小数
    return if ((value >= 100000000)) {
        "${Format.E_UP.format(value / 100000000)}亿"
    } else {
        Format.E_UP.format(value)
    }
}


//fun Long.formatBalance(): String {
//    return if (this < 100000000) {
//        this.toString()
//    } else {
//        this.formatNum()
//    }
//}

inline fun <reified T> RecyclerView.getMutable(): MutableList<T> {
    return bindingAdapter.mutable as MutableList<T>
}


fun oldAccountExit(block: () -> Unit) {
    if (MMKVProvider.loginResult != null) {
        DRouter.build(IRoomProvider::class.java).getService().loginOutRoom {
            block.invoke()
        }
    }
}


fun loginOut(logoutRoom: Boolean = true) {
    if (logoutRoom) {
        //先退出房间
        DRouter.build(IRoomProvider::class.java).getService().loginOutRoom {
            //退出云信
            signOut()
        }

    } else {
        //退出云信
        signOut()
    }
}

private fun signOut() {
    NIMClient.getService(AuthService::class.java).logout()
    //清除MMKV
    clearMMKV()
    //切换创建账号不跳转登录页
    jump(RouterPath.PATH_LOGIN)
}


private var loadingDialog: WeakReference<LoadingDialog>? = null

/**
 * 显示loading弹窗
 */
fun showLoading(context: String = "加载中") {
    if (loadingDialog == null || loadingDialog?.get() == null) {
        loadingDialog = WeakReference(LoadingDialog(context))
    }
    loadingDialog?.get()?.show((topActivity as AppCompatActivity).supportFragmentManager)
}

/**
 * 关闭loading弹窗
 */
fun hideLoading() {
    loadingDialog?.get()?.dismiss()
}


fun showAuthenticationDialog(block: () -> Unit) {
    ActivityLifecycleImpl.getTopActivity()?.let {
        VerifyNameDialog(it) {
            block.invoke()
        }.show()
    }
}


fun handleSchema(schema: String?) {
    if (schema.isNullOrEmpty()) {
        return
    }
    if (schema.startsWith("djs://pages/room")) {
        val uri = Uri.parse(schema)
        val roomId = uri.getQueryParameter("roomId")
        val roomType = uri.getQueryParameter("roomType")
        val stochastic = uri.getQueryParameter("stochastic")
        jumpRoom(crId = roomId, roomType = roomType, stochastic = stochastic)
    } else if (schema.startsWith(RouterPath.PATH_MAIN)) {
        val uri = Uri.parse(schema)
        val index = uri.getQueryParameter("index")
        try {
            logE("index__" + index?.toInt().orZero())
            jump(RouterPath.PATH_MAIN, "index" to index?.toInt().orZero())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    } else if (schema.startsWith(RouterPath.PATH_WEBVIEW)) {
        val index = schema.indexOf("url=")
        val url: String? = if (index != -1) {
            schema.substring(index + 4, schema.length)
        } else {
            val uri = Uri.parse(schema)
            uri.getQueryParameter("url")
        }
        logE("PATH_WEBVIEW_url$url")
        jump(
            RouterPath.PATH_WEBVIEW,
            "url" to "${url}?token=${MMKVProvider.loginResult?.token}",
            "showTitle" to true
        )
    } else {
        jump(path = schema)
    }
}

/**
 *  跳转房间
 *  @param crId 房间id
 *  @param roomType 房间类型
 */
fun jumpRoom(
    crId: String? = null,
    roomType: String? = null,
    stochastic: String? = null,
    userId: String? = null,
    roomList: MutableList<RoomListBean> = mutableListOf()
) {
    val service = DRouter.build(IRoomProvider::class.java).getService()
    service.jumpRoom(crId, roomType, stochastic, userId, roomList)
}


/**

如果小于 1 秒钟内，显示刚刚
如果在 1 分钟内，显示 XXX秒前
如果在 1 小时内，显示 XXX分钟前
如果在 1 小时外的今天内，显示今天15:32
如果是昨天的，显示昨天15:32
其余显示，2016-10-15 13:12:11
时间不合法的情况全部日期和时间信息，如星期六 十月 27 14:21:20 CST 2007
 */
fun getFriendlyTimeSpanByNow(millis: Long): String {
    val now = System.currentTimeMillis()
    val span = now - millis
    if (span < TimeConstants.MIN) {
        return "刚刚"
    } else if (span < TimeConstants.HOUR) {
        return String.format(Locale.getDefault(), "%d分钟前", span / TimeConstants.MIN)
    }
    // 获取当天 00:00
    val wee = getWeeOfToday()
    return if (millis >= wee) {
        String.format("今天%tR", millis)
    } else if (millis >= wee - TimeConstants.DAY) {
        String.format("昨天%tR", millis)
    } else {
        String.format("%tF", millis) + " " + String.format("%tT", millis)
    }
}

private fun getWeeOfToday(): Long {
    val cal = Calendar.getInstance()
    cal[Calendar.HOUR_OF_DAY] = 0
    cal[Calendar.SECOND] = 0
    cal[Calendar.MINUTE] = 0
    cal[Calendar.MILLISECOND] = 0
    return cal.timeInMillis
}

fun getGiftTypeWithKey(giftInfo: String?): String {
    val type: Type = object : TypeToken<Map<String, Int>>() {}.type
    val countMap: Map<String, Long> = GsonUtils.fromJson(giftInfo, type)
    return countMap.entries.firstOrNull()?.key ?: ""
}


inline fun <reified T> getModels(value: String?): List<T>? {
    val typeToken: Type = object : TypeToken<List<T>?>() {}.type
    try {
        return GsonUtils.fromJson<List<T>?>(value, typeToken)
    } catch (exception: Exception) {
        logE("getModels" + exception.message)
    }
    return null
}

//获取首充所有礼物(不获取金币)
fun getFirstChargeAllGiftValue(giftInfo: String?): List<CommonGiftModel>? {
    val listCommonGiftModel = ArrayList<CommonGiftModel>()
    val type: Type = object : TypeToken<Map<String, Any?>>() {}.type
    val map: MutableMap<String, Any?> = GsonUtils.fromJson(giftInfo, type)
    for (entry in map) {
        logE(entry.key + "--->" + entry.value)
        when (entry.key) {
            Constants.GiftType.PROPS.type -> {
                // {"\"002\":[{\"giftId\":\"377d2c2376be07a359e627c7667f563b\",\"giftNum\":1,\"giftName\":\"生日蛋糕\",\"giftImg\":\"https://macrodjs-oos.oss-cn-hangzhou.aliyuncs.com/admin/gift/images/%E7%94%9F%E6%97%A5%E8%9B%8B%E7%B3%95-1675389045872.png\"},{\"giftId\":\"8bb9d30212997806e9bd4d3a85b50a9b\",\"giftNum\":1,\"giftName\":\"木马\",\"giftImg\":\"https://macrodjs-oos.oss-cn-hangzhou.aliyuncs.com/admin/gift/images/%E6%97%8B%E8%BD%AC%E6%9C%A8%E9%A9%AC-1672214371685.png\"},{\"giftId\":\"204\",\"giftNum\":1,\"giftName\":\"抖音一号\",\"giftImg\":\"https://macrodjs-oos.oss-cn-hangzhou.aliyuncs.com/admin/gift/images/%E9%9F%B3%E6%B4%BE_%E7%A4%BC%E7%89%A9%E5%9B%BE%20%2819%29-1673863091846.png\"}]}
                val value = GsonUtils.toJson(entry.value)
                val listGiftData: List<GiftModel> = getModels<GiftModel>(value).orEmpty()
                if (listGiftData.isNotEmpty()) {
                    val giftModel: GiftModel = listGiftData[0]
                    listCommonGiftModel.add(
                        CommonGiftModel(
                            icon = giftModel.giftImg,
                            name = giftModel.giftName,
                            number = giftModel.giftNum
                        )
                    )
                }
            }

            Constants.GiftType.MOUNT.type -> {
                //       [{"carId":"1","carIndate":"3","icon":"http:swdf","name":"坐骑"},{"carId":"2","carIndate":"7","icon":"http:swdf","name":"坐骑"}]
                val value = GsonUtils.toJson(entry.value)
                val listMountData: List<MountModel> = getModels<MountModel>(value).orEmpty()
                if (listMountData.isNotEmpty()) {
                    val mountModel: MountModel = listMountData[0]
                    listCommonGiftModel.add(
                        CommonGiftModel(
                            icon = mountModel.icon,
                            name = mountModel.name,
                            day = mountModel.carIndate
                        )
                    )
                }
            }

            Constants.GiftType.HEADGEAR.type -> {
                val value = GsonUtils.toJson(entry.value)
//             [{"headwearId":"1","headerIndate":"3","icon":"http:swdf","name":"头饰"},{"headwearId":"2","headerIndate":"7","icon":"http:swdf","name":"头饰"}]
                val listHeadGearModel: List<HeadGearModel> =
                    getModels<HeadGearModel>(value).orEmpty()
                if (listHeadGearModel.isNotEmpty()) {
                    listCommonGiftModel.add(
                        CommonGiftModel(
                            icon = listHeadGearModel[0].icon,
                            name = listHeadGearModel[0].name,
                            day = listHeadGearModel[0].headerIndate.toInt()
                        )
                    )
                }
            }

//            Constants.GiftType.COIN.type -> {
//                //  //金币
//                //  "006" : "100,0"
//                listCommonGiftModel.add(
//                    CommonGiftModel(
//                        icon = R.mipmap.common_icon_firstcharge_coin,
//                        name = "金币${Format.E.format(entry.value as Double) ?: 0}"
//                    )
//                )
//            }

            else -> {
                return null
            }
        }
    }
    return listCommonGiftModel
}

fun getFirstGiftTypeImgText(giftInfo: String): MutableList<Pair<Any?, String?>> {
    val type: Type = object : TypeToken<Map<String, Any>>() {}.type
    val params: MutableList<Pair<Any?, String?>> = mutableListOf()
    try {
        val countMap: Map<String, Any> = GsonUtils.fromJson(giftInfo, type)
        for ((k, v) in countMap) {
            logE("------$k -> $v")
            getGift(k, v)?.let { params.addAll(it) }
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
    return params

}

fun getGift(key: String?, value: Any?): MutableList<Pair<Any?, String?>>? {
    when (key) {
        Constants.GiftType.INTEGRAL.type -> {
            // "{\"001\":300}"
            val params: MutableList<Pair<Int?, String?>> = mutableListOf()
            params.add(
                Pair(
                    R.mipmap.common_icon_gift_integral, "积分x${(value as Double).toInt()}"
                )
            )
            return params.toMutableList()
        }

        Constants.GiftType.PROPS.type -> {
            // {"\"002\":[{\"giftId\":\"377d2c2376be07a359e627c7667f563b\",\"giftNum\":1,\"giftName\":\"生日蛋糕\",\"giftImg\":\"https://macrodjs-oos.oss-cn-hangzhou.aliyuncs.com/admin/gift/images/%E7%94%9F%E6%97%A5%E8%9B%8B%E7%B3%95-1675389045872.png\"},{\"giftId\":\"8bb9d30212997806e9bd4d3a85b50a9b\",\"giftNum\":1,\"giftName\":\"木马\",\"giftImg\":\"https://macrodjs-oos.oss-cn-hangzhou.aliyuncs.com/admin/gift/images/%E6%97%8B%E8%BD%AC%E6%9C%A8%E9%A9%AC-1672214371685.png\"},{\"giftId\":\"204\",\"giftNum\":1,\"giftName\":\"抖音一号\",\"giftImg\":\"https://macrodjs-oos.oss-cn-hangzhou.aliyuncs.com/admin/gift/images/%E9%9F%B3%E6%B4%BE_%E7%A4%BC%E7%89%A9%E5%9B%BE%20%2819%29-1673863091846.png\"}]}
            val params: MutableList<Pair<Any?, String?>> = mutableListOf()
            val listGiftModule: List<GiftModel>? = getJsonModel(value)
            if (listGiftModule != null) {
                var i = 0
                for (item in listGiftModule) {
                    if (i == 2) {
                        break
                    }
                    params.add(
                        Pair(item.giftImg, "${item.giftName}(${item.giftNum.toInt()})天")
                    )
                    i += 1
                }
            }
            return params
        }

        Constants.GiftType.MOUNT.type -> {
            // [{"carId":"1","carIndate":"3","icon":"http:swdf","name":"坐骑"},{"carId":"2","carIndate":"7","icon":"http:swdf","name":"坐骑"}]
            val listMountModel: List<MountModel>? = getJsonModel(value)
            val params: MutableList<Pair<Any?, String?>> = mutableListOf()
            if (listMountModel != null) {
                var i = 0
                for (item in listMountModel) {
                    if (i == 2) {
                        break
                    }
                    params.add(
                        Pair(
                            item.icon, "${item.name}(${item.carIndate}天)"
                        )
                    )
                    i += 1
                }
            }
            return params
        }

        Constants.GiftType.HEADGEAR.type -> {
            // [{"headwearId":"1","headerIndate":"3","icon":"http:swdf","name":"头饰"},{"headwearId":"2","headerIndate":"7","icon":"http:swdf","name":"头饰"}]
            val listHeadGearModel: List<HeadGearModel>? = getJsonModel(value)
            val params: MutableList<Pair<Any?, String?>> = mutableListOf()
            //最多取2个
            if (listHeadGearModel != null) {
                var i = 0
                for (item in listHeadGearModel) {
                    if (i == 2) {
                        break
                    }
                    params.add(
                        Pair(
                            item.icon, "${item.name}(${item.headerIndate.toInt()}天)"
                        )
                    )
                    i += 1
                }
            }
            return params
        }

        Constants.GiftType.BLIND_BOX.type -> {
            // "{\"001\":300}"
            val listBlindBoxModelItem: List<BlindBoxModelItem>? = getJsonModel(value)
            val params: MutableList<Pair<Any?, String?>> = mutableListOf()
            //最多取2个
            if (listBlindBoxModelItem != null) {
                var i = 0
                for (item in listBlindBoxModelItem) {
                    if (i == 2) {
                        break
                    }
                    params.add(Pair(R.mipmap.common_icon_reward, "盲盒x${item.boxNum.toInt()}"))
                    i += 1
                }
            }
            return params
        }

        Constants.GiftType.COIN.type -> {
            // "{\"001\":300}"
            val params: MutableList<Pair<Int?, String?>> = mutableListOf()
            params.add(
                Pair(
                    R.mipmap.common_icon_firstcharge_coin, "金币x${(value as Double).toInt()}"
                )
            )
            return params.toMutableList()

        }

        else -> {
            return null
        }
    }
}


inline fun <reified T> getJsonModel(value: Any?): List<T>? {
    val value = GsonUtils.toJson(value)
    val typeToken: Type = object : TypeToken<List<T>?>() {}.type
    try {
        return Gson().fromJson<List<T>?>(value, typeToken)
    } catch (exception: Exception) {
        logE("getJsonModel" + exception.message)
    }
    return null
}

fun String.removeSpace(): String {
    val regex = "\\s"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(this)
    return matcher.replaceAll("")
}

fun setApplicationValue(type: String? = null, walletType: String? = null, value: String? = null) {
    when (type) {
        TypeSendSms -> {
            Constants.sendSmsType = Pair(walletType, value)
        }

        TypeFaceRecognition -> {
            Constants.faceRecognitionType = value
        }

        else -> ""
    }

}


fun exitApp() {
    try {
        Process.killProcess(Process.myPid())
    } catch (e: Exception) {
        logE(e)
    }
    try {
        exitProcess(0)
    } catch (e: Exception) {
        logE(e)
    }
}

fun getH5Url(url: String, needToken: Boolean = false): String {
    var realUrl = if (url.startsWith("http")) {
        url
    } else {
        "${BaseUrlConfig.getH5BaseUrl()}${url}"
    }
    if (needToken) {
        realUrl += "?token=${MMKVProvider.loginResult?.token}"
    }
    return realUrl
}


/**
 * 拼接图片url缩放参数
 */
fun String.resizeImageUrl(width: Int, height: Int): String =
    "${this}?x-oss-process=image/resize,m_fixed,h_${height},w_${width}"

fun LifecycleOwner.getSearchData(
    scope: CoroutineScope,
    editText: EditText,
    block: (CharSequence?) -> Unit
) {
    scope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            editText.textChanges().debounce(500)
                .filter {
                    it != null
                }
                //避免向用户展示不需要的结果，只提供最后一个搜索查询（最新）的结果
                .flatMapLatest { value ->
                    flow<String> {
                        block.invoke(value)
                    }
                }.collect()
        }
    }
}

fun customToast(@StringRes id: Int? = null, isCustom: Boolean = true) {
    id?.let {
        if (it > 0) {
            if (isCustom) {
                val view =
                    LayoutInflater.from(application).inflate(R.layout.common_custom_toast, null)
                view.findViewById<TextView>(R.id.text).text = StringUtils.getString(id)
                customToast(view)
            } else {
                toast(it)
            }
        }
    }
}


fun customToast(message: CharSequence?, isCustom: Boolean = true) {
    message?.let {
        if (it.isNotBlank()) {
            if (isCustom) {
                val view =
                    LayoutInflater.from(application).inflate(R.layout.common_custom_toast, null)
                view.findViewById<TextView>(R.id.text).text = message
                customToast(view)
            } else {
                toast(message)
            }
        }
    }
}

fun SVGAImageView.loadSVGA(url: String) {
    val imageView = this
    var parser = SVGAParser.shareParser()
    if (parser == null) {
        parser = SVGAParser(context = imageView.context)
    }
    parser.decodeFromURL(URL(url), object : SVGAParser.ParseCompletion {
        override fun onComplete(videoItem: SVGAVideoEntity) {
            val drawable = SVGADrawable(videoItem)
            imageView.setImageDrawable(drawable)
            imageView.startAnimation()
        }

        override fun onError() {
        }
    })
}


fun randomRange(start: Int, end: Int): Int {
    return ((start + Math.random() * (end - start + 1)).toInt())
}

inline fun <reified T> Bundle.getBundleParcelable(key: String): T {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, T::class.java)!!
    } else {
        getParcelable(key)!!
    }
}

fun showAdolescentDialog(userId: String): Boolean {
    var adolescentList = MMKVProvider.adolescentList
    var isShow = false
    var time = Date().time
    if (!adolescentList.isNullOrEmpty()) {
        val adolescentTimeBeanList =
            fromJson<ArrayList<com.kissspace.common.AdolescentTimeBean>>(adolescentList)
        val findAdolescentTimeBean = adolescentTimeBeanList.findLast {
            it.userId == userId
        }
        if (findAdolescentTimeBean != null) {
            isShow = !DJSDate.isSameDay(
                time,
                findAdolescentTimeBean.time
            )
            findAdolescentTimeBean.time = time
            adolescentList = toJson(adolescentTimeBeanList)
        } else {
            isShow = true
            if (adolescentTimeBeanList.size > 9) {
                adolescentTimeBeanList.removeAt(0)
            }
            adolescentTimeBeanList.add(com.kissspace.common.AdolescentTimeBean(userId, time))
            adolescentList = toJson(adolescentTimeBeanList)
        }
    } else {
        var list = ArrayList<com.kissspace.common.AdolescentTimeBean>()
        list.add(com.kissspace.common.AdolescentTimeBean(userId, time))
        adolescentList = toJson(list)
        isShow = true
    }
    MMKVProvider.adolescentList = adolescentList
    return isShow
}

/**
 * 监听recyclerview是否滚动到底部
 */
fun RecyclerView.addScrollOnBottomListener(block: () -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            var lastPosition = -1
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                when (val layoutManager = layoutManager) {
                    is GridLayoutManager -> {
                        lastPosition = layoutManager.findLastVisibleItemPosition()
                    }

                    is LinearLayoutManager -> {
                        lastPosition = layoutManager.findLastVisibleItemPosition()
                    }

                    is StaggeredGridLayoutManager -> {
                        val lastPositions = IntArray(layoutManager.spanCount)
                        layoutManager.findLastVisibleItemPositions(lastPositions)
                        lastPosition = lastPositions.max()
                    }
                }
                if (lastPosition == recyclerView.layoutManager?.itemCount.orZero() - 1) {
                    block()
                }
            }
        }
    })
}

fun ViewPager2.setViewPagerTouchSlop() {
    val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
    recyclerViewField.isAccessible = true
    val recyclerView = recyclerViewField.get(this) as RecyclerView
    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
    touchSlopField.isAccessible = true
    val touchSlop = touchSlopField.get(recyclerView) as Int
    touchSlopField.set(recyclerView, touchSlop * 8)
}

fun alphaTo(view: View, alpha: Float) {
    ViewCompat.animate(view).alpha(alpha).start()
}

