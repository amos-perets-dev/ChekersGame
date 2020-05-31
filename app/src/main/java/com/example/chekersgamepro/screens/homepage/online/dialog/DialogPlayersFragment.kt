package com.example.chekersgamepro.screens.homepage.online.dialog

import android.animation.Animator
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import com.airbnb.lottie.LottieAnimationView
import com.example.chekersgamepro.R
import com.example.chekersgamepro.screens.homepage.online.OnlineBaseFragment
import com.example.chekersgamepro.views.custom.IBaseViewAnimationAngle
import com.example.chekersgamepro.views.custom.circle.AngleAnimation
import com.example.chekersgamepro.views.custom.circle.CircleImageViewCustom
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dialog_online_players_fragment.view.*
import java.util.concurrent.TimeUnit


class DialogPlayersFragment(imageProfileHp: CircleImageView
                            , private val dialogStateCreator: DialogStateCreator) : OnlineBaseFragment(imageProfileHp) {

//    private val dialogPlayersViewModel by lazy {
////        DialogPlayersInjector.createViewModelFragment(this, dialogStateCreator)
//    }

    companion object {
        fun newInstance(imageProfileHp: CircleImageView, dialogStateCreator: DialogStateCreator): DialogPlayersFragment {
            return DialogPlayersFragment(imageProfileHp, dialogStateCreator)
        }
    }

    private lateinit var imageAnimation: AngleAnimation

    private lateinit var declineRequestGameButton: CircleImageViewCustom

//    private lateinit var closeRequestGameButton: ButtonCustom

    private lateinit var acceptRequestGameButton: LottieAnimationView

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val msg = view.msg_online


//        AnimationUtil.translateWithScale(
//                msg
//                , 1000
//                , 0f
//                , 0f
//                , 1f
//                , Consumer {  msg.setTextWithAnimate(dialogPlayersViewModel.getMsg()) })
//
//
//        this.declineRequestGameButton = view.decline_request_game_button
//        this.acceptRequestGameButton = view.accept_request_game_button
//        this.acceptRequestGameButton.setLayerType(View.LAYER_TYPE_HARDWARE, null)
//
////        this.closeRequestGameButton = view.close_request_game_button
//
//        initLottie(acceptRequestGameButton)
//
////        if (dialogPlayersViewModel.isRequestGameMsg()) {
//        initViewAngleAnimation(declineRequestGameButton, 2)
////        } /*else {
////            initViewAngleAnimation(closeRequestGameButton, 0)
////        }*/
//
//        declineRequestGameButton.setOnTouchListener(
//                TouchListener(View.OnClickListener { dialogPlayersViewModel.onClickDecline() }, 0.8f))
//
////        closeRequestGameButton.setOnTouchListener(
////                TouchListener(View.OnClickListener { dialogPlayersViewModel.onClickClose() }, 0.8f))
//
//        compositeDisposableOnDestroyed.addAll(
//                dialogPlayersViewModel.getLevelPlayer()
//                        .subscribeOn(Schedulers.io())
//                        .subscribe(view.level_player_dialog::setText),
//
//                dialogPlayersViewModel
//                        .getImageProfile()
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(view.image_profile_dialog::setImageBitmap),
//
//                dialogPlayersViewModel.getPlayerName()
//                        .subscribe(view.name_player_dialog::setText),
//
//                animateTouch(
//                        imageAnimation.isAnimationEnd
//                                .filter(Functions.equalsWith(true))
//                ),
//
//                RxView.clicks(acceptRequestGameButton)
//                        .doOnNext { dialogPlayersViewModel.clickOnActionsButtons() }
//                        .doOnNext { acceptRequestGameButton.playAnimation() }
//                        .flatMapCompletable {
//                            finishAnimate(acceptRequestGameButton)
//                                    .doOnEvent { dialogPlayersViewModel.onClickAccept() }
//                        }
//                        .subscribe(),
//
////
////                RxView.clicks(closeRequestGameButton)
////                        .subscribe(Functions.actionConsumer(dialogPlayersViewModel::onClickClose)),
//
//
//                dialogPlayersViewModel
//                        .isAvoidActionsButtons(this)
//                        .subscribe {
//                            cancelDeclineButton()
//                            acceptRequestGameButton.isEnabled = false
////                            closeRequestGameButton.isEnabled = false
//                        },
//
//                msg
//                        .isSetTextAnimationEnd
//                        .doOnNext {
//                            declineRequestGameButton.startAnimation(imageAnimation)
////                            closeRequestGameButton.startAnimation(imageAnimation)
//                        }
//                        .subscribe(),
//
//                dialogPlayersViewModel
//                        .isFinishRequestGame()
//                        .subscribe { dismissAllowingStateLoss() }
//        )
//
//
//        view.total_games_details_dialog.setTextTotalGames(dialogPlayersViewModel.getTotalWin(), dialogPlayersViewModel.getTotalLoss())
//
////        this.declineRequestGameButton.visibility = if (dialogPlayersViewModel.isRequestGameMsg()) View.VISIBLE else View.GONE
//        this.acceptRequestGameButton.visibility = if (dialogPlayersViewModel.isRequestGameMsg()) View.VISIBLE else View.GONE
////        this.closeRequestGameButton.visibility = if (dialogPlayersViewModel.isRequestGameMsg()) View.GONE else View.VISIBLE

    }

    private fun animateTouch(animationEnd: Observable<Boolean>): Disposable {

        val view = /*if (dialogPlayersViewModel.isRequestGameMsg()) {
            (*/declineRequestGameButton /*as View)
        } else {
            (closeRequestGameButton as View)
        }*/

        return animationEnd
                .doOnNext { view.dispatchTouchEvent(getMotionEvent(MotionEvent.ACTION_DOWN)) }
                .delay(70, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe {
                    view.dispatchTouchEvent(getMotionEvent(MotionEvent.ACTION_UP))
                }
    }

    private fun initViewAngleAnimation(view: IBaseViewAnimationAngle, margin: Int) {
//        view.setCircleColor(getColor(R.color.activity_home_page_dialog_players_draw_circle_button_color))
//        view.setCircleWidth(getInteger(R.integer.activity_home_page_dialog_players_draw_circle_stroke_width).toFloat())
//        view.setDurationAnimation(
//                getInteger(R.integer.view_default_draw_circle_animation_duration_dialog_players).toLong())
//        view.setMarginDrawCircle(margin)
//
//        imageAnimation = AngleAnimation(view)
//        imageAnimation.setAngle(true)

    }

    private fun getMotionEvent(action: Int): MotionEvent? {
        // Obtain MotionEvent object
        // Obtain MotionEvent object
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis() + 100
        val x = 0.0f
        val y = 0.0f
// List of meta states found here:     developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        // List of meta states found here:     developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        val metaState = 0
        val motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                action,
                x,
                y,
                metaState
        )
        motionEvent.recycle()
        return motionEvent
    }

    private fun cancelDeclineButton() {
        imageAnimation.cancelAnimate()
        view!!.decline_request_game_button.isEnabled = false
    }

    private fun initLottie(acceptRequestGameButton: LottieAnimationView) {
        acceptRequestGameButton.setAnimation("check_animation.json")
        acceptRequestGameButton.progress = 100f
    }

    private fun finishAnimate(acceptRequestGameButton: LottieAnimationView): Completable {
        return Completable.create { emitter ->
            acceptRequestGameButton.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    emitter.onComplete()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }
            })
        }
    }

    override fun onGlobalLayout() {
        super.onGlobalLayout()
        if (view != null) {
            val view = view!!.dialog_online_players
            val measuredHeight = view.measuredHeight
            val measuredWidth = view.measuredWidth
            val layoutParams = view.layoutParams
            val translationX = (measuredWidth * 0.15)
            val translationY = (measuredHeight * 0.1)
            layoutParams.width = (measuredWidth * 0.85).toInt()
            layoutParams.height = (measuredHeight * 0.9).toInt()
            view.layoutParams = layoutParams
            view.requestLayout()

            view.translationY = (translationY / 2).toFloat()
            view.translationX = (translationX / 2).toFloat()
            view.requestLayout()

            // Init actions buttons

            // Init declineButton
            val layoutParamsDeclineButton = this.declineRequestGameButton.layoutParams
            val viewMeasuredHeight1 = view.measuredHeight
            val viewMeasuredWidth1 = view.measuredWidth
            val factorSize: Int

            factorSize =
                    if (viewMeasuredHeight1 > viewMeasuredWidth1) {
                        viewMeasuredWidth1
                    } else {
                        viewMeasuredHeight1
                    }

            val size = factorSize / 7
            layoutParamsDeclineButton.height = size
            layoutParamsDeclineButton.width = size
            this.declineRequestGameButton.layoutParams = layoutParamsDeclineButton
            this.declineRequestGameButton.requestLayout()

            // Init accept button
            val layoutParamsAcceptButton = this.acceptRequestGameButton.layoutParams
            layoutParamsAcceptButton.height = size
            layoutParamsAcceptButton.width = size
            this.acceptRequestGameButton.layoutParams = layoutParamsAcceptButton
            this.acceptRequestGameButton.requestLayout()

        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.dialog_online_players_fragment
    }
}