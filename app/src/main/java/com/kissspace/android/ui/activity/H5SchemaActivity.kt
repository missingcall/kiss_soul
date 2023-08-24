package com.kissspace.android.ui.activity

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.didi.drouter.annotation.Router
import com.kissspace.common.config.Constants.TypeFaceRecognition
import com.kissspace.common.router.jump
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.loginOut
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.mmkv.isLogin
import com.kissspace.common.util.setApplicationValue
import com.kissspace.common.widget.CommonSingleConfirmDialog
import com.kissspace.util.appName
import com.kissspace.util.logE

/**
 * @Author gaohangbo
 * @Date 2023/3/15 16:47.
 * @Describe schema跳转中间页
 */
@Router(uri = RouterPath.PATH_H5_SCHEMA)
class H5SchemaActivity : AppCompatActivity() {
    var errorContent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri: Uri? = intent.data //获取H5规则, 可携带数据
        val phone = uri?.getQueryParameter("phone")//自定义携带参数
        val errorCode = uri?.getQueryParameter("errorCode")//自定义携带参数
        val type = uri?.getQueryParameter("type")//自定义携带参数
        logE("phone$phone")
        if (phone != null) {
            if (phone != MMKVProvider.userPhone||!isLogin()) {
                errorContent = "登录应用的${appName}账号与当前${appName}账号不一致,应用内没有账号登录，请检查后重试"
                showErrorDialog(this, errorContent)
            } else {
                if (errorCode != null) {
                    logE("errorCode$errorCode")
                    logE("type$type")
                    setApplicationValue(
                        type= TypeFaceRecognition,
                        value= type
                    )
                    if (errorCode == "51514") {
                        jump(RouterPath.PATH_USER_IDENTITY_AUTH,"isH5BindAlipay" to true)
                    } else if (errorCode == "50138" || errorCode == "50142" || errorCode == "51516") {
                        jump(RouterPath.PATH_USER_BIOMETRIC,"isH5BindAlipay" to true)
                    }
                }else{
                    jump(RouterPath.PATH_BIND_ALIPAY, "isH5BindAlipay" to true)
                }
            }
        }

    }
}


fun showErrorDialog(context: Context, errorContent: String?) {
    CommonSingleConfirmDialog(context, positiveText = "知道了", title = errorContent.orEmpty()) {
        loginOut()
    }.show()
}