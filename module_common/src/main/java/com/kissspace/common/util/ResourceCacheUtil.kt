package com.kissspace.common.util

import coil.request.ImageRequest
import com.blankj.utilcode.util.CacheDiskUtils
import com.drake.net.Get
import com.drake.net.utils.scopeNet
import com.kissspace.util.application
import com.kissspace.util.md516
import java.io.File

private val pageCachePath = File(application.cacheDir, "pag").absolutePath
private val mp4CachePath = File(application.cacheDir, "mp4").absolutePath
private val audioCachePath = File(application.cacheDir, "audio").absolutePath

/**
 * 获取本地PAG文件路径
 * @param url 文件url
 * @param block 返回路口
 */
fun getPagPath(url: String, block: (String) -> Unit) {
    if (url.isNullOrEmpty()) {
        block("")
        return
    }
    if (url.startsWith("assets")){
        block(url)
        return
    }
    val path = CacheDiskUtils.getInstance("pag").getString(url.md516())
    if (path.isNullOrEmpty() || !path.endsWith(".pag")) {
        scopeNet {
            val file = Get<File>(url) {
                setDownloadTempFile(true)
                setDownloadFileName("${url.md516()}.pag")
                setDownloadDir(pageCachePath)
            }.await()
            CacheDiskUtils.getInstance("pag").put(url.md516(), file.absolutePath)
            block(CacheDiskUtils.getInstance("pag").getString(url.md516()))
        }
    } else {
        block(path)
    }
}


fun getMP4Path(url: String, block: (String) -> Unit) {
    if (url.isNullOrEmpty()) {
        block("")
        return
    }
    val path = CacheDiskUtils.getInstance("mp4").getString(url.md516())
    if (path.isNullOrEmpty() || !path.endsWith(".mp4")) {
        scopeNet {
            val file = Get<File>(url) {
                setDownloadTempFile(true)
                setDownloadFileName("${url.md516()}.mp4")
                setDownloadDir(mp4CachePath)
            }.await()
            CacheDiskUtils.getInstance("mp4").put(url.md516(), file.absolutePath)
            block(CacheDiskUtils.getInstance("mp4").getString(url.md516()))
        }
    } else {
        block(path)
    }
}

fun getAudioPath(url: String, block: (String) -> Unit) {
    if (url.isNullOrEmpty()) {
        block("")
        return
    }
    val path = CacheDiskUtils.getInstance("audio").getString(url.md516())
    if (path.isNullOrEmpty() || !path.endsWith(".aac")) {
        scopeNet {
            val file = Get<File>(url) {
                setDownloadTempFile(true)
                setDownloadFileName("${url.md516()}.aac")
                setDownloadDir(audioCachePath)
            }.await()
            CacheDiskUtils.getInstance("audio").put(url.md516(), file.absolutePath)
            block(CacheDiskUtils.getInstance("audio").getString(url.md516()))
        }
    } else {
        block(path)
    }
}


