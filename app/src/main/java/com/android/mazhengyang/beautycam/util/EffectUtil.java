package com.android.mazhengyang.beautycam.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import java.io.IOException;

/**
 * Created by mazhengyang on 18-9-28.
 */

public class EffectUtil {

    private static final String TAG = EffectUtil.class.getSimpleName();

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

    //
    public static float[] quseMatrix = new float[]{
            1.5f, 1.5f, 1.5f, 0, -1,
            1.5f, 1.5f, 1.5f, 0, -1,
            1.5f, 1.5f, 1.5f, 0, -1,
            0, 0, 0, 1, 0,
    };

    public enum EFFECT {
        ORIGINAL, SKETCH, GRAY, REVERSE, PASTTIME
    }

    public static Bitmap createBitmap(EFFECT type, byte[] data) {

        Log.d(TAG, "createBitmap: effect=" + type);

        Bitmap bitmap = createOriginal(data);

        switch (type) {
            case SKETCH:
                bitmap = compress(bitmap, 480, 800);
                Bitmap temp = createEffect(bitmap, grayMatrix);
                Bitmap newBitmap = SobelUtil.createSobel(temp);
                if (!bitmap.isRecycled()) {
                    Log.d(TAG, "sobel: recycle bitmap");
                    bitmap.recycle();
                }
                return newBitmap;
            case GRAY:
                bitmap = createEffect(bitmap, grayMatrix);
                break;
            case REVERSE:
                bitmap = createEffect(bitmap, reverseMatrix);
                break;
            case PASTTIME:
                bitmap = createEffect(bitmap, pasttimeMatrix);
                break;
            case ORIGINAL:
            default:
                break;
        }

        return bitmap;
    }


    private static Bitmap createOriginal(final byte[] data) {

        final int rotation = ImageUtil.getOrientation(data);

        Log.d(TAG, "createBitmap: rotation=" + rotation);

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

        return bitmap;
    }

    /**
     * @param bitmap
     * @param matrix
     * @return
     */
    public static Bitmap createEffect(Bitmap bitmap, float[] matrix) {

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        Bitmap b = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix(matrix);
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
            bitmap.recycle();
            return result;
        }
        return bitmap;
    }


}
