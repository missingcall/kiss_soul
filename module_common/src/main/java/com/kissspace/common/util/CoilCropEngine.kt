package com.kissspace.common.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import com.luck.picture.lib.utils.ActivityCompatHelper
import com.kissspace.util.loadImage
import com.yalantis.ucrop.UCropImageEngine


class CoilCropEngine : UCropImageEngine {
    override fun loadImage(context: Context?, url: String?, imageView: ImageView?) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        imageView?.loadImage(url)
    }

    override fun loadImage(
        context: Context?,
        url: Uri?,
        maxWidth: Int,
        maxHeight: Int,
        call: UCropImageEngine.OnCallbackListener<Bitmap>?
    ) {

    }

}