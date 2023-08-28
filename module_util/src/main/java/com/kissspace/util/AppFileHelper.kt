package com.kissspace.util

import android.content.Context
import java.io.File

/**
 * @Author gaohangbo
 * @Date 2023/4/3 16:20.
 * @Describe
 */
object AppFileHelper {
    const val NIM_PATH = "Nim_Cache"
    const val DOWNLOAD_PATH= "DownLoad_Cache/apk"
    const val AUDIO_PATH = "AUDIO"
    fun initStoragePathInternal(context: Context) {
        val nimPath = context.getExternalFilesDir(NIM_PATH)
        val downloadPath = context.getExternalFilesDir(DOWNLOAD_PATH)
        val audioPath = context.getExternalFilesDir(AUDIO_PATH)
        checkAndMakeDir(nimPath)
        checkAndMakeDir(downloadPath)
        checkAndMakeDir(audioPath)
    }

    private fun checkAndMakeDir(file: File?) {
        if (file?.exists()?.not() == true) {
            val result = file.mkdirs()
        }
    }
}