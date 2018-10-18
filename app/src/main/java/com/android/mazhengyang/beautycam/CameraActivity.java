package com.android.mazhengyang.beautycam;

import android.Manifest;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.mazhengyang.beautycam.ui.animation.AnimationUtil;
import com.android.mazhengyang.beautycam.ui.CameraControl;
import com.android.mazhengyang.beautycam.ui.widget.GalleryButton;
import com.android.mazhengyang.beautycam.ui.ICameraControl;
import com.android.mazhengyang.beautycam.ui.animation.AnimationCallback;
import com.android.mazhengyang.beautycam.utils.DataBuffer;
import com.android.mazhengyang.beautycam.utils.store.MediaSaver;
import com.android.mazhengyang.beautycam.utils.Sobel;
import com.android.mazhengyang.beautycam.utils.store.StoreCallback;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by mazhengyang on 18-9-13.
 */

public class CameraActivity extends Activity implements ICameraControl.CameraControlCallback,
        GalleryButton.GalleryButtonCallback {

    private static final String TAG = CameraActivity.class.getSimpleName();

    private static final int REQUEST_BEAYTIFY_PHOTO = 1000;
    private static final int PERMISSIONS_REQUEST_CAMERA = 1024;
    private static final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 1025;

    private static final int STATE_IDLE = 0;
    private static final int STATE_TAKING = 1;
    private int mState = STATE_IDLE;

    private ICameraControl cameraControl;
    private AnimationUtil mAnimationUtil;
    private MediaSaver mMediaSaver;

    @BindView(R.id.snap_view)
    ImageView mSnapView;
    @BindView(R.id.btn_gallery)
    GalleryButton mGalleryButton;
    @BindView(R.id.btn_shutter)
    ImageView mShutterBtn;
    @BindView(R.id.btn_reverse)
    ImageView reverseCameraBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        float density = getResources().getDisplayMetrics().density;
        Log.d(TAG, "onCreate: display size=" + size.x + "x" + size.y);
        Log.d(TAG, "onCreate: display density=" + density);

        TextureView textureView = findViewById(R.id.camera_textureview);
        cameraControl = new CameraControl(this, textureView);
        cameraControl.setCallback(this);

        mAnimationUtil = new AnimationUtil();
        mMediaSaver = new MediaSaver();

        mGalleryButton.loadLatest(this);
        mShutterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShutterClick(v);
            }
        });

        if (!checkPermission()) {
            onRequestCameraPermission();
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        setState(STATE_IDLE);
        cameraControl.start();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
        cameraControl.stop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        mMediaSaver.finish();
        mMediaSaver = null;

        DataBuffer.cleanByteArray();
//        DataBuffer.cleanBitmap(); //do in Storage
    }

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            Log.d(TAG, "checkPermission: not camera permission");
            return false;
        }
    }

    public boolean onRequestCameraPermission() {
        Log.d(TAG, "onRequestStoragePermission: ");
        ActivityCompat.requestPermissions(CameraActivity.this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSIONS_REQUEST_CAMERA);
        return false;
    }

    @Override
    public boolean onRequestStoragePermission() {
        Log.d(TAG, "onRequestStoragePermission: ");
        ActivityCompat.requestPermissions(CameraActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult: requestCode = " + requestCode);
        Log.d(TAG, "onRequestPermissionsResult: grantResults.length = " + grantResults.length);
        if (grantResults.length > 0) {
            Log.d(TAG, "onRequestPermissionsResult: grantResults[0] = " + grantResults[0]);
        }

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraControl.start();

                    mGalleryButton.loadLatest(this);
                } else {
                    Toast.makeText(getApplicationContext(), getApplicationContext().
                            getString(R.string.no_camera_permission), Toast.LENGTH_LONG)
                            .show();
                    finish();
                }
                break;
            }
            case PERMISSIONS_REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mGalleryButton.loadLatest(this);
                } else {
                    Toast.makeText(getApplicationContext(), getApplicationContext().
                            getString(R.string.no_storage_permission), Toast.LENGTH_LONG)
                            .show();
                }
                break;
            }
            default:
                break;
        }
    }

    private void setState(int state) {
        mState = state;
    }

    private boolean isState(int state) {
        return mState == state;
    }

    @Override
    public void onBackPressed() {
        if (isState(STATE_TAKING)) {
            Sobel.setStop(true);
            cameraControl.start();
            mShutterBtn.setEnabled(true);
            reverseCameraBtn.setEnabled(true);
            setState(STATE_IDLE);
        } else {
            super.onBackPressed();
        }
    }

//    private byte[] bitmap2bytes(){
//        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable);
//        ByteArrayOutputStream bs = new ByteArrayOutputStream();
//        bmp.compress(Bitmap.CompressFormat.PNG, 100, bs);
//        return bs.toByteArray();
//    }

    @Override
    public void onPictureTaken(final byte[] data) {
        if (isState(STATE_TAKING)) {
            DataBuffer.setByteArray(data);
            Intent intent = new Intent(this, BeautifyPhotoActivity.class);
            startActivityForResult(intent, REQUEST_BEAYTIFY_PHOTO);
            //  startActivity(intent);
        } else {
            Log.d(TAG, "onPictureTaken: interrupt, mState =" + mState);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ");
        switch (requestCode) {
            case REQUEST_BEAYTIFY_PHOTO: {
                Bitmap bitmap = DataBuffer.getBitmap();
                if (bitmap != null) {
                    playStorePhotoAnim(bitmap);
                }
                break;
            }
        }
    }

    private void playStorePhotoAnim(final Bitmap b) {
        Log.d(TAG, "playStorePhotoAnim: ");

        mAnimationUtil.storeAnimators(mSnapView,
                new AnimationCallback() {
                    @Override
                    public void start() {
                        Log.d(TAG, "start: ");
                        mSnapView.setImageBitmap(b);
                        mSnapView.setVisibility(View.VISIBLE);
                        cameraControl.start();
                    }

                    @Override
                    public void end() {
                        Log.d(TAG, "end: ");
                        mSnapView.setVisibility(View.INVISIBLE);
                        mSnapView.setImageBitmap(null);
                        setState(STATE_IDLE);
                        storeImage(b);
                    }
                });
    }

    private void onShutterClick(View view) {
        if (isState(STATE_IDLE)) {
            setState(STATE_TAKING);
            cameraControl.capture();
        }
    }

    public void onReverseClick(View view) {
        if (isState(STATE_IDLE)) {
            AnimatorSet animatorSet = mAnimationUtil.rotateAnimators(reverseCameraBtn);
            animatorSet.start();
            cameraControl.reverseCamera();
        }
    }

    private void storeImage(Bitmap bitmap) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        Date d = new Date(System.currentTimeMillis());
        String title = "image_" + format.format(d);
        Log.d(TAG, "storeImage: title = " + title);
        mMediaSaver.addImage(bitmap, title, new StoreCallback() {
            @Override
            public void success() {
                Log.d(TAG, "storeImage success: ");
                DataBuffer.cleanBitmap();
                mGalleryButton.loadLatest(CameraActivity.this);
            }

            @Override
            public void fail() {
                Log.d(TAG, "storeImage fail: ");
                DataBuffer.cleanBitmap();
            }
        });
    }

}
