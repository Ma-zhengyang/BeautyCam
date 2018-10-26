package com.android.mazhengyang.beautycam.utils;

import android.content.Context;

import com.android.mazhengyang.beautycam.Bean.EffectBean;
import com.android.mazhengyang.beautycam.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mazhengyang on 18-10-17.
 */

public class EffectFactory {

    private static EffectFactory mInstance;

    public static EffectFactory getInst() {
        if (mInstance == null) {
            synchronized (EffectFactory.class) {
                if (mInstance == null)
                    mInstance = new EffectFactory();
            }
        }
        return mInstance;
    }

    public EffectFactory() {
    }

    public List<EffectBean> getLocalFilters(Context context) {
        List<EffectBean> filters = new ArrayList<>();
        filters.add(new EffectBean(context, R.string.str_filger_normal, R.drawable.filger_normal, 0, GPUImageFilterTools.FilterType.NORMAL));
        filters.add(new EffectBean(context, R.string.str_filter_aimei, R.drawable.filter_aimei, 0, GPUImageFilterTools.FilterType.ACV_AIMEI));
        filters.add(new EffectBean(context, R.string.str_filter_danlan, R.drawable.filter_danlan, 0, GPUImageFilterTools.FilterType.ACV_DANLAN));
        filters.add(new EffectBean(context, R.string.str_filter_danhuang, R.drawable.filter_danhuang, 0, GPUImageFilterTools.FilterType.ACV_DANHUANG));
        filters.add(new EffectBean(context, R.string.str_filter_fugu, R.drawable.filter_fugu, 0, GPUImageFilterTools.FilterType.ACV_FUGU));
        filters.add(new EffectBean(context, R.string.str_filter_gaoleng, R.drawable.filter_gaoleng, 0, GPUImageFilterTools.FilterType.ACV_GAOLENG));
        filters.add(new EffectBean(context, R.string.str_filter_huaijiu, R.drawable.filter_huaijiu, 0, GPUImageFilterTools.FilterType.ACV_HUAIJIU));
        filters.add(new EffectBean(context, R.string.str_filter_jiaopian, R.drawable.filter_jiaopian, 0, GPUImageFilterTools.FilterType.ACV_JIAOPIAN));
        filters.add(new EffectBean(context, R.string.str_filter_keai, R.drawable.filter_keai, 0, GPUImageFilterTools.FilterType.ACV_KEAI));
        filters.add(new EffectBean(context, R.string.str_filter_luomo, R.drawable.filter_luomo, 0, GPUImageFilterTools.FilterType.ACV_LUOMO));
        filters.add(new EffectBean(context, R.string.str_filter_jiaqiang, R.drawable.filter_jiaqiang, 0, GPUImageFilterTools.FilterType.ACV_MORENJIAQIANG));
        filters.add(new EffectBean(context, R.string.str_filter_nuanxin, R.drawable.filter_nuanxin, 0, GPUImageFilterTools.FilterType.ACV_NUANXIN));
        filters.add(new EffectBean(context, R.string.str_filter_qingxin, R.drawable.filter_qingxin, 0, GPUImageFilterTools.FilterType.ACV_QINGXIN));
        filters.add(new EffectBean(context, R.string.str_filter_rixi, R.drawable.filter_rixi, 0, GPUImageFilterTools.FilterType.ACV_RIXI));
        filters.add(new EffectBean(context, R.string.str_filter_wennuan, R.drawable.filter_wennuan, 0, GPUImageFilterTools.FilterType.ACV_WENNUAN));

        return filters;
    }

}
