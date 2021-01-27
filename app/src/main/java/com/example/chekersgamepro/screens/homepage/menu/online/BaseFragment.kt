package com.example.chekersgamepro.screens.homepage.menu.online

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.AppCompatTextView
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersFragment
import com.example.chekersgamepro.util.DisplayUtil
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.online_base_fragment.view.*

open class BaseFragment : CheckersFragment(), ViewTreeObserver.OnGlobalLayoutListener {
    private lateinit var closeTab: AppCompatTextView

    private var actionOk : AppCompatTextView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener(this)

        this.closeTab = view.close_player_list
        this.closeTab.text = getTitle()
        closeFragmentBySwipe(this.closeTab)
        actionOk = view.action_ok_button
        actionOk?.text = getActionOkButtonText()
        actionOk?.visibility = getActionOkButtonVisibility()
    }

    protected open fun getTitle() = ""
    protected open fun getActionOkButtonText() = ""
    protected open fun getActionOkButtonVisibility() = View.GONE

    override fun onGlobalLayout() {
        view?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

    }

    override fun getViewInflater(inflater: LayoutInflater, container: ViewGroup?): View? {
        val view = inflater.inflate(R.layout.online_base_fragment, container, false)
        view.container_views.addView(LayoutInflater.from(activity).inflate(getLayoutResId(), null, true))
        return view
    }

    protected fun getOnClickActionOk(): Single<Any>? {
//        view?.let { button ->
           return actionOk?.let { RxView.clicks(it) }
                   ?.firstOrError()
//        }
//
//        return null
    }

    override fun onDestroyView() {
        animateImageProfile(100, 0F)
        super.onDestroyView()
    }

    override fun changeWindowSize() {


        if (dialog != null) {
            val window = dialog?.window
            if (window != null) { // height  - to prohibit screen resizing when keyboard is open
                val imageProfile = activity!!.image_profile_hp

                val translationYImageProfile = activity!!.resources.displayMetrics.heightPixels *0.085

                animateImageProfile(300, -translationYImageProfile.toFloat())
                val displayMatrix = DisplayUtil.getDisplayMatrix(activity!!)

                val height = displayMatrix.heightPixels - (imageProfile.bottom * 1.13)

                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, (height.toInt() + translationYImageProfile * 1.1).toInt())

                window.setWindowAnimations(R.style.AvatarFragmentShowHideStyle)
                window.setGravity(Gravity.BOTTOM)
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
    }

    private fun animateImageProfile(duration : Long, translationY : Float){
        val imageProfile = activity!!.image_profile_hp

        imageProfile.animate()
                .translationY(translationY)
                .setDuration(duration)
                .start()
    }

}