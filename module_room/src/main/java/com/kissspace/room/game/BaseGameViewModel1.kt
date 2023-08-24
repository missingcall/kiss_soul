package com.kissspace.room.game

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import com.kissspace.common.config.ConstantsKey
import com.kissspace.common.util.mmkv.MMKVProvider
import tech.sud.mgp.SudMGPWrapper.decorator.SudFSMMGDecorator
import tech.sud.mgp.SudMGPWrapper.decorator.SudFSMMGListener
import tech.sud.mgp.SudMGPWrapper.decorator.SudFSTAPPDecorator
import tech.sud.mgp.SudMGPWrapper.model.GameConfigModel
import tech.sud.mgp.SudMGPWrapper.model.GameViewInfoModel
import tech.sud.mgp.SudMGPWrapper.state.MGStateResponse
import tech.sud.mgp.SudMGPWrapper.utils.SudJsonUtils
import tech.sud.mgp.core.ISudFSMStateHandle
import tech.sud.mgp.core.ISudListenerInitSDK
import tech.sud.mgp.core.SudMGP

abstract class BaseGameViewModel1 : SudFSMMGListener {
    private var gameRoomId: String? = null
    private var playingGameId: Long = 0
    val sudFSTAPPDecorator = SudFSTAPPDecorator() // app调用sdk的封装类
    private val sudFSMMGDecorator = SudFSMMGDecorator() // 用于处理游戏SDK部分回调业务

    private var isRunning = true // 业务是否还在运行

    var gameView: View? = null
    var gameConfigModel = GameConfigModel() // 游戏配置

    protected val handler = Handler(Looper.getMainLooper())

    /**
     * 外部调用切换游戏，传不同的gameId即可加载不同的游戏
     * gameId传0 等同于关闭游戏
     *
     * @param activity   游戏所在页面，用作于生命周期判断
     * @param gameRoomId 游戏房间id，房间隔离，同一房间才能一起游戏
     * @param gameId     游戏id，传入不同的游戏id，即可加载不同的游戏，传0等同于关闭游戏
     */
    fun switchGame(activity: Activity, gameRoomId: String, gameId: Long) {
        if (TextUtils.isEmpty(gameRoomId)) {
            Toast.makeText(activity, "gameRoomId can not be empty", Toast.LENGTH_LONG).show()
            return
        }
        if (!isRunning) {
            return
        }
        if (playingGameId == gameId && gameRoomId == this.gameRoomId) {
            return
        }
        destroyMG()
        this.gameRoomId = gameRoomId
        playingGameId = gameId
        login(activity, gameId)
    }

    /**
     * 第1步，获取短期令牌code，用于换取游戏Server访问APP Server的长期ssToken
     * 接入方客户端 调用 接入方服务端 login 获取 短期令牌code
     * 参考文档时序图：sud-mgp-doc(https://docs.sud.tech/zh-CN/app/Client/StartUp-Android.html)
     *
     * @param activity 游戏所在页面
     * @param gameId   游戏id
     */
    private fun login(activity: Activity, gameId: Long) {
        if (activity.isDestroyed || gameId <= 0) {
            return
        }
        // 请求登录code
        getCode(activity, getUserId(), getAppId(), object : GameGetCodeListener {
            override fun onSuccess(code: String?) {
                if (!isRunning || gameId != playingGameId) {
                    return
                }
                initSdk(activity, gameId, code!!)
            }

            override fun onFailed() {
                delayLoadGame(activity, gameId)
            }

        })
    }

    /**
     * 第2步，初始化SudMGP sdk
     *
     * @param activity 游戏所在页面
     * @param gameId   游戏id
     * @param code     令牌
     */
    private fun initSdk(activity: Activity, gameId: Long, code: String) {
        SudMGP.initSDK(
            activity,
            ConstantsKey.GAME_APP_ID,
            ConstantsKey.GAME_APP_KEY,
            isTestEnv(),
            object : ISudListenerInitSDK {
                override fun onSuccess() {
                    loadGame(activity, code, gameId)
                }

                override fun onFailure(errCode: Int, errMsg: String) {
                    // TODO: 2022/6/13 下面toast可以根据业务需要决定是否保留
                    if (isTestEnv()) {
                        Toast.makeText(
                            activity,
                            "initSDK onFailure:$errMsg($errCode)", Toast.LENGTH_LONG
                        ).show()
                    }
                    delayLoadGame(activity, gameId)
                }
            })
    }

    /**
     * 第3步，加载游戏
     * APP和游戏的相互调用
     * ISudFSTAPP：APP调用游戏的接口
     * ISudFSMMG：游戏调APP的响应回调
     *
     * @param activity 游戏所在页面
     * @param code     登录令牌
     * @param gameId   游戏id
     */
    private fun loadGame(activity: Activity, code: String, gameId: Long) {
        if (activity.isDestroyed || !isRunning || gameId != playingGameId) {
            return
        }

        // 给装饰类设置回调
        sudFSMMGDecorator.setSudFSMMGListener(this)

        // 调用游戏sdk加载游戏
        val iSudFSTAPP = SudMGP.loadMG(
            activity,
            MMKVProvider.userId!!,
            gameRoomId,
            code,
            gameId,
            "zh-CN",
            sudFSMMGDecorator
        )

        // 如果返回空，则代表参数问题或者非主线程
        if (iSudFSTAPP == null) {
            Toast.makeText(activity, "loadMG params error", Toast.LENGTH_LONG).show()
            delayLoadGame(activity, gameId)
            return
        }

        // APP调用游戏接口的装饰类设置
        sudFSTAPPDecorator.setISudFSTAPP(iSudFSTAPP)

        // 获取游戏视图，将其抛回Activity进行展示
        // Activity调用：gameContainer.addView(view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        gameView = iSudFSTAPP.gameView
        onAddGameView(gameView)
    }

    /**
     * 游戏加载失败的时候，延迟一会再重新加载
     *
     * @param activity 游戏所在页面
     * @param gameId   游戏id
     */
    private fun delayLoadGame(activity: Activity, gameId: Long) {
        handler.postDelayed({ login(activity, gameId) }, 5000)
    }

    // region 生命周期相关
    /** 页面销毁的时候调用  */
    fun onDestroy() {
        isRunning = false
        destroyMG()
    }

    // endregion 生命周期相关
    private fun destroyMG() {
        if (playingGameId > 0) {
            sudFSTAPPDecorator.destroyMG()
            sudFSMMGDecorator.destroyMG()
            playingGameId = 0
            gameView = null
            onRemoveGameView()
        }
    }

    /** 获取当前游戏房id  */
    open fun getGameRoomId(): String? {
        return gameRoomId
    }

    // region 子类需要实现的方法

    // region 子类需要实现的方法
    /**
     * 向接入方服务器获取code
     */
    abstract fun getCode(
        activity: Activity?,
        userId: String?,
        appId: String?,
        listener: GameGetCodeListener
    )

    /**
     * 设置当前用户id(接入方定义)
     *
     * @return 返回用户id
     */
    protected abstract fun getUserId(): String?

    /**
     * 设置游戏所用的appId
     *
     * @return 返回游戏服务appId
     */
    protected abstract fun getAppId(): String?

    /**
     * 设置游戏所用的appKey
     *
     * @return 返回游戏服务appKey
     */
    protected abstract fun getAppKey(): String?

    /**
     * 设置游戏的语言代码
     * 参考文档：https://docs.sud.tech/zh-CN/app/Client/Languages/
     *
     * @return 返回语言代码
     */
    protected abstract fun getLanguageCode(): String?

    /**
     * 设置游戏的安全操作区域
     *
     * @param gameViewInfoModel 游戏视图大小
     */
    protected abstract fun getGameRect(gameViewInfoModel: GameViewInfoModel?)

    /**
     * true 加载游戏时为测试环境
     * false 加载游戏时为生产环境
     */
    protected abstract fun isTestEnv(): Boolean

    /**
     * 将游戏View添加到页面中
     *
     * @param gameView
     */
    protected abstract fun onAddGameView(gameView: View?)

    /**
     * 将页面中的游戏View移除
     */
    protected abstract fun onRemoveGameView()

    // endregion 子类需要实现的方法

    // region 游戏侧回调

    // endregion 子类需要实现的方法
    // region 游戏侧回调
    /**
     * 游戏日志
     * 最低版本：v1.1.30.xx
     */
    override fun onGameLog(str: String?) {

    }

    /**
     * 游戏开始
     * 最低版本：v1.1.30.xx
     */
    override fun onGameStarted() {}

    /**
     * 游戏销毁
     * 最低版本：v1.1.30.xx
     */
    override fun onGameDestroyed() {}

    /**
     * Code过期，需要实现
     * APP接入方需要调用handle.success或handle.fail
     *
     * @param dataJson {"code":"value"}
     */
    override fun onExpireCode(handle: ISudFSMStateHandle, dataJson: String?) {
        processOnExpireCode(sudFSTAPPDecorator, handle)
    }

    /**
     * 获取游戏View信息，需要实现
     * APP接入方需要调用handle.success或handle.fail
     *
     * @param handle   handle
     * @param dataJson {}
     */
    override fun onGetGameViewInfo(handle: ISudFSMStateHandle, dataJson: String) {
        gameView?.let {
            processOnGetGameViewInfo(it, handle)
        }

    }

    /**
     * 获取游戏Config，需要实现
     * APP接入方需要调用handle.success或handle.fail
     *
     * @param handle   handle
     * @param dataJson {}
     * 最低版本：v1.1.30.xx
     */
    override fun onGetGameCfg(handle: ISudFSMStateHandle, dataJson: String?) {
        processOnGetGameCfg(handle, dataJson)
    }

    // endregion 游戏侧回调
    /** 处理code过期  */
     fun processOnExpireCode(
        sudFSTAPPDecorator: SudFSTAPPDecorator,
        handle: ISudFSMStateHandle
    ) {
        // code过期，刷新code
        getCode(null, getUserId(), getAppId(), object : GameGetCodeListener {
            override fun onSuccess(code: String?) {
                if (!isRunning) return
                val mgStateResponse = MGStateResponse()
                mgStateResponse.ret_code = MGStateResponse.SUCCESS
                sudFSTAPPDecorator.updateCode(code, null)
                handle.success(SudJsonUtils.toJson(mgStateResponse))
            }

            override fun onFailed() {
                val mgStateResponse = MGStateResponse()
                mgStateResponse.ret_code = -1
                handle.failure(SudJsonUtils.toJson(mgStateResponse))
            }
        })
    }

    /**
     * 处理游戏视图信息(游戏安全区)
     * 文档：https://docs.sud.tech/zh-CN/app/Client/API/ISudFSMMG/onGetGameViewInfo.html
     */
    open fun processOnGetGameViewInfo(gameView: View, handle: ISudFSMStateHandle) {
        //拿到游戏View的宽高
        val gameViewWidth = gameView.measuredWidth
        val gameViewHeight = gameView.measuredHeight
        if (gameViewWidth > 0 && gameViewHeight > 0) {
            notifyGameViewInfo(handle, gameViewWidth, gameViewHeight)
            return
        }

        //如果游戏View未加载完成，则监听加载完成时回调
        gameView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                gameView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val width = gameView.measuredWidth
                val height = gameView.measuredHeight
                notifyGameViewInfo(handle, width, height)
            }
        })
    }

    /** 通知游戏，游戏视图信息  */
    private  fun notifyGameViewInfo(
        handle: ISudFSMStateHandle,
        gameViewWidth: Int,
        gameViewHeight: Int
    ) {
        val gameViewInfoModel = GameViewInfoModel()
        gameViewInfoModel.ret_code = 0
        // 游戏View大小
        gameViewInfoModel.view_size.width = gameViewWidth
        gameViewInfoModel.view_size.height = gameViewHeight

        // 游戏安全操作区域
        getGameRect(gameViewInfoModel)

        // 给游戏侧进行返回
        val json = SudJsonUtils.toJson(gameViewInfoModel)
        // 如果设置安全区有疑问，可将下面的日志打印出来，分析json数据
        // Log.d("SudBaseGameViewModel", "notifyGameViewInfo:" + json);
        handle.success(json)
    }

    open fun onPause() {
        // playMG和pauseMG要配对
        sudFSTAPPDecorator.pauseMG()
    }

    open fun onResume() {
        // playMG和pauseMG要配对
        sudFSTAPPDecorator.playMG()
    }

    /**
     * 处理游戏配置
     * 文档：https://docs.sud.tech/zh-CN/app/Client/API/ISudFSMMG/onGetGameCfg.html
     */
    open fun processOnGetGameCfg(handle: ISudFSMStateHandle, dataJson: String?) {
        handle.success(SudJsonUtils.toJson(gameConfigModel))
    }

    /** 游戏login(getCode)监听  */
    interface GameGetCodeListener {
        /** 成功  */
        fun onSuccess(code: String?)

        /** 失败  */
        fun onFailed()
    }
}