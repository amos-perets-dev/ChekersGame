package com.example.chekersgamepro.screens.homepage.menu.online.dialog

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.Window
import androidx.core.text.TextUtilsCompat
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersActivity
import com.example.chekersgamepro.util.TouchListener
import com.example.chekersgamepro.util.animation.AnimationUtil
import com.example.chekersgamepro.views.custom.IBaseViewAnimationAngle
import com.example.chekersgamepro.views.custom.circle.AngleAnimation
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.internal.functions.Functions
import kotlinx.android.synthetic.main.dialog_online_players_fragment.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KFunction0


class DialogOnlinePlayersActivity : CheckersActivity() {

    private val ANIMATE_ACTIONS_BUTTONS_DURATION = getInteger(R.integer.activity_home_page_dialog_players_animate_actions_buttons_duration)
    private val ANIMATE_MSG_BOX_OPEN_DURATION = getInteger(R.integer.activity_home_page_dialog_players_animate_msg_box_open_duration).toLong()
    private val DELAY_CLOSE_SCREEN = getInteger(R.integer.activity_home_page_dialog_players_delay_close_screen).toLong()

    private val dialogPlayersViewModel by lazy {
        DialogPlayersInjector.createViewModelActivity(this)
    }

    companion object {
        var isDialogPlayersAlreadyOpen = false
    }

    private val waitingPlayerMsg by lazy {
        waiting_player_msg_dialog
    }

    private val totalGames by lazy {
        total_games_details_dialog
    }

    private val declineRequestGameButton by lazy {
        decline_request_game_button
    }

    private val acceptRequestGameButton by lazy {
        accept_request_game_button
    }

    private val compositeDisposable = CompositeDisposable()

    private lateinit var imageAnimation: AngleAnimation

    private var isLeftToRight = false


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("TEST_GAME", "DialogOnlinePlayersActivity onCreate")
        supportPostponeEnterTransition()
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        window.setGravity(Gravity.TOP)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_online_players_fragment)

        this.isLeftToRight = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR

        initViewAngleAnimation(this.declineRequestGameButton)

        this.waitingPlayerMsg.setOnTouchListener(this::touchOnWaitingPlayerMsg)

        setPlayerDetailsAsync()

        touchListener(this.declineRequestGameButton, dialogPlayersViewModel::onClickCancel)
        touchListener(this.acceptRequestGameButton, dialogPlayersViewModel::onClickAccept)

        compositeDisposable.add(getDialogStateAsync())

        compositeDisposable.add(getMsgTextAsync())

        compositeDisposable.add(isAnimationCircleFinishAsync())

        compositeDisposable.add(timerCloseRequestGameAsync())

        compositeDisposable.add(setTextAnimationFinishAsync())

        compositeDisposable.add(isClickOnActionsButtonsAsync())

        compositeDisposable.add(dialogPlayersViewModel
                .acceptOnlineGame()
                .subscribe { finish() }
        )

        compositeDisposable.add(
                dialogPlayersViewModel
                        .isClickOnBackPressInvalid(this)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { AnimationUtil.animatePulse(this.waitingPlayerMsg, 1.1f) }
        )
    }

    private fun isClickOnActionsButtonsAsync() =
            this.dialogPlayersViewModel
                    .isClickOnActionsButtons(this)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(this::changeAvailabilityActionsButton)
                    .delay(DELAY_CLOSE_SCREEN, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                    .subscribe { finish() }

    private fun setTextAnimationFinishAsync() =
            msg_online
                    .isSetTextAnimationFinish
                    .flatMap { dialogPlayersViewModel.getDialogState() }
                    .flatMapCompletable(this::animateActionButtonsByState)
                    .subscribe()

    private fun timerCloseRequestGameAsync() =
            this.dialogPlayersViewModel
                    .getMsgDuration(ANIMATE_ACTIONS_BUTTONS_DURATION)
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(this::timerHideCloseRequestIcon)
                    .subscribe()

    private fun getMsgTextAsync() =
            this.dialogPlayersViewModel
                    .getMsgText()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { imageAnimation.clearAnimate() }
                    .subscribe(this::setMsgText)

    private fun getDialogStateAsync() =
            this.dialogPlayersViewModel
                    .getDialogState()
                    .subscribe(this::changeViewsByDialogState)

    private fun isAnimationCircleFinishAsync() =
            this.imageAnimation.isAnimationCircleFinish
                    .filter(Functions.equalsWith(true))
                    .doOnNext(this::dispatchEventDown)
                    .delay(70, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                    .subscribe(this::dispatchEventUp)

    private fun animateActionButtonsByState(state: DialogState): Completable {
        val completable =
                if (state.ordinal == DialogState.MSG_WITH_BUTTONS.ordinal) {
                    changeVisibilityAndScaleButtonsByState(false)
                    AnimationUtil.scaleXY(1f, ANIMATE_ACTIONS_BUTTONS_DURATION.toLong(), this.declineRequestGameButton, this.acceptRequestGameButton)
                } else {
                    Completable.complete()
                }

        return completable
                .doOnEvent {
                    if (state.ordinal == DialogState.MSG_WITH_BUTTONS.ordinal) {
                        changeVisibilityAndScaleButtonsByState(true)
                    }
                    declineRequestGameButton.startAnimation(imageAnimation)
                }
    }


    private fun timerHideCloseRequestIcon(duration: Long): Observable<Long> {
        return Observable.timer(duration, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnNext { waiting_player_msg_dialog.isEnabled = false }
                .doOnNext { this.dialogPlayersViewModel.timerFinishToCloseRequestGame() }
                .doOnNext(this::setCancelRequestGameIcon)
    }

    private fun setCancelRequestGameIcon(ignored: Long) {
        val drawable = getDrawable(R.drawable.ic_cancel_request_game_unavailable)
        if (this.isLeftToRight) {
            waiting_player_msg_dialog.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        } else {
            waiting_player_msg_dialog.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }
    }

    private fun setMsgText(msgText: String) {
        AnimationUtil.translateWithScale(
                msg_online
                , ANIMATE_MSG_BOX_OPEN_DURATION
                , 0f
                , 0f
                , 1f
                , Consumer { msg_online.setTextWithAnimate(msgText) })
    }

    private fun dispatchEventDown(ignored: Boolean) {
        this.declineRequestGameButton.dispatchTouchEvent(getMotionEvent(MotionEvent.ACTION_DOWN))
    }

    private fun dispatchEventUp(ignored: Boolean) {
        this.declineRequestGameButton.dispatchTouchEvent(getMotionEvent(MotionEvent.ACTION_UP))
    }

    private fun changeViewsByDialogState(it: DialogState) {
        when (it.ordinal) {
            DialogState.WAITING.ordinal -> {
                Log.d("TEST_GAME", "DialogOnlinePlayersActivity -> changeViewsByDialogState->  WAITING")

                declineRequestGameButton.visibility = View.GONE
                acceptRequestGameButton.visibility = View.GONE
                waitingPlayerMsg.visibility = View.VISIBLE

            }
            DialogState.MSG_ONLY.ordinal -> {
                Log.d("TEST_GAME", "DialogOnlinePlayersActivity -> changeViewsByDialogState->  MSG_ONLY")
                imageAnimation.clearAnimate()
                declineRequestGameButton.visibility = View.VISIBLE
                acceptRequestGameButton.visibility = View.GONE
                waitingPlayerMsg.visibility = View.GONE

            }
            DialogState.MSG_WITH_BUTTONS.ordinal -> {
                Log.d("TEST_GAME", "DialogOnlinePlayersActivity -> changeViewsByDialogState->  MSG_WITH_BUTTONS")
                declineRequestGameButton.visibility = View.GONE
                acceptRequestGameButton.visibility = View.GONE
                waitingPlayerMsg.visibility = View.GONE
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun touchListener(view: View, action: KFunction0<Unit>) {
        view.setOnTouchListener(TouchListener(View.OnClickListener { action.invoke() }, 0.8f))
    }

    private fun changeVisibilityAndScaleButtonsByState(isVisible: Boolean) {
        changeVisibilityAndScaleButtons(this.acceptRequestGameButton, isVisible)
        changeVisibilityAndScaleButtons(this.declineRequestGameButton, isVisible)
    }

    private fun changeVisibilityAndScaleButtons(view: View, isVisible: Boolean) {
        view.visibility = View.VISIBLE
        view.scaleX = if (isVisible) 1f else 0f
        view.scaleY = if (isVisible) 1f else 0f
        view.isEnabled = isVisible
    }

    private fun setPlayerDetailsAsync() {
        var counter = 0

        this.compositeDisposable.addAll(

                this.dialogPlayersViewModel.getRemotePlayerAvatar()
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(image_profile_dialog::setImageBitmap)
                        .doOnNext { supportStartPostponedEnterTransition() }
                        .subscribe(),

                this.dialogPlayersViewModel.getRemotePlayerLevel()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(level_player_dialog::setText),

                this.dialogPlayersViewModel
                        .getRemotePlayerName()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(name_player_dialog::setText),

                this.dialogPlayersViewModel.getWaitingText()
                        .distinctUntilChanged()
                        .observeOn(AndroidSchedulers.mainThread())
                        .map { computeCenterAndGenerateText(it) }
                        .flatMap { text ->
                            Observable.interval(300, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                                    .map { counter++ }
                                    .map { getThreeDotsTextByInterval(it, text) }
                                    .doOnNext { this.waitingPlayerMsg.text = it }
                                    .flatMap { this.dialogPlayersViewModel.isWaitingPlayer() }
                                    .doOnNext { Log.d("TEST_GAME", "isWaitingPlayer: $it") }
                                    .takeUntil { !it }
                                    .filter(Functions.equalsWith(false))
                        }
                        .subscribe(),

                this.dialogPlayersViewModel.getRemotePlayerTotalGames()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::setTotalGames)
        )
    }

    private fun touchOnWaitingPlayerMsg(v: View, event: MotionEvent): Boolean {

        Log.d("TEST_GAME", "isLeftToRight: $this.isLeftToRight")

        val DRAWABLE_LEFT = 0
        val DRAWABLE_RIGHT = 2

        if (event.action == MotionEvent.ACTION_UP) {
            val computeDrawableClick =
                    if (this.isLeftToRight)
                        event.rawX >= waitingPlayerMsg.right - waitingPlayerMsg.compoundDrawables[DRAWABLE_RIGHT].bounds.width()
                    else
                        (event.rawX <= (waitingPlayerMsg.compoundDrawables[DRAWABLE_LEFT].bounds.width()))

            if (computeDrawableClick) {

                Log.d("TEST_GAME", "CLICK ON DRAWABLE")
                this.dialogPlayersViewModel.onClickCancelRequestGame()
                return true
            }
        }
        return false
    }

    private fun changeAvailabilityActionsButton(ignored: Boolean) {
        cancelDeclineButton()

        this.acceptRequestGameButton.isEnabled = false

        this.waitingPlayerMsg.isEnabled = false
        this.waitingPlayerMsg.isClickable = false
        this.waitingPlayerMsg.isActivated = false
    }

    private fun cancelDeclineButton() {
        this.imageAnimation.cancelAnimate()
        this.declineRequestGameButton.isEnabled = false
    }

    private fun getMotionEvent(action: Int): MotionEvent? {

        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis() + 100
        val x = 0.0f
        val y = 0.0f

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

    private fun initViewAngleAnimation(view: IBaseViewAnimationAngle) {
        view.setCircleColor(getColorInt(R.color.activity_home_page_dialog_players_draw_circle_button_color))
        view.setCircleWidth(getInteger(R.integer.activity_home_page_dialog_players_draw_circle_stroke_width).toFloat())
        view.setDurationAnimation(
                getInteger(R.integer.view_default_draw_circle_animation_duration_dialog_players).toLong())
        this.imageAnimation = AngleAnimation(view)
        this.imageAnimation.setAngle(true)

    }

    private fun setTotalGames(pairTotalGames: Pair<String, String>) {
        totalGames.setTextTotalGames(pairTotalGames.first, pairTotalGames.second)
    }

    private fun computeCenterAndGenerateText(waitingText: String): String {

        val textThreeDots = "$waitingText..."
        val bounds = Rect()
        val textPaint: Paint = this.waitingPlayerMsg.paint
        textPaint.getTextBounds(textThreeDots, 0, textThreeDots.length, bounds)
        val textWidth: Int = bounds.width()
        val paddingLeft = ((this.waitingPlayerMsg.measuredWidth - textWidth) * 0.35).toInt()
        val paddingRight = (this.waitingPlayerMsg.measuredWidth * 0.05).toInt()

        if (this.isLeftToRight) {
            this.waitingPlayerMsg.setPadding(paddingLeft, 0, paddingRight, 0)

        } else {
            this.waitingPlayerMsg.setPadding(paddingRight, 0, paddingLeft, 0)

        }

        return waitingText
    }

    private fun getThreeDotsTextByInterval(counter: Int, text: String): String {
        val divCounter = counter % 4
        return when (divCounter) {
            1 -> "$text."
            2 -> "$text.."
            3 -> "$text..."
            else -> text
        }
    }

    override fun onResume() {
        super.onResume()
        isDialogPlayersAlreadyOpen = true
    }

    override fun onPause() {
        super.onPause()
        isDialogPlayersAlreadyOpen = false
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
    }

    override fun onDestroy() {
        cancelDeclineButton()
        compositeDisposable.dispose()
        super.onDestroy()
    }

    override fun onBackPressed() {
        this.dialogPlayersViewModel.onBackPress()
    }
}