package com.kissspace.message.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.recyclerview.widget.RecyclerView
import com.kissspace.common.router.jump
import com.kissspace.common.model.LoveWallListBean
import com.kissspace.common.router.RouterPath
import com.kissspace.util.ellipsizeString
import com.kissspace.util.resToColor
import com.kissspace.module_message.R
import com.kissspace.util.loadImage
import com.kissspace.util.loadImageCircle
import com.youth.banner.adapter.BannerAdapter

class MessageBannerAdapter(data: List<LoveWallListBean>) :
    BannerAdapter<LoveWallListBean, MessageBannerAdapter.BannerViewHolder>(data) {

    inner class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mRootView: ConstraintLayout
        var mAvatarLeft: ImageView
        var mAvatarRight: ImageView
        var mInfo: TextView
        var mGiftImage: ImageView

        init {
            mRootView = itemView.findViewById(R.id.root)
            mAvatarLeft = itemView.findViewById(R.id.iv_avatar_left)
            mAvatarRight = itemView.findViewById(R.id.iv_avatar_right)
            mInfo = itemView.findViewById(R.id.tv_info)
            mGiftImage = itemView.findViewById(R.id.iv_gift)

        }
    }

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): BannerViewHolder {
        val itemView = LayoutInflater.from(parent?.context)
            .inflate(R.layout.message_banner_item, parent, false)
        return BannerViewHolder(itemView)
    }

    override fun onBindView(
        holder: BannerViewHolder,
        data: LoveWallListBean,
        position: Int,
        size: Int
    ) {
        val resource = when (position % 2) {
            0 -> R.mipmap.message_bg_love_wall_first
            else -> R.mipmap.message_bg_love_wall_blue
        }
        holder.mRootView.setBackgroundResource(resource)
        holder.mAvatarLeft.loadImageCircle(data.sourceUserProfilePath)
        holder.mAvatarRight.loadImageCircle(data.targetUserProfilePath)
        holder.mGiftImage.loadImage(data.url)
        holder.mInfo.text = buildSpannedString {
            color(R.color.white.resToColor()) {
                bold {
                    append(data.sourceUserNickname.ellipsizeString(4))
                }
            }
            color(R.color.white.resToColor()) {
                append(" 对 ")
            }
            color(R.color.white.resToColor()) {
                bold {
                    append(data.targetUserNickname.ellipsizeString(4))
                }
            }
            color(R.color.white.resToColor()) {
                append("一见倾心，壕刷")
            }
            color(R.color.color_FFEC4D.resToColor()) {
                bold {
                    append(data.giftName.ellipsizeString(4) + "x" + data.number)
                }
            }
        }

        holder.mRootView.setOnClickListener {
            jump(RouterPath.PATH_LOVE_WALL)
        }
    }
}


