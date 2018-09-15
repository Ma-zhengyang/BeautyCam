package com.android.mazhengyang.beautycam;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.mazhengyang.beautycam.Animation.RotateAnimation;
import com.android.mazhengyang.beautycam.Util.SobelUtil;
import com.android.mazhengyang.beautycam.ui.CameraView;
import com.android.mazhengyang.beautycam.ui.PermissionCallback;
import com.wang.avi.AVLoadingIndicatorView;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by mazhengyang on 18-9-13.
 */

public class CameraActivity extends Activity {

    private static final String TAG = CameraActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_CAMERA = 800;
    private static final int PERMISSIONS_EXTERNAL_STORAGE = 801;

    private CameraView cameraView;
    private ImageView takePhotoBtn;
    private ImageView reverseCameraBtn;

    private ImageView sobelPhoto;
    private boolean sobelPhotoShow = false;

    private AVLoadingIndicatorView mProgress;

    private PermissionCallback permissionCallback = new PermissionCallback() {
        @Override
        public boolean onRequestPermission() {
            ActivityCompat.requestPermissions(CameraActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraView = findViewById(R.id.camera_view);
        cameraView.getCameraControl().setPermissionCallback(permissionCallback);

        takePhotoBtn = findViewById(R.id.take_photo_button);
        takePhotoBtn.setOnClickListener(takeButtonOnClickListener);

        reverseCameraBtn = findViewById(R.id.reverse);
        reverseCameraBtn.setOnClickListener(reverseButtonOnClickListener);

        sobelPhoto = findViewById(R.id.sobel_photo);
        mProgress = findViewById(R.id.progress);

        setOrientation(getResources().getConfiguration());
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    private void setView(boolean taking) {
        if (taking) {
            takePhotoBtn.setEnabled(false);
            reverseCameraBtn.setEnabled(false);
            mProgress.show();
        } else {
            takePhotoBtn.setEnabled(true);
            reverseCameraBtn.setEnabled(true);
            mProgress.hide();
        }
    }

    private View.OnClickListener takeButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setView(true);
            cameraView.takePicture(takePictureCallback);
        }
    };

    private View.OnClickListener reverseButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RotateAnimation rotateAnimation = new RotateAnimation();
            ValueAnimator valueAnimator = rotateAnimation.getAnimators(reverseCameraBtn);
            valueAnimator.setDuration(1000);
            valueAnimator.start();
            cameraView.reverseCamera();
        }
    };

    private CameraView.OnTakePictureCallback takePictureCallback = new CameraView.OnTakePictureCallback() {

        @Override
        public void onPictureTaken(final byte[] data) {

            Observable.create(new ObservableOnSubscribe<Bitmap>() {
                @Override
                public void subscribe(ObservableEmitter<Bitmap> emitter) throws Exception {
                    long start = System.currentTimeMillis();
                    Log.d(TAG, "subscribe: start=" + start);

                    Bitmap bitmap = SobelUtil.createBitmap(data);

                    long end = System.currentTimeMillis();
                    Log.d(TAG, "subscribe: end=" + end);

                    Log.d(TAG, "subscribe: used=" + (end - start));

                    emitter.onNext(bitmap);
                }
            }).observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Consumer<Bitmap>() {
                        @Override
                        public void accept(Bitmap bitmap) throws Exception {
                            sobelPhoto.setImageBitmap(bitmap);
                            sobelPhoto.setVisibility(View.VISIBLE);
                            sobelPhotoShow = true;
                            setView(false);
                        }
                    });

        }
    };

    @Override
    public void onBackPressed() {
        if (sobelPhotoShow) {
            cameraView.start();
            sobelPhoto.setImageBitmap(null);
            sobelPhoto.setVisibility(View.INVISIBLE);
            sobelPhotoShow = false;
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setOrientation(newConfig);
    }

    private void setOrientation(Configuration newConfig) {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int cameraViewOrientation = CameraView.ORIENTATION_PORTRAIT;
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                cameraViewOrientation = CameraView.ORIENTATION_PORTRAIT;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
                    cameraViewOrientation = CameraView.ORIENTATION_HORIZONTAL;
                } else {
                    cameraViewOrientation = CameraView.ORIENTATION_INVERT;
                }
                break;
            default:
                cameraView.setOrientation(CameraView.ORIENTATION_PORTRAIT);
                break;
        }
        cameraView.setOrientation(cameraViewOrientation);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraView.getCameraControl().refreshPermission();
                } else {
                    Toast.makeText(getApplicationContext(), "camera_permission_required", Toast.LENGTH_LONG)
                            .show();
                }
                break;
            }
            case PERMISSIONS_EXTERNAL_STORAGE:
            default:
                break;
        }
    }

}
