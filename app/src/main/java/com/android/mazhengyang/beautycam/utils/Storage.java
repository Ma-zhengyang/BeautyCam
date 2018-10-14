package com.android.mazhengyang.beautycam.utils;

import java.io.File;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

public class Storage {

    private static final String TAG = Storage.class.getSimpleName();

    public static void writeFile(String path, Bitmap bitmap) {
        try {
            File file = new File(path);
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
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

        String dir = null;

        try {

            String state = Environment.getExternalStorageState();
            if (!state.equals(Environment.MEDIA_MOUNTED)) {
                return null;
            }

            String sd = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();

            dir = sd + "/BeautyCam/";
            File f = new File(dir);
            if (!f.exists()) {
                f.mkdir();
            }

        } catch (Exception e) {
            Log.e(TAG, "Create RESULT.TXT Fail..." + e.getMessage());
        }

        return dir + title + ".jpg";
    }

}
