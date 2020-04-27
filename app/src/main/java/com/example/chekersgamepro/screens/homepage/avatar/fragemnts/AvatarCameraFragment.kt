package com.example.chekersgamepro.screens.homepage.avatar.fragemnts

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersFragment
import com.example.chekersgamepro.screens.homepage.avatar.AvatarViewModel
import com.example.chekersgamepro.screens.homepage.avatar.ViewPagerManager
import com.example.chekersgamepro.util.CameraUtil
import com.example.chekersgamepro.util.PermissionUtil
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.personal_avatar_camera_item.view.*

class AvatarCameraFragment(private val avatarViewModel: AvatarViewModel) : AvatarFragmentBase(avatarViewModel), CheckersFragment.FragmentLifecycle {

    private lateinit var cameraUtil: CameraUtil

    private var isAlreadyOpenPermission = false

    companion object {
        fun newInstance(viewModel: AvatarViewModel): AvatarCameraFragment {
            return AvatarCameraFragment(viewModel)
        }
    }

    override fun getLayoutResId(): Int = R.layout.personal_avatar_camera_item

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraUtil = CameraUtil(view)

        val permissionText = view.permission_text

        if (PermissionUtil.isAllPermissionsGranted(android.Manifest.permission.CAMERA)) {
            isAlreadyOpenPermission = true
        } else{
            setPermissionMsgVisibility(permissionText, true)
        }

        val permissionGranted = PermissionUtil.isCameraPermissionGranted(activity)

        compositeDisposableOnDestroyed.addAll(
                avatarViewModel
                        .saveImage(activity!!)
                        .distinctUntilChanged()
                        .subscribe(Functions.actionConsumer(cameraUtil::hideButtonsCameraVisibility)),

                cameraUtil.imageFromCamera
                        .subscribeOn(Schedulers.io())
                        .subscribe(avatarViewModel::setChangeAvatarData),

                permissionGranted
                        .subscribe(this::openCameraByState),

                RxView.clicks(permissionText)
                        .flatMap { permissionGranted }
                        .subscribe(this::openCameraByState)

        )
    }

    private fun openCameraByState(isGranted: Boolean) {
        Log.d("TEST_GAM", "openCameraByState openCameraByState")
        val permissionText = view!!.permission_text
        if (isGranted) {
            setPermissionMsgVisibility(permissionText, false)
            cameraUtil.initCamera()
            if (!isAlreadyOpenPermission) startCamera(cameraUtil)
        } else {
            setPermissionMsgVisibility(permissionText, true)
        }
    }

    private fun setPermissionMsgVisibility(textViewMsg: AppCompatTextView, isShow: Boolean) {
        textViewMsg.permission_text.visibility = if (isShow) View.VISIBLE else View.INVISIBLE
        textViewMsg.permission_text.isClickable = isShow
        textViewMsg.permission_text.isEnabled = isShow
    }

    private fun startCamera(cameraUtil: CameraUtil) {
        Observable.just("")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { cameraUtil.startCamera() }
                .subscribe()
    }

}