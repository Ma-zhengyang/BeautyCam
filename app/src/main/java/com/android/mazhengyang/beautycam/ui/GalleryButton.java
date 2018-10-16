package com.android.mazhengyang.beautycam.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.util.Log;

import com.android.mazhengyang.beautycam.utils.ImageUtil;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

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

    public void loadLatest(GalleryButtonCallback callback) {

        Log.d(TAG, "loadLatest: ");

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "loadLatest: no storage permission, callback=" + callback);
                if (callback != null) {
                    callback.onRequestStoragePermission();
                }
                return;
            }
        } else {
            Log.d(TAG, "loadLatest: granted camera permission first");
            return;
        }

        Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(ObservableEmitter<Bitmap> emitter) throws Exception {

                String state = Environment.getExternalStorageState();
                if (!state.equals(Environment.MEDIA_MOUNTED)) {
                    return;
                }

                String sd = Environment.getExternalStorageDirectory()
                        .getAbsolutePath();

                String dir = sd + "/BeautyCam/";
                File f = new File(dir);
                if (!f.exists()) {
                    return;
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

                Log.d(TAG, "subscribe: target=" + target);

                if (target != null) {
                    Bitmap bitmap = ImageUtil.decodeFile(dir.concat(target), getMeasuredWidth(), true);
                    if (bitmap != null) {
                        emitter.onNext(bitmap);
                    }
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) throws Exception {
                        if (bitmap != null) {
                            setImageBitmap(bitmap);
                        }
                    }
                });

    }

}
