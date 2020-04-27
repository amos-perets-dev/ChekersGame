package com.example.chekersgamepro.checkers

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.chekersgamepro.R
import com.example.chekersgamepro.util.SwipeUtil
import io.reactivex.disposables.CompositeDisposable

open class CheckersFragment : AppCompatDialogFragment(), ViewTreeObserver.OnGlobalLayoutListener {

    protected val compositeDisposableOnDestroyed = CompositeDisposable()

    private val lifecycleListenerList  = ArrayList<LifecycleListener>()

    protected val checkersApplication = CheckersApplication.create()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

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

    override fun onDestroy() {
        notifyOnDestroy()
        this.compositeDisposableOnDestroyed.clear()
        Log.d("TEST_GAME", "AvatarPickerFragment -> onDestroy")

        super.onDestroy()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    protected fun closeFragmentBySwipe(view: View){
        view.setOnTouchListener(SwipeUtil(context, this::closeFragment))
    }

    protected fun closeFragment(){
        dismissAllowingStateLoss()
    }

    public interface LifecycleListener{
        fun onDestroy()
    }

    interface FragmentLifecycle {
        fun onPauseFragment(){

        }
        fun onResumeFragment(newPosition: Int){

        }
        fun isRightDirection(isRightDirection : Boolean){

        }
    }

    override fun onGlobalLayout() {

    }



    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            val window = dialog!!.window
            if (window != null) { // height  - to prohibit screen resizing when keyboard is open
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                // window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                window.setWindowAnimations(R.style.AvatarFragmentShowHideStyle)
                window.setGravity(Gravity.BOTTOM)
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
    }

}