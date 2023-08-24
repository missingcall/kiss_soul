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
    fun initStoragePathInternal(context: Context) {
        val dataDir = context.getExternalFilesDir(NIM_PATH)
        val dataDir1 = context.getExternalFilesDir(DOWNLOAD_PATH)
        checkAndMakeDir(dataDir)
        checkAndMakeDir(dataDir1)
    }

    private fun checkAndMakeDir(file: File?) {
        if (file?.exists()?.not() == true) {
            val result = file.mkdirs()
        }
    }
}