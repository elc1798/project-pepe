package tech.elc1798.projectpepe.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import tech.elc1798.projectpepe.Constants;
import tech.elc1798.projectpepe.R;
import tech.elc1798.projectpepe.net.FileUploader;
import static tech.elc1798.projectpepe.Constants.IMG_CACHE_STORAGE_DIRECTORY;

/**
 * Activity to confirm uploading the image taken by the camera
 */
public class ConfirmImageActivity extends AppCompatActivity {

    private static final String TAG = "PEPE_CONFIRM_IMG:";
    private static final String ERROR_SETTING_IMAGE = "Could not set image";
    private static final String THREAD_SLEEP_INTERRUPT_MESSAGE = "Thread sleep interrupted";
    private static final int COUNTDOWN_LATCH_WAIT_TIME = 0;
    private static final int THREAD_SLEEP_TIME = 100;

    private File imageFile;

    /**
     * Spawns a thread that waits until the CameraActivity (parent activity) finishes writing. After writing is done,
     * will load the picture file into the ImageView and set up the rest of the views in the activity.
     *
     * @param savedInstanceState {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_image_activity_layout);

        Intent intent = this.getIntent();
        String imageFilename = intent.getStringExtra(CameraActivity.CONFIRM_IMAGE_FILE_NAME_INTENT_EXTRA_ID);

        File imgDirectory = this.getDir(
                IMG_CACHE_STORAGE_DIRECTORY,
                Context.MODE_PRIVATE
        );

        imageFile = new File(imgDirectory, imageFilename);

        final Thread setImageThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (CameraActivity.getFileWriteState() != CameraActivity.IOState.NOT_RUNNING) {
                    try {
                        Thread.sleep(THREAD_SLEEP_TIME);
                    } catch (InterruptedException e) {
                        Log.d(TAG, THREAD_SLEEP_INTERRUPT_MESSAGE);
                    }
                }

                // Stop the rotating progress bar
                stopProgressBar();

                setImage();
                setUpUploadButton();
            }
        });

        setImageThread.start();
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
    private void setUpUploadButton() {
        final ImageButton uploadButton = (ImageButton) this.findViewById(R.id.confirm_image_button);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uploadButton.setVisibility(View.VISIBLE);
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CountDownLatch latch = new CountDownLatch(1);
                FileUploader.uploadFile(ConfirmImageActivity.this, imageFile, Constants.PEPE_FILE_UPLOAD_URL, latch);

                try {
                    latch.await(COUNTDOWN_LATCH_WAIT_TIME, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Log.d(TAG, "CountDownLatch interrupted!");
                }
                ConfirmImageActivity.this.finish();
            }
        });
    }

    /**
     * Makes the progress bar invisible
     */
    private void stopProgressBar() {
        final ProgressBar progressBar = (ProgressBar) this.findViewById(R.id.confirm_image_progress_bar);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
