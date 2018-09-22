package com.android.mazhengyang.beautycam.ui;

import android.content.Context;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by mazhengyang on 18-9-13.
 */

/**
 * 负责，相机的管理。同时提供，裁剪遮罩功能。
 */
public class CameraView extends FrameLayout implements ICameraControl.OnTakePictureCallback {

    private static final String TAG = CameraView.class.getSimpleName();

    /**
     * 照相回调
     */
    public interface OnTakePictureCallback {
        void onPictureTaken(byte[] data);
    }

    /**
     * 垂直方向 {@link #setOrientation(int)}
     */
    public static final int ORIENTATION_PORTRAIT = 0;
    /**
     * 水平方向 {@link #setOrientation(int)}
     */
    public static final int ORIENTATION_HORIZONTAL = 90;
    /**
     * 水平翻转方向 {@link #setOrientation(int)}
     */
    public static final int ORIENTATION_INVERT = 270;


    @IntDef({ORIENTATION_PORTRAIT, ORIENTATION_HORIZONTAL, ORIENTATION_INVERT})
    public @interface Orientation {

    }

    private ICameraControl cameraControl;

    /**
     * 相机预览View
     */
    private View displayView;

    private OnTakePictureCallback callback;

    public ICameraControl getCameraControl() {
        return cameraControl;
    }

    public void setOrientation(@Orientation int orientation) {
        cameraControl.setDisplayOrientation(orientation);
    }

    public CameraView(Context context) {
        super(context);
        init();
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void start() {
        Log.d(TAG, "start: ");
        cameraControl.start();
        setKeepScreenOn(true);
    }

    public void stop() {
        Log.d(TAG, "stop: ");
        cameraControl.stop();
        setKeepScreenOn(false);
    }

    public void canPreview(boolean can){
        Log.d(TAG, "canPreview: can=" + can);
        cameraControl.canPreview(can);
    }

    public void takePicture(final OnTakePictureCallback callback) {
        this.callback = callback;
        cameraControl.takePicture(this);
    }

    public void reverseCamera() {
        cameraControl.reverseCamera();
    }

    private void init() {
        cameraControl = new Camera1Control(getContext());

        displayView = cameraControl.getDisplayView();
        addView(displayView);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.d(TAG, "onLayout: left=" + left);
        Log.d(TAG, "onLayout: top=0");
        Log.d(TAG, "onLayout: right=" + right);
        Log.d(TAG, "onLayout: bottom=" + bottom);

        displayView.layout(left, 0, right, bottom - top);
    }

    @Override
    public void onPictureTaken(byte[] data) {
        callback.onPictureTaken(data);
    }

}
