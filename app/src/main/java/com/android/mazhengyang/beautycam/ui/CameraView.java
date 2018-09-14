package com.android.mazhengyang.beautycam.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.android.mazhengyang.beautycam.Util.ImageUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by mazhengyang on 18-9-13.
 */

/**
 * 负责，相机的管理。同时提供，裁剪遮罩功能。
 */
public class CameraView extends FrameLayout {

    private static final String TAG = CameraView.class.getSimpleName();

    /**
     * 照相回调
     */
    public interface OnTakePictureCallback {
        void onPictureTaken(Bitmap bitmap);
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

    private CameraViewTakePictureCallback cameraViewTakePictureCallback = new CameraViewTakePictureCallback();

    private ICameraControl cameraControl;

    /**
     * 相机预览View
     */
    private View displayView;

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
        cameraControl.start();
        setKeepScreenOn(true);
    }

    public void stop() {
        cameraControl.stop();
        setKeepScreenOn(false);
    }

    public void reverseCamera(){
        cameraControl.reverseCamera();
    }

    public void takingPicture(boolean taking) {
        cameraControl.takingPicture(taking);
    }

    public void takePicture(final File file, final OnTakePictureCallback callback) {
        cameraViewTakePictureCallback.file = file;
        cameraViewTakePictureCallback.callback = callback;
        cameraControl.takePicture(cameraViewTakePictureCallback);
    }

    private void init() {
        cameraControl = new Camera1Control(getContext());

        displayView = cameraControl.getDisplayView();
        addView(displayView);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        displayView.layout(left, 0, right, bottom - top);
    }

    private class CameraViewTakePictureCallback implements ICameraControl.OnTakePictureCallback {

        private File file;
        private OnTakePictureCallback callback;

        @Override
        public void onPictureTaken(final byte[] data) {
            CameraThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    final int rotation = ImageUtil.getOrientation(data);

                    Log.d(TAG, "run: rotation=" + rotation);

                    // BitmapRegionDecoder不会将整个图片加载到内存。
                    BitmapRegionDecoder decoder = null;
                    try {
                        decoder = BitmapRegionDecoder.newInstance(data, 0, data.length, true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    BitmapFactory.Options options = new BitmapFactory.Options();

                    // 最大图片大小。
                    int maxPreviewImageSize = 2560;
                    int size = Math.min(decoder.getWidth(), decoder.getHeight());
                    size = Math.min(size, maxPreviewImageSize);

                    options.inSampleSize = ImageUtil.calculateInSampleSize(options, size, size);
                    options.inScaled = true;
                    options.inDensity = Math.max(options.outWidth, options.outHeight);
                    options.inTargetDensity = size;
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

                    if (rotation != 0) {
                        // 只能是裁剪完之后再旋转了。有没有别的更好的方案呢？
                        Matrix matrix = new Matrix();
                        matrix.postRotate(rotation);
                        Bitmap rotatedBitmap = Bitmap.createBitmap(
                                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                        if (bitmap != rotatedBitmap) {
                            // 有时候 createBitmap会复用对象
                            bitmap.recycle();
                        }
                        bitmap = rotatedBitmap;
                    }

                    // 扫描成功阻止多线程同时回调
//                    if (!cameraControl.getAbortingScan().compareAndSet(false, true)) {
//                        bitmap.recycle();
//                        return;
//                    }

                    callback.onPictureTaken(bitmap);
                }
            });
        }
    }

}
