/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kissspace.login.widget

import android.content.Context
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.MediaController
import android.widget.MediaController.MediaPlayerControl
import android.widget.VideoView
import java.io.IOException

/**
 * Displays a video file.  The TextureVideoView class
 * can load images from various sources (such as resources or content
 * providers), takes care of computing its measurement from the video so that
 * it can be used in any layout manager, and provides various display options
 * such as scaling and tinting.
 *
 *
 *
 * *Note: VideoView does not retain its full state when going into the
 * background.*  In particular, it does not restore the current play state,
 * play position or selected tracks.  Applications should
 * save and restore these on their own in
 * [android.app.Activity.onSaveInstanceState] and
 * [android.app.Activity.onRestoreInstanceState].
 *
 *
 * Also note that the audio session id (from [.getAudioSessionId]) may
 * change from its previously returned value when the VideoView is restored.
 *
 *
 *
 * This code is based on the official Android sources for 6.0.1_r10 with the following differences:
 *
 *  1. extends [TextureView] instead of a [android.view.SurfaceView]
 * allowing proper view animations
 *  1. removes code that uses hidden APIs and thus is not available (e.g. subtitle support)
 *
 */
open class FixedTextureVideoView : TextureView, MediaPlayerControl {
    private val TAG = "TextureVideoView"

    // settable by the client
    private var mUri: Uri? = null
    private var mHeaders: Map<String, String>? = null

    // mCurrentState is a TextureVideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the TextureVideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private var mCurrentState = STATE_IDLE
    private var mTargetState = STATE_IDLE

    // All the stuff we need for playing and showing a video
    private var mSurface: Surface? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var mAudioSession = 0
    var videoWidth = 0
        private set
    var videoHeight = 0
        private set
    private var mMediaController: MediaController? = null
    private var mOnCompletionListener: OnCompletionListener? = null
    private var mOnPreparedListener: OnPreparedListener? = null
    private var mCurrentBufferPercentage = 0
    private var mOnErrorListener: OnErrorListener? = null
    private var mOnInfoListener: OnInfoListener? = null
    private var mSeekWhenPrepared // recording the seek position while preparing
            = 0
    private var mCanPause = false
    private var mCanSeekBack = false
    private var mCanSeekForward = false
    private var fixedWidth = 0
    private var fixedHeight = 0
    private var matrix: Matrix? = null

    constructor(context: Context?) : super(context!!) {
        initVideoView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0) {
        initVideoView()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    ) {
        initVideoView()
    }

    fun setFixedSize(width: Int, height: Int) {
        fixedHeight = height
        fixedWidth = width
        Log.d(TAG, "setFixedSize,width=" + width + "height=" + height)
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (fixedWidth == 0 || fixedHeight == 0) {
            defaultMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            setMeasuredDimension(fixedWidth, fixedHeight)
        }
        Log.d(
            TAG,
            String.format("onMeasure, fixedWidth=%d,fixedHeight=%d", fixedWidth, fixedHeight)
        )
    }

    private fun defaultMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //Log.i("@@@@", "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + ", "
        //        + MeasureSpec.toString(heightMeasureSpec) + ")");
        var width = getDefaultSize(videoWidth, widthMeasureSpec)
        var height = getDefaultSize(videoHeight, heightMeasureSpec)
        if (videoWidth > 0 && videoHeight > 0) {
            val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
            val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
            val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize
                height = heightSpecSize

                // for compatibility, we adjust size based on aspect ratio
                if (videoWidth * height < width * videoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * videoWidth / videoHeight
                } else if (videoWidth * height > width * videoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * videoHeight / videoWidth
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize
                height = width * videoHeight / videoWidth
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize
                width = height * videoWidth / videoHeight
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = videoWidth
                height = videoHeight
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize
                    width = height * videoWidth / videoHeight
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize
                    height = width * videoHeight / videoWidth
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        setMeasuredDimension(width, height)
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = FixedTextureVideoView::class.java.name
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = FixedTextureVideoView::class.java.name
    }

    fun resolveAdjustedSize(desiredSize: Int, measureSpec: Int): Int {
        return getDefaultSize(desiredSize, measureSpec)
    }

    private fun initVideoView() {
        videoWidth = 0
        videoHeight = 0
        surfaceTextureListener = mSurfaceTextureListener
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
        mCurrentState = STATE_IDLE
        mTargetState = STATE_IDLE
    }

    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    fun setVideoPath(path: String?) {
        setVideoURI(Uri.parse(path))
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    fun setVideoURI(uri: Uri?) {
        setVideoURI(uri, null)
    }

    /**
     * Sets video URI using specific headers.
     *
     * @param uri     the URI of the video.
     * @param headers the headers for the URI request.
     * Note that the cross domain redirection is allowed by default, but that can be
     * changed with key/value pairs through the headers parameter with
     * "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
     * to disallow or allow cross domain redirection.
     */
    private fun setVideoURI(uri: Uri?, headers: Map<String, String>?) {
        mUri = uri
        mHeaders = headers
        mSeekWhenPrepared = 0
        openVideo()
        requestLayout()
        invalidate()
    }

    fun stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer?.stop()
            mMediaPlayer?.release()
            mMediaPlayer = null
            mCurrentState = STATE_IDLE
            mTargetState = STATE_IDLE
            val am =
                context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.abandonAudioFocus(null)
        }
    }

    private fun openVideo() {
        if (mUri == null || mSurface == null) {
            // not ready for playback just yet, will try again later
            return
        }
        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false)
        val am = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        try {
            mMediaPlayer = MediaPlayer()
            if (mAudioSession != 0) {
                mMediaPlayer!!.audioSessionId = mAudioSession
            } else {
                mAudioSession = mMediaPlayer!!.audioSessionId
            }
            mMediaPlayer!!.setOnPreparedListener(mPreparedListener)
            mMediaPlayer!!.setOnVideoSizeChangedListener(mSizeChangedListener)
            mMediaPlayer!!.setOnCompletionListener(mCompletionListener)
            mMediaPlayer!!.setOnErrorListener(mErrorListener)
            mMediaPlayer!!.setOnInfoListener(mInfoListener)
            mMediaPlayer!!.setOnBufferingUpdateListener(mBufferingUpdateListener)
            mCurrentBufferPercentage = 0
            mMediaPlayer!!.setDataSource(context.applicationContext, mUri!!, mHeaders)
            mMediaPlayer!!.setSurface(mSurface)
            mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mMediaPlayer!!.setScreenOnWhilePlaying(true)
            mMediaPlayer!!.prepareAsync()

            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING
            attachMediaController()
        } catch (ex: IOException) {
            Log.w(TAG, "Unable to open content: $mUri", ex)
            mCurrentState = STATE_ERROR
            mTargetState = STATE_ERROR
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
            return
        } catch (ex: IllegalArgumentException) {
            Log.w(TAG, "Unable to open content: $mUri", ex)
            mCurrentState = STATE_ERROR
            mTargetState = STATE_ERROR
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
            return
        }
    }

    fun setMediaController(controller: MediaController?) {
        if (mMediaController != null) {
            mMediaController!!.hide()
        }
        mMediaController = controller
        attachMediaController()
    }

    private fun attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController!!.setMediaPlayer(this)
            val anchorView = if (this.parent is View) this.parent as View else this
            mMediaController!!.setAnchorView(anchorView)
            mMediaController!!.isEnabled = isInPlaybackState
        }
    }

    //需求:视频等比例放大,直至一边铺满View的某一边,另一边超出View的另一边,再移动到View的正中央,这样长边两边会被裁剪掉同样大小的区域,视频看起来不会变形
    //也即是:先把视频区(实际的大小显示区)与View(定义的大小)区的两个中心点重合, 然后等比例放大或缩小视频区,直至一条边与View的一条边相等,另一条边超过
    //View的另一条边,这时再裁剪掉超出的边, 使视频区与View区大小一样. 这样在不同尺寸的手机上,视频看起来不会变形,只是水平或竖直方向的两端被裁剪了一些.
    private fun transformVideo(videoWidth: Int, videoHeight: Int) {
        if (resizedHeight == 0 || resizedWidth == 0) {
            Log.d(
                TAG,
                "transformVideo, getResizedHeight=" + resizedHeight + "," + "getResizedWidth=" + resizedWidth
            )
            return
        }
        val sx = resizedWidth.toFloat() / videoWidth.toFloat()
        val sy = resizedHeight.toFloat() / videoHeight.toFloat()
        Log.d(TAG, "transformVideo, sx=$sx")
        Log.d(TAG, "transformVideo, sy=$sy")
        val maxScale = Math.max(sx, sy)
        if (matrix == null) {
            matrix = Matrix()
        } else {
            matrix!!.reset()
        }

        //第2步:把视频区移动到View区,使两者中心点重合.
        matrix!!.preTranslate(
            ((resizedWidth - videoWidth) / 2).toFloat(),
            ((resizedHeight - videoHeight) / 2).toFloat()
        )

        //第1步:因为默认视频是fitXY的形式显示的,所以首先要缩放还原回来.
        matrix!!.preScale(
            videoWidth / resizedWidth.toFloat(),
            videoHeight / resizedHeight.toFloat()
        )

        //第3步,等比例放大或缩小,直到视频区的一边超过View一边, 另一边与View的另一边相等. 因为超过的部分超出了View的范围,所以是不会显示的,相当于裁剪了.
        matrix!!.postScale(
            maxScale,
            maxScale,
            (resizedWidth / 2).toFloat(),
            (resizedHeight / 2).toFloat()
        ) //后两个参数坐标是以整个View的坐标系以参考的
        Log.d(TAG, "transformVideo, maxScale=$maxScale")
        setTransform(matrix)
        postInvalidate()
        Log.d(TAG, "transformVideo, videoWidth=$videoWidth,videoHeight=$videoHeight")
    }

    val resizedWidth: Int
        get() = if (fixedWidth == 0) {
            width
        } else {
            fixedWidth
        }
    val resizedHeight: Int
        get() = if (fixedHeight == 0) {
            height
        } else {
            fixedHeight
        }
    var mSizeChangedListener: OnVideoSizeChangedListener = object : OnVideoSizeChangedListener {
        override fun onVideoSizeChanged(mp: MediaPlayer, width: Int, height: Int) {
            videoWidth = mp.videoWidth
            videoHeight = mp.videoHeight
            if (videoWidth != 0 && videoHeight != 0) {
                surfaceTexture!!.setDefaultBufferSize(videoWidth, videoHeight)
                requestLayout()
                transformVideo(videoWidth, videoHeight)
                Log.d(
                    TAG,
                    String.format(
                        "OnVideoSizeChangedListener, mVideoWidth=%d,mVideoHeight=%d",
                        videoWidth,
                        videoHeight
                    )
                )
            }
        }
    }
    var mPreparedListener: OnPreparedListener = object : OnPreparedListener {
        override fun onPrepared(mp: MediaPlayer) {
            mCurrentState = STATE_PREPARED
            mCanSeekForward = true
            mCanSeekBack = mCanSeekForward
            mCanPause = mCanSeekBack
            if (mOnPreparedListener != null) {
                mOnPreparedListener!!.onPrepared(mMediaPlayer)
            }
            if (mMediaController != null) {
                mMediaController!!.isEnabled = true
            }
            //            mVideoWidth = mp.getVideoWidth();
//            mVideoHeight = mp.getVideoHeight();
            val seekToPosition =
                mSeekWhenPrepared // mSeekWhenPrepared may be changed after seekTo() call
            if (seekToPosition != 0) {
                seekTo(seekToPosition)
            }
            if (videoWidth != 0 && videoHeight != 0) {
                //Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
                surfaceTexture!!.setDefaultBufferSize(videoWidth, videoHeight)
                // We won't get a "surface changed" callback if the surface is already the right size, so
                // start the video here instead of in the callback.
                if (mTargetState == STATE_PLAYING) {
                    start()
                    if (mMediaController != null) {
                        mMediaController!!.show()
                    }
                } else if (!isPlaying &&
                    (seekToPosition != 0 || currentPosition > 0)
                ) {
                    if (mMediaController != null) {
                        // Show the media controls when we're paused into a video and make 'em stick.
                        mMediaController!!.show(0)
                    }
                }
            } else {
                // We don't know the video size yet, but should start anyway.
                // The video size might be reported to us later.
                if (mTargetState == STATE_PLAYING) {
                    start()
                }
            }
        }
    }
    private val mCompletionListener = OnCompletionListener {
        mCurrentState = STATE_PLAYBACK_COMPLETED
        mTargetState = STATE_PLAYBACK_COMPLETED
        if (mMediaController != null) {
            mMediaController!!.hide()
        }
        if (mOnCompletionListener != null) {
            mOnCompletionListener!!.onCompletion(mMediaPlayer)
        }
    }
    private val mInfoListener = OnInfoListener { mp, arg1, arg2 ->
        if (mOnInfoListener != null) {
            mOnInfoListener!!.onInfo(mp, arg1, arg2)
        }
        true
    }
    private val mErrorListener = OnErrorListener { mp, framework_err, impl_err ->
        Log.d(TAG, "Error: $framework_err,$impl_err")
        mCurrentState = STATE_ERROR
        mTargetState = STATE_ERROR
        if (mMediaController != null) {
            mMediaController!!.hide()
        }

        /* If an error handler has been supplied, use it and finish. */
        if (mOnErrorListener != null) {
            if (mOnErrorListener!!.onError(mMediaPlayer, framework_err, impl_err)) {
                return@OnErrorListener true
            }
        }

        /* Otherwise, pop up an error dialog so the user knows that
             * something bad has happened. Only try and pop up the dialog
             * if we're attached to a window. When we're going away and no
             * longer have a window, don't bother showing the user an error.
             */
//        if (windowToken != null) {
//            val r = context.resources
//            val messageId: Int
//            messageId =
//                if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
//                    R.string.VideoView_error_text_invalid_progressive_playback
//                } else {
//                    R.string.VideoView_error_text_unknown
//                }
//            AlertDialog.Builder(context)
//                .setMessage(messageId)
//                .setPositiveButton(
//                    R.string.VideoView_error_button
//                ) { dialog, whichButton -> /* If we get here, there is no onError listener, so
//                                         * at least inform them that the video is over.
//                                         */
//                    if (mOnCompletionListener != null) {
//                        mOnCompletionListener!!.onCompletion(mMediaPlayer)
//                    }
//                }
//                .setCancelable(false)
//                .show()
//        }
        true
    }
    private val mBufferingUpdateListener =
        OnBufferingUpdateListener { mp, percent -> mCurrentBufferPercentage = percent }

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    fun setOnPreparedListener(l: OnPreparedListener?) {
        mOnPreparedListener = l
    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    fun setOnCompletionListener(l: OnCompletionListener?) {
        mOnCompletionListener = l
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, TextureVideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    fun setOnErrorListener(l: OnErrorListener?) {
        mOnErrorListener = l
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    fun setOnInfoListener(l: MediaPlayer.OnInfoListener?) {
        mOnInfoListener = l
    }

    var mSurfaceTextureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            val isValidState = mTargetState == STATE_PLAYING
            val hasValidSize = width > 0 && height > 0
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared)
                }
                start()
            }
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            mSurface = Surface(surface)
            openVideo()
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            // after we return from this we can't use the surface any more
            if (mSurface != null) {
                mSurface!!.release()
                mSurface = null
            }
            if (mMediaController != null) mMediaController!!.hide()
            release(true)
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            // do nothing
        }
    }

    /*
     * release the media player in any state
     */
    private fun release(cleartargetstate: Boolean) {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.reset()
            mMediaPlayer!!.release()
            mMediaPlayer = null
            mCurrentState = STATE_IDLE
            if (cleartargetstate) {
                mTargetState = STATE_IDLE
            }
            val am =
                context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.abandonAudioFocus(null)
            var a: VideoView
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (isInPlaybackState && mMediaController != null) {
            toggleMediaControlsVisiblity()
        }
        return super.onTouchEvent(ev)
    }

    override fun onTrackballEvent(ev: MotionEvent): Boolean {
        if (isInPlaybackState && mMediaController != null) {
            toggleMediaControlsVisiblity()
        }
        return super.onTrackballEvent(ev)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val isKeyCodeSupported =
            keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && keyCode != KeyEvent.KEYCODE_VOLUME_MUTE && keyCode != KeyEvent.KEYCODE_MENU && keyCode != KeyEvent.KEYCODE_CALL && keyCode != KeyEvent.KEYCODE_ENDCALL
        if (isInPlaybackState && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
            ) {
                if (mMediaPlayer!!.isPlaying) {
                    pause()
                    mMediaController!!.show()
                } else {
                    start()
                    mMediaController!!.hide()
                }
                return true
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer!!.isPlaying) {
                    start()
                    mMediaController!!.hide()
                }
                return true
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE
            ) {
                if (mMediaPlayer!!.isPlaying) {
                    pause()
                    mMediaController!!.show()
                }
                return true
            } else {
                toggleMediaControlsVisiblity()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun toggleMediaControlsVisiblity() {
        if (mMediaController!!.isShowing) {
            mMediaController!!.hide()
        } else {
            mMediaController!!.show()
        }
    }

    override fun start() {
        if (isInPlaybackState) {
            mMediaPlayer!!.start()
            mCurrentState = STATE_PLAYING
        }
        mTargetState = STATE_PLAYING
    }

    override fun pause() {
        if (isInPlaybackState) {
            if (mMediaPlayer!!.isPlaying) {
                mMediaPlayer!!.pause()
                mCurrentState = STATE_PAUSED
            }
        }
        mTargetState = STATE_PAUSED
    }

    fun suspend() {
        release(false)
    }

    fun resume() {
        openVideo()
    }

    override fun getDuration(): Int {
        return if (isInPlaybackState) {
            mMediaPlayer!!.duration
        } else -1
    }

    override fun getCurrentPosition(): Int {
        return if (isInPlaybackState) {
            mMediaPlayer!!.currentPosition
        } else 0
    }

    override fun seekTo(msec: Int) {
        mSeekWhenPrepared = if (isInPlaybackState) {
            mMediaPlayer!!.seekTo(msec)
            0
        } else {
            msec
        }
    }

    override fun isPlaying(): Boolean {
        return isInPlaybackState && mMediaPlayer!!.isPlaying
    }

    override fun getBufferPercentage(): Int {
        return if (mMediaPlayer != null) {
            mCurrentBufferPercentage
        } else 0
    }

    private val isInPlaybackState: Boolean
        private get() = mMediaPlayer != null && mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING

    override fun canPause(): Boolean {
        return mCanPause
    }

    override fun canSeekBackward(): Boolean {
        return mCanSeekBack
    }

    override fun canSeekForward(): Boolean {
        return mCanSeekForward
    }

    override fun getAudioSessionId(): Int {
        if (mAudioSession == 0) {
            val foo = MediaPlayer()
            mAudioSession = foo.audioSessionId
            foo.release()
        }
        return mAudioSession
    }

    companion object {
        // all possible internal states
        private const val STATE_ERROR = -1
        private const val STATE_IDLE = 0
        private const val STATE_PREPARING = 1
        private const val STATE_PREPARED = 2
        private const val STATE_PLAYING = 3
        private const val STATE_PAUSED = 4
        private const val STATE_PLAYBACK_COMPLETED = 5
    }
}