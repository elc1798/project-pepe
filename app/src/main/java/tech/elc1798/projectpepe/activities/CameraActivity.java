package tech.elc1798.projectpepe.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import tech.elc1798.projectpepe.R;
import tech.elc1798.projectpepe.imgprocessing.CameraStreamingActivity;

import static tech.elc1798.projectpepe.Constants.COMPRESSION_RATE;
import static tech.elc1798.projectpepe.Constants.IMG_CACHE_FILENAME_FORMAT;
import static tech.elc1798.projectpepe.Constants.IMG_CACHE_STORAGE_DIRECTORY;

/**
 * Implementation of {@code CameraStreamingActivity}
 */
public class CameraActivity extends CameraStreamingActivity {

    // Public constants
    public static final String CONFIRM_IMAGE_FILE_NAME_INTENT_EXTRA_ID = "camera_act_img_intent_extra_id";

    public enum IOState {
        NOT_RUNNING, RUNNING
    }

    // Private constants
    private static final String TAG = "PEPE_CAMERA:";
    private static final String UNABLE_TO_SAVE_IMG = "Unable to save image!";

    // Private statics
    private static IOState fileWriteState;

    // Public statics
    public static IOState getFileWriteState() {
        return fileWriteState;
    }

    // Private instance variables
    private boolean picSnapButtonListenerSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        fileWriteState = IOState.NOT_RUNNING;
        picSnapButtonListenerSet = false;

        super.onCreate(savedInstanceState);

        setToggleCameraButtonOnClickListener();
        setPictureSnapOnClickListener();
    }

    @Override
    public void onOpenCVLoad() {
    }

    @Override
    public String getTag() {
        return TAG;
    }

    /**
     * Implementation: Remove the alpha channel of the image
     *
     * @param inputMat The matrix representation of the image (frame) captured by the camera
     * @return The input picture with the largest detected face boxed.
     */
    @Override
    public Mat processImage(Mat inputMat) {
        // Remove the alpha channel for us to draw on
        Mat rgb = inputMat.clone();
        Imgproc.cvtColor(inputMat, rgb, Imgproc.COLOR_RGBA2RGB);

        return rgb;
    }

    private void setToggleCameraButtonOnClickListener() {
        ImageButton flipCameraButton = (ImageButton) this.findViewById(R.id.camera_flip_button);

        flipCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity.this.flipCameraView();
            }
        });
    }

    /**
     * Sets the OnClickListener for the ImageButton on the CameraView
     */
    private void setPictureSnapOnClickListener() {
        // Don't continue the function if already ran
        if (picSnapButtonListenerSet) {
            return;
        }

        // Get the ImageButton and set its OnClickListener
        ImageButton imgButton = (ImageButton) this.findViewById(R.id.camera_button);
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop the camera
                stopCamera();

                Bitmap bitmap = CameraActivity.this.getCurrentFrameBitmap();

                // If getCurrentFrameBitmap returns null, do nothing
                if (bitmap == null) {
                    CameraActivity.this.loadOpenCVBindings();
                    return;
                }

                // Bitmaps are too large to send through intent parcelables, so we store it in a storage directory
                String uniqueFileName = saveBitmapToUniqueFile(bitmap);

                // Spawn the intent
                Intent confirmImageIntent = new Intent(CameraActivity.this, ConfirmImageActivity.class);
                confirmImageIntent.putExtra(CONFIRM_IMAGE_FILE_NAME_INTENT_EXTRA_ID, uniqueFileName);
                CameraActivity.this.startActivity(confirmImageIntent);
            }
        });

        // Set our boolean to true so we don't keep resetting the on click listener
        picSnapButtonListenerSet = true;
    }

    /**
     * Generates a unique file name and saves the provided bitmap to a file with that filename. Spawns a thread
     * internally for IO operations
     *
     * @param bitmap The bitmap to save
     * @return The generated file name
     * @throws IOException Upon error during file read / write
     */
    private String saveBitmapToUniqueFile(final Bitmap bitmap) {
        File imgDirectory = this.getDir(
                IMG_CACHE_STORAGE_DIRECTORY,
                Context.MODE_PRIVATE
        );

        String uniqueFileName = String.format(
                IMG_CACHE_FILENAME_FORMAT,
                Long.toString(System.currentTimeMillis())
        );

        final File imgFile = new File(imgDirectory, uniqueFileName);

        Thread ioThread = new Thread(new Runnable() {
            @Override
            public void run() {
                CameraActivity.fileWriteState = IOState.RUNNING;

                try {
                    FileOutputStream outputStream = new FileOutputStream(imgFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_RATE, outputStream);
                    outputStream.close();
                } catch (Exception e) {
                    Toast.makeText(CameraActivity.this, UNABLE_TO_SAVE_IMG, Toast.LENGTH_SHORT).show();
                } finally {
                    CameraActivity.fileWriteState = IOState.NOT_RUNNING;
                }
            }
        });
        ioThread.start();

        return uniqueFileName;
    }
}
