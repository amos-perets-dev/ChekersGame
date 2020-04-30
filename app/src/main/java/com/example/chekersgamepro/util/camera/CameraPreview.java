package com.example.chekersgamepro.util.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.example.chekersgamepro.R;
import com.example.chekersgamepro.checkers.CheckersApplication;
import com.example.chekersgamepro.screens.homepage.avatar.AvatarState;
import com.example.chekersgamepro.screens.homepage.avatar.model.data.AvatarData;
import com.example.chekersgamepro.views.custom.ImageViewShadow;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;

@SuppressLint("ViewConstructor")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera camera;

    private SurfaceHolder surfaceHolder;

    private Camera.ShutterCallback shutterCallback;

    private Camera.PictureCallback jpegCallback;

    private ImageViewShadow refresh, shutter;

    private PublishSubject<AvatarData> image = PublishSubject.create();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.surfaceHolder = getHolder();
        this.surfaceHolder.addCallback(this);
        this.surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        /** Handles data for jpeg picture */
        this.shutterCallback = () -> { };

        this.jpegCallback = this::onPictureTaken;
    }

    public void initCamera(View view) {
        Log.d("TEST_GAMe", "CameraUtil -> initCamera");

        this.shutter = view.findViewById(R.id.shutter_photo);
        this.refresh = view.findViewById(R.id.refresh_photo);

        this.refresh.setOnClickListener(v -> notifyCaptureOrRefresh(null));
        this.shutter.setOnClickListener(v -> captureImage());
    }

    public static Bitmap createBitmapFromData(byte[] data) {
        int width;
        int height;
        Matrix matrix = new Matrix();
        Camera.CameraInfo info = new Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);
        // Convert ByteArray to Bitmap
        Bitmap bitPic = BitmapFactory.decodeByteArray(data, 0, data.length);
        width = bitPic.getWidth();
        height = bitPic.getHeight();

        // Perform matrix rotations/mirrors depending on camera that took the photo
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            float[] mirrorY = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
            Matrix matrixMirrorY = new Matrix();
            matrixMirrorY.setValues(mirrorY);

            matrix.postConcat(matrixMirrorY);
        }

        matrix.postRotate(90);


        // Create new Bitmap out of the old one
        Bitmap bitPicFinal = Bitmap.createBitmap(bitPic, 0, 0, width, height, matrix, true);
        bitPic.recycle();
        int desWidth;
        int desHeight;
        desWidth = bitPicFinal.getWidth();
        desHeight = desWidth;
        Bitmap croppedBitmap = Bitmap.createBitmap(bitPicFinal, 0, bitPicFinal.getHeight() / 2 - bitPicFinal.getWidth() / 2, desWidth, desHeight);
        croppedBitmap = Bitmap.createScaledBitmap(croppedBitmap, 528, 528, true);
        return croppedBitmap;
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

    private Camera.Parameters getCameraParam() {

        if (null == camera) return null;

        Camera.Parameters parameters = camera.getParameters();
        //modify parameter
        parameters.setAutoExposureLock(false);
        parameters.setAutoWhiteBalanceLock(false);
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();

        Camera.Size optimalPreviewSize =
                getOptimalPreviewSize(
                        supportedPreviewSizes, getMeasuredWidth(), getMeasuredHeight());
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
            setCameraDisplayOrientation(CheckersApplication.create().getActivityByContext(getContext())
                    , CAMERA_FACING_FRONT, camera);
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

    public void setCameraDisplayOrientation(Activity activity,
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
//        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        result = (info.orientation + degrees) % 360;
        result = (360 - result) % 360;  // compensate the mirror
//        } else {  // back-facing
//            result = (info.orientation - degrees + 360) % 360;
//        }
        camera.setDisplayOrientation(result);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int arg1, int arg2, int arg3) {
        Log.d("TEST_GAME", "surfaceChanged - jpeg");


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("TEST_GAME", "surfaceCreated - jpeg");
        compositeDisposable.add(
                Observable.just(new Object())
                        .observeOn(Schedulers.io())
                        .doOnNext(Functions.actionConsumer(this::startCamera))
                        .subscribe()
        );
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("TEST_GAME", "surfaceDestroyed - jpeg");
        if (camera != null ){
            camera.stopPreview();
            camera.release();
        }
        compositeDisposable.clear();

    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

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
        if (refresh != null && shutter != null) {
            refresh.setVisibility(GONE);
            shutter.setVisibility(GONE);
        }
    }

    private void onPictureTaken(byte[] data, Camera camera) {
        Bitmap bitmap = createBitmapFromData(data);

        notifyCaptureOrRefresh(bitmap);

        camera.startPreview();
    }
}

