package com.android.mazhengyang.beautycam.ui.animation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

/**
 * Created by mazhengyang on 18-10-12.
 */

public class AnimationUtil {

    private static final String TAG = AnimationUtil.class.getSimpleName();

    public AnimationUtil() {
    }

    public AnimatorSet rotateAnimators(View view) {

        PropertyValuesHolder rotation = PropertyValuesHolder.ofFloat("rotation", 0f, 360f);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, rotation);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(objectAnimator);
        animatorSet.setDuration(500);

        return animatorSet;
    }

    public void storeAnimators(View view, final AnimationCallback callback) {

        //reset x y
        ObjectAnimator resetX = ObjectAnimator.ofFloat(view, "translationX", -view.getRootView().getWidth() / 2, 0);
        ObjectAnimator resetY = ObjectAnimator.ofFloat(view, "translationY", view.getRootView().getHeight() / 2, 0);
        resetX.setDuration(0);
        resetY.setDuration(0);
        final AnimatorSet resetAnimatorSet = new AnimatorSet();
        resetAnimatorSet.playTogether(resetX, resetY);

        //scale
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.1f);
        scaleX.setDuration(1000);
        scaleY.setDuration(1000);
        //translate
        ObjectAnimator translationX = ObjectAnimator.ofFloat(view, "translationX", 0, -view.getRootView().getWidth() / 2);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(view, "translationY", 0, view.getRootView().getHeight() / 2);
        translationX.setDuration(1000);
        translationY.setDuration(1000);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(translationX, translationY, scaleX, scaleY);
        animatorSet.setInterpolator(new AccelerateInterpolator());
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (callback != null) {
                    callback.start();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (callback != null) {
                    callback.end();
                }
                resetAnimatorSet.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
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
