/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <freeboub@gmail.com> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return Olivier Bouillet
 * ----------------------------------------------------------------------------
 */
package com.boub.ethicsearchwidget

import android.app.Activity
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import com.boub.ethicsearchwidget.ESWSearchConfiguration.ESWSearchProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

internal class ESWImageManager constructor( activity : Activity ) {
    private var mDefaulMicrophoneBitmap: Bitmap
    private var mDefaultbitmap: Bitmap? = null
    private var mTransparentpaint: Paint = Paint()
    private var mActivity: Activity
    private val micSize = 50

    init {
        m_instance = this
        mActivity = activity
        mTransparentpaint.alpha = 150
        val defaultBitmap =
            BitmapFactory.decodeResource(resources, R.mipmap.yin_yang_small)
        mDefaultbitmap = getResizedBitmap(defaultBitmap, 100, 100)
        defaultBitmap.recycle()
        mDefaulMicrophoneBitmap = Bitmap.createBitmap(mDefaultbitmap!!)
        val comboImage = Canvas(mDefaulMicrophoneBitmap)
        val microphoneBitmap =
            BitmapFactory.decodeResource(resources, R.drawable.microphone)
        val b =
            getResizedBitmap(microphoneBitmap, micSize, micSize)
        microphoneBitmap.recycle()
        comboImage.drawBitmap(b, 50f, 50f, mTransparentpaint)
    }

    private val resources: Resources
        get() = mActivity.resources

    class DownloadImage : AsyncTask<ESWSearchProvider?, Int?, Drawable?>() {
        private var mImageView: ImageView? = null
        private var mProgressBar: ProgressBar? = null
        private var mListView: ListView? = null

        override fun doInBackground(vararg arg0: ESWSearchProvider?): Drawable? {
            // This is done in a background thread
            val arg = arg0[0]
            if ( arg != null ) {
                val holder = arg.mHolder
                if ( holder != null ) {
                    mImageView = holder.image
                    mProgressBar = holder.progressBar
                    m_instance.mActivity.runOnUiThread {
                        mListView = holder.itemLayout.rootView.findViewById(R.id.configure_list)
                    }
                }

                return if (arg.mResourceId != -1) {
                    val bmp = BitmapFactory.decodeResource(
                        m_instance.resources,
                        arg.mResourceId
                    )
                    updateImage(
                        bmp,
                        arg.getLogoPath(m_instance.mActivity),
                        arg.getLogoVoicePath(m_instance.mActivity)
                    )
                } else {
                    downloadImage(
                        arg.mLogo,
                        arg.getLogoPath(m_instance.mActivity),
                        arg.getLogoVoicePath(m_instance.mActivity)
                    )
                }
            }
            else
            {
                Log.e(TAG, "no image provided")
            }
            return null
        }

        /**
         * Called after the image has been downloaded
         * -> this calls a function on the main thread again
         */
        override fun onPostExecute(image: Drawable?) {
            mImageView!!.setImageDrawable(image)
            mImageView!!.visibility = View.VISIBLE
            mProgressBar!!.visibility = View.GONE
        }

        /**
         * Download the Image from the _url to strDestPath
         *
         * @param _url
         * @return
         */
        private fun downloadImage(
            _url: String,
            _strDestPath: String,
            _strDestPathVoice: String
        ): Drawable? {
            //Prepare to download image
            try {
                return updateImage(BitmapFactory.decodeStream( URL(_url).openStream() ), _strDestPath, _strDestPathVoice)
            } catch (e: Exception) {
                Log.e("Error reading file", e.toString())
            }
            return null
        }

        /**
         * Download the Image from the _url to strDestPath
         *
         * @param _inputBitmap
         * @return
         */
        private fun updateImage(
            _inputBitmap: Bitmap,
            _strDestPath: String,
            _strDestPathVoice: String
        ): Drawable? {
            //Prepare to download image
            try {
                val w = _inputBitmap.width
                val h = _inputBitmap.height
                val targetw = 200
                val targeth = targetw * h / w
                val scaledEngineBitmap =
                    getResizedBitmap(_inputBitmap, targeth, targetw)
                saveBitmap(_strDestPath, scaledEngineBitmap)
                val comboImage =
                    Canvas(scaledEngineBitmap)
                val microphoneBitmap = BitmapFactory.decodeResource(
                    instance.resources, R.drawable.microphone
                )
                val micSize: Int
                micSize = if (targeth > targetw) {
                    targetw / 2
                } else {
                    targeth / 2
                }
                val microphoneBitmapScaled =
                    getResizedBitmap(microphoneBitmap, micSize, micSize)
                microphoneBitmap.recycle()
                comboImage.drawBitmap(
                    microphoneBitmapScaled,
                    targetw - micSize.toFloat(),
                    targeth - micSize.toFloat(),
                    instance.mTransparentpaint
                )
                saveBitmap(_strDestPathVoice, scaledEngineBitmap)
                return BitmapDrawable.createFromPath(_strDestPath)
            } catch (e: Exception) {
                Log.e("Error reading file", e.toString())
            }
            saveBitmap(
                _strDestPath,
                instance.mDefaultbitmap
            )
            saveBitmap(
                _strDestPathVoice,
                instance.mDefaulMicrophoneBitmap
            )
            return BitmapDrawable(
                instance.resources,
                instance.mDefaultbitmap
            )
        }
    }

    companion object {
        private const val TAG = "ESWImageManager"
        lateinit var  m_instance : ESWImageManager

        private fun getResizedBitmap(bm: Bitmap, newHeight: Int, newWidth: Int): Bitmap {
            val width = bm.width
            val height = bm.height
            val scaleWidth = newWidth.toFloat() / width
            val scaleHeight = newHeight.toFloat() / height

            // Create a matrix for the manipulation
            val matrix = Matrix()

            // Resize the bit map
            matrix.postScale(scaleWidth, scaleHeight)

            // Recreate the new Bitmap
            return Bitmap.createScaledBitmap(bm, newWidth, newHeight, false)
        }

        private fun saveBitmap(_path: String, _bm: Bitmap?) {
            val file =
                File(_path) // the File to save , append increasing numeric counter to prevent files from getting overwritten.
            try {
                val fOut = FileOutputStream(file)
                _bm!!.compress(Bitmap.CompressFormat.PNG, 100, fOut)
                fOut.flush()
                fOut.close()
            } catch (e: IOException) {
                Log.e(TAG, "cannot save image to : $_path")
                e.printStackTrace()
            }
        }

        val instance: ESWImageManager
            get() {
                return m_instance
            }
    }

}