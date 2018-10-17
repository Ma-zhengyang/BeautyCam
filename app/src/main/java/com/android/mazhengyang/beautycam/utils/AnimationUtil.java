package com.android.mazhengyang.beautycam.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.android.mazhengyang.beautycam.ui.GalleryButton;

/**
 * Created by mazhengyang on 18-10-12.
 */

public class AnimationUtil {

    private static final String TAG = AnimationUtil.class.getSimpleName();

    public AnimationUtil() {
    }

    public AnimatorSet rotateAnimators(View view) {

        PropertyValuesHolder rotation = PropertyValuesHolder.ofFloat("rotationY", 0f, 360f);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, rotation);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(objectAnimator);
        animatorSet.setDuration(500);

        return animatorSet;
    }

    public AnimatorSet storeAnimators(View view, int[] location) {

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.1f);
        scaleX.setDuration(800);
        scaleY.setDuration(800);
        ObjectAnimator translationX = ObjectAnimator.ofFloat(view, "translationX", 0, -view.getRootView().getWidth() / 2 + location[0]);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(view, "translationY", 0, view.getRootView().getHeight() / 2);
        translationX.setDuration(800);
        translationY.setDuration(800);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, translationX, translationY);
        animatorSet.setInterpolator(new AccelerateInterpolator());

        //reset x y
        ObjectAnimator resetX = ObjectAnimator.ofFloat(view, "translationX", -view.getRootView().getWidth() / 2 + location[0], 0);
        ObjectAnimator resetY = ObjectAnimator.ofFloat(view, "translationY", view.getRootView().getHeight() / 2, 0);
        resetX.setDuration(0);
        resetY.setDuration(0);
        final AnimatorSet resetAnimatorSet = new AnimatorSet();
        resetAnimatorSet.playTogether(resetX, resetY);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                Log.d(TAG, "onAnimationEnd: start reset");
                resetAnimatorSet.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });

        return animatorSet;
    }

    public AnimatorSet hideAnimators(View view) {

        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, alpha);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(objectAnimator);
        animatorSet.setInterpolator(new AccelerateInterpolator());
        animatorSet.setDuration(500);

        return animatorSet;
    }

    public AnimatorSet showAnimators(View view) {

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 0.0f, 1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 0.0f, 1.0f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY, alpha);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(objectAnimator);
        animatorSet.setInterpolator(new AccelerateInterpolator());
        animatorSet.setDuration(500);

        return animatorSet;
    }

}
