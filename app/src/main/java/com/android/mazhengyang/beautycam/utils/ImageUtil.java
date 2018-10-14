package com.android.mazhengyang.beautycam.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.util.Log;

import com.android.mazhengyang.beautycam.CameraApplicaton;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mazhengyang on 18-9-13.
 */

public class ImageUtil {
    private static final String TAG = "ImageUtil";

    public static final int ORIGINAL = 0;
    public static final int SKETCH = 1;
    public static final int GRAY = 2;
    public static final int REVERSE = 3;
    public static final int PASTTIME = 4;
    public static final int HIGHSATURATION = 5;

    public ImageUtil() {
    }

    // Returns the degrees in clockwise. Values are 0, 90, 180, or 270.
    public static int getOrientation(byte[] jpeg) {
        if (jpeg == null) {
            return 0;
        }

        int offset = 0;
        int length = 0;

        // ISO/IEC 10918-1:1993(E)
        while (offset + 3 < jpeg.length && (jpeg[offset++] & 0xFF) == 0xFF) {
            int marker = jpeg[offset] & 0xFF;

            // Check if the marker is a padding.
            if (marker == 0xFF) {
                continue;
            }
            offset++;

            // Check if the marker is SOI or TEM.
            if (marker == 0xD8 || marker == 0x01) {
                continue;
            }
            // Check if the marker is EOI or SOS.
            if (marker == 0xD9 || marker == 0xDA) {
                break;
            }

            // Get the length and check if it is reasonable.
            length = pack(jpeg, offset, 2, false);
            if (length < 2 || offset + length > jpeg.length) {
                Log.e(TAG, "Invalid length");
                return 0;
            }

            // Break if the marker is EXIF in APP1.
            if (marker == 0xE1 && length >= 8
                    && pack(jpeg, offset + 2, 4, false) == 0x45786966
                    && pack(jpeg, offset + 6, 2, false) == 0) {
                offset += 8;
                length -= 8;
                break;
            }

            // Skip other markers.
            offset += length;
            length = 0;
        }

        // JEITA CP-3451 Exif Version 2.2
        if (length > 8) {
            // Identify the byte order.
            int tag = pack(jpeg, offset, 4, false);
            if (tag != 0x49492A00 && tag != 0x4D4D002A) {
                Log.e(TAG, "Invalid byte order");
                return 0;
            }
            boolean littleEndian = (tag == 0x49492A00);

            // Get the offset and check if it is reasonable.
            int count = pack(jpeg, offset + 4, 4, littleEndian) + 2;
            if (count < 10 || count > length) {
                Log.e(TAG, "Invalid offset");
                return 0;
            }
            offset += count;
            length -= count;

            // Get the count and go through all the elements.
            count = pack(jpeg, offset - 2, 2, littleEndian);
            while (count-- > 0 && length >= 12) {
                // Get the tag and check if it is orientation.
                tag = pack(jpeg, offset, 2, littleEndian);
                if (tag == 0x0112) {
                    // We do not really care about type and count, do we?
                    int orientation = pack(jpeg, offset + 8, 2, littleEndian);
                    switch (orientation) {
                        case 1:
                            return 0;
                        case 3:
                            return 180;
                        case 6:
                            return 90;
                        case 8:
                            return 270;
                        default:
                            return 0;
                    }
                }
                offset += 12;
                length -= 12;
            }
        }

        Log.i(TAG, "Orientation not found");
        return 0;
    }

    private static int pack(byte[] bytes, int offset, int length,
                            boolean littleEndian) {
        int step = 1;
        if (littleEndian) {
            offset += length - 1;
            step = -1;
        }

        int value = 0;
        while (length-- > 0) {
            value = (value << 8) | (bytes[offset] & 0xFF);
            offset += step;
        }
        return value;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     *          颜色矩阵              颜色分量矩阵
     *       | a b c d e |           |R|
     * A =   | f g h i j |      C=   |G|
     *       | k l m n o |           |B|
     *       | p q r s t |           |A|
     *                               |1|
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

    public Bitmap createBitmap(int effect, boolean waterMark, byte[] data) {

        Log.d(TAG, "createBitmap: effect=" + effect);

        Bitmap bitmap = createOriginal(data, waterMark);

        switch (effect) {
            case SKETCH:
                setStop(false);
                bitmap = compress(bitmap, 480, 800);
                Bitmap temp = createEffect(bitmap, grayMatrix);
                Bitmap newBitmap = createSobel(temp);
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
            case HIGHSATURATION:
                bitmap = createEffect(bitmap, highSaturationMatrix);
                break;
            case ORIGINAL:
            default:
                break;
        }

        return bitmap;
    }


    private Bitmap createOriginal(final byte[] data, boolean waterMark) {

        final int rotation = getOrientation(data);

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

        options.inSampleSize = calculateInSampleSize(options, size, size);
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

        if (waterMark) {
            return drawWaterMark(bitmap);
        }

        return bitmap;
    }

    /**
     * @param bitmap
     * @param matrix
     * @return
     */
    public Bitmap createEffect(Bitmap bitmap, float[] matrix) {

        Bitmap bmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.set(matrix);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return bmp;
    }


    /**
     * Bitmap压缩
     *
     * @param
     * @param
     * @param
     * @return
     */
    private Bitmap compress(final Bitmap bitmap, int reqWidth, int reqHeight) {
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

    /**
     * @param src
     * @return
     */
    private Bitmap drawWaterMark(Bitmap src) {

        int width = src.getWidth();
        int height = src.getHeight();

        Log.d(TAG, "addWaterMark: width=" + width + ", height=" + height);

        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(newBitmap);

        mCanvas.save();
        mCanvas.drawBitmap(src, 0, 0, null);

        Paint textPaint = new Paint();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String time = sdf.format(new Date(System.currentTimeMillis()));
        textPaint.setColor(Color.GRAY);
        textPaint.setTextSize(CameraApplicaton.dip2px(20.0f));
        float textWidth = textPaint.measureText(time, 0, time.length());
        Log.d(TAG, "addWaterMark: time=" + time + ", textWidth=" + textWidth);
        mCanvas.drawText(time, width - textWidth - 10, height - 10, textPaint);

        mCanvas.save(Canvas.ALL_SAVE_FLAG);
        mCanvas.restore();


        if (!src.isRecycled()) {
            Log.d(TAG, "addWaterMark: recycle bitmap");
            src.recycle();
        }

        return newBitmap;

    }

    //===================sobel==========================
    /**
     * Gx                  Gy                   P
     * -1  0  +1           +1  +2  +1         x-1,y-1  x,y-1  x+1,y-1
     * -2  0  +2           0   0   0          x-1,y    x,y    x+1,y
     * -1  0  +1           -1  -2  -1         x-1,y+1  x,y+1  x+1,y+1
     */

    private boolean mStop = false;

    public void setStop(boolean stop) {
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
                if (mStop) {
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

        return Bitmap.createBitmap(cmap, temp.getWidth(), temp.getHeight(),
                Bitmap.Config.ARGB_8888);
    }
}
