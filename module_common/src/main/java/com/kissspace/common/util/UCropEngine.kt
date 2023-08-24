package com.kissspace.common.util

import android.net.Uri
import androidx.fragment.app.Fragment
import com.luck.picture.lib.engine.CropFileEngine
import com.yalantis.ucrop.UCrop

class UCropEngine(private val isCircle: Boolean = true) : CropFileEngine {
    override fun onStartCrop(
        fragment: Fragment?,
        srcUri: Uri?,
        destinationUri: Uri?,
        dataSource: ArrayList<String>?,
        requestCode: Int
    ) {
        val options = UCrop.Options()
        options.setCircleDimmedLayer(isCircle)
        options.setFreeStyleCropEnabled(true)
        options.setShowCropGrid(false)
        options.setHideBottomControls(true)
        val uCrop = UCrop.of(srcUri!!, destinationUri!!, dataSource)
        uCrop.withOptions(options)
        uCrop.withAspectRatio(1f,1f)
        uCrop.setImageEngine(CoilCropEngine())
        uCrop.start(fragment!!.requireActivity(), fragment, requestCode)
    }

}