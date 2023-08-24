package com.kissspace.room.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.kissspace.module_room.R
import com.kissspace.room.manager.RoomServiceManager
import com.kissspace.util.isNotEmpty

class RoomPagerAdapter : RecyclerView.Adapter<RoomPagerAdapter.MHolder>() {
    private var mData = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.room_layout_vp_item, parent, false)
        return MHolder(itemView)
    }

    override fun onBindViewHolder(holder: MHolder, position: Int) {

    }

    override fun getItemCount(): Int = if (RoomServiceManager.roomList.isNotEmpty()) Int.MAX_VALUE else 1

    fun setData(data: List<String>) {
        mData.clear()
        mData.addAll(data)
        notifyDataSetChanged()
    }

    class MHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}