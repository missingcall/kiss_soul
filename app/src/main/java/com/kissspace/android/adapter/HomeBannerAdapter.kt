package com.kissspace.android.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.kissspace.android.R
import com.kissspace.common.ext.safeClick
import com.kissspace.common.model.VideoBannerListBean
import com.kissspace.common.util.jumpRoom
import com.kissspace.util.loadImage
import com.kissspace.util.loadImageCircle
import com.youth.banner.adapter.BannerAdapter

class HomeBannerAdapter(private val context: Context, data: List<VideoBannerListBean>) :
    BannerAdapter<VideoBannerListBean, HomeBannerAdapter.BannerViewHolder>(data) {

    inner class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivPoster: ImageView
        var tvQuestion: TextView
        var avatar: ImageView
        var guessInfoLayout:ConstraintLayout
        var tvNickName:TextView
        var ivWait:ImageView
        var tvIntegralLeft:TextView
        var tvIntegralRight:TextView
        var tvMembersLeft : TextView
        var tvMembersRight : TextView
        var layoutProgress : ConstraintLayout
        var root:ConstraintLayout

        init {
            root = itemView.findViewById(R.id.root)
            ivPoster = itemView.findViewById(R.id.iv_poster)
            tvQuestion = itemView.findViewById(R.id.tv_question)
            avatar = itemView.findViewById(R.id.iv_avatar)
            guessInfoLayout = itemView.findViewById(R.id.layout_info)
            tvNickName = itemView.findViewById(R.id.tv_nickname)
            ivWait = itemView.findViewById(R.id.iv_wait)
            tvIntegralLeft = itemView.findViewById(R.id.tv_integral_left)
            tvIntegralRight = itemView.findViewById(R.id.tv_integral_right)
            tvMembersLeft = itemView.findViewById(R.id.tv_members_left)
            tvMembersRight = itemView.findViewById(R.id.tv_members_right)
            layoutProgress = itemView.findViewById(R.id.layout_progress)
        }
    }

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): BannerViewHolder {
        val itemView = LayoutInflater.from(parent?.context)
            .inflate(R.layout.layout_home_banner_item, parent, false)
        return BannerViewHolder(itemView)
    }

    override fun onBindView(
        holder: BannerViewHolder?,
        data: VideoBannerListBean?,
        position: Int,
        size: Int
    ) {
        holder?.avatar?.loadImageCircle(data?.url)
        holder?.tvNickName?.text = data?.nickName
        holder?.root?.safeClick {
            data?.let {
                jumpRoom(it.chatRoomId)
            }
        }
        if(data?.integralGuessId?.isNullOrEmpty() == true){
            holder?.ivPoster?.loadImage(R.mipmap.app_bg_home_banner_not)
            holder?.guessInfoLayout?.visibility = View.GONE
            holder?.ivWait?.visibility = View.VISIBLE
            holder?.tvQuestion?.text = "预言即将开始..."
        }else{
            holder?.guessInfoLayout?.visibility = View.VISIBLE
            holder?.ivWait?.visibility = View.GONE
            data?.let {
                holder?.tvQuestion?.text = it.desc
                holder?.tvIntegralLeft?.text= it.leftBet.toString()
                holder?.tvIntegralRight?.text= it.rightBet.toString()
                holder?.tvMembersLeft?.text= "左方：${it.leftNum}人"
                holder?.tvMembersRight?.text= "${it.rightNum}人：右方"
                if(it.rightBet == it.leftBet){
                    holder?.ivPoster?.loadImage(R.mipmap.app_bg_home_banner_not)
                    holder?.layoutProgress?.setBackgroundResource(R.mipmap.bg_home_banner_bottom_middle)
                }else {
                    holder?.ivPoster?.loadImage(if (it.rightBet > it.leftBet) R.mipmap.app_bg_home_banner_right else R.mipmap.app_bg_home_banner_left)
                    holder?.layoutProgress?.setBackgroundResource(if (it.rightBet > it.leftBet) R.mipmap.bg_home_banner_bottom_right else R.mipmap.bg_home_banner_bottom_left)
                }
            }
        }
    }
}