package com.android.mazhengyang.beautycam.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.mazhengyang.beautycam.CameraApplicaton;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mazhengyang on 18-9-13.
 */

public class ImageUtil {
    private static final String TAG = ImageUtil.class.getSimpleName();


    public ImageUtil() {
    }

    public static int getRotate(final String filename) {

        try {
            final ExifInterface exif = new ExifInterface(filename);
            switch (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    return 0;
            }
        } catch (IOException e) {
            return 0;
        }
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

    //添加时间水印
    public static Bitmap drawWaterMark(Bitmap src) {

        int width = src.getWidth();
        int height = src.getHeight();

        Log.d(TAG, "drawWaterMark: width=" + width + ", height=" + height);

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
        Log.d(TAG, "drawWaterMark: time=" + time + ", textWidth=" + textWidth);
        mCanvas.drawText(time, width - textWidth - 10, height - 10, textPaint);

        mCanvas.save(Canvas.ALL_SAVE_FLAG);
        mCanvas.restore();

        if (!src.isRecycled()) {
            Log.d(TAG, "drawWaterMark: recycle bitmap");
            src.recycle();
        }

        return newBitmap;
    }

    //获取原图
    public static Bitmap getOriginalBitmap(final byte[] data) {

        final int rotation = getOrientation(data);

        Log.d(TAG, "createOriginal: rotation=" + rotation);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);

        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            if (bitmap != rotatedBitmap) {
                bitmap.recycle();
            }
            bitmap = rotatedBitmap;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Log.d(TAG, "getOriginalBitmap: width=" + width + ", height=" + height);

        return bitmap;
    }

    //得到指定大小的Bitmap对象
    public static Bitmap getResizedBitmap(byte[] data, int width, int height) {

        final int rotation = getOrientation(data);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;//true,不分配空间,但可计算出原始图片的长度和宽度,
        opts.outWidth = width;
        opts.outHeight = height;
        opts.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);

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


    private static long freeMemory() {
        return Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    }

    private static void limitMemoryUsage(@NonNull BitmapFactory.Options options) {

        float bufferScale = 2f;

        if (options.inSampleSize < 1) {
            options.inSampleSize = 1;
        }

        if (freeMemory() < ((options.outWidth * options.outHeight * 4) / (options.inSampleSize * options.inSampleSize)) * 1.5f) {
            System.gc();
            System.gc();
        }

        while (freeMemory() < ((options.outWidth * options.outHeight * 4) / (options.inSampleSize * options.inSampleSize)) * bufferScale) {
            options.inSampleSize += 1;
        }
    }

    private static Bitmap decodeFile(final String pathName, final int startInSampleSize) {
        final BitmapFactory.Options opts = new BitmapFactory.Options();

        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, opts);
        limitMemoryUsage(opts);
        opts.inJustDecodeBounds = false;

        int inSampleSize = startInSampleSize;
        opts.inSampleSize = inSampleSize;
        opts.inDither = false;
        opts.inMutable = true;

        return BitmapFactory.decodeFile(pathName, opts);
    }

    public static Bitmap decodeFile(final String filename, final int minSize, final boolean square) {

        final int rotation = getRotate(filename);

        final BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, opts);

        final int size = Math.max(opts.outWidth, opts.outHeight);
        if (size > minSize && minSize > 0) {
            opts.inSampleSize = size / minSize;
        } else {
            opts.inSampleSize = 1;
        }

        Bitmap bitmap = decodeFile(filename, opts.inSampleSize);

        if (bitmap == null) return null;

        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            if (bitmap != rotatedBitmap) {
                bitmap.recycle();
            }
            bitmap = rotatedBitmap;
        }

        if (square && bitmap.getWidth() != bitmap.getHeight()) {
            if (bitmap.getWidth() > bitmap.getHeight()) {
                bitmap = Bitmap.createBitmap(bitmap, (bitmap.getWidth() - bitmap.getHeight()) / 2,
                        0,
                        bitmap.getHeight(),
                        bitmap.getHeight());
            } else if (bitmap.getWidth() < bitmap.getHeight()) {
                bitmap = Bitmap.createBitmap(bitmap, 0,
                        (bitmap.getHeight() - bitmap.getWidth()) / 2,
                        bitmap.getWidth(),
                        bitmap.getWidth());
            }
        }
        return bitmap;
    }

}
