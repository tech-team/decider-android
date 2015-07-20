package org.techteam.decider.util;

import android.content.Context;

import java.io.File;

public class CacheHelper {
    public static void deleteCache(Context context) {
        File dir = context.getCacheDir();
        if (dir != null && dir.isDirectory()) {
            deleteDir(dir, false);
        }
    }

    public static boolean deleteDir(File dir, boolean removeSelf) {
        if (dir != null) {
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child), true);
                    if (!success) {
                        return false;
                    }
                }
            }
            return !removeSelf || dir.delete();
        } else {
            return false;
        }
    }
}
