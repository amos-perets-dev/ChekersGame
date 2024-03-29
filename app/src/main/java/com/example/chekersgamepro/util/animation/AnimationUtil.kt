package com.example.chekersgamepro.util.animation

import android.animation.*
import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.airbnb.lottie.model.layer.Layer
import com.google.android.gms.common.util.CollectionUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import java.util.*
import java.util.concurrent.TimeUnit

class AnimationUtil {

    companion object {

        @SuppressLint("ObjectAnimatorBinding")
        fun animatePulse(view: View, scale : Float = 1.2f) {
            view.setLayerType(View.LAYER_TYPE_HARDWARE, null)

            if (view.tag is ObjectAnimator){
                Log.d("TEST_GAME", "AnimationUtil if (view.tag is ObjectAnimator) cancel")

                (view.tag as ObjectAnimator).cancel()
            }

            val animatePulse = ObjectAnimator.ofPropertyValuesHolder(view,
                    PropertyValuesHolder.ofFloat("scaleX", scale),
                    PropertyValuesHolder.ofFloat("scaleY", scale))
            animatePulse.duration = 300

            animatePulse.repeatCount = 3
            animatePulse.repeatMode = ObjectAnimator.REVERSE

            animatePulse.start()

            view.tag = animatePulse
        }

        fun animateViews(
                totalGames: AppCompatTextView,
                moneyIcon: AppCompatImageView,
                textViewPlayerName: AppCompatTextView,
                textViewMoneyChanges: AppCompatTextView,
                textViewLevelChanges: AppCompatTextView,
                imageProfile: CircleImageView): Completable {


            return Completable.create { emitter ->

                val listOfTextViewsTranslation =
                        CollectionUtils.listOf<View>(textViewPlayerName, textViewMoneyChanges, textViewLevelChanges, moneyIcon, totalGames)
                for (textView in listOfTextViewsTranslation) {
                    textView.animate().withLayer().setStartDelay(100).translationX(0f).setDuration(700).start()
                }

                imageProfile.animate()
                        .withLayer()
                        .alpha(1f)
                        .withEndAction { emitter.onComplete() }
                        .setStartDelay(250)
                        .setDuration(800)
                        .start()

            }

        }

        public fun translateWithScale(target: View
                                      , duration: Long
                                      , translationX: Float = -1f
                                      , translationY: Float
                                      , scale: Float
                                      , finishAnimate: Consumer<Boolean>) {

            // cancel animator
            if (target.tag is Animator) {
                (target.tag as Animator).cancel()
            }

            // scale
            val scaleUpX = ObjectAnimator.ofFloat(target, "scaleX", scale).setDuration(duration.toLong())
            val scaleUpY = ObjectAnimator.ofFloat(target, "scaleY", scale).setDuration(duration.toLong())


            val translateX = ObjectAnimator.ofFloat(target, "translationX", translationX).setDuration(duration.toLong())
            val translateY = ObjectAnimator.ofFloat(target, "translationY", translationY).setDuration(duration.toLong())
            // sequential animator
            val set = AnimatorSet()
            set.interpolator = AccelerateDecelerateInterpolator()
            set.playTogether(scaleUpX, scaleUpY, translateX, translateY)

            set.start()
            // set the AnimatorSet as a tag, to cancel for animate next time.
            set.addListener(object : Animation.AnimationListener, Animator.AnimatorListener {

                override fun onAnimationRepeat(p0: Animator?) {
                    Log.d("TEST_GAME", "translateWithScale -> onAnimationRepeat")

                }

                override fun onAnimationEnd(p0: Animator?) {
                    finishAnimate.accept(true)
                    Log.d("TEST_GAME", "translateWithScale -> onAnimationEnd")
                }

                override fun onAnimationCancel(p0: Animator?) {
                    Log.d("TEST_GAME", "translateWithScale -> onAnimationCancel")

                }

                override fun onAnimationStart(p0: Animator?) {
                    Log.d("TEST_GAME", "translateWithScale -> onAnimationStart")
                }

                override fun onAnimationRepeat(p0: Animation?) {
                    Log.d("TEST_GAME", "2 translateWithScale -> onAnimationRepeat")
                }

                override fun onAnimationEnd(p0: Animation?) {
                    Log.d("TEST_GAME", "2 translateWithScale -> onAnimationEnd")
                }

                override fun onAnimationStart(p0: Animation?) {
                    Log.d("TEST_GAME", "2 translateWithScale -> onAnimationStart")
                }

            })

            target.tag = set

        }

        fun translateWithAlpha(rightView: View, leftView: View, target: AppCompatImageView, durationToIncreaseMoneyGame: Long): Observable<Boolean> {

            return Observable.create { emitter ->

                rightView.alpha = 1f

                leftView.alpha = 1f

                // PARAMS
                // alpha
                val alphaRightView = ObjectAnimator.ofFloat(rightView, "alpha", 0f)
                val alphaLeftView = ObjectAnimator.ofFloat(leftView, "alpha", 0f)

//                val marginTop = convertDpToPixel(20f)
                val translationYRightView = ((target.y - rightView.y))
                val translationYLeftView = ((target.y - leftView.y))
                // translateY
                val translateYRightView = ObjectAnimator.ofFloat(rightView, "translationY", translationYRightView)
                val translateYLeftView = ObjectAnimator.ofFloat(leftView, "translationY", translationYLeftView)

                val translationXRightView = (-(rightView.x - target.x))
                val translationXLeftView = target.x - leftView.x
                // translateX
                val translateXRightView = ObjectAnimator.ofFloat(rightView, "translationX", translationXRightView)
                val translateXLeftView = ObjectAnimator.ofFloat(leftView, "translationX", translationXLeftView)

                // sequential animator
                val set = AnimatorSet()
                set.interpolator = AccelerateDecelerateInterpolator()
                set.playTogether(alphaRightView, alphaLeftView, translateYRightView, translateYLeftView, translateXRightView, translateXLeftView)

                set.duration = durationToIncreaseMoneyGame
                set.start()
                // set the AnimatorSet as a tag, to cancel for animate next time.
                set.addListener(object : Animation.AnimationListener, Animator.AnimatorListener {

                    override fun onAnimationRepeat(p0: Animator?) {

                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        Log.d("TEST_GAME", "11 onAnimationEnd")
                        emitter.onNext(true)
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                        Log.d("TEST_GAME", "AnimationUtil onAnimationCancel")

//                        rightView.translationX = 0f
//                        rightView.translationY = 0f
//                        rightView.alpha = 1f
//
//                        leftView.translationX = 0f
//                        leftView.translationY = 0f
//                        leftView.alpha = 1f
                    }

                    override fun onAnimationStart(p0: Animator?) {
                        Log.d("TEST_GAME", "AnimationUtil onAnimationStart 666")
                    }

                    override fun onAnimationRepeat(p0: Animation?) {

                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        Log.d("TEST_GAME", "2 onAnimationEnd")
                    }

                    override fun onAnimationStart(p0: Animation?) {
                        Log.d("TEST_GAME", "AnimationUtil onAnimationStart 555")
                    }

                })

                rightView.tag = set
                leftView.tag = set
            }
        }

        fun translateWithRotation(playerName: View, imageProfile: View, vs: View, isNeedWaitFinish: Boolean, animateNameImageProfileVsDuration: Long): Completable {

            return Completable.create { emitter ->

                // cancel animator
                if (playerName.tag is Animator) {
                    (playerName.tag as Animator).cancel()
                }

                val rotate = if (imageProfile.translationX < 0) 360f else -360f

                // PARAMS
                // translateX
                val translateXPlayerName = ObjectAnimator.ofFloat(playerName, "translationX", 0f)
                val rotation = ObjectAnimator.ofFloat(imageProfile, "rotation", rotate)
                val translateXImageProfile = ObjectAnimator.ofFloat(imageProfile, "translationX", 0f)

                val alpha = ObjectAnimator.ofFloat(vs, "alpha", 1f)

                // sequential animator
                val set = AnimatorSet()
                set.interpolator = AccelerateDecelerateInterpolator()
                set.playTogether(translateXPlayerName, rotation, translateXImageProfile, alpha)
                set.duration = animateNameImageProfileVsDuration

                set.start()
                // set the AnimatorSet as a tag, to cancel for animate next time.
                set.addListener(object : Animation.AnimationListener, Animator.AnimatorListener {

                    override fun onAnimationRepeat(p0: Animator?) {

                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        Log.d("TEST_GAME", "AnimationUtil -> onAnimationEnd")

                        if (isNeedWaitFinish) {
                            set.cancel()
                            emitter.onComplete()
                        }
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                        Log.d("TEST_GAME", "AnimationUtil -> onAnimationCancel")
//                    set.cancel()
                    }

                    override fun onAnimationStart(p0: Animator?) {
                        Log.d("TEST_GAME", "AnimationUtil -> onAnimationStart 1")

                        if (!isNeedWaitFinish) {
                            emitter.onComplete()
                        }
                    }

                    override fun onAnimationRepeat(p0: Animation?) {}

                    override fun onAnimationEnd(p0: Animation?) {}

                    override fun onAnimationStart(p0: Animation?) {}


                })

                if (!isNeedWaitFinish) {
                    emitter.onComplete()
                }

            }.delay(if (isNeedWaitFinish) 100 else 0, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())


        }

        fun translateY(translate: Float = 0f, duration: Long, vararg views: View): Completable {
            return Completable.create { emitter ->
                for ((index, view) in views.withIndex()) {
                    view.animate()
                            .withLayer()
                            .translationY(translate)
                            .setDuration(duration)
                            .withEndAction {
                                if (index == views.size - 1) emitter.onComplete()
                            }
                            .start()
                }
            }
        }

        fun scaleXY(scale: Float = 0f, duration: Long, vararg views: View): Completable {
            return Completable.create { emitter ->
                for ((index, view) in views.withIndex()) {
                    view.animate()
                            .withLayer()
                            .scaleX(scale)
                            .scaleY(scale)
                            .setDuration(duration)
                            .withEndAction {
                                if (index == views.size - 1) emitter.onComplete()
                            }
                            .start()
                }
            }
        }

        //        fun translateY(view : View, translate : Float = 0f, duration : Long) {
//          view.animate().withLayer().translationY(translate).setDuration(duration).start()
//        }
        fun translateY(target: View, translationY: Float = 0f, duration: Long) {
            // cancel animator
            if (target.tag is Animator) {
                (target.tag as Animator).cancel()
            }

            target.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            target.isDrawingCacheEnabled = true

            val translateY = ObjectAnimator.ofFloat(target, "translationY", translationY).setDuration(duration.toLong())
            // sequential animator
            val set = AnimatorSet()
            set.interpolator = AccelerateDecelerateInterpolator()
            set.play(translateY)

            set.start()
            // set the AnimatorSet as a tag, to cancel for animate next time.
            set.addListener(object : Animation.AnimationListener, Animator.AnimatorListener {

                override fun onAnimationRepeat(p0: Animator?) {
                    Log.d("TEST_GAME", "translateXY -> onAnimationRepeat")

                }

                override fun onAnimationEnd(p0: Animator?) {
                    Log.d("TEST_GAME", "translateY -> onAnimationEnd uuu")
                }

                override fun onAnimationCancel(p0: Animator?) {
                    Log.d("TEST_GAME", "translateXY -> onAnimationCancel")

                }

                override fun onAnimationStart(p0: Animator?) {
                    Log.d("TEST_GAME", "translateXY -> onAnimationStart")
                }

                override fun onAnimationRepeat(p0: Animation?) {
                    Log.d("TEST_GAME", "2 translateXY -> onAnimationRepeat")
                }

                override fun onAnimationEnd(p0: Animation?) {
                    Log.d("TEST_GAME", "2 translateXY -> onAnimationEnd bbbb")
                }

                override fun onAnimationStart(p0: Animation?) {
                    Log.d("TEST_GAME", "2 translateXY -> onAnimationStart")
                }

            })

            target.tag = set

        }

        fun translateXY(target: View, translationX: Float = 0f, translationY: Float = 0f, duration: Long): Completable {

            return Completable.create { emitter ->

                // cancel animator
                if (target.tag is Animator) {
                    (target.tag as Animator).cancel()
                }

                target.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                target.isDrawingCacheEnabled = true

                val translateX = ObjectAnimator.ofFloat(target, "translationX", translationX).setDuration(duration.toLong())
                val translateY = ObjectAnimator.ofFloat(target, "translationY", translationY).setDuration(duration.toLong())
                // sequential animator
                val set = AnimatorSet()
                set.interpolator = AccelerateDecelerateInterpolator()
                set.playTogether(translateX, translateY)

                set.start()
                // set the AnimatorSet as a tag, to cancel for animate next time.
                set.addListener(object : Animation.AnimationListener, Animator.AnimatorListener {

                    override fun onAnimationRepeat(p0: Animator?) {
                        Log.d("TEST_GAME", "translateXY -> onAnimationRepeat")

                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        emitter.onComplete()
                        Log.d("TEST_GAME", "translateXY -> onAnimationEnd kkkk")
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                        Log.d("TEST_GAME", "translateXY -> onAnimationCancel")

                    }

                    override fun onAnimationStart(p0: Animator?) {
                        Log.d("TEST_GAME", "translateXY -> onAnimationStart")
                    }

                    override fun onAnimationRepeat(p0: Animation?) {
                        Log.d("TEST_GAME", "2 translateXY -> onAnimationRepeat")
                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        Log.d("TEST_GAME", "2 translateXY -> onAnimationEnd mkmk")
                    }

                    override fun onAnimationStart(p0: Animation?) {
                        Log.d("TEST_GAME", "2 translateXY -> onAnimationStart")
                    }

                })

                target.tag = set
            }
        }

        fun alpha(view: View, alpha: Float = 0f, duration: Long = 0L) {
            Log.d("TEST_GAME", "AnimationUtil -> alpha")
            view.animate().withLayer().alpha(alpha).setDuration(duration).start()
        }

        fun animateTransitionAlpha(view: View, it: Drawable) {

            val alphaAnimator: ObjectAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 0.0f, 1.0f)
            alphaAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    view.setBackgroundDrawable(it)
                }
            })
            alphaAnimator.setDuration(800)
            alphaAnimator.start()
        }

        fun animateTranslateX(view: View, from: Float, to: Float) {

            val alphaAnimator : ObjectAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, to)
            alphaAnimator.setDuration(800)
            alphaAnimator.startDelay = 400
            alphaAnimator.start()
        }

        fun animateTranslateY(view: View, from: Float, to: Float) {

            val alphaAnimator: ObjectAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, from, to)
            alphaAnimator.setDuration(800)
            alphaAnimator.start()
        }

        fun scaleWithRotation(view : View, animateDuration: Long) : Completable {
            view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            return Completable.create {emitter ->
                // cancel animator
                if (view.tag is Animator) {
                    (view.tag as Animator).cancel()
                }

                val rotate = 360f

                // PARAMS
                // translateX
                val rotation = ObjectAnimator.ofFloat(view, View.ROTATION, rotate)
                val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f)
                val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f)


                // sequential animator
                val set = AnimatorSet()
                set.interpolator = AccelerateDecelerateInterpolator()
                set.playTogether(/*rotation,*/ scaleX, scaleY)
                set.duration = animateDuration

                set.start()
                // set the AnimatorSet as a tag, to cancel for animate next time.
                set.addListener(object : Animation.AnimationListener, Animator.AnimatorListener {

                    override fun onAnimationRepeat(p0: Animator?) {

                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        Log.d("TEST_GAME", "AnimationUtil -> onAnimationEnd")
                        emitter.onComplete()
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                        Log.d("TEST_GAME", "AnimationUtil -> onAnimationCancel")
//                    set.cancel()
                    }

                    override fun onAnimationStart(p0: Animator?) {
                        Log.d("TEST_GAME", "AnimationUtil -> onAnimationStart 1")


                    }

                    override fun onAnimationRepeat(p0: Animation?) {}

                    override fun onAnimationEnd(p0: Animation?) {}

                    override fun onAnimationStart(p0: Animation?) {}


                })
            }



        }


    }

}