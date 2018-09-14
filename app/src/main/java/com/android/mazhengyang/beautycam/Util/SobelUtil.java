package com.android.mazhengyang.beautycam.Util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

/**
 * Created by mazhengyang on 18-9-12.
 */

public class SobelUtil {

    private static final String TAG = SobelUtil.class.getSimpleName();

    /**
     * Gx                  Gy                   P
     * -1  0  +1           +1  +2  +1         x-1,y-1  x,y-1  x+1,y-1
     * -2  0  +2           0   0   0          x-1,y    x,y    x+1,y
     * -1  0  +1           -1  -2  -1         x-1,y+1  x,y+1  x+1,y+1
     */

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

    private static double getPixel(int x, int y, Bitmap bitmap) {
        if (x < 0 || x >= bitmap.getWidth() || y < 0 || y >= bitmap.getHeight()) {
            return 0;
        }
        return bitmap.getPixel(x, y);
    }

    public static Bitmap doSobel(Bitmap bitmap) {

        bitmap = compress(bitmap, 480, 800);
        Bitmap temp = convertToGrey(bitmap);
        int w = temp.getWidth();
        int h = temp.getHeight();

        Log.d(TAG, "doSobel: w=" + w + ", h=" + h);

        int[] mmap = new int[w * h];
        double[] tmap = new double[w * h];
        int[] cmap = new int[w * h];

        temp.getPixels(mmap, 0, temp.getWidth(), 0, 0, temp.getWidth(),
                temp.getHeight());

        double max = Double.MIN_VALUE;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                double gx = GX(i, j, temp);
                double gy = GY(i, j, temp);
                tmap[j * w + i] = Math.sqrt(gx * gx + gy * gy);
                if (max < tmap[j * w + i]) {
                    max = tmap[j * w + i];
                }
            }
        }

        double top = max * 0.06;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (tmap[j * w + i] > top) {
                    cmap[j * w + i] = mmap[j * w + i];
                } else {
                    cmap[j * w + i] = Color.WHITE;
                }
            }
        }

        if (!bitmap.isRecycled()) {
            Log.d(TAG, "doSobel: recycle bitmap");
            bitmap.recycle();
        }

        return Bitmap.createBitmap(cmap, temp.getWidth(), temp.getHeight(),
                Bitmap.Config.ARGB_8888);
    }

    /**
     * 将彩色图转换为灰度图
     *
     * @param bitmap
     * @return
     */
    public static Bitmap convertToGrey(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Log.d(TAG, "convertToGrey: width=" + width);
        Log.d(TAG, "convertToGrey: height=" + height);

        int[] pixels = new int[width * height];

        Log.d(TAG, "convertToGrey: pixels length=" + pixels.length);

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }


    /**
     * 将彩色图转换为灰度图
     *
     * @param bitmap
     * @return
     */
    public static Bitmap convertToGrey2(Bitmap bitmap) {

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        Bitmap b = Bitmap.createBitmap(width, height,
                Bitmap.Config.RGB_565);
        Canvas c = new Canvas(b);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bitmap, 0, 0, paint);
        return b;
    }

    /**
     * Bitmap压缩
     *
     * @param
     * @param
     * @param
     * @return
     */
    public static Bitmap compress(final Bitmap bitmap, int reqWidth, int reqHeight) {
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
            bitmap.recycle();
            return result;
        }
        return bitmap;
    }

}
