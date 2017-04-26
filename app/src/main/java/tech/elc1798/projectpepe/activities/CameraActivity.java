package tech.elc1798.projectpepe.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import tech.elc1798.projectpepe.R;
import tech.elc1798.projectpepe.imgprocessing.CameraStreamingActivity;
import tech.elc1798.projectpepe.imgprocessing.HaarCascade;

/**
 * Implementation of {@code CameraStreamingActivity}
 */
public class CameraActivity extends CameraStreamingActivity {

    // Public constants
    public static final String CAMERA_ACTIVITY_IMAGE_FILE_NAME_INTENT_EXTRA_ID = "camera_act_img_intent_extra_id";
    public static final String IMG_CACHE_STORAGE_DIRECTORY = "snapshots";

    public enum IOState {
        NOT_RUNNING, RUNNING
    }

    // Private constants
    private static final String TAG = "PEPE_CAMERA:";
    private static final String FACE_CLASSIFIER_XML_FILE = "frontalfacecascade.xml";
    private static final String UNABLE_TO_SAVE_IMG = "Unable to save image!";
    private static final String IMG_CACHE_FILENAME_FORMAT = "%s.png";
    private static final double CLASSIFIER_SCALE_FACTOR = 1.1;
    private static final int MIN_SIZE_WIDTH = 250;
    private static final int MIN_SIZE_HEIGHT = 150;
    private static final int MAX_SIZE_WIDTH = 2000;
    private static final int MAX_SIZE_HEIGHT = 2000;
    private static final int RECTANGLE_THICKNESS = 3;
    private static final int CIRCLE_RADIUS = 1;
    private static final int CIRCLE_THICKNESS = 5;
    private static final int FRAME_PROCESS_RATE = 8;
    private static final int COMPRESSION_RATE = 100;
    private static final Scalar COLOR_GREEN = new Scalar(0, 255, 0);
    private static final Scalar COLOR_RED = new Scalar(255, 0, 0);

    // Private statics
    private static IOState fileWriteState;

    // Public statics
    public static IOState getFileWriteState() {
        return fileWriteState;
    }

    // Private instance variables
    private HaarCascade classifier;
    private Rect cached;
    private int frameCount;
    private boolean picSnapButtonListenerSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        fileWriteState = IOState.NOT_RUNNING;
        classifier = null;
        cached = new Rect(0, 0, 0, 0);
        frameCount = 0;
        picSnapButtonListenerSet = false;

        super.onCreate(savedInstanceState);

        setToggleCameraButtonOnClickListener();
        setPictureSnapOnClickListener();
    }

    @Override
    public void onOpenCVLoad() {
        // If the classifier is not yet instantiated, instantiate it.
        if (classifier == null) {
            classifier = new HaarCascade(this, FACE_CLASSIFIER_XML_FILE);
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }

    /**
     * Implementation: Runs face detection on the input frame
     *
     * @param inputMat The matrix representation of the image (frame) captured by the camera
     * @return The input picture with the largest detected face boxed.
     */
    @Override
    public Mat processImage(Mat inputMat) {
        // Create a grayscaled version of our image
        Mat grayscaleNormalized = inputMat.clone();
        Imgproc.cvtColor(inputMat, grayscaleNormalized, Imgproc.COLOR_RGB2GRAY);

        // Use equalizeHist to normalize the lighting (brightness) of the image
        Imgproc.equalizeHist(grayscaleNormalized, grayscaleNormalized);

        // If the classifier is usable, and we are on the zeroth iteration of our cycle, get the largest detection of
        // our HaarCascade classifier
        if (!classifier.isEmpty() && frameCount == 0) {
            MatOfRect detections = classifier.detect(
                    grayscaleNormalized,
                    CLASSIFIER_SCALE_FACTOR,
                    new Size(MIN_SIZE_WIDTH, MIN_SIZE_HEIGHT),
                    new Size(MAX_SIZE_WIDTH, MAX_SIZE_HEIGHT)
            );

            if (detections.toArray().length != 0) {
                cached = new Rect(0, 0, 0, 0);
            }

            for (Rect rect : detections.toArray()) {
                if (rect.area() > cached.area()) {
                    cached = rect;
                }
            }
        }

        Point diagonalEndpointUpper = new Point(cached.x, cached.y);
        Point diagonalEndpointLower = new Point(cached.x + cached.width, cached.y + cached.height);
        Point diagonalMidpoint = new Point(cached.x + cached.width / 2, cached.y + cached.height / 2);

        // Remove the alpha channel for us to draw on
        Mat rgb = inputMat.clone();
        Imgproc.cvtColor(inputMat, rgb, Imgproc.COLOR_RGBA2RGB);

        // Draw a bounding rectangle and center circle of detection
        Imgproc.rectangle(rgb, diagonalEndpointUpper, diagonalEndpointLower, COLOR_GREEN, RECTANGLE_THICKNESS);
        Imgproc.circle(rgb, diagonalMidpoint, CIRCLE_RADIUS, COLOR_RED, CIRCLE_THICKNESS);

        // Increment cycle tick count
        frameCount = (frameCount + 1) % FRAME_PROCESS_RATE;
        return rgb;
    }

    private void setToggleCameraButtonOnClickListener() {
        ImageButton flipCameraButton = (ImageButton) this.findViewById(R.id.camera_flip_button);
        final CameraActivity cameraActivityRef = this;

        flipCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraActivityRef.flipCameraView();
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

        // Inner class declaration requires all variables to be final
        final CameraActivity cameraActivityContextRef = this;

        // Get the ImageButton and set its OnClickListener
        ImageButton imgButton = (ImageButton) this.findViewById(R.id.camera_button);
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop the camera
                stopCamera();

                Bitmap bitmap = cameraActivityContextRef.getCurrentFrameBitmap();

                // If getCurrentFrameBitmap returns null, do nothing
                if (bitmap == null) {
                    cameraActivityContextRef.loadOpenCVBindings();
                    return;
                }

                // Bitmaps are too large to send through intent parcelables, so we store it in a storage directory
                String uniqueFileName = saveBitmapToUniqueFile(bitmap);

                // Spawn the intent
                Intent confirmImageIntent = new Intent(cameraActivityContextRef, ConfirmImageActivity.class);
                confirmImageIntent.putExtra(CAMERA_ACTIVITY_IMAGE_FILE_NAME_INTENT_EXTRA_ID, uniqueFileName);
                cameraActivityContextRef.startActivity(confirmImageIntent);
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
        final CameraActivity cameraActivityContextRef = this;

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
                    Toast.makeText(cameraActivityContextRef, UNABLE_TO_SAVE_IMG, Toast.LENGTH_SHORT).show();
                } finally {
                    CameraActivity.fileWriteState = IOState.NOT_RUNNING;
                }
            }
        });
        ioThread.start();

        return uniqueFileName;
    }
}
