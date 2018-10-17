package com.android.mazhengyang.beautycam.effect;

import android.content.Context;

import com.android.mazhengyang.beautycam.utils.GPUImageFilterTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mazhengyang on 18-10-17.
 */

public class EffectService {

    private static EffectService mInstance;

    public static EffectService getInst() {
        if (mInstance == null) {
            synchronized (EffectService.class) {
                if (mInstance == null)
                    mInstance = new EffectService();
            }
        }
        return mInstance;
    }

    private EffectService() {
    }

    public List<FilterEffect> getLocalFilters(Context context) {
        List<FilterEffect> filters = new ArrayList<>();
        filters.add(new FilterEffect(context, "原始", GPUImageFilterTools.FilterType.NORMAL, 0));
        filters.add(new FilterEffect(context, "暧昧", GPUImageFilterTools.FilterType.ACV_AIMEI, 0));
        filters.add(new FilterEffect(context, "淡蓝", GPUImageFilterTools.FilterType.ACV_DANLAN, 0));
        filters.add(new FilterEffect(context, "蛋黄", GPUImageFilterTools.FilterType.ACV_DANHUANG, 0));
        filters.add(new FilterEffect(context, "复古", GPUImageFilterTools.FilterType.ACV_FUGU, 0));
        filters.add(new FilterEffect(context, "高冷", GPUImageFilterTools.FilterType.ACV_GAOLENG, 0));
        filters.add(new FilterEffect(context, "怀旧", GPUImageFilterTools.FilterType.ACV_HUAIJIU, 0));
        filters.add(new FilterEffect(context, "胶片", GPUImageFilterTools.FilterType.ACV_JIAOPIAN, 0));
        filters.add(new FilterEffect(context, "可爱", GPUImageFilterTools.FilterType.ACV_KEAI, 0));
        filters.add(new FilterEffect(context, "落寞", GPUImageFilterTools.FilterType.ACV_LOMO, 0));
        filters.add(new FilterEffect(context, "加强", GPUImageFilterTools.FilterType.ACV_MORENJIAQIANG, 0));
        filters.add(new FilterEffect(context, "暖心", GPUImageFilterTools.FilterType.ACV_NUANXIN, 0));
        filters.add(new FilterEffect(context, "清新", GPUImageFilterTools.FilterType.ACV_QINGXIN, 0));
        filters.add(new FilterEffect(context, "日系", GPUImageFilterTools.FilterType.ACV_RIXI, 0));
        filters.add(new FilterEffect(context, "温暖", GPUImageFilterTools.FilterType.ACV_WENNUAN, 0));

        return filters;
    }

}
