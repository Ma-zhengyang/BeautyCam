package com.android.mazhengyang.beautycam.animations;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;

/**
 * Created by mazhengyang on 18-8-22.
 */

public class RotateAnimation {

    public ValueAnimator getAnimators(View view) {
        return ObjectAnimator.ofFloat(view, "rotationY", 0f, 180f);
    }
}