package org.techteam.decider.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class Clipboard {
    public static void copyText(Context context, String label, String text) {
        ClipboardManager clipboard = (ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clip = ClipData.newPlainText(label, text);

        clipboard.setPrimaryClip(clip);
    }

    public static void copyText(Context context, int labelResId, String text) {
        copyText(context, context.getString(labelResId), text);
    }
}
