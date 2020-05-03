package com.example.chekersgamepro.checkers

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Base64
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.example.chekersgamepro.R
import com.example.chekersgamepro.util.FileUtils
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import kotlin.math.roundToInt


open class CheckersImageUtil {
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
                            if (drawable != null) {
                                emitter.onSuccess(drawableToBitmap(drawable)!!)
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

    fun bitmapToDrawable(bitmap: Bitmap): Drawable = BitmapDrawable(resources, bitmap)

    fun blurBitmapFromDrawble(drawableResId: Int) {
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

    fun createBitmapFromByteArray(byteArray: ByteArray): Bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

    fun compressBitmap(image: Bitmap): Bitmap {
        val createScaledBitmap = resize(image, 600, 600)
        Log.d("TEST_GAME", "2 imageBitmap size after compress: ${sizeOfBitmap(createScaledBitmap)}")

        return createBitmapFromByteArray(createByteArrayFromBitmap(createScaledBitmap))

    }

    private fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        var image = image
        return if (maxHeight > 0 && maxWidth > 0) {
            val width = image.width
            val height = image.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > ratioBitmap) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
            image
        } else {
            image
        }
    }

    public fun modifyOrientation(bitmap: Bitmap, image_absolute_path: String): Bitmap {
        val ei = ExifInterface(image_absolute_path)
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotate(bitmap, 90F)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotate(bitmap, 180F)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotate(bitmap, 270F)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flip(bitmap, horizontal = true, vertical = false)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flip(bitmap, horizontal = false, vertical = true)
            else -> bitmap
        }
    }

    public fun modifyOrientationFromCamera(bitmap: Bitmap, image_absolute_path: String): Bitmap {
        val ei = ExifInterface(image_absolute_path)
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        return when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flip(bitmap, horizontal = true, vertical = false)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flip(bitmap, horizontal = false, vertical = true)
            else -> bitmap
        }
    }

    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.preScale(if (horizontal) -1f else 1f, if (vertical) -1f else 1f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    fun creteBitmapFromData(data: Intent?, isCompress : Boolean): Bitmap {
        val imageUri = data?.data!!
        val path = FileUtils.getPath(context, imageUri)
        val imageStream = context.contentResolver.openInputStream(imageUri!!)
        val bitmap = BitmapFactory.decodeStream(imageStream)

        var modifyOrientationBitmap = modifyOrientation(bitmap, path!!)

        if (isCompress){
            modifyOrientationBitmap = compressBitmap(modifyOrientationBitmap)
        }



        return modifyOrientationBitmap
    }

    fun createByteArrayFromEncodeBase(encodeBase: String): ByteArray = Base64.decode(encodeBase, Base64.DEFAULT)

    fun encodeBase64Image(image: Bitmap): String? = Base64.encodeToString(createByteArrayFromBitmap(image), Base64.DEFAULT)

    fun encodeBase64Image(byteArray: ByteArray): String? = Base64.encodeToString(byteArray, Base64.DEFAULT)

    @SuppressLint("ObsoleteSdkInt")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    fun sizeOfBitmap(bitmap: Bitmap): Int {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            bitmap.rowBytes * bitmap.height
        } else {
            bitmap.byteCount
        }
    }

    fun decodeBase64(input: String?): Bitmap? {
        val decodedBytes = Base64.decode(input, 0)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    fun decodeBase64Async(input: String?): Observable<Bitmap> {
        val decodedBytes = Base64.decode(input, 0)
        return Observable.just( BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size))
                .subscribeOn(Schedulers.io())
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