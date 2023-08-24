package com.kissspace.common.util

import com.kissspace.module_common.R

/**
 *@author: adan
 *@date: 2023/5/26
 *@Description:
 */
object EmojiGameUtil {

    private const val DICE = "骰子"
    private const val MAI_XU = "麦序"
    private const val FINGER_GAME = "猜拳"

    fun getEmojiPath(name:String,index:String) :String{
        when (name) {
            DICE -> {
                return "assets://dice/dice_${index}.pag"
            }
            MAI_XU -> {
                return "assets://maixu/maixu_${index}.pag"
            }
            FINGER_GAME -> {
                return "assets://finger/finger_${index}.pag"
            }
        }
        return ""
    }


    fun getEmojiImagePath(name:String,index:String) :Int{
        when (name) {
            DICE -> {
                when(index){
                    "1" ->  return R.mipmap.common_dice_1
                    "2" ->  return R.mipmap.common_dice_2
                    "3" ->  return R.mipmap.common_dice_3
                    "4" ->  return R.mipmap.common_dice_4
                    "5" ->  return R.mipmap.common_dice_5
                    "6" ->  return R.mipmap.common_dice_6
                }
            }
            MAI_XU -> {
                when(index){
                    "1" ->  return R.mipmap.common_maixu_0
                    "2" ->  return R.mipmap.common_maixu_1
                    "3" ->  return R.mipmap.common_maixu_2
                    "4" ->  return R.mipmap.common_maixu_3
                    "5" ->  return R.mipmap.common_maixu_4
                    "6" ->  return R.mipmap.common_maixu_5
                    "7" ->  return R.mipmap.common_maixu_6
                    "8" ->  return R.mipmap.common_maixu_7
                    "9" ->  return R.mipmap.common_maixu_8
                }
            }
            FINGER_GAME -> {
                when(index){
                    "1" ->  return R.mipmap.common_finger_1
                    "2" ->  return R.mipmap.common_finger_2
                    "3" ->  return R.mipmap.common_finger_3
                }
            }
        }
        return 0
    }

    fun getEmojiIndex(name: String):String{
        when (name) {
            DICE -> {
                return randomRange(1,6).toString()
            }
            MAI_XU -> {
                return  randomRange(1,9).toString()
            }
            FINGER_GAME -> {
                return  randomRange(1,3).toString()
            }
        }
        return ""
    }

    fun isEmojiGame(name: String):Boolean{
        return name == DICE || name == MAI_XU || name == FINGER_GAME
    }
}