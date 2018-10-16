package com.android.mazhengyang.beautycam.ui;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.android.mazhengyang.beautycam.R;

/**
 * Created by mazhengyang on 18-10-16.
 */

public class ShutterButton extends android.support.v7.widget.AppCompatButton implements View.OnClickListener {

    @NonNull
    final AnimationDrawable frameAnimation;
    View.OnClickListener listener;

    public ShutterButton(Context context) {
        this(context, null, 0);
    }

    public ShutterButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShutterButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setBackgroundResource(R.drawable.button_shutter_pressed_animation);
        frameAnimation = (AnimationDrawable) getBackground();
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
        super.setOnClickListener(this);
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        listener.onClick(v);
        playAnimation();
    }

    private void playAnimation() {
        post(new Runnable() {
            public void run() {
                if (frameAnimation.isRunning()) {
                    frameAnimation.stop();
                }
                frameAnimation.start();
            }
        });
    }
}
