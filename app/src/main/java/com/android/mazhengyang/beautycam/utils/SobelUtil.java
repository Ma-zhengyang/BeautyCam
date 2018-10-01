package com.android.mazhengyang.beautycam.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

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

    private boolean mStop = false;
    public void setStop(boolean stop){
        mStop = stop;
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

    private static double getPixel(int x, int y, Bitmap bitmap) {
        if (x < 0 || x >= bitmap.getWidth() || y < 0 || y >= bitmap.getHeight()) {
            return 0;
        }
        return bitmap.getPixel(x, y);
    }

    public Bitmap createSobel(Bitmap temp) {

        int w = temp.getWidth();
        int h = temp.getHeight();

        int[] mmap = new int[w * h];
        double[] tmap = new double[w * h];
        int[] cmap = new int[w * h];

        temp.getPixels(mmap, 0, temp.getWidth(), 0, 0, temp.getWidth(),
                temp.getHeight());

        double max = Double.MIN_VALUE;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if(mStop){
                   return null;
                }
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
                if(mStop){
                    return null;
                }
                if (tmap[j * w + i] > top) {
                    cmap[j * w + i] = mmap[j * w + i];
                } else {
                    cmap[j * w + i] = Color.WHITE;
                }
            }
        }

        return Bitmap.createBitmap(cmap, temp.getWidth(), temp.getHeight(),
                Bitmap.Config.ARGB_8888);
    }

}
