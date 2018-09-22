package com.android.mazhengyang.beautycam;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
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
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
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

    private ImageView mPhotoView;
    private boolean mShowed = false;
    private AVLoadingIndicatorView mProgress;

    private Disposable mDisposable;

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

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        Log.d(TAG, "onCreate: display size=" + size.x + "x" + size.y);

        cameraView = findViewById(R.id.camera_view);
        cameraView.getCameraControl().setPermissionCallback(permissionCallback);

        takePhotoBtn = findViewById(R.id.take_photo_button);
        takePhotoBtn.setOnClickListener(takeButtonOnClickListener);

        reverseCameraBtn = findViewById(R.id.reverse);
        reverseCameraBtn.setOnClickListener(reverseButtonOnClickListener);

        mPhotoView = findViewById(R.id.teked_photo);
        mProgress = findViewById(R.id.progress);

        setOrientation(getResources().getConfiguration());
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        if (!mShowed && mDisposable == null) {
            cameraView.start();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
        if (!mShowed) {
            cameraView.stop();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            Log.d(TAG, "onDestroy: dispose");
            mDisposable.dispose();
            mDisposable = null;
        }
    }

    private View.OnClickListener takeButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            takePhotoBtn.setEnabled(false);
            reverseCameraBtn.setEnabled(false);
            mProgress.show();

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

                    if(data == null){
                        emitter.onError(new Throwable("data is null."));
                    }

                    cameraView.canPreview(false);

                    long start = System.currentTimeMillis();
                    Log.d(TAG, "subscribe: start=" + start);

                    Bitmap bitmap = SobelUtil.createBitmap(data);

                    long end = System.currentTimeMillis();
                    Log.d(TAG, "subscribe: end=" + end + ", used=" + (end - start));

                    emitter.onNext(bitmap);
                }
            }).observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Observer<Bitmap>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            Log.d(TAG, "onSubscribe: ");
                            mDisposable = d;
                        }

                        @Override
                        public void onNext(Bitmap bitmap) {
                            Log.d(TAG, "onNext: ");
                            mPhotoView.setBackgroundColor(Color.GRAY);
                            mPhotoView.setImageBitmap(bitmap);
                            mPhotoView.setVisibility(View.VISIBLE);
                            mShowed = true;
                            mProgress.hide();
                            mDisposable = null;
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "onError: " + e);
                        }

                        @Override
                        public void onComplete() {
                            Log.d(TAG, "onComplete: ");
                        }
                    });

        }
    };

    @Override
    public void onBackPressed() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            Log.d(TAG, "onBackPressed: dispose");
            mDisposable.dispose();
            mDisposable = null;
            reset();
            return;
        }
        if (mShowed) {
            Log.d(TAG, "onBackPressed: mShowed");
            reset();
            return;
        }
        super.onBackPressed();
    }

    private void reset() {
        Log.d(TAG, "reset:");

        mPhotoView.setBackgroundColor(Color.TRANSPARENT);
        mPhotoView.setImageBitmap(null);
        mPhotoView.setVisibility(View.INVISIBLE);
        mShowed = false;
        takePhotoBtn.setEnabled(true);
        reverseCameraBtn.setEnabled(true);
        mProgress.hide();
        cameraView.canPreview(true);
        cameraView.start();
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
