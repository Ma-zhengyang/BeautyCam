package com.android.mazhengyang.beautycam;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.mazhengyang.beautycam.ui.CameraControl;
import com.android.mazhengyang.beautycam.ui.ICameraControl;
import com.android.mazhengyang.beautycam.ui.animation.AnimationCallback;
import com.android.mazhengyang.beautycam.ui.widget.GalleryButton;
import com.android.mazhengyang.beautycam.utils.DataBuffer;
import com.android.mazhengyang.beautycam.utils.store.MediaSaver;
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
    private MediaSaver mMediaSaver;

    @BindView(R.id.snap_view)
    ImageView snapView;
    @BindView(R.id.focus_view)
    View focusView;
    @BindView(R.id.btn_gallery)
    GalleryButton galleryBtn;
    @BindView(R.id.btn_shutter)
    ImageView shutterBtn;
    @BindView(R.id.btn_reverse)
    ImageView reverseBtn;
    @BindView(R.id.btn_flash)
    ImageView flashBtn;

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

        mMediaSaver = new MediaSaver();

        if (!hasCameraPermission()) {
            onRequestCameraPermission();
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        setState(STATE_IDLE);
        cameraControl.start();

        galleryBtn.loadLatest(this);
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

    @Override
    public void onBackPressed() {
        if (isState(STATE_TAKING)) {
            cameraControl.start();
            shutterBtn.setEnabled(true);
            reverseBtn.setEnabled(true);
            setState(STATE_IDLE);
        } else {
            super.onBackPressed();
        }
    }

//    private byte[] bitmap2bytes(){
//        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.);
//        ByteArrayOutputStream bs = new ByteArrayOutputStream();
//        bmp.compress(Bitmap.CompressFormat.PNG, 100, bs);
//        return bs.toByteArray();
//    }

    @Override
    public void updateFlashIcon(String mode) {
        if(mode == null){
            flashBtn.setVisibility(View.GONE);
            return;
        }
        flashBtn.setVisibility(View.VISIBLE);

        if (mode.equals(Camera.Parameters.FLASH_MODE_AUTO)) {
            flashBtn.setImageResource(R.drawable.ic_btn_flash_auto);
        } else if (mode.equals(Camera.Parameters.FLASH_MODE_ON)) {
            flashBtn.setImageResource(R.drawable.ic_btn_flash_on);
        } else if (mode.equals(Camera.Parameters.FLASH_MODE_OFF)) {
            flashBtn.setImageResource(R.drawable.ic_btn_flash_off);
        }
    }

    @Override
    public void updateFocusRect(final boolean success) {
        CameraApplicaton.getAnim().playScale(focusView, new AnimationCallback() {
            @Override
            public void start() {
                focusView.setBackground(getDrawable(R.drawable.camera_focus_start));
            }

            @Override
            public void end() {
                if (success) {
                    focusView.setBackground(getDrawable(R.drawable.camera_focus_success));
                } else {
                    focusView.setBackground(getDrawable(R.drawable.camera_focus_fail));
                }
            }
        });
    }

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

    private void setState(int state) {
        mState = state;
    }

    private boolean isState(int state) {
        return mState == state;
    }

    private void playStorePhotoAnim(final Bitmap b) {
        Log.d(TAG, "playStorePhotoAnim: ");

        CameraApplicaton.getAnim().playSaveImage(snapView,
                new AnimationCallback() {
                    @Override
                    public void start() {
                        Log.d(TAG, "start: ");
                        snapView.setImageBitmap(b);
                        snapView.setVisibility(View.VISIBLE);
                        cameraControl.start();
                    }

                    @Override
                    public void end() {
                        Log.d(TAG, "end: ");
                        snapView.setVisibility(View.INVISIBLE);
                        snapView.setImageBitmap(null);
                        setState(STATE_IDLE);
                        storeImage(b);
                    }
                });
    }

    public void onGalleryClick(View view) {
        if (isState(STATE_IDLE)) {
            Intent intent = new Intent(this, GalleryActivity.class);
            startActivity(intent);
        }
    }

    public void onShutterClick(View view) {
        if (isState(STATE_IDLE)) {
            setState(STATE_TAKING);
            cameraControl.capture();
        }
    }

    public void onReverseClick(View view) {
        if (isState(STATE_IDLE)) {
            CameraApplicaton.getAnim().playRotate(reverseBtn);
            cameraControl.reverse();
        }
    }

    public void onFlashClick(View view) {
        if (isState(STATE_IDLE)) {
            cameraControl.updateFlash();
        }
    }

    private void storeImage(Bitmap bitmap) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date d = new Date(System.currentTimeMillis());
        String title = "IMG_" + format.format(d);
        Log.d(TAG, "storeImage: title = " + title);
        mMediaSaver.addImage(bitmap, title, new StoreCallback() {
            @Override
            public void success() {
                Log.d(TAG, "storeImage success: ");
                DataBuffer.cleanBitmap();
                galleryBtn.loadLatest(CameraActivity.this);
            }

            @Override
            public void fail() {
                Log.d(TAG, "storeImage fail: ");
                DataBuffer.cleanBitmap();
            }
        });
    }

    private boolean hasCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            Log.d(TAG, "hasCameraPermission: not camera permission");
            return false;
        }
    }

    private boolean onRequestCameraPermission() {
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

                    galleryBtn.loadLatest(this);
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
                    galleryBtn.loadLatest(this);
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

}
