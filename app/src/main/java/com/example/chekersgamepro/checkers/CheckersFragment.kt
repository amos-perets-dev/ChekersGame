package com.example.chekersgamepro.checkers

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatDialogFragment
import com.bumptech.glide.manager.LifecycleListener
import com.example.chekersgamepro.R
import com.example.chekersgamepro.util.SwipeUtil
import io.reactivex.disposables.CompositeDisposable

open class CheckersFragment : AppCompatDialogFragment(), ViewTreeObserver.OnGlobalLayoutListener {

    protected val compositeDisposableOnDestroyed = CompositeDisposable()

    private val lifecycleListenerList  = ArrayList<LifecycleListener>()

    private val checkersApplication = CheckersApplication.create()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        return getViewInflater(inflater, container)
    }

    protected open fun getViewInflater(inflater: LayoutInflater, container: ViewGroup?): View? {
        return inflater.inflate(getLayoutResId(), container, false)

    }

    protected open fun getLayoutResId() : Int =  0

    private fun notifyOnDestroy(){
        lifecycleListenerList.forEach { it.onDestroy() }
    }

    protected fun addListener(lifecycleListener : LifecycleListener){
        lifecycleListenerList.add(lifecycleListener)
    }

    protected fun getInteger(resId: Int) = checkersApplication.getInteger(resId)

    protected fun getColor(resId: Int) = checkersApplication.getColorRes(resId)

    override fun onDestroy() {

        notifyOnDestroy()
        this.compositeDisposableOnDestroyed.clear()

        super.onDestroy()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.actionBar?.hide()

    }

    protected fun closeFragmentBySwipe(view: View){
        view.setOnTouchListener(SwipeUtil(context, this::closeFragment))
    }

    private fun closeFragment(){
        dismissAllowingStateLoss()
    }

    public interface LifecycleListener{
        fun onDestroy()
    }

    interface FragmentLifecycle {
        fun onPauseFragment(){
            Log.d("TEST_GAME", "CheckersFragment -> onPauseFragment")

        }
        fun onResumeFragment(newPosition: Int){
            Log.d("TEST_GAME", "CheckersFragment -> onResumeFragment")
        }
        fun isRightDirection(isRightDirection : Boolean){
            Log.d("TEST_GAME", "CheckersFragment -> isRightDirection")

        }
    }

    override fun onGlobalLayout() {

    }

    override fun onStart() {
        super.onStart()
        changeWindowSize()
    }

    protected open fun changeWindowSize(){
        if (dialog != null) {
            val window = dialog?.window
            if (window != null) { // height  - to prohibit screen resizing when keyboard is open
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                // window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                window.setWindowAnimations(R.style.AvatarFragmentShowHideStyle)
                window.setGravity(Gravity.BOTTOM)
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window.setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
            }
        }
    }


}