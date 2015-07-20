package org.techteam.decider.gui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.techteam.decider.R;
import org.techteam.decider.gui.views.TouchImageView;
import org.techteam.decider.util.ImageLoaderInitializer;

public class PreviewImageActivity extends AppCompatActivity {
    public final static String IMAGE_URL = "IMAGE_URL";

    TouchImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_image_preview);

        imageView = (TouchImageView) findViewById(R.id.img);

        String url = getIntent().getStringExtra(IMAGE_URL);

        ImageLoader imageLoader = ImageLoaderInitializer.getImageLoader(this);
        imageLoader.displayImage(url, imageView);
    }
}
