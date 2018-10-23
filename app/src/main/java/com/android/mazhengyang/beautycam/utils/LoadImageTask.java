package com.android.mazhengyang.beautycam.utils;

import android.graphics.Bitmap;
import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by mazhengyang on 18-10-17.
 */

public class LoadImageTask {

    private static final String TAG = LoadImageTask.class.getSimpleName();

    public LoadImageTask() {

    }

    public void load(final byte[] data, final LoadImageCallback callback) {
        load(data, null, callback, true, 0, 0);
    }

    public void load(final byte[] data, final LoadImageCallback callback, int width, int height) {
        load(data, null, callback, false, width, height);
    }

    public void load(final String path, final LoadImageCallback callback, int width) {
        load(null, path, callback, false, width, 0);
    }

    private void load(final byte[] data, final String path, final LoadImageCallback callback, final boolean isOriginal,
                      final int width, final int height) {
        Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(ObservableEmitter<Bitmap> emitter) throws Exception {

                long start = System.currentTimeMillis();
                Log.d(TAG, "subscribe: start=" + start);

                Bitmap bitmap = null;

                if (data != null) {
                    if (isOriginal) {
                        bitmap = ImageUtil.getOriginalBitmap(data);
                        bitmap = ImageUtil.drawWaterMark(bitmap);
                    } else {
                        bitmap = ImageUtil.getResizedBitmap(data, width, height);
                    }
                } else if (path != null) {
                    bitmap = ImageUtil.decodeFile(path, width, true);
                }

                long end = System.currentTimeMillis();
                Log.d(TAG, "subscribe: end=" + end + ", used=" + (end - start));

                if (bitmap != null) {
                    emitter.onNext(bitmap);
                } else {
                    Log.d(TAG, "subscribe: bitmap is null");
                }

            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) throws Exception {
                        if (callback != null) {
                            callback.callback(bitmap);
                        }
                    }
                });
    }

}
