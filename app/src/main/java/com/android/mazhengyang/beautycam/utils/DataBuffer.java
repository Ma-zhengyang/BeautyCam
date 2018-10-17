package com.android.mazhengyang.beautycam.utils;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created by mzy on 2018/10/17.
 */

public class DataBuffer {

    private static final String TAG = DataBuffer.class.getSimpleName();

    /**
     * 用于CameraActivity和BeautifyPhotoActivity之间传递数据
     */

    private static byte[] array;

    public static void setByteArray(byte[] d) {
        array = d;
    }

    public static byte[] getByteArray() {
        return array;
    }

    public static void cleanByteArray() {
        Log.d(TAG, "cleanByteArray: ");
        array = null;
    }

    private static Bitmap bitmap;

    public static void setBitmap(Bitmap b) {
        bitmap = b;
    }

    public static Bitmap getBitmap() {
        return bitmap;
    }

    public static void cleanBitmap() {
        if (bitmap != null && !bitmap.isRecycled()) {
            Log.d(TAG, "cleanBitmap: ");
            bitmap.recycle();
            bitmap = null;
        }
    }
}
