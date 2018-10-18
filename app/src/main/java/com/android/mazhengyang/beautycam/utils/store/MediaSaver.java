package com.android.mazhengyang.beautycam.utils.store;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;

public class MediaSaver extends Thread {

    private static final String TAG = MediaSaver.class.getSimpleName();

    private static final int SAVE_QUEUE_LIMIT = 3;

    private ArrayList<SaveRequest> mQueue;
    private boolean mStop;

    public MediaSaver() {
        mQueue = new ArrayList<SaveRequest>();
        start();
    }

    // Runs in main thread
    public synchronized boolean queueFull() {
        return (mQueue.size() >= SAVE_QUEUE_LIMIT);
    }

    // Runs in main thread
    public void addImage(Bitmap bitmap, String title, StoreCallback callback) {

        SaveRequest r = new SaveRequest();
        r.bitmap = bitmap;
        r.title = title;
        r.callback = callback;
        synchronized (this) {
            while (mQueue.size() >= SAVE_QUEUE_LIMIT) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    // ignore.
                }
            }
            mQueue.add(r);
            notifyAll(); // Tell saver thread there is new work to do.
        }
    }

    // Runs in saver thread
    @Override
    public void run() {
        while (true) {
            SaveRequest r;
            synchronized (this) {
                if (mQueue.isEmpty()) {
                    notifyAll(); // notify main thread in waitDone

                    // Note that we can only stop after we saved all images
                    // in the queue.
                    if (mStop)
                        break;

                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        // ignore.
                    }
                    continue;
                }
                if (mStop)
                    break;
                r = mQueue.remove(0);
                notifyAll(); // the main thread may wait in addImage
            }
            storeImage(r.bitmap, r.title, r.callback);
        }
        if (!mQueue.isEmpty()) {
            Log.e(TAG, "Media saver thread stopped with " + mQueue.size() + " images unsaved");
            mQueue.clear();
        }
    }

    // Runs in main thread
    public void finish() {
        synchronized (this) {
            mStop = true;
            notifyAll();
        }
    }

    // Runs in saver thread
    private void storeImage(Bitmap bitmap, String title, StoreCallback callback) {
        Storage.addImage(title, bitmap, callback);
    }

    // Each SaveRequest remembers the data needed to save an image.
    private static class SaveRequest {
        Bitmap bitmap;
        String title;
        StoreCallback callback;
    }
}
