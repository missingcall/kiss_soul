package com.kissspace.util

import android.content.Context
import android.os.Environment
import android.text.TextUtils
import com.blankj.utilcode.util.FileUtils
import java.io.File
import java.io.IOException
import java.math.BigDecimal

const val ApkName = "djsoul.apk"
inline val fileSeparator: String get() = File.separator
inline val apkFileDir: String get() = application.getExternalFilesDir(AppFileHelper.DOWNLOAD_PATH)?.absolutePath.toString()

inline val apkAbsolutePath: String get() = application.getExternalFilesDir(AppFileHelper.DOWNLOAD_PATH)?.absolutePath+ "/"+ ApkName

fun getTotalCacheSize(): String {
    var cacheSize = FileUtils.getLength(application.cacheDir)
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        cacheSize += FileUtils.getLength(application.externalCacheDir)
    }
    return getFormatSize(cacheSize)
}

fun clearAllCache() {
    deleteDir(application.cacheDir)
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        deleteDir(application.externalCacheDir)
    }
}

/**
 * 删除文件，不包括云信目录
 */
fun deleteDir(dir: File?): Boolean {
    if (dir != null && dir.isDirectory && !dir.absolutePath.contains("nim")) {
        val children = dir.list()
        for (i in children.indices) {
            val success = deleteDir(File(dir, children[i]))
            if (!success) {
                return false
            }
        }
    }
    return if (dir!!.absolutePath.contains("nim")) {
        true
    } else {
        dir.delete()
    }
}


fun getFormatSize(size: Long): String {
    val kiloByte = (size / 1024).toDouble()
    if (kiloByte < 1) {
        return (size / 1024).toString() + "kB"
    }
    val megaByte = kiloByte / 1024
    if (megaByte < 1) {
        val result1 = BigDecimal(kiloByte.toString())
        return result1.setScale(1, BigDecimal.ROUND_HALF_UP)
            .toPlainString() + "KB"
    }
    val gigaByte = megaByte / 1024
    if (gigaByte < 1) {
        val result2 = BigDecimal(megaByte.toString())
        return result2.setScale(1, BigDecimal.ROUND_HALF_UP)
            .toPlainString() + "MB"
    }
    val teraBytes = gigaByte / 1024
    if (teraBytes < 1) {
        val result3 = BigDecimal(gigaByte.toString())
        return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
            .toPlainString() + "GB"
    }
    val result4 = BigDecimal(teraBytes)
    return (result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
            + "TB")
}

fun getNimSdkPath(context: Context):String{
    var storageRootPath = context.getExternalFilesDir(AppFileHelper.NIM_PATH)?.absolutePath
    if (TextUtils.isEmpty(storageRootPath)) {
        try {
            //内部存储目录
            if (context.externalCacheDir != null) {
                storageRootPath = context.externalCacheDir?.canonicalPath
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    if(TextUtils.isEmpty(storageRootPath)){
        storageRootPath =
            Environment.getExternalStorageDirectory().toString() + "/" + application.packageName
    }
    return storageRootPath.orEmpty()
}