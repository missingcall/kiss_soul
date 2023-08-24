package com.kissspace.mine.widget

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.kissspace.common.util.getMP4Path
import com.kissspace.module_mine.databinding.MineDialogPreviewDressUoBinding
import java.io.File
import java.net.URL

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/5 10:05
 * @Description: 预览装扮弹窗
 *
 */
class PreviewDressUpDialog : DialogFragment() {
    private lateinit var mBinding: MineDialogPreviewDressUoBinding
    private lateinit var mUrl: String

    companion object {
        fun newInstance(url: String) = PreviewDressUpDialog().apply {
            arguments = bundleOf("url" to url)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mUrl = it.getString("url")!!
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        mBinding = MineDialogPreviewDressUoBinding.inflate(layoutInflater)
        dialog.setContentView(mBinding.root)
        dialog.window?.attributes?.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            flags = flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        getMP4Path(mUrl) {
            mBinding.animview.startPlay(File(it))
        }
        mBinding.ivClose.setOnClickListener {
            dismiss()
        }
        return dialog
    }

}