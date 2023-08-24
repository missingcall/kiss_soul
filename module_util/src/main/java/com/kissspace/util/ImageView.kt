package com.kissspace.util

import android.widget.ImageView
import androidx.annotation.DrawableRes
import coil.ImageLoader
import coil.load
import coil.request.CachePolicy
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.blankj.utilcode.util.StringUtils

fun ImageView.loadImage(url: Any?, @DrawableRes placeholder: Int? = null, radius: Float? = null) {
    load(url) {
        placeholder?.let { placeholder(getDrawable(placeholder)) }
        radius?.let {
            transformations(RoundedCornersTransformation(radius.dp))
        }
    }
}


fun ImageView.loadImageCircle(
    url: Any?, @DrawableRes placeholder: Int? = null
) {
    load(url){
        placeholder?.let { placeholder(getDrawable(placeholder)) }
        transformations(CircleCropTransformation())
    }
}


fun ImageView.loadImage(
    url: Any?,
    @DrawableRes placeholder: Int? = null,
    topLeftRadius: Float = 0f,
    topRightRadius: Float = 0f,
    bottomLeftRadius: Float = 0f,
    bottomRightRadius: Float = 0f
) {
    load(url) {
        placeholder?.let { placeholder(getDrawable(placeholder)) }
        transformations(RoundedCornersTransformation(
            topLeft = topLeftRadius.dp,
            topRight = topRightRadius.dp,
            bottomLeft = bottomLeftRadius.dp,
            bottomRight = bottomRightRadius.dp
        ))
    }
}


fun ImageView.loadImage(url: Any?, width: Int, height: Int, radius: Float? = null) {
    load(url) {
        size(width, height)
        radius?.let {
            transformations(RoundedCornersTransformation(radius.dp))
        }
    }
}
