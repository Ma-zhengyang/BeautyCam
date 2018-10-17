package com.android.mazhengyang.beautycam.effect;

import android.content.Context;

import com.android.mazhengyang.beautycam.utils.GPUImageFilterTools;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * Created by mazhengyang on 18-10-17.
 */

public class FilterEffect {

    private String title;
    private GPUImageFilterTools.FilterType type;
    private GPUImageFilter filter;
    private int degree;

    public FilterEffect(Context context, String title, GPUImageFilterTools.FilterType type, int degree) {
        this.type = type;
        this.degree = degree;
        this.title = title;
        filter = GPUImageFilterTools.createFilterForType(context, type);
    }


    public GPUImageFilterTools.FilterType getType() {
        return type;
    }

    public GPUImageFilter getFilter() {
        return filter;
    }

    public String getTitle() {
        return title;
    }

    public int getDegree() {
        return degree;
    }
}
