package org.techteam.decider.gui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.gui.WorkingFileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShareHelper {
    public static void share(final Context context, QuestionEntry entry) {
        ImageLoader.getInstance().loadImage(
                "http://img3.wikia.nocookie.net/__cb20121227201208/jamesbond/images/6/61/Generic_Placeholder_-_Profile.jpg",
                new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        File imagePath = new File(context.getFilesDir(), "images");
                        imagePath.mkdir();

                        File file = new File(imagePath, "default_image.jpg");

                        if (file.exists()) {
                            file.delete();
                        }

                        FileOutputStream out = null;
                        try {
                            out = new FileOutputStream(file);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        loadedImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Uri uri = WorkingFileProvider.getUriForFile(context, "org.techteam.decider.lol", file);

                        ArrayList<Uri> images = new ArrayList<>();
                        images.add(uri);
                        images.add(uri);

                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        sendIntent.setType("image/*");
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "test");
                        sendIntent.putExtra(Intent.EXTRA_STREAM, images);

                        //grant permisions for all apps that can handle given intent
                        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(sendIntent, PackageManager.MATCH_DEFAULT_ONLY);
                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }

                        context.startActivity(Intent.createChooser(sendIntent, "Select app"));
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }
                });
    }
}
