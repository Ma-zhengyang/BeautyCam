package com.android.mazhengyang.beautycam;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
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
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.mazhengyang.beautycam.animations.RotateAnimation;
import com.android.mazhengyang.beautycam.animations.ScaleAnimation;
import com.android.mazhengyang.beautycam.ui.CameraControl;
import com.android.mazhengyang.beautycam.ui.ICameraControl;
import com.android.mazhengyang.beautycam.ui.rain.RainView;
import com.android.mazhengyang.beautycam.ui.snow.SnowView;
import com.android.mazhengyang.beautycam.utils.ImageUtil;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.wang.avi.AVLoadingIndicatorView;

import butterknife.BindView;
import butterknife.ButterKnife;
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

    private static final int STATE_IDLE = 0;
    private static final int STATE_TAKING = 1;
    private static final int STATE_TAKED = 2;
    private int mState = STATE_IDLE;

    private int effect = ImageUtil.ORIGINAL;
    private boolean waterMark = false;
    private ICameraControl cameraControl;
    private ImageUtil mImageUtil;

    @BindView(R.id.control_shutter)
    View mControlShutter;
    @BindView(R.id.control_effect)
    View mControlEffect;
    @BindView(R.id.btn_shutter)
    ImageView mShutterButton;
    @BindView(R.id.btn_reverse)
    ImageView reverseCameraBtn;
    @BindView(R.id.taked_photo)
    ImageView mPhotoView;
    @BindView(R.id.progress)
    AVLoadingIndicatorView mProgress;
    @BindView(R.id.snow_view)
    SnowView snowView;
    @BindView(R.id.rain_view)
    RainView rainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        Log.d(TAG, "onCreate: display size=" + size.x + "x" + size.y);

        TextureView textureView = findViewById(R.id.camera_textureview);
        cameraControl = new CameraControl(this, textureView);
        cameraControl.setCallback(this);

        mImageUtil = new ImageUtil();

        initSlidingMenu();
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

    @Override
    public void onBackPressed() {
        if (isState(STATE_TAKING)) {
            back();
        } else if (isState(STATE_TAKED)) {
            hidePhoto();
        } else {
            super.onBackPressed();
        }
    }

    private void setState(int state) {
        mState = state;
    }

    private boolean isState(int state) {
        return mState == state;
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

                if (isState(STATE_TAKING)) {
                    long start = System.currentTimeMillis();
                    Log.d(TAG, "subscribe: start=" + start);

                    Bitmap bitmap = mImageUtil.createBitmap(effect, waterMark, data);

                    long end = System.currentTimeMillis();
                    Log.d(TAG, "subscribe: end=" + end + ", used=" + (end - start));

                    if (bitmap != null) {
                        emitter.onNext(bitmap);
                    } else {
                        Log.d(TAG, "subscribe: bitmap is null");
                    }
                } else {
                    Log.d(TAG, "subscribe: interrupt, mState =" + mState);
                }

            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) throws Exception {

                        Log.d(TAG, "accept: bitmap " + bitmap.getWidth() + "x" + bitmap.getHeight());

                        if (isState(STATE_TAKING)) {
                            showPhoto(bitmap);
                        } else {
                            Log.d(TAG, "subscribe: accept, mState =" + mState);
                        }

                    }
                });

    }

    public void onShutterClick(View view) {
        //mShutterButton.getBackground().setColorFilter(0x77FFFF00, PorterDuff.Mode.SRC_ATOP);
        mShutterButton.setEnabled(false);
        reverseCameraBtn.setEnabled(false);
        mProgress.show();
        setState(STATE_TAKING);
        cameraControl.capture();
    }

    public void onReverseClick(View view) {
        RotateAnimation rotateAnimation = new RotateAnimation();
        ValueAnimator valueAnimator = rotateAnimation.getAnimators(reverseCameraBtn);
        valueAnimator.setDuration(1000);
        valueAnimator.start();
        cameraControl.reverseCamera();
    }

    public void onCancelClick(View view) {
        hidePhoto();
    }

    public void onConfirmClick(View view) {
    }

    private void showPhoto(final Bitmap bitmap) {
        ScaleAnimation scaleAnimation = new ScaleAnimation();
        AnimatorSet animatorSet = scaleAnimation.getOutAnimators(mPhotoView);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                mProgress.hide();
                mControlShutter.setVisibility(View.INVISIBLE);
                mControlEffect.setVisibility(View.VISIBLE);
                mPhotoView.setVisibility(View.VISIBLE);
                mPhotoView.setImageBitmap(bitmap);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mPhotoView.setBackgroundColor(Color.BLACK);
                setState(STATE_TAKED);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.start();
    }

    private void hidePhoto() {
        ScaleAnimation scaleAnimation = new ScaleAnimation();
        AnimatorSet animatorSet = scaleAnimation.getInAnimators(mPhotoView);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mProgress.hide();
                mControlShutter.setVisibility(View.VISIBLE);
                mControlEffect.setVisibility(View.INVISIBLE);
                //mShutterButton.getBackground().clearColorFilter();
                mPhotoView.setBackgroundColor(Color.TRANSPARENT);
                cameraControl.start();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mPhotoView.setVisibility(View.INVISIBLE);
                mPhotoView.setImageBitmap(null);
                mShutterButton.setEnabled(true);
                reverseCameraBtn.setEnabled(true);
                setState(STATE_IDLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animatorSet.start();
    }

    private void back() {
        mImageUtil.setStop(true);
        mProgress.hide();
        cameraControl.start();
        mShutterButton.setEnabled(true);
        reverseCameraBtn.setEnabled(true);
        setState(STATE_IDLE);
    }


    private void initSlidingMenu() {
        SlidingMenu menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
//        menu.setShadowDrawable(R.drawable.);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.slidemenu_layout);

        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                snowView.hide();
                rainView.hide();
                int id = group.getCheckedRadioButtonId();
                switch (id) {
                    case R.id.radio_original:
                        effect = ImageUtil.ORIGINAL;
                        break;
                    case R.id.radio_sketch:
                        effect = ImageUtil.SKETCH;
                        break;
                    case R.id.radio_gray:
                        effect = ImageUtil.GRAY;
                        break;
                    case R.id.radio_reverse:
                        effect = ImageUtil.REVERSE;
                        break;
                    case R.id.radio_pasttime:
                        effect = ImageUtil.PASTTIME;
                        break;
                    case R.id.radio_highsaturation:
                        effect = ImageUtil.HIGHSATURATION;
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
        });

        CheckBox mWaterMark = findViewById(R.id.water_mark);
        mWaterMark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                waterMark = isChecked;
            }
        });

    }
}
