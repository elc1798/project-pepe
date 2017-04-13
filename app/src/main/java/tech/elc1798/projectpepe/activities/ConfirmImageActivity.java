package tech.elc1798.projectpepe.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;

import tech.elc1798.projectpepe.Constants;
import tech.elc1798.projectpepe.R;
import tech.elc1798.projectpepe.net.NetworkRequestAsyncTask;

public class ConfirmImageActivity extends AppCompatActivity {

    private static final String ERROR_SETTING_IMAGE = "Could not set image";

    private File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_image_activity_layout);

        Intent intent = this.getIntent();
        String imageFilename = intent.getStringExtra(CameraActivity.CAMERA_ACTIVITY_IMAGE_FILE_NAME_INTENT_EXTRA_ID);

        File imgDirectory = this.getDir(
                CameraActivity.IMG_CACHE_STORAGE_DIRECTORY,
                Context.MODE_PRIVATE
        );

        imageFile = new File(imgDirectory, imageFilename);

        setImage();
        setOnClickListener();
    }

    /**
     * Sets the image of the image view to the cached image stored in the cache directory
     */
    private void setImage() {
        try {
            // Attempt to open the cached image into a FileInputStream
            FileInputStream fileInputStream = new FileInputStream(imageFile);

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

    /**
     * Sets the onClickListener of the button to upload the image to the Project PEPE server
     */
    private void setOnClickListener() {
        Button uploadButton = (Button) this.findViewById(R.id.confirm_image_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NetworkRequestAsyncTask(imageFile).execute(Constants.PROJECT_SERVER_UPLOAD_ENDPOINT_URL);
            }
        });
    }
}
