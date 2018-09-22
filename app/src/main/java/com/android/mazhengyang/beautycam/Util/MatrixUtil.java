package com.android.mazhengyang.beautycam.Util;

/**
 * Created by mazhengyang on 18-9-21.
 */

public class MatrixUtil {

    /**
     *     颜色矩阵              颜色分量矩阵
     *       | a b c d e |           |R|
     * A =   | f g h i j |      C=   |G|
     *       | k l m n o |           |B|
     *       | p q r s t |           |A|
     *                               |1|
     *
     * R1 = aR + bG + cB + dA + e
     * G1 = fR + gG + hB + iA + j
     * B1 = kR + lG + mB + nA + o
     * A1 = pR + qG + rB + sA + t
     *
     * 第一行的 abcde 用来决定新的颜色值中的R——红色
     * 第二行的 fghij 用来决定新的颜色值中的G——绿色
     * 第三行的 klmno 用来决定新的颜色值中的B——蓝色
     * 第四行的 pqrst 用来决定新的颜色值中的A——透明度
     * 矩阵A中第五列——ejot值分别用来决定每个分量中的 offset ，即偏移量
     *
     */

    //初始颜色矩阵
    public static float[] normalMatrix = new float[]{
            1, 0, 0, 0, 0,
            0, 1, 0, 0, 0,
            0, 0, 1, 0, 0,
            0, 0, 0, 0, 0,
    };

    //一些常用的图像处理效果的颜色矩阵

    //灰度效果
    public static float[] grayMatrix = new float[]{
            0.33f, 0.59f, 0.11f, 0.0f, 0.0f,
            0.33f, 0.59f, 0.11f, 0.0f, 0.0f,
            0.33f, 0.59f, 0.11f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f,
    };

    //图像反转
    public static float[] reverseMatrix = new float[]{
            -1, 0, 0, 1, 1,
            0, -1, 0, 1, 1,
            0, 0, -1, 1, 1,
            0, 0, 0, 1, 0,
    };

    //怀旧效果
    public static float[] pasttimeMatrix = new float[]{
            0.393f, 0.769f, 0189f, 0.0f, 0.0f,
            0.349f, 0.686f, 0.168f, 0.0f, 0.0f,
            0.272f, 0.534f, 0.131f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f,
    };

    //图像反转
    public static float[] quseMatrix = new float[]{
            1.5f, 1.5f, 1.5f, 0, -1,
            1.5f, 1.5f, 1.5f, 0, -1,
            1.5f, 1.5f, 1.5f, 0, -1,
            0, 0, 0, 1, 0,
    };


}
