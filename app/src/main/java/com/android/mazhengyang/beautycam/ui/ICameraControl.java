package com.android.mazhengyang.beautycam.ui;

/**
 * Created by mazhengyang on 18-9-13.
 */

public interface ICameraControl {

    //回调
    interface CameraControlCallback {

        void onPictureTaken(byte[] data);
        void onFocus(boolean success);
        void turnLight(int drawable);
    }

    void setCallback(CameraControlCallback callback);

    //打开相机
    void start();
    //关闭相机
    void stop();
    //拍照
    void capture();
    //前后摄像切换
    void reverse();
    //闪光灯
    void updateFlashMode();
}
