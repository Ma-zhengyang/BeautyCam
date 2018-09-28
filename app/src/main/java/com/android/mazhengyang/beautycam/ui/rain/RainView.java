package com.android.mazhengyang.beautycam.ui.rain;

/**
 * Created by mazhengyang on 18-9-28.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 雨滴视图, DELAY时间重绘, 绘制NUM_SNOWFLAKES个雨滴
 */
public class RainView extends View {

    private static final String TAG = RainView.class.getSimpleName();

    private static final int NUM_SNOWFLAKES = 150; // 雨滴数量
    private static final int DELAY = 5; // 延迟
    private RainFlake[] mSnowFlakes; // 雨滴

    private Paint paint;
    private Rect mClipRect = new Rect();
    private boolean show = false;

    public RainView(Context context) {
        super(context);
    }

    public RainView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RainView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {

            mClipRect.set(0, 0, w, h);

            initSnow(w, h);
        }
    }

    private void initSnow(int width, int height) {

        Log.d(TAG, "initSnow: ");

        if (paint == null) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG); // 抗锯齿
            paint.setColor(Color.WHITE); // 雨滴的颜色
            paint.setStyle(Paint.Style.FILL); // 填充;
        }

        mSnowFlakes = new RainFlake[NUM_SNOWFLAKES];
        //mSnowFlakes所有的雨滴都生成放到这里面
        for (int i = 0; i < NUM_SNOWFLAKES; ++i) {
            mSnowFlakes[i] = RainFlake.create(width, height, paint);
        }
    }

    public void show() {
        this.show = true;
        invalidate();
    }

    public void hide() {
        this.show = false;
        invalidate();
    }

    public void clip(int left, int top, int right, int bottom) {
        mClipRect.set(left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (show) {
            canvas.save();

            canvas.clipRect(mClipRect.left, mClipRect.top, mClipRect.right, mClipRect.bottom);

            //for返回SnowFlake
            for (RainFlake s : mSnowFlakes) {
                //然后进行绘制
                s.draw(canvas);
            }

            canvas.restore();

            // 隔一段时间重绘一次, 动画效果
            getHandler().postDelayed(runnable, DELAY);
        }

    }

    // 重绘线程
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //自动刷新
            invalidate();
        }
    };
}

