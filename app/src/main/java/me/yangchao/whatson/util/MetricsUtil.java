package me.yangchao.whatson.util;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by richard on 5/7/17.
 */

public class MetricsUtil {
    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
