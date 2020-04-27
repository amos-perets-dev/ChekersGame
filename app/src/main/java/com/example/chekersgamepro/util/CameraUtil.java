package com.example.chekersgamepro.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Environment;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.example.chekersgamepro.checkers.CheckersApplication;
import com.example.chekersgamepro.R;
import com.example.chekersgamepro.checkers.CheckersImageUtil;
import com.example.chekersgamepro.screens.homepage.avatar.AvatarState;
import com.example.chekersgamepro.screens.homepage.avatar.model.data.AvatarData;
import com.example.chekersgamepro.views.custom.ImageViewShadow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
import static android.view.View.GONE;

@SuppressLint("ViewConstructor")
public class CameraUtil implements SurfaceHolder.Callback {

    private static final int ORIENTATION_PORTRAIT_NORMAL = 1;
    private static final int ORIENTATION_PORTRAIT_INVERTED = 2;
    private static final int ORIENTATION_LANDSCAPE_NORMAL = 3;
    private static final int ORIENTATION_LANDSCAPE_INVERTED = 4;

    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera.ShutterCallback shutterCallback;
    private Camera.PictureCallback jpegCallback;


    private ImageViewShadow refresh, shutter;
    private View view;
    private Activity activity;

    private OrientationEventListener mOrientationEventListener;
    private int mOrientation = -1;

    private int degrees;

    private PublishSubject<AvatarData> image = PublishSubject.create();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    @SuppressLint("WrongConstant")
    public CameraUtil(View view) {

        this.activity = CheckersApplication.create().getActivityByContext(view.getContext());
        this.activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR | ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.view = view;

    }

    public void initCamera() {
        Log.d("TEST_GAMe", "CameraUtil -> initCamera");

        this.shutter = view.findViewById(R.id.shutter_photo);
        this.refresh = view.findViewById(R.id.refresh_photo);

        this.refresh.setOnClickListener(v -> notifyCaptureOrRefresh(null));
        this.shutter.setOnClickListener(v -> captureImage());

        this.surfaceView = view.findViewById(R.id.camera);
        this.surfaceHolder = surfaceView.getHolder();
        this.surfaceHolder.addCallback(this);
        this.surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        /** Handles data for jpeg picture */
        this.shutterCallback = new Camera.ShutterCallback() {
            @Override
            public void onShutter() {

            }
        };
        this.jpegCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                Log.d("TEST_GAME", "onPictureTaken'd");
                String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "0000" + ".jpeg";

//                File pictureFile = new File(fileName);

//                try {
//                    FileOutputStream fos = new FileOutputStream(pictureFile);

                    Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length);

                    Bitmap rotatedImage = getRotatedImage(realImage, degrees);

                    notifyCaptureOrRefresh(rotatedImage);

                    camera.startPreview();

//                    byte[] byteArrayFromBitmap = CheckersImageUtil.create().createByteArrayFromBitmap(rotatedImage);
//                    fos.write(byteArrayFromBitmap);
//                    fos.close();

//                } catch (FileNotFoundException e) {
//                    Log.d("Info", "File not found: " + e.getMessage());
//                } catch (IOException e) {
//                    Log.d("TAG", "Error accessing file: " + e.getMessage());
//                }
            }
        };

        if (mOrientationEventListener == null) {
            mOrientationEventListener = new OrientationEventListener(activity, SensorManager.SENSOR_DELAY_NORMAL) {

                @Override
                public void onOrientationChanged(int orientation) {

                    // determine our orientation based on sensor response
                    int lastOrientation = mOrientation;

                    if (orientation >= 315 || orientation < 45) {
                        if (mOrientation != ORIENTATION_PORTRAIT_NORMAL) {
                            mOrientation = ORIENTATION_PORTRAIT_NORMAL;
                        }
                    } else if (orientation < 315 && orientation >= 225) {
                        if (mOrientation != ORIENTATION_LANDSCAPE_NORMAL) {
                            mOrientation = ORIENTATION_LANDSCAPE_NORMAL;
                        }
                    } else if (orientation < 225 && orientation >= 135) {
                        if (mOrientation != ORIENTATION_PORTRAIT_INVERTED) {
                            mOrientation = ORIENTATION_PORTRAIT_INVERTED;
                        }
                    } else { // orientation <135 && orientation > 45
                        if (mOrientation != ORIENTATION_LANDSCAPE_INVERTED) {
                            mOrientation = ORIENTATION_LANDSCAPE_INVERTED;
                        }
                    }

                    if (lastOrientation != mOrientation) {
                        changeRotation(mOrientation, lastOrientation);
                    }
                }
            };
        }
        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }
    }

    private void notifyCaptureOrRefresh(Bitmap bitmap) {
        AvatarState avatarState = bitmap == null
                ? AvatarState.REFRESH_AVATAR_CAMERA
                : AvatarState.CAPTURE_AVATAR;
        image.onNext(new AvatarData(avatarState, bitmap));
    }

    public Observable<AvatarData> getImageFromCamera() {
        return image.hide();
    }

    /**
     * Performs required action to accommodate new orientation
     *
     * @param orientation
     * @param lastOrientation
     */
    private void changeRotation(int orientation, int lastOrientation) {
        switch (orientation) {
            case ORIENTATION_PORTRAIT_NORMAL:
                degrees = 270;
                Log.v("CameraActivity", "Orientation = 90");
                break;
            case ORIENTATION_LANDSCAPE_NORMAL:
                degrees = 0;

                Log.v("CameraActivity", "Orientation = 0");
                break;
            case ORIENTATION_PORTRAIT_INVERTED:
                degrees = 90;

                Log.v("CameraActivity", "Orientation = 270");
                break;
            case ORIENTATION_LANDSCAPE_INVERTED:
                degrees = 180;

                Log.v("CameraActivity", "Orientation = 180");
                break;
        }
    }

    /**
     * Rotates given Drawable
     *
     * @param degrees Rotate drawable by Degrees
     * @return Rotated Drawable
     */
    private Bitmap getRotatedImage(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Camera.Parameters getCameraParam() {

        if (null == camera) return null;

        Camera.Parameters parameters = camera.getParameters();
        //modify parameter
        parameters.setAutoExposureLock(false);
        parameters.setAutoWhiteBalanceLock(false);
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();

        Camera.Size optimalPreviewSize =
                getOptimalPreviewSize(
                        supportedPreviewSizes, surfaceView.getMeasuredWidth(), surfaceView.getMeasuredHeight());
        parameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);

        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        return parameters;
    }

    private void captureImage() {
        camera.takePicture(shutterCallback, null, jpegCallback);
    }

    public void startCamera() {
        Log.d("TEST_GAME", "start_camera");

        try {
            camera = Camera.open(CAMERA_FACING_FRONT);
            Camera.Parameters param = getCameraParam();
            setCameraDisplayOrientation(activity, CAMERA_FACING_FRONT, camera);
            camera.setPreviewDisplay(surfaceHolder);

            camera.setParameters(param);

            camera.startPreview();

        } catch (RuntimeException e) {
            Log.d("TEST_GAME", "1 init_camera: " + e);
            return;
        } catch (IOException e) {
            Log.d("TEST_GAME", "2 init_camera: " + e);

            e.printStackTrace();
        }


    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private int getRotate() {

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(CAMERA_FACING_FRONT, info);
        Activity activity = CheckersApplication.create().getActivityByContext(view.getContext());
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; //Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; //Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;//Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;//Landscape right
        }
        int rotate = (info.orientation - degrees + 360) % 360;
        return rotate;
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int arg1, int arg2, int arg3) {
        Log.d("TEST_GAME", "surfaceChanged - jpeg");


    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("TEST_GAME", "surfaceCreated - jpeg");
        compositeDisposable.add(
                Observable.just(new Object())
                        .observeOn(Schedulers.io())
                        .doOnNext(Functions.actionConsumer(this::startCamera))
                        .subscribe()
        );
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("TEST_GAME", "surfaceDestroyed - jpeg");
        camera.stopPreview();
        camera.release();
        mOrientationEventListener.disable();
        compositeDisposable.clear();

    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void hideButtonsCameraVisibility() {
        if (refresh != null && shutter != null){
            refresh.setVisibility(GONE);
            shutter.setVisibility(GONE);
        }
    }
}

