package com.example.chekersgamepro.checkers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Base64
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.example.chekersgamepro.R
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import kotlin.math.roundToInt


class CheckersImageUtil {
    private val resources = CheckersApplication.create().resources

    private val context = CheckersApplication.create().applicationContext

    fun loadBitmap(imagePath: String, context: Context, drawable: Drawable? = null): Single<Bitmap> {
        return Single.create { emitter ->
            Glide.with(context)
                    .asBitmap()
                    .load(imagePath)
                    .placeholder(R.drawable.ic_computer_game)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            if(drawable != null){
                                emitter.onSuccess( drawableToBitmap(drawable)!!)
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // this is called when imageView is cleared on lifecycle call or for
                            // some other reason.
                            // if you are referencing the bitmap somewhere else too other than this imageView
                            // clear it here as you can no longer have the bitmap
                        }

                        override fun onResourceReady(avater: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                            emitter.onSuccess(avater)
                        }
                    })
        }
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            val bitmap1 = drawable.bitmap
            if (bitmap1 != null) {
                return bitmap1
            }
        }
        val bitmap: Bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap


    }

    fun bitmapToDrawable(bitmap: Bitmap): Drawable  = BitmapDrawable(resources, bitmap)

    fun blurBitmapFromDrawble(drawableResId: Int){
        val options = BitmapFactory.Options()
        options.inSampleSize = 8
        val blurTemplate = BitmapFactory.decodeResource(resources, drawableResId, options)

    }

    fun blurBitmapFromBitmap(image: Bitmap, levelBlur: Float = 17.5f): Bitmap? {
        val BITMAP_SCALE = 0.2f
        val BLUR_RADIUS = levelBlur
        val width = (image.width * BITMAP_SCALE).roundToInt()
        val height = (image.height * BITMAP_SCALE).roundToInt()
        val inputBitmap = Bitmap.createScaledBitmap(image, width, height, false)
        val outputBitmap = Bitmap.createBitmap(inputBitmap)
        val rs = RenderScript.create(context)
        val theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
        val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)
        theIntrinsic.setRadius(BLUR_RADIUS)
        theIntrinsic.setInput(tmpIn)
        theIntrinsic.forEach(tmpOut)
        tmpOut.copyTo(outputBitmap)
        return outputBitmap
    }

    fun preloadImageCompletable(context: Context?, url: String): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val requestListener: RequestListener<Drawable?> = object : RequestListener<Drawable?> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable?>?, isFirstResource: Boolean): Boolean {
                    emitter.onError(e!!)
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable?>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    emitter.onComplete()
                    return false
                }

            }
            Glide.with(context!!)
                    .load(url)
                    .listener(requestListener)
                    .preload()
        }.subscribeOn(AndroidSchedulers.mainThread())
    }

    fun createByteArrayFromBitmap(bitmap: Bitmap): ByteArray {
        val streamGuestOrComputer = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, streamGuestOrComputer)
        return streamGuestOrComputer.toByteArray()
    }

    fun createBitmapFromByteArray(byteArray: ByteArray): Bitmap =  BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

    fun compressBitmap(image: Bitmap): Bitmap  = createBitmapFromByteArray(createByteArrayFromBitmap(image))

    fun createByteArrayFromEncodeBase(encodeBase: String): ByteArray =  Base64.decode(encodeBase, Base64.DEFAULT)

    fun encodeBase64Image(image : Bitmap): String?  = Base64.encodeToString(createByteArrayFromBitmap(image), Base64.DEFAULT)

    fun encodeBase64Image(byteArray: ByteArray): String?  = Base64.encodeToString(byteArray, Base64.DEFAULT)


    fun decodeBase64(input: String?): Bitmap? {
        val decodedBytes = Base64.decode(input, 0)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    companion object Factory {

        private var checkersImageUtil: CheckersImageUtil? = null

        @JvmStatic
        fun create(): CheckersImageUtil {
            if (checkersImageUtil == null) {
                checkersImageUtil = CheckersImageUtil()
            }
            return checkersImageUtil as CheckersImageUtil
        }
    }

}