package com.android.mazhengyang.beautycam.ui.animation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

/**
 * Created by mazhengyang on 18-10-12.
 */

public class AnimationUtil {

    private static final String TAG = AnimationUtil.class.getSimpleName();

    public AnimationUtil() {
    }

    public void playPreview(View view) {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 0.0f, 1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 0.0f, 1.0f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.3f, 1.0f);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY, alpha);
        objectAnimator.setDuration(1000).start();
    }

    public void playRotate(View view) {

        PropertyValuesHolder rotation = PropertyValuesHolder.ofFloat("rotation", 0f, 360f);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, rotation);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(objectAnimator);
        animatorSet.setDuration(500);
        animatorSet.start();
    }

    public void playScale(final View view, final AnimationCallback callback) {

        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.3f, 1.0f, 0.3f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(800);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (callback != null) {
                    callback.start();
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (callback != null) {
                    callback.end();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(scaleAnimation);
    }

    public void playSaveImage(View view, final AnimationCallback callback) {

        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.3f, 1.0f, 0.3f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.0f, ScaleAnimation.RELATIVE_TO_SELF, 1.0f);
        scaleAnimation.setDuration(1000);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (callback != null) {
                    callback.start();
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (callback != null) {
                    callback.end();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(scaleAnimation);
    }

}
