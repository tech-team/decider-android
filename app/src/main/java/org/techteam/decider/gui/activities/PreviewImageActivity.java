package org.techteam.decider.gui.activities;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.techteam.decider.R;
import org.techteam.decider.gui.views.TouchImageView;
import org.techteam.decider.util.ImageLoaderInitializer;

import me.zhanghai.android.materialprogressbar.IndeterminateProgressDrawable;

public class PreviewImageActivity extends ToolbarActivity {
    public final static String IMAGE_URL = "IMAGE_URL";

    TouchImageView imageView;
    LinearLayout progressView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_image_preview);

        imageView = (TouchImageView) findViewById(R.id.image);
        progressView = (LinearLayout) findViewById(R.id.progress);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setIndeterminateDrawable(new IndeterminateProgressDrawable(this));

        String url = getIntent().getStringExtra(IMAGE_URL);

        final ImageLoader imageLoader = ImageLoaderInitializer.getImageLoader(this);
        imageLoader.displayImage(url, imageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                new AlertDialog.Builder(PreviewImageActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getString(R.string.loading_image))
                        .setMessage(getString(R.string.loading_image_failed))
                        .setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onBackPressed();
                            }
                        })
                        .show();
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progressView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
    }
}
