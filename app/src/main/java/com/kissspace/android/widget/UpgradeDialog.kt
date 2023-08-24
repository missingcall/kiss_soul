package com.kissspace.android.widget

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.AppUtils
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.scopeNetLife
import com.kissspace.android.R
import com.kissspace.android.databinding.DialogUpgradeBinding
import com.kissspace.common.model.UpgradeBean
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.widget.BaseDialogFragment
import com.kissspace.util.ApkName
import com.kissspace.util.apkAbsolutePath
import com.kissspace.util.apkFileDir
import java.io.File

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/2/28 10:22
 * @Description: 版本升级弹窗
 *
 */

class UpgradeDialog : BaseDialogFragment<DialogUpgradeBinding>(Gravity.CENTER) {
    private lateinit var upgradeBean: UpgradeBean
    private var isJumpSettingActivity = false
    private var apkFile: File? = null
    private var onDismissCallback: (() -> Unit?)? = null

    companion object {
        fun newInstance(model: UpgradeBean) = UpgradeDialog().apply {
            arguments = bundleOf("upgradeBean" to model)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        upgradeBean = arguments?.getParcelable("upgradeBean")!!
    }


    override fun getLayoutId() = R.layout.dialog_upgrade

    override fun initView() {
        isCancelable = upgradeBean.isForcedUpdate == 0
        mBinding.tvDesc.text = upgradeBean.descript
        mBinding.ivClose.visibility =
            if (upgradeBean.isForcedUpdate == 1) View.GONE else View.VISIBLE
        mBinding.tvQuit.visibility =
            if (upgradeBean.isForcedUpdate == 1) View.VISIBLE else View.INVISIBLE

        mBinding.ivClose.setOnClickListener {
            dismiss()
        }
        mBinding.tvQuit.setOnClickListener {
            AppUtils.exitApp()
        }
        mBinding.tvUpdate.setOnClickListener {
            if (apkFile != null) {
                installProcess()
            } else {
                mBinding.tvUpdate.visibility = View.GONE
                mBinding.tvProgress.visibility = View.VISIBLE
                scopeNetLife {
                    val file =
                        Get<File>(upgradeBean.downUrl) {
                            setDownloadDir(apkFileDir)
                            setDownloadFileName(ApkName)
                            setDownloadFileNameDecode(true)
                            addDownloadListener(object : ProgressListener() {
                                override fun onProgress(p: Progress) {
                                    mBinding.tvProgress.post {
                                        mBinding.tvProgress.text =
                                            "正在下载，请稍等...${p.progress()}%"
                                    }
                                }
                            })
                        }.await()
                    mBinding.tvProgress.visibility = View.GONE
                    mBinding.tvUpdate.visibility = View.VISIBLE
                    mBinding.tvUpdate.text = "立即安装"
                    apkFile = file
                    installProcess()
                }
            }

        }
    }

    fun setDismissCallback(block: () -> Unit) {
        this.onDismissCallback = block
    }

    override fun show(fm: FragmentManager) {
        MMKVProvider.lastShowUpgradeDate = System.currentTimeMillis()
        show(fm, "UpgradeDialog")
    }


    private fun installProcess() {
        apkFile?.let {
            install(it, requireContext())
        }
    }

    override fun onResume() {
        super.onResume()
        if (isJumpSettingActivity) {
            installProcess()
        }
    }


    private fun install(file: File, context: Context): Boolean {
        val intent = Intent(Intent.ACTION_VIEW)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
        } else {
            val uri: Uri = FileProvider.getUriForFile(context, context.packageName + ".FileProvider", file)
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return true
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
    }


    private fun chmodApkFile(){
       try {
           val command = "chmod 777 $apkAbsolutePath"
           val runtime = Runtime.getRuntime()
           runtime.exec(command)
       }catch (e:Exception){
           e.printStackTrace()
       }
    }
}