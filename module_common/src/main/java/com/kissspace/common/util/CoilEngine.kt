package com.kissspace.common.util

import android.content.Context
import android.widget.ImageView
import com.luck.picture.lib.engine.ImageEngine
import com.luck.picture.lib.utils.ActivityCompatHelper
import com.kissspace.util.loadImage


class CoilEngine : ImageEngine {
    override fun loadImage(context: Context, url: String, imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        imageView?.loadImage(url)
    }

    override fun loadImage(
        context: Context?,
        imageView: ImageView?,
        url: String?,
        maxWidth: Int,
        maxHeight: Int
    ) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        imageView?.loadImage(url, width = maxWidth, height = maxHeight)
    }

    override fun loadAlbumCover(context: Context, url: String, imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView?.loadImage(url, width = 180, height = 180, radius = 8f)
    }

    override fun loadGridImage(context: Context, url: String, imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView?.loadImage(url, width = 270, height = 270)
    }


    override fun pauseRequests(context: Context?) {

    }

    override fun resumeRequests(context: Context?) {

    }
}