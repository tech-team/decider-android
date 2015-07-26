package org.techteam.decider.util.image_selector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.android.camera.CropImageIntentBuilder;

import org.techteam.decider.R;
import org.techteam.decider.content.ImageData;
import org.techteam.decider.gui.activities.AddQuestionActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class ImageSelector implements View.OnClickListener {
    private static final String TAG = AddQuestionActivity.class.getName();

    private static final byte TAKE_PICTURE = 1;
    private static final byte SELECT_IMAGE = 2;
    private static final byte CROP_IMAGE = 3;

    private static final String ORIGINAL_FILE_EXTENSION = ".original.jpg";
    private static final String PREVIEW_FILE_EXTENSION = ".preview.jpg";
    private final byte imageId;

    private final Context context;
    private final ActivityStarter activityStarter;
    private final ImageHolder imageHolder;

    //TODO: aren't they messed up?
    private int aspectWidth = 9;
    private int aspectHeight = 16;
    private int previewWidth = 1280;
    private int previewHeight = previewWidth * aspectHeight / aspectWidth;

    public ImageSelector(Context context, ActivityStarter activityStarter, ImageView imageView, byte imageId) {
        this.context = context;
        this.activityStarter = activityStarter;
        this.imageId = imageId;
        imageHolder = new ImageHolder(imageView);

        imageView.setOnClickListener(this);
    }

    public ImageSelector(Context context, ActivityStarter activityStarter, ImageView imageView) {
        this(context, activityStarter, imageView, (byte) 0);
    }

    public void setParams(int aspectWidth, int aspectHeight, int previewWidth, int previewHeight) {
        this.aspectWidth = aspectWidth;
        this.aspectHeight = aspectHeight;
        this.previewWidth = previewWidth;
        this.previewHeight = previewHeight;
    }

    @Override
    public void onClick(View v) {
        final CharSequence fromCamera = context.getString(R.string.take_photo);
        final CharSequence fromGallery = context.getString(R.string.choose_from_gallery);
        final CharSequence items[] = { fromCamera, fromGallery };

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which == -1)
                            return;

                        if (items[which].equals(fromCamera)) {
                            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

                            File output = new File(dir, UUID.randomUUID() + ".jpeg");
                            imageHolder.setSource(Uri.fromFile(output));

                            takePicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));

                            CompoundRequestCode rc = new CompoundRequestCode(imageId, TAKE_PICTURE);
                            activityStarter.startActivityForResult(takePicture, rc.getValue());
                        } else if (items[which].equals(fromGallery)) {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                    MediaStore.Images.Media.INTERNAL_CONTENT_URI);

                            CompoundRequestCode rc = new CompoundRequestCode(imageId, SELECT_IMAGE);
                            activityStarter.startActivityForResult(pickPhoto, rc.getValue());
                        }
                    }
                })
                .show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        CompoundRequestCode rc = new CompoundRequestCode(requestCode);
        byte requestImageId = rc.getImageId();
        requestCode = rc.getRequestCode();

        if (requestImageId != imageId)
            return;

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_IMAGE || requestCode == TAKE_PICTURE) {
                Uri selectedImage = imageHolder.getSource();

                // we've just taken a photo
                if (data != null && requestCode == SELECT_IMAGE) {
                    selectedImage = fixUri(data.getData());
                }

                if (selectedImage == null) {
                    Log.e(TAG, "selectedImage is still null even after fixUri");
                    return;
                }

                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int deviceWidth = size.x;
                int deviceHeight = size.y;

                double ratio = (double) deviceHeight / (double) deviceWidth;

                int desiredWidth = deviceWidth / 2;
                int desiredHeight = (int) (deviceWidth * ratio);

                Bitmap bitmap = null;
                try {
                    bitmap = BitmapUtils.prepareBitmap(context, selectedImage, desiredWidth, desiredHeight);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (bitmap == null) {
                    Log.wtf(TAG, "prepared bitmap is null");
                    return;
                }

                selectedImage = saveBitmap(bitmap, selectedImage);
                imageHolder.setSource(selectedImage);


                // generate cropped path
                File cacheDir = context.getCacheDir();
                File croppedFile = new File(cacheDir,
                        selectedImage.getLastPathSegment() + PREVIEW_FILE_EXTENSION);

                imageHolder.setPreview(Uri.fromFile(croppedFile));

                cropImage();
            } else if (requestCode == CROP_IMAGE) {
                showImage();
            }
        }
    }

    private Uri saveBitmap(Bitmap bmp, Uri uri) {

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(uri.getPath());
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return uri;
    }

    private void cropImage() {
        CropImageIntentBuilder cropBuilder = new CropImageIntentBuilder(
                aspectWidth, aspectHeight,
                previewWidth, previewHeight,
                imageHolder.getPreview());
        cropBuilder.setScaleUpIfNeeded(true);

        cropBuilder.setSourceImage(imageHolder.getSource());

        Intent intent = cropBuilder.getIntent(context);

        CompoundRequestCode rc = new CompoundRequestCode(imageId, CROP_IMAGE);
        activityStarter.startActivityForResult(intent, rc.getValue());
    }


    private void showImage() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap myBitmap = BitmapFactory.decodeFile(imageHolder.getPreview().getPath(), options);
        imageHolder.getImageView().setImageBitmap(myBitmap);
    }

    public ImageData getImageData() {
        if (imageHolder == null) {
            return null;
        }

        if (imageHolder.getSource() == null || imageHolder.getPreview() == null) {
            return null;
        }

        String original = uriToPath(imageHolder.getSource());
        String preview = null;
        if (imageHolder.getPreview() != null) {
            preview = imageHolder.getPreview().getPath();
        }
        ImageData image = new ImageData(original, preview);
        image.setOriginalUri(imageHolder.getSource());
        image.setPreviewUri(imageHolder.getPreview());
        return image;
    }

    private String uriToPath(Uri uri) {
        if (uri == null) {
            return null;
        }
        if (!uri.getPath().startsWith("/media")) {
            return uri.getPath();
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(context, uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private Uri fixUri(Uri uri) {
        InputStream is = null;
        FileOutputStream out = null;

        try {
            is = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapUtils.loadBitmap(is);

            // generate fixed path
            File cacheDir = context.getCacheDir();
            File fixedPath = new File(cacheDir,
                    UUID.randomUUID().toString() + ORIGINAL_FILE_EXTENSION);

            if (fixedPath.exists()) {
                fixedPath.delete();
            }
            out = new FileOutputStream(fixedPath);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            return Uri.fromFile(fixedPath);

        } catch (FileNotFoundException e) {
            Log.e(TAG, "[fixUri Error]: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }

    public void restoreFromImageData(ImageData imageData) {
        if (imageData == null)
            return;

        imageHolder.setSource(imageData.getOriginalUri());
        imageHolder.setPreview(imageData.getPreviewUri());
        showImage();
    }

    public ImageHolder getImageHolder() {
        return imageHolder;
    }
}
