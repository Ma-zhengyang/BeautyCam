package com.android.mazhengyang.beautycam.util;

import java.util.Random;

/**
 * Created by mazhengyang on 18-9-25.
 */

public class RandomUtil {
    private static Random RANDOM = new Random();

    public RandomUtil() {
    }

    public float getRandom(float lower, float upper) {
        float min = Math.min(lower, upper);
        return getRandom(Math.max(lower, upper) - min) + min;
    }

    public float getRandom(float upper) {
        return RANDOM.nextFloat() * upper;
    }

    public int getRandom(int upper) {
        return RANDOM.nextInt(upper);
    }

    public int[] getLine(int width, int height) {
        int[] nline = new int[4];
        nline[0] = getRandom(width);
        nline[1] = getRandom(height);
        nline[2] = nline[0] - 2;
        nline[3] = nline[1] + getRandom(50);
        return nline;
    }

}
