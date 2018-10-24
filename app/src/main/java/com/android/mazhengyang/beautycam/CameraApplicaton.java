package com.android.mazhengyang.beautycam;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.android.mazhengyang.beautycam.ui.animation.AnimationUtil;

/**
 * Created by mazhengyang on 18-10-12.
 */

public class CameraApplicaton extends Application {

    private final String TAG = CameraApplicaton.class.getSimpleName();

    private static CameraApplicaton baseApplication;

    private static AnimationUtil mAnimationUtil;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
        baseApplication = this;
        mAnimationUtil = new AnimationUtil();
    }

    public static Context getAppContext() {
        return baseApplication;
    }

    public static float dip2px(float dpValue) {
        final float scale = baseApplication.getResources().getDisplayMetrics().density;
        return dpValue * scale + 0.5f;
    }

    public static AnimationUtil getAnim(){
        return mAnimationUtil;
    }

}
