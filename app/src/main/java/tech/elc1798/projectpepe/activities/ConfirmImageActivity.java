package tech.elc1798.projectpepe.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;

import tech.elc1798.projectpepe.R;

public class ConfirmImageActivity extends AppCompatActivity {

    private static final String ERROR_SETTING_IMAGE = "Could not set image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_image_activity_layout);

        setImage();
    }

    /**
     * Sets the image of the image view to the cached image stored in the cache directory
     */
    private void setImage() {
        // Load the internal storage location of the image
        File imgDirectory = this.getDir(
                CameraActivity.IMG_CACHE_STORAGE_DIRECTORY,
                Context.MODE_PRIVATE
        );

        try {
            // Attempt to open the cached image into a FileInputStream
            FileInputStream fileInputStream = new FileInputStream(
                    new File(imgDirectory, CameraActivity.IMG_CACHE_FILENAME)
            );

            // Load the bitmap
            final Bitmap finalBitmap = BitmapFactory.decodeStream(fileInputStream);
            final ImageView imageView = (ImageView) this.findViewById(R.id.confirm_image_image_view);

            // Tell the UI thread to set the image
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(finalBitmap);
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, ERROR_SETTING_IMAGE, Toast.LENGTH_SHORT).show();
        }
    }
}
