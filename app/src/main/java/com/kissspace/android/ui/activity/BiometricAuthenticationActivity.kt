package com.kissspace.android.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Outline
import android.graphics.PixelFormat
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowManager
import androidx.activity.viewModels
import com.didi.drouter.annotation.Router
import com.netease.nis.alivedetected.ActionType
import com.netease.nis.alivedetected.AliveDetector
import com.netease.nis.alivedetected.DetectedListener
import com.permissionx.guolindev.PermissionX
import com.kissspace.android.R
import com.kissspace.android.databinding.AppBiometricAuthenticBinding
import com.kissspace.android.viewmodel.IdentityAuthViewModel
import com.kissspace.common.binding.dataBinding
import com.kissspace.common.config.Constants
import com.kissspace.common.config.ConstantsKey.NETEASE_SHILED
import com.kissspace.common.router.jump
import com.kissspace.common.router.parseIntent
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.*
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.util.logE
import com.kissspace.util.orZero
import com.kissspace.util.postDelay
import com.kissspace.util.setWindowBrightness
import com.kissspace.util.toast

/**
 * @Author gaohangbo
 * @Date 2023/2/3 17:31.
 * @Describe 活体检测页面
 */
@Router(uri = RouterPath.PATH_USER_BIOMETRIC)
class BiometricAuthenticationActivity :
    com.kissspace.common.base.BaseActivity(R.layout.app_biometric_authentic) {

    private val mBinding by dataBinding<AppBiometricAuthenticBinding>()
    private val mViewModel by viewModels<IdentityAuthViewModel>()
    private val stateTipMap: Map<String, String> = mapOf(
        "straight_ahead" to "请看着镜头",
        "open_mouth" to "张张嘴",
        "turn_head_to_left" to "向左侧转头",
        "turn_head_to_right" to "向右侧转头",
        "blink_eyes" to "眨眨眼"
    )
    private var mAliveDetector: AliveDetector? = null
    private var mActions: Array<ActionType>? = null
    private var mCurrentCheckStepIndex = 0
    private var mCurrentActionType = ActionType.ACTION_STRAIGHT_AHEAD
    private var isH5BindAlipay by parseIntent<Boolean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        setWindowBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
        BroadcastDispatcher.registerScreenOff(this)
    }

    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar) {
            mAliveDetector?.stopDetect()
            if (isH5BindAlipay) {
                jump(RouterPath.PATH_MAIN, "index" to 0)
            } else {
                finish()
            }
        }
        mBinding.surfaceView.holder?.setFormat(PixelFormat.TRANSLUCENT)

        BroadcastDispatcher.addScreenStatusChangedListener(object :
            BroadcastDispatcher.ScreenStatusChangedListener {
            override fun onForeground() {
                startBiometricAuthentication()
            }

            override fun onBackground() {
                mAliveDetector?.stopDetect()
            }

        })
        mAliveDetector = AliveDetector.getInstance()
        mAliveDetector?.setDebugMode(com.kissspace.android.BuildConfig.DEBUG)

        mAliveDetector?.init(this, mBinding.surfaceView, NETEASE_SHILED)
        mBinding.surfaceView.apply {
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    //设置圆形
                    outline?.setOval(
                        0, 0, view?.width.orZero(), view?.width.orZero()
                    );
                }
            }
            clipToOutline = true
        }
        mBinding.tvCollect.safeClick {
            PermissionX.init(this).permissions(Manifest.permission.CAMERA)
                .onExplainRequestReason { scope, deniedList ->
                    val message =
                        "为了您能正常使用【人脸识别】功能，kiss空间需向你申请相机权限"
                    scope.showRequestReasonDialog(deniedList, message, "确定", "取消")
                }
                .explainReasonBeforeRequest()
                .request { allGranted, _, _ ->
                    if (allGranted) {
                        //开始活体检测
                        startBiometricAuthentication()
                        mBinding.ivBiometric.visibility = View.GONE
                        mBinding.clTop.visibility = View.VISIBLE
                        mBinding.tvTip.visibility = View.VISIBLE
                        mBinding.tvTitle.visibility = View.GONE
                        mBinding.tvCollect.visibility = View.GONE
                        mBinding.faceOutline.visibility = View.VISIBLE
                    } else {
                        toast("请打开相机权限")
                    }
                }
        }

    }

    fun showError(errorText: String) {
        toast(errorText)
        mBinding.tvCollect.visibility = View.VISIBLE
        mBinding.faceOutline.visibility = View.GONE
        mBinding.tvTip.visibility = View.GONE
        mBinding.tvTitle.visibility = View.VISIBLE
    }

    /**
     * actionIds
     */
    private fun buildActionCommand(actionCommands: Array<ActionType>?): String {
        val commands = StringBuilder()
        actionCommands?.let {
            for (actionType in it) {
                commands.append(actionType.actionID)
            }
        }
        return if (TextUtils.isEmpty(commands.toString())) "" else commands.toString()
    }


    @SuppressLint("SetTextI18n")
    private fun setTipText(tip: String?, isErrorType: Boolean) {
        if (isErrorType) {
            logE("tip-----$tip")
            when (tip) {
                "请移动人脸到摄像头视野中间" -> mBinding.tvErrorTip.text = "保持面部在框内"
                "请正视摄像头视野中间并保持不动" -> mBinding.tvErrorTip.text =
                    "请正视摄像头\n并保持不动"

                else -> mBinding.tvErrorTip.text = tip
            }
            mBinding.viewTipBackground.visibility = View.VISIBLE
//            mBinding.blurView.visibility = View.VISIBLE
            mBinding.viewTipBackground.visibility = View.VISIBLE
        } else {
            mBinding.viewTipBackground.visibility = View.INVISIBLE
//            mBinding.blurView.visibility = View.INVISIBLE
            mBinding.viewTipBackground.visibility = View.INVISIBLE
            logE("mBinding.tvTip.text-----$tip")
            mBinding.tvTip.text = tip
            mBinding.tvErrorTip.text = ""
        }
    }

    override fun onDestroy() {
        setWindowBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
        mAliveDetector?.stopDetect()
        mAliveDetector?.destroy()
        BroadcastDispatcher.unRegisterScreenOff(this)
        super.onDestroy()
    }

    private fun startBiometricAuthentication() {
        mAliveDetector?.setDetectedListener(object : DetectedListener {
            override fun onReady(isInitSuccess: Boolean) {
                // 开始倒计时
                //pv_count_time?.startCountTimeAnimation()
                // 引擎初始化完成
                if (isInitSuccess)
                    logE("活体检测引擎初始化完成")
                else {
                    toast("活体检测引擎初始化失败")
                    logE("活体检测引擎初始化失败")
                }
            }

            override fun onActionCommands(actionTypes: Array<ActionType>?) {
                // 此次活体检测下发的待检测动作指令序列
                mActions = actionTypes
                val commands = buildActionCommand(actionTypes)
                logE("活体检测动作序列为:$commands")
                //showIndicatorOnUiThread(commands.length - 1)
            }

            override fun onStateTipChanged(actionType: ActionType?, stateTip: String?) {
                // 单步动作
                if (actionType == ActionType.ACTION_PASSED && actionType.actionID != mCurrentActionType.actionID) {
                    mCurrentCheckStepIndex++
                    mActions?.let {
                        if (mCurrentCheckStepIndex < it.size) {
                            mCurrentActionType = it[mCurrentCheckStepIndex]
                        }
                    }
                }
                when (actionType) {
                    ActionType.ACTION_STRAIGHT_AHEAD -> setTipText("", false)
                    ActionType.ACTION_OPEN_MOUTH -> setTipText(
                        stateTipMap["open_mouth"],
                        false
                    )

                    ActionType.ACTION_TURN_HEAD_TO_LEFT -> setTipText(
                        stateTipMap["turn_head_to_left"], false
                    )

                    ActionType.ACTION_TURN_HEAD_TO_RIGHT -> setTipText(
                        stateTipMap["turn_head_to_right"], false
                    )

                    ActionType.ACTION_BLINK_EYES -> setTipText(
                        stateTipMap["blink_eyes"],
                        false
                    )

                    ActionType.ACTION_ERROR -> setTipText(stateTip, true)
                    else -> {
                    }
                }
            }

            override fun onPassed(isPassed: Boolean, token: String?) {
                // 检测通过
                if (isPassed) {
                    logE("活体检测通过,token is:$token")
                    mViewModel.faceRecognition(
                        token.orEmpty(),
                        Constants.faceRecognitionType?.toInt().orZero(),
                        onSuccess = {
                            toast("活体检测通过")
                            MMKVProvider.authentication = true
                            finishActivity()
                        },
                        onError = {
                            logE("活体检测接口失败----${it}")
                            showError(it.orEmpty())
                            finishActivity()
                        })
                } else {
                    logE("活体检测不通过,token is:$token")
                    showError("检测失败，请重试")
                    finishActivity()
                }
            }

            override fun onCheck() {
                logE("活体检测onCheck")
            }

            override fun onError(code: Int, msg: String?, token: String?) {
                hideLoading()
                logE("listener [onError] 活体检测出错,原因:$msg token:$token")
                showError("检测失败，请重试")
            }

            override fun onOverTime() {
                logE("活体检测超时")
                showError("检测超时, 请在规定时间内完成动作")
            }

        })
        mAliveDetector?.sensitivity = AliveDetector.SENSITIVITY_NORMAL
        mAliveDetector?.setTimeOut(30000)
        mAliveDetector?.startDetect()
    }

    fun finishActivity() {
        postDelay(300) {
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isH5BindAlipay) {
            jump(RouterPath.PATH_MAIN, "index" to 0)
        }

    }
}