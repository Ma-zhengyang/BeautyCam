package com.android.mazhengyang.beautycam.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

/**
 * Created by mazhengyang on 18-10-16.
 */

public class colorMatrix {

    private static final String TAG = colorMatrix.class.getSimpleName();

    /**
     * 颜色矩阵              颜色分量矩阵
     * | a b c d e |           |R|
     * A =   | f g h i j |      C=   |G|
     * | k l m n o |           |B|
     * | p q r s t |           |A|
     * |1|
     * <p>
     * R1 = aR + bG + cB + dA + e
     * G1 = fR + gG + hB + iA + j
     * B1 = kR + lG + mB + nA + o
     * A1 = pR + qG + rB + sA + t
     * <p>
     * 第一行的 abcde 用来决定新的颜色值中的R——红色
     * 第二行的 fghij 用来决定新的颜色值中的G——绿色
     * 第三行的 klmno 用来决定新的颜色值中的B——蓝色
     * 第四行的 pqrst 用来决定新的颜色值中的A——透明度
     * 矩阵A中第五列——ejot值分别用来决定每个分量中的 offset ，即偏移量
     */

    //初始颜色矩阵
    public static float[] normalMatrix = new float[]{
            1, 0, 0, 0, 0,
            0, 1, 0, 0, 0,
            0, 0, 1, 0, 0,
            0, 0, 0, 0, 0,
    };

    //一些常用的图像处理效果的颜色矩阵

    //灰度效果
    public static float[] grayMatrix = new float[]{
            0.33f, 0.59f, 0.11f, 0.0f, 0.0f,
            0.33f, 0.59f, 0.11f, 0.0f, 0.0f,
            0.33f, 0.59f, 0.11f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f,
    };

    //图像反转
    public static float[] reverseMatrix = new float[]{
            -1, 0, 0, 1, 1,
            0, -1, 0, 1, 1,
            0, 0, -1, 1, 1,
            0, 0, 0, 1, 0,
    };

    //怀旧效果
    public static float[] pasttimeMatrix = new float[]{
            0.393f, 0.769f, 0189f, 0.0f, 0.0f,
            0.349f, 0.686f, 0.168f, 0.0f, 0.0f,
            0.272f, 0.534f, 0.131f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f,
    };

    //高饱和度
    public static float[] highSaturationMatrix = new float[]{
            1.438F, -0.122F, -0.016F, 0, -0.03F,
            -0.062F, 1.378F, -0.016F, 0, 0.05F,
            -0.062F, -0.122F, 1.483F, 0, -0.02F,
            0, 0, 0, 1, 0,
    };

    public static final int ORIGINAL = 0;
    public static final int SKETCH = 1;
    public static final int GRAY = 2;
    public static final int REVERSE = 3;
    public static final int PASTTIME = 4;
    public static final int HIGHSATURATION = 5;

    /**
     * @param bitmap
     * @param matrix
     * @return
     */
    public static Bitmap doColor(Bitmap bitmap, float[] matrix) {

        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.set(matrix);
        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if(!bitmap.isRecycled()){
            Log.d(TAG, "doColor: recycle bitmap");
            bitmap.recycle();
        }
        return newBitmap;
    }

    public static Bitmap create(int effect, Bitmap bitmap) {
        switch (effect) {
            case SKETCH:
                Sobel.setStop(false);
                Bitmap gray = doColor(bitmap, grayMatrix);
                return Sobel.create(gray);
            case GRAY:
                return doColor(bitmap, grayMatrix);
            case REVERSE:
                return doColor(bitmap, reverseMatrix);
            case PASTTIME:
                return doColor(bitmap, pasttimeMatrix);
            case HIGHSATURATION:
                return doColor(bitmap, highSaturationMatrix);
            case ORIGINAL:
            default:
                return bitmap;
        }
    }
}
