package com.example.chekersgamepro.util

import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.chekersgamepro.checkers.CheckersApplication
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
import io.reactivex.Observable

class PermissionUtil {


    companion object{
        private val context = CheckersApplication.create().applicationContext

        fun isPermissionGranted(permission : String) : Boolean{
            return (ContextCompat.checkSelfPermission(context, permission)
                    == PackageManager.PERMISSION_GRANTED)
        }

        fun isAllPermissionsGranted(vararg permissions: String): Boolean = permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        fun isCameraPermissionGranted(activity: Activity?) : Observable<Boolean>{

            return Observable.create {emitter ->
                Dexter.withActivity(activity)
                        .withPermissions(android.Manifest.permission.CAMERA)
                        .withListener(object : BaseMultiplePermissionsListener() {
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

        fun isStoragePermissionGranted(activity: Activity?, writeExternalStorage: String) : Observable<Boolean>{

            return Observable.create {emitter ->
                Dexter.withActivity(activity)
                        .withPermissions( writeExternalStorage)
                        .withListener(object : BaseMultiplePermissionsListener() {
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