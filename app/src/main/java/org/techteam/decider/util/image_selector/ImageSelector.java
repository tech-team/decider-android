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
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.camera.CropImageIntentBuilder;

import org.techteam.decider.R;
import org.techteam.decider.content.question.ImageData;
import org.techteam.decider.gui.fragments.AddQuestionFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class ImageSelector implements View.OnClickListener {
    private static final String TAG = AddQuestionFragment.class.getName();

    private static final short TAKE_PICTURE = 1;
    private static final short SELECT_IMAGE = 2;
    private static final short CROP_IMAGE = 3;

    private static final int ASPECT_WIDTH = 9;
    private static final int ASPECT_HEIGHT = 16;

    private static final int PREVIEW_WIDTH = 1280;
    private static final int PREVIEW_HEIGHT = PREVIEW_WIDTH * ASPECT_HEIGHT / ASPECT_WIDTH;

    private static final String ORIGINAL_FILE_EXTENSION = ".original.jpg";
    private static final String PREVIEW_FILE_EXTENSION = ".preview.jpg";
    private final short imageId;

    private final Context context;
    private final ActivityStarter activityStarter;
    private final ImageHolder imageHolder;

    public ImageSelector(Context context, ActivityStarter activityStarter, ImageView imageView, short imageId) {
        this.context = context;
        this.activityStarter = activityStarter;
        this.imageId = imageId;
        imageHolder = new ImageHolder(imageView);

        imageView.setOnClickListener(this);
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
        short requestImageId = rc.getImageId();
        requestCode = rc.getRequestCode();

        if (requestImageId != imageId)
            return;

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_IMAGE || requestCode == TAKE_PICTURE) {
                Uri selectedImage = imageHolder.getSource();

                // we've just taken a photo
                if (data != null) {
                    selectedImage = fixUri(data.getData());
                }

                imageHolder.setSource(selectedImage);

                if (selectedImage == null) {
                    Log.e(TAG, "selectedImage is still null even after fixUri");
                    return;
                }

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

    private void cropImage() {
        CropImageIntentBuilder cropBuilder = new CropImageIntentBuilder(
                ASPECT_WIDTH, ASPECT_HEIGHT,
                PREVIEW_WIDTH, PREVIEW_HEIGHT,
                imageHolder.getPreview());
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
        if (imageHolder.getSource() == null || imageHolder.getPreview() == null) {
            return null;
        }

        String original = uriToPath(imageHolder.getSource());
        String preview = imageHolder.getPreview().getPath();
        ImageData image = new ImageData(original, preview);
        image.setOriginalUri(imageHolder.getSource());
        image.setPreviewUri(imageHolder.getPreview());
        return image;
    }

    private String uriToPath(Uri uri) {
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