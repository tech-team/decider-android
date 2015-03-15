package org.techteam.decider.util;

import android.content.Context;
import android.widget.Toast;

public abstract class Toaster {

    public static void toast(Context context, int resId) {
        toast(context, context.getString(resId), false);
    }

    public static void toast(Context context, String message) {
        toast(context, message, false);
    }

    public static void toastLong(Context context, int resId) {
        toast(context, context.getString(resId), true);
    }

    public static void toastLong(Context context, String message) {
        toast(context, message, true);
    }

    private static void toast(Context context, String message, boolean longToast) {
        int length = longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast.makeText(context, message, length).show();
    }
}
