package com.kissspace.message.widget

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.commit
import com.kissspace.common.base.BaseFragment
import com.kissspace.message.ui.fragment.ChatFragment
import com.kissspace.message.ui.fragment.MessageFragment
import com.kissspace.module_message.R


/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/26 13:05
 * @Description: 聊天弹窗
 *
 */
class ChatDialog : DialogFragment() {
    private lateinit var account: String

    companion object {
        fun newInstance(userId: String) = ChatDialog().apply {
            arguments = bundleOf("account" to userId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        account = arguments?.getString("account")!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =
            Dialog(requireContext(), com.kissspace.module_common.R.style.Theme_CustomDialog)
        dialog.setContentView(R.layout.message_dialog_chat)
        dialog.window?.apply {
            val attr = attributes
            attr.width = WindowManager.LayoutParams.MATCH_PARENT
            attr.height = WindowManager.LayoutParams.WRAP_CONTENT
            attr.gravity = Gravity.BOTTOM
            attributes = attr
            setWindowAnimations(com.kissspace.module_common.R.style.DialogBottomInAnimation)
        }
        if (account.isNotEmpty()) {
            childFragmentManager.commit(true) {
                replace(
                    R.id.container,
                    ChatFragment.newInstance(account, account.substring(3, account.length), true)
                )
            }
        } else {
            childFragmentManager.commit(true) {
                replace(R.id.container, MessageFragment())
            }
        }
        return dialog
    }

    fun addFragment(fragment: BaseFragment) {
        childFragmentManager.commit(true) {
            addToBackStack(null)
            setCustomAnimations(
                com.kissspace.module_common.R.anim.slide_right_in,
                com.kissspace.module_common.R.anim.slide_left_out,
                com.kissspace.module_common.R.anim.slide_right_in,
                com.kissspace.module_common.R.anim.slide_left_out
            )
            replace(R.id.container, fragment)
        }
    }


}


