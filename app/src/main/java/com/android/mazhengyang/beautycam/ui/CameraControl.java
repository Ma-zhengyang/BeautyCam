package com.android.mazhengyang.beautycam.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.android.mazhengyang.beautycam.R;

import java.io.IOException;
import java.util.List;

/**
 * Created by mazhengyang on 18-9-13.
 */

public class CameraControl implements ICameraControl {

    private static final String TAG = CameraControl.class.getSimpleName();

    private int displayOrientation = 0;
    //垂直方向
    public static final int ORIENTATION_PORTRAIT = 0;
    //水平方向
    public static final int ORIENTATION_HORIZONTAL = 90;
    //水平翻转方向
    public static final int ORIENTATION_INVERT = 270;

    private Context context;
    private Camera camera;

    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private Camera.CameraInfo mCameraInfo;

    private int mPreviewWidth = 0;
    private int mPreviewHeight = 0;

    private boolean mOrientationResize;
    private boolean mPrevOrientationResize;

    private boolean focusing = false;

    private Matrix mMatrix = null;
    private float mAspectRatio = 4f / 3f; // 支持预览的尺寸，长宽比
    private boolean mAspectRatioResize;

    private Handler mHandler = new Handler();

    private TextureView textureView;
    private SurfaceTexture surfaceTexture;
    private Camera.Parameters parameters;

    private ICameraControl.CameraControlCallback callback;

    public CameraControl(Context context, TextureView textureView) {
        this.context = context;
        this.textureView = textureView;
        textureView.setSurfaceTextureListener(surfaceTextureListener);
        textureView.addOnLayoutChangeListener(layoutChangeListener);
        textureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAutoFocus();
            }
        });
    }

    @Override
    public void setCallback(CameraControlCallback callback) {
        this.callback = callback;
    }

    @Override
    public void start() {
        Log.d(TAG, "start: ");
        startPreview();
    }

    @Override
    public void stop() {
        Log.d(TAG, "stop: ");
        if (camera != null) {
            camera.setPreviewCallback(null);
            stopPreview();
            // 避免同步代码，为了先设置null后release
            Camera tempC = camera;
            camera = null;
            tempC.release();
            camera = null;
            parameters = null;
        } else {
            Log.d(TAG, "stop: camera is null.");
        }
    }

    // 开启预览
    private void startPreview() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (mPreviewWidth == 0 || mPreviewHeight == 0) {
            return;
        }

        if (camera == null) {
            initCamera(cameraId);
        } else {
            camera.startPreview();
            startAutoFocus();
        }
    }

    private void stopPreview() {
        if (camera != null) {
            camera.stopPreview();
        }
    }

    @Override
    public void reverse() {
        stop();
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            initCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else {
            initCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
    }

    @Override
    public void updateFlashMode() {
        if (camera == null || parameters == null
                || parameters.getSupportedFlashModes() == null) {
            if (callback != null) {
                callback.turnLight(-1);
            }
            return;
        }

        String mode = parameters.getFlashMode();
        Log.d(TAG, "updateFlashMode: mode=" + mode);
        List<String> supportedModes = parameters.getSupportedFlashModes();
        if (Camera.Parameters.FLASH_MODE_OFF.equals(mode)
                && supportedModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
            Log.d(TAG, "updateFlashMode: set to FLASH_MODE_AUTO");
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            camera.setParameters(parameters);
            if (callback != null) {
                callback.turnLight(R.drawable.ic_btn_flash_auto);
            }
        } else if (Camera.Parameters.FLASH_MODE_ON.equals(mode)) {
            if (supportedModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                Log.d(TAG, "updateFlashMode: set to FLASH_MODE_OFF");
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
                if (callback != null) {
                    callback.turnLight(R.drawable.ic_btn_flash_off);
                }
            }
        } else if (Camera.Parameters.FLASH_MODE_AUTO.equals(mode)
                && supportedModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
            Log.d(TAG, "updateFlashMode: set to FLASH_MODE_ON");
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            camera.setParameters(parameters);
            if (callback != null) {
                callback.turnLight(R.drawable.ic_btn_flash_on);
            }
        }
    }

    private void initCamera(int facing) {

        Log.d(TAG, "initCamera: facing=" + facing);

        try {
            if (camera == null) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == facing) {
                        mCameraInfo = cameraInfo;
                        cameraId = i;
                    }
                }
                try {
                    camera = Camera.open(facing);
                } catch (Throwable e) {
                    Log.e(TAG, "initCamera: " + e);
                    return;
                }

            }
            if (parameters == null) {
                parameters = camera.getParameters();
                parameters.setPreviewFormat(ImageFormat.NV21);
            }

            setDisplayOrientation();

            camera.setPreviewTexture(surfaceTexture);

            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            for (Camera.Size size : previewSizes) {
                Log.d(TAG, "initCamera: SupportedPreviewSizes " + size.width + "x" + size.height);
            }
            Camera.Size optimalSize = getOptimalPreviewSize(previewSizes, mPreviewWidth, mPreviewHeight);
            parameters.setPreviewSize(optimalSize.width, optimalSize.height);

            resizeForPreviewAspectRatio(parameters);

            List<String> supportedModes = parameters.getSupportedFlashModes();
            if (supportedModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                if (callback != null) {
                    callback.turnLight(R.drawable.ic_btn_flash_auto);
                }
            }

            startPreview();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private View.OnLayoutChangeListener layoutChangeListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right,
                                   int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            Log.d(TAG, "onLayoutChange: " + String.format("oldLeft=%d, oldTop=%d, oldRight=%d, oldBottom=%d",
                    oldLeft, oldTop, oldRight, oldBottom));
            Log.d(TAG, "onLayoutChange: " + String.format("left=%d, top=%d, right=%d, bottom=%d",
                    left, top, right, bottom));

            int width = right - left;
            int height = bottom - top;
            if (mPreviewWidth != width || mPreviewHeight != height
                    || (mOrientationResize != mPrevOrientationResize)
                    || mAspectRatioResize) {
                mPreviewWidth = width;
                mPreviewHeight = height;
                setTransformMatrix(width, height);
                mAspectRatioResize = false;

                Log.d(TAG, "onLayoutChange: mPreviewWidth=" + mPreviewWidth + ", mPreviewHeight=" + mPreviewHeight);
            }
        }
    };

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable: ");
            surfaceTexture = surface;
            initCamera(cameraId);

            if (mPreviewWidth != 0 && mPreviewHeight != 0) {
                // Re-apply transform matrix for new surface texture
                setTransformMatrix(mPreviewWidth, mPreviewHeight);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged: ");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.d(TAG, "onSurfaceTextureDestroyed: ");
            surfaceTexture = null;
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private boolean supportAutoFocus() {
        return cameraId == Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    private void startAutoFocus() {

        if (!supportAutoFocus()) {
            return;
        }

        if (camera != null && !focusing) {
            try {
                Log.d(TAG, "startAutoFocus run: ");
                focusing = true;
                camera.autoFocus(mAutoFocusCallback);
            } catch (Throwable e) {
                Log.d(TAG, "startAutoFocus: fail " + e);
            }
        }
    }

    private void cancelAutoFocus() {
        if (!supportAutoFocus()) {
            return;
        }
        camera.cancelAutoFocus();
        focusing = false;
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize != null && optimalSize.height <= 720) {
            Log.d(TAG, "getOptimalPreviewSize, optimalSize.height=" + optimalSize.height);
            optimalSize = null; // make sure size not too low, or preview may blurry.
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        Log.d(TAG, "getOptimalPreviewSize: optimalSize width=" + optimalSize.width
                + " ,height=" + optimalSize.height);

        return optimalSize;
    }

    private void setAspectRatio(float ratio) {
        if (ratio <= 0.0)
            throw new IllegalArgumentException();

        if (mOrientationResize
                && context.getResources().getConfiguration().orientation
                != Configuration.ORIENTATION_PORTRAIT) {
            ratio = 1 / ratio;
        }

        Log.d(TAG, "setAspectRatio() ratio[" + ratio + "] mAspectRatio["
                + mAspectRatio + "]");
        mAspectRatio = ratio;
        mAspectRatioResize = true;
//        textureView.requestLayout();
    }

    private void cameraOrientationPreviewResize(boolean orientation) {
        mPrevOrientationResize = mOrientationResize;
        mOrientationResize = orientation;
    }

    private void setPreviewFrameLayoutCameraOrientation() {
        // if camera mount angle is 0 or 180, we want to resize preview
        if (mCameraInfo.orientation % 180 == 0) {
            cameraOrientationPreviewResize(true);
        } else {
            cameraOrientationPreviewResize(false);
        }
    }

    private void resizeForPreviewAspectRatio(Camera.Parameters params) {
        setPreviewFrameLayoutCameraOrientation();
        Camera.Size size = params.getPreviewSize();
        Log.d(TAG, "resizeForPreviewAspectRatio: size = " + size.width + "x" + size.height);
        setAspectRatio((float) size.width / size.height);
    }

    private static int getDisplayOrientation(int degrees, int cameraId) {
        // See android.hardware.Camera.setDisplayOrientation for
        // documentation.
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    private static int getDisplayRotation(Context context) {
        int rotation = ((Activity) context).getWindowManager()
                .getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    private void setDisplayOrientation() {
        int mDisplayRotation = getDisplayRotation(context);

        displayOrientation = mDisplayRotation;

        int mDisplayOrientation = getDisplayOrientation(mDisplayRotation,
                cameraId);
        int mCameraDisplayOrientation = mDisplayOrientation;
        // Change the camera display orientation
        camera.setDisplayOrientation(mCameraDisplayOrientation);
    }

    private void setTransformMatrix(int width, int height) {
        mMatrix = textureView.getTransform(mMatrix);
        float scaleX = 1f, scaleY = 1f;
        float scaledTextureWidth, scaledTextureHeight;
        if (mOrientationResize) {
            scaledTextureWidth = height * mAspectRatio;
            if (scaledTextureWidth > width) {
                scaledTextureWidth = width;
                scaledTextureHeight = scaledTextureWidth / mAspectRatio;
            } else {
                scaledTextureHeight = height;
            }
        } else {
            if (width > height) {
                scaledTextureWidth = Math.max(width, (height * mAspectRatio));
                scaledTextureHeight = Math.max(height, (width / mAspectRatio));
            } else {
                scaledTextureWidth = Math.max(width, (height / mAspectRatio));
                scaledTextureHeight = Math.max(height, (width * mAspectRatio));
            }
        }

        scaleX = scaledTextureWidth / width;
        scaleY = scaledTextureHeight / height;
        mMatrix.setScale(scaleX, scaleY, (float) width / 2, (float) height / 2);
        textureView.setTransform(mMatrix);

        // Calculate the new preview rectangle.
        RectF previewRect = new RectF(0, 0, width, height);
        mMatrix.mapRect(previewRect);
    }

    @Override
    public void capture() {

        switch (displayOrientation) {
            case ORIENTATION_PORTRAIT:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    parameters.setRotation(270);
                } else {
                    parameters.setRotation(90);
                }
                break;
            case ORIENTATION_HORIZONTAL:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    //TODO
                } else {
                    parameters.setRotation(0);
                }
                break;
            case ORIENTATION_INVERT:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    //TODO
                } else {
                    parameters.setRotation(180);
                }
                break;
        }

        try {
            //setPictureSize必须放在setRotation后面
            List<Camera.Size> pictureSizes = camera.getParameters().getSupportedPictureSizes();
            for (Camera.Size size : pictureSizes) {
                Log.d(TAG, "takePicture: getSupportedPictureSizes " + size.width + "x" + size.height);
            }

            Camera.Size picSize = getOptimalPreviewSize(pictureSizes, mPreviewWidth, mPreviewHeight);

            Log.d(TAG, "takePicture: getOptimalSize " + picSize.width + "x" + picSize.height);

            parameters.setPictureSize(picSize.width, picSize.height);

            try {
                camera.setParameters(parameters);
            } catch (Exception e) {
                Log.e(TAG, "takePicture: " + e);
            }

            cancelAutoFocus();

            camera.takePicture(mShutterCallback, mRawPictureCallback,
                    new JpegPictureCallback());

        } catch (RuntimeException e) {
            Log.e(TAG, "capture: fail " + e);
        }
    }

    private final ShutterCallback mShutterCallback = new ShutterCallback();
    private final RawPictureCallback mRawPictureCallback =
            new RawPictureCallback();
    private final AutoFocusCallback mAutoFocusCallback =
            new AutoFocusCallback();

    private final class ShutterCallback
            implements android.hardware.Camera.ShutterCallback {
        public void onShutter() {
        }
    }

    private final class RawPictureCallback implements Camera.PictureCallback {
        public void onPictureTaken(
                byte[] rawData, android.hardware.Camera camera) {
        }
    }

    private final class JpegPictureCallback implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (callback != null) {
                callback.onPictureTaken(data);
            }
        }
    }

    private final class AutoFocusCallback
            implements android.hardware.Camera.AutoFocusCallback {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            Log.d(TAG, "onAutoFocus: success:" + success);
            mHandler.postDelayed(runnable, 1000);
            if (callback != null) {
                callback.onFocus(success);
            }
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            focusing = false;
        }
    };
}
