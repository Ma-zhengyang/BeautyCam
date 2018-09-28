package com.android.mazhengyang.beautycam;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.mazhengyang.beautycam.animation.RotateAnimation;
import com.android.mazhengyang.beautycam.ui.CameraControl;
import com.android.mazhengyang.beautycam.ui.ICameraControl;
import com.android.mazhengyang.beautycam.ui.rain.RainView;
import com.android.mazhengyang.beautycam.ui.snow.SnowView;
import com.android.mazhengyang.beautycam.util.EffectUtil;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
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

public class CameraActivity extends Activity implements ICameraControl.CameraControlCallback {

    private static final String TAG = CameraActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_CAMERA = 1024;
    private static final int PERMISSIONS_EXTERNAL_STORAGE = 1025;

    private View mControlTake, mControlEffect;
    private ICameraControl cameraControl;
    private SnowView snowView;
    private RainView rainView;
    private ImageView takePhotoBtn;
    private ImageView reverseCameraBtn;
    private ImageView cancelBtn;
    private ImageView confirmBtn;
    private ImageView mPhotoView;
    private boolean photoShow = false;
    private EffectUtil.EFFECT mEffect = EffectUtil.EFFECT.ORIGINAL;
    private AVLoadingIndicatorView mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        Log.d(TAG, "onCreate: display size=" + size.x + "x" + size.y);

        initView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraControl.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraControl.start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged: ");
    }

    private void initView() {
        SlidingMenu menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
//        menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.slidemenu_layout);

        TextureView textureView = findViewById(R.id.camera_textureview);
        cameraControl = new CameraControl(this, textureView);
        cameraControl.setCallback(this);

        takePhotoBtn = findViewById(R.id.take_photo_button);
        takePhotoBtn.setOnClickListener(takeBtnClickListener);

        reverseCameraBtn = findViewById(R.id.reverse_camera_button);
        reverseCameraBtn.setOnClickListener(reverseBtnClickListener);

        cancelBtn = findViewById(R.id.btn_cancel);
        cancelBtn.setOnClickListener(cancelBtnClickListener);

        confirmBtn = findViewById(R.id.btn_confirm);
        confirmBtn.setOnClickListener(confirmBtnClickListener);

        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(onCheckedChangeListener);

        mPhotoView = findViewById(R.id.taked_photo);
        mProgress = findViewById(R.id.progress);

        mControlTake = findViewById(R.id.control_take);
        mControlEffect = findViewById(R.id.control_effect);

        snowView = findViewById(R.id.snow_view);
        rainView = findViewById(R.id.rain_view);
    }

    private View.OnClickListener takeBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            takePhotoBtn.setEnabled(false);
            reverseCameraBtn.setEnabled(false);
            mProgress.show();

            cameraControl.takePicture();
        }
    };

    private View.OnClickListener reverseBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RotateAnimation rotateAnimation = new RotateAnimation();
            ValueAnimator valueAnimator = rotateAnimation.getAnimators(reverseCameraBtn);
            valueAnimator.setDuration(1000);
            valueAnimator.start();
            cameraControl.reverseCamera();
        }
    };

    private View.OnClickListener cancelBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            back();
        }
    };

    private View.OnClickListener confirmBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            snowView.hide();
            rainView.hide();
            int id = group.getCheckedRadioButtonId();
            switch (id) {
                case R.id.radio_original:
                    mEffect = EffectUtil.EFFECT.ORIGINAL;
                    break;
                case R.id.radio_sketch:
                    mEffect = EffectUtil.EFFECT.SKETCH;
                    break;
                case R.id.radio_gray:
                    mEffect = EffectUtil.EFFECT.GRAY;
                    break;
                case R.id.radio_reverse:
                    mEffect = EffectUtil.EFFECT.REVERSE;
                    break;
                case R.id.radio_pasttime:
                    mEffect = EffectUtil.EFFECT.PASTTIME;
                    break;
                case R.id.radio_snow:
                    snowView.show();
                    break;
                case R.id.radio_rain:
                    rainView.show();
                    break;
                default:
                    break;
            }
        }
    };

    private void back() {
        mControlTake.setVisibility(View.VISIBLE);
        mControlEffect.setVisibility(View.INVISIBLE);
        takePhotoBtn.setEnabled(true);
        reverseCameraBtn.setEnabled(true);
        mProgress.hide();

        cameraControl.start();
        mPhotoView.setImageBitmap(null);
        mPhotoView.setVisibility(View.INVISIBLE);
        photoShow = false;
    }

    @Override
    public void onBackPressed() {
        if (photoShow) {
            back();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onRequestPermission() {
        ActivityCompat.requestPermissions(CameraActivity.this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSIONS_REQUEST_CAMERA);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraControl.refreshPermission();
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

    @Override
    public void onPictureTaken(final byte[] data) {
        Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(ObservableEmitter<Bitmap> emitter) throws Exception {
                long start = System.currentTimeMillis();
                Log.d(TAG, "subscribe: start=" + start);

                Bitmap bitmap = EffectUtil.createBitmap(mEffect, data);

                long end = System.currentTimeMillis();
                Log.d(TAG, "subscribe: end=" + end + ", used=" + (end - start));

                emitter.onNext(bitmap);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) throws Exception {
                        Log.d(TAG, "accept: bitmap " + bitmap.getWidth() + "x" + bitmap.getHeight());
                        mControlTake.setVisibility(View.INVISIBLE);
                        mControlEffect.setVisibility(View.VISIBLE);
                        mPhotoView.setImageBitmap(bitmap);
                        mPhotoView.setVisibility(View.VISIBLE);
                        photoShow = true;
                        mProgress.hide();
                    }
                });

    }

    @Override
    public void clipEffectView(int left, int top, int right, int bottom) {

        snowView.clip(left, top, right, bottom);
        rainView.clip(left, top, right, bottom);
    }

}
