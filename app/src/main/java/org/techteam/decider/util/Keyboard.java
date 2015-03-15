package org.techteam.decider.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public abstract class Keyboard {
    public static void hideSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }
}
