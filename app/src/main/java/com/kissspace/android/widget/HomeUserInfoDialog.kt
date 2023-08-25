package com.kissspace.android.widget


import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import cc.shinichi.library.ImagePreview
import com.blankj.utilcode.util.TimeUtils
import com.kissspace.android.databinding.DialogHomeUserInfoBinding
import com.kissspace.common.config.Constants
import com.kissspace.common.model.HomeUserListBean
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.util.jumpRoom
import com.kissspace.common.util.randomRange
import com.kissspace.common.widget.BaseBottomSheetDialogFragment
import com.kissspace.util.getAge
import com.kissspace.util.loadImage
import com.kissspace.util.loadImageCircle


class HomeUserInfoDialog : BaseBottomSheetDialogFragment<DialogHomeUserInfoBinding>(Gravity.CENTER) {
    private lateinit var userInfoBean: HomeUserListBean
    private var onDismissCallback: (() -> Unit?)? = null
    private var mPosition:Int = 0
    private lateinit var mList : List<HomeUserListBean>
    private lateinit var mPhoto:MutableList<String>

    companion object {
        fun newInstance(model: List<HomeUserListBean>,position:Int) = HomeUserInfoDialog().apply {
            arguments = bundleOf("userInfoBean" to model,"position" to position)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mList = arguments?.getParcelableArrayList("userInfoBean")!!
        mPosition = arguments?.getInt("position")!!
        userInfoBean = mList[mPosition]
    }

    override fun getViewBinding() =  DialogHomeUserInfoBinding.inflate(layoutInflater)

    private fun initData() {
        mBinding.ivAvatar.loadImageCircle(userInfoBean.profilePath)
        mBinding.tvName.text = userInfoBean.nickname
        mBinding.tvAge.text = "${getAge(userInfoBean.birthday)}岁 ${TimeUtils.getZodiac(userInfoBean.birthday,
            TimeUtils.getSafeDateFormat("yyyy-MM-dd"))}"
        mBinding.ivSex.setImageResource(if (userInfoBean.sex == Constants.SEX_MALE) com.kissspace.module_mine.R.mipmap.mine_icon_sex_male else com.kissspace.module_mine.R.mipmap.mine_sex_female)
        mBinding.tvDes.text = userInfoBean.personalSignature
        if (!userInfoBean.bgPath.isNullOrEmpty()){
            val bgList = userInfoBean.bgPath!!.split(",")
            mPhoto = bgList.toMutableList()
            if (bgList.size > 1){
                mBinding.ivUserBgOne.loadImage(bgList[0])
                mBinding.ivUserBgTwo.loadImage(bgList[1])
                mBinding.ivUserBgOne.visibility = View.VISIBLE
                mBinding.ivUserBgTwo.visibility = View.VISIBLE
            }else{
                mBinding.ivUserBgOne.loadImage(bgList[0])
                mBinding.ivUserBgOne.visibility = View.VISIBLE
            }
        }
    }

    override fun initView() {
        initData()
        mBinding.ivClose.setOnClickListener {
            dismiss()
        }
        mBinding.ivMessage.setOnClickListener {
            dismiss()
            if (!userInfoBean.stayRoomId.isNullOrEmpty()){
                jumpRoom(userInfoBean.stayRoomId)
            }else{
                jump(RouterPath.PATH_USER_PROFILE, "userId" to userInfoBean.userId)
            }
        }
        mBinding.ivSelect.setOnClickListener {
            val randomRange = randomRange(0, mList.size - 1)
            if (randomRange == mPosition){
                mBinding.ivSelect.performClick()
            }else{
                mBinding.ivUserBgOne.visibility = View.INVISIBLE
                mBinding.ivUserBgTwo.visibility = View.INVISIBLE
                userInfoBean = mList[randomRange]
                initData()
            }
        }
        mBinding.ivUserBgOne.setOnClickListener {
            previewPicture(0,mBinding.ivUserBgOne)
        }
        mBinding.ivUserBgTwo.setOnClickListener {
            previewPicture(1,mBinding.ivUserBgTwo)
        }
    }
    fun setDismissCallback(block: () -> Unit) {
        this.onDismissCallback = block
    }

    override fun show(fm: FragmentManager) {
        show(fm, "HomeUserInfoDialog")
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
    }

    private fun previewPicture(
        modelPosition: Int,
        target: ImageView,
    ) {
        if (!mPhoto.isNullOrEmpty()){
            ImagePreview.instance
                .setContext(requireActivity())
                .setIndex(modelPosition)
                .setImageList(mPhoto)
                .setTransitionView(target)
                .setTransitionShareElementName("shared_element_container")
                .setEnableUpDragClose(true)
                .setEnableDragClose(true)
                .setEnableDragCloseIgnoreScale(true)
                .setShowDownButton(false)
                .setShowIndicator(false)
                .start()
        }
    }
}