package com.example.chekersgamepro.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.chekersgamepro.checkers.CheckersApplication
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import io.reactivex.Observable
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener as BaseMultiplePermissionsListener1

class PermissionUtil {


    companion object{
        private val checkersApplication = CheckersApplication.create()
        private val context = checkersApplication.applicationContext

        fun isPermissionGranted(permission : String) : Boolean{
            return (ContextCompat.checkSelfPermission(context, permission)
                    == PackageManager.PERMISSION_GRANTED)
        }

        fun isAllPermissionsGranted(vararg permissions: String): Boolean = permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        fun isCameraPermissionGranted(context: Context) : Observable<Boolean>{
            return isPermissionGranted(context, android.Manifest.permission.CAMERA)

        }

        fun isStoragePermissionGranted(context: Context) : Observable<Boolean>{

           return isPermissionGranted(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        private fun isPermissionGranted(context: Context, permission: String) : Observable<Boolean>{
            return Observable.create {emitter ->
                Dexter.withActivity(context as Activity?)
                        .withPermissions( permission)
                        .withListener(object : BaseMultiplePermissionsListener1() {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                                super.onPermissionsChecked(report)
                                if (report.areAllPermissionsGranted()) {
                                    emitter.onNext(true)
                                } else {
                                    emitter.onNext(false)
                                }
                            }
                        })
                        .check()
            }
        }

        fun isStorageAndCameraPermissionGranted(context: Context) : Observable<Boolean>{

            return Observable.create {emitter ->
                Dexter.withActivity(context as Activity?)
                        .withPermissions( android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)
                        .withListener(object : BaseMultiplePermissionsListener1() {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                                super.onPermissionsChecked(report)
                                if (report.areAllPermissionsGranted()) {
                                    emitter.onNext(true)
                                } else {
                                    emitter.onNext(false)
                                }
                            }
                        })
                        .check()
            }
        }

    }

}