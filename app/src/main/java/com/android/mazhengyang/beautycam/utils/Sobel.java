package com.android.mazhengyang.beautycam.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;

/**
 * Created by mazhengyang on 18-10-16.
 */

public class Sobel {

    private static final String TAG = Sobel.class.getSimpleName();

    /**
     * Gx                  Gy                   P
     * -1  0  +1           +1  +2  +1         x-1,y-1  x,y-1  x+1,y-1
     * -2  0  +2           0   0   0          x-1,y    x,y    x+1,y
     * -1  0  +1           -1  -2  -1         x-1,y+1  x,y+1  x+1,y+1
     */

    private static boolean mStop = false;

    public static void setStop(boolean stop) {
        mStop = stop;
    }

    private static double getPixel(int x, int y, Bitmap bitmap) {
        if (x < 0 || x >= bitmap.getWidth() || y < 0 || y >= bitmap.getHeight()) {
            return 0;
        }
        return bitmap.getPixel(x, y);
    }

    private static double GX(int x, int y, Bitmap bitmap) {
        return (-1) * getPixel(x - 1, y - 1, bitmap)
                + 1 * getPixel(x + 1, y - 1, bitmap)
                + (-2) * getPixel(x - 1, y, bitmap)
                + 2 * getPixel(x + 1, y, bitmap)
                + (-1) * getPixel(x - 1, y + 1, bitmap)
                + 1 * getPixel(x + 1, y + 1, bitmap);
    }

    private static double GY(int x, int y, Bitmap bitmap) {
        return 1 * getPixel(x - 1, y - 1, bitmap)
                + 2 * getPixel(x, y - 1, bitmap)
                + 1 * getPixel(x + 1, y - 1, bitmap)
                + (-1) * getPixel(x - 1, y + 1, bitmap)
                + (-2) * getPixel(x, y + 1, bitmap)
                + (-1) * getPixel(x + 1, y + 1, bitmap);
    }

    /**
     * Bitmap压缩
     *
     * @param
     * @param
     * @param
     * @return
     */
    private static Bitmap compress(final Bitmap bitmap, int reqWidth, int reqHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (height > reqHeight || width > reqWidth) {
            float scaleWidth = (float) reqWidth / width;
            float scaleHeight = (float) reqHeight / height;
            float scale = scaleWidth < scaleHeight ? scaleWidth : scaleHeight;

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);
            if (!bitmap.isRecycled()) {
                Log.d(TAG, "compress: recycle bitmap");
                bitmap.recycle();
            }
            return result;
        }
        return bitmap;
    }

    public static Bitmap create(Bitmap bitmap) {

        Bitmap compressBitmap = compress(bitmap, 480, 800);

        int w = compressBitmap.getWidth();
        int h = compressBitmap.getHeight();

        int[] mmap = new int[w * h];
        double[] tmap = new double[w * h];
        int[] cmap = new int[w * h];

        compressBitmap.getPixels(mmap, 0, compressBitmap.getWidth(), 0, 0, compressBitmap.getWidth(),
                compressBitmap.getHeight());

        double max = Double.MIN_VALUE;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (mStop) {
                    return null;
                }
                double gx = GX(i, j, compressBitmap);
                double gy = GY(i, j, compressBitmap);
                tmap[j * w + i] = Math.sqrt(gx * gx + gy * gy);
                if (max < tmap[j * w + i]) {
                    max = tmap[j * w + i];
                }
            }
        }

        double top = max * 0.06;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (mStop) {
                    return null;
                }
                if (tmap[j * w + i] > top) {
                    cmap[j * w + i] = mmap[j * w + i];
                } else {
                    cmap[j * w + i] = Color.WHITE;
                }
            }
        }

        Bitmap newBitmap = Bitmap.createBitmap(cmap, compressBitmap.getWidth(), compressBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        if (!compressBitmap.isRecycled()) {
            Log.d(TAG, "create: recycle bitmap");
            compressBitmap.recycle();
        }

        return newBitmap;
    }
}
