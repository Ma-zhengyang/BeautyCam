package com.android.mazhengyang.beautycam.Bean;

import android.content.Context;

import com.android.mazhengyang.beautycam.utils.GPUImageFilterTools;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * Created by mazhengyang on 18-10-17.
 */

public class EffectBean {

    private GPUImageFilter filter;
    private int resTitle;
    private int resImg;
    private int degree;
    private boolean checked = false;


    public EffectBean(Context context, int title,
                      int resImg, int degree, GPUImageFilterTools.FilterType type) {
        this.resTitle = title;
        this.resImg = resImg;
        this.degree = degree;
        filter = GPUImageFilterTools.createFilterForType(context, type);
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }

    public GPUImageFilter getFilter() {
        return filter;
    }

    public int getTitle() {
        return resTitle;
    }

    public int getResImg() {
        return resImg;
    }

    public int getDegree() {
        return degree;
    }
}
