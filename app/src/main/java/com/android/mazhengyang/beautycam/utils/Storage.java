package com.android.mazhengyang.beautycam.utils;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

public class Storage {

    private static final String TAG = Storage.class.getSimpleName();

    public static synchronized void writeFile(String path, Bitmap bitmap) {
        try {
            Log.d(TAG, "writeFile: path=" + path);
            FileOutputStream out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            DataBuffer.cleanBitmap();

        } catch (Exception e) {
            Log.e(TAG, "Failed to write data", e);
        }
    }

    // Save the image and add it to media store.
    public static void addImage(String title, Bitmap bitmap) {
        // Save the image.
        String path = generateFilepath(title);
        if (path != null) {
            writeFile(path, bitmap);
        }
    }

    public static String generateFilepath(String title) {

        try {

            String state = Environment.getExternalStorageState();
            if (!state.equals(Environment.MEDIA_MOUNTED)) {
                return null;
            }

            String sd = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();

            String dir = sd + "/BeautyCam/";
            File f = new File(dir);
            if (!f.exists()) {
                f.mkdir();
            }

            if (f.exists()) {
                return dir + "/" + title + ".jpg";
            }else{
                Log.d(TAG, "generateFilepath: BeautyCam not exist");
            }

        } catch (Exception e) {
            Log.e(TAG, "Create RESULT.TXT Fail..." + e.getMessage());
        }

        return null;
    }

}
