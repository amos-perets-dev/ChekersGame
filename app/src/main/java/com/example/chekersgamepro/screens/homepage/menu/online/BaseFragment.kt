package com.example.chekersgamepro.screens.homepage.menu.online

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.AppCompatTextView
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersFragment
import com.example.chekersgamepro.util.DisplayUtil
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.online_base_fragment.view.*

open class BaseFragment : CheckersFragment(), ViewTreeObserver.OnGlobalLayoutListener {
    protected lateinit var closeTab: AppCompatTextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener(this)

        this.closeTab = view.close_player_list
        this.closeTab.text = getTitle()
        closeFragmentBySwipe(this.closeTab)

    }

    protected open fun getTitle() = ""

    override fun onGlobalLayout() {
        view?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

    }

    override fun getViewInflater(inflater: LayoutInflater, container: ViewGroup?): View? {
        val view = inflater.inflate(R.layout.online_base_fragment, container, false)
        view.container_views.addView(LayoutInflater.from(activity).inflate(getLayoutResId(), null, true))
        return view
    }

    override fun changeWindowSize() {

        if (dialog != null) {
            val window = dialog!!.window
            if (window != null) { // height  - to prohibit screen resizing when keyboard is open
                val displayMatrix = DisplayUtil.getDisplayMatrix(activity!!)
                val imageProfile = activity!!.image_profile_hp

                val height = displayMatrix.heightPixels - (imageProfile.bottom * 1.13)

                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, height.toInt())

                window.setWindowAnimations(R.style.AvatarFragmentShowHideStyle)
                window.setGravity(Gravity.BOTTOM)
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
    }

}