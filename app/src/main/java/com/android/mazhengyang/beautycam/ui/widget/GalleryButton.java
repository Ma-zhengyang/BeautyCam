package com.android.mazhengyang.beautycam.ui.widget;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.util.Log;

import com.android.mazhengyang.beautycam.utils.LoadImageCallback;
import com.android.mazhengyang.beautycam.utils.LoadImageTask;

import java.io.File;

/**
 * Created by mazhengyang on 18-10-16.
 */

public class GalleryButton extends android.support.v7.widget.AppCompatImageButton {

    private static final String TAG = GalleryButton.class.getSimpleName();

    public GalleryButton(Context context) {
        this(context, null);
    }

    public GalleryButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GalleryButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public interface GalleryButtonCallback {
        boolean onRequestStoragePermission();
    }

    private boolean checkPermission(GalleryButtonCallback callback) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "loadLatest: no storage permission, callback=" + callback);
                if (callback != null) {
                    callback.onRequestStoragePermission();
                }
                return false;
            } else {
                return true;
            }
        } else {
            Log.d(TAG, "loadLatest: granted camera permission first");
            return false;
        }
    }

    private String getLatestName() {
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }

        String sd = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        String dir = sd.concat("/BeautyCam/");
        File f = new File(dir);
        if (!f.exists()) {
            return null;
        }

        File[] files = f.listFiles();
        Long max = -1L;
        String target = null;

        for (int i = 0; i < files.length; i++) {
            String title = files[i].getName();

            if (title.startsWith("image_")) {

                Log.d(TAG, "subscribe: title=" + title);

                String time = title.substring(title.indexOf("_") + 1, title.lastIndexOf("."));
                time = time.replace("_", "");
                time = time.replace("-", "");
                time = time.replace(":", "");

                Long t = Long.valueOf(time);

                if (max < t) {
                    max = t;
                    target = title;
                }
            }
        }

        if(target == null){
            return null;
        }

        Log.d(TAG, "subscribe: target=" + target);

        return dir.concat(target);
    }

    public void loadLatest(GalleryButtonCallback callback) {

        Log.d(TAG, "loadLatest: ");

        if (!checkPermission(callback)) {
            return;
        }

        String name = getLatestName();

        if(name == null){
            return;
        }

        new LoadImageTask().load(name, new LoadImageCallback() {
            @Override
            public void callback(Bitmap result) {
                Log.d(TAG, "callback: smallImageBackgroud=" + result);
                if (result != null) {
                    setImageBitmap(result);
                }
            }
        }, getMeasuredWidth());

    }

}
