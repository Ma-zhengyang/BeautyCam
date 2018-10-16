package com.android.mazhengyang.beautycam.ui;

/**
 * Created by mazhengyang on 18-9-13.
 */

public interface ICameraControl {

    /**
     * 照相回调。
     */
    interface CameraControlCallback {
        boolean onRequestCameraPermission();

        void onPictureTaken(byte[] data);
    }

    void setCallback(CameraControlCallback callback);

    /**
     * 获取到拍照权限时，调用些函数以继续。
     */
    void refreshPermission();

    /**
     * 打开相机。
     */
    void start();

    /**
     * 关闭相机
     */
    void stop();

    /**
     * 拍照
     */
    void capture();

    /**
     * 前后摄像切换
     */
    void reverseCamera();
}
