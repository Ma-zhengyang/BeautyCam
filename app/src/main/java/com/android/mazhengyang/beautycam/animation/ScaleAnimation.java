package com.android.mazhengyang.beautycam.animation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

/**
 * Created by mzy on 2018/9/29.
 */

public class ScaleAnimation {

    public ScaleAnimation() {
    }

    public AnimatorSet getInAnimators(View view) {

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.0f);
        PropertyValuesHolder rotation = PropertyValuesHolder.ofFloat("rotation", 0f, 360f);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY, rotation);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(objectAnimator);
        animatorSet.setInterpolator(new AccelerateInterpolator());
        animatorSet.setDuration(500);

        return animatorSet;
    }

    public AnimatorSet getOutAnimators(View view) {

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 0.0f, 1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 0.0f, 1.0f);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(objectAnimator);
        animatorSet.setInterpolator(new AccelerateInterpolator());
        animatorSet.setDuration(500);

        return animatorSet;
    }
}
