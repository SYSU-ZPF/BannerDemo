package com.zpf.bannerdemo;

import android.content.Context;
import android.util.TypedValue;

public class Util {

    /**
     * dp转px
     *
     * @param dpVal dp值
     * @return result in px
     */
    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }
}
