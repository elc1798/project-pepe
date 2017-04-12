package tech.elc1798.projectpepe.imgprocessing;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import tech.elc1798.projectpepe.R;

/**
 * Abstracts away the overhead of streaming camera feed on Android using OpenCV
 */
public abstract class CameraStreamingActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final int OPENCV_FLIP_VERTICAL = 1;

    private JavaCameraView cameraView;
    private ImageView imageView;

    /**
     * Takes in an input matrix and performs operations on the matrix, returning the resultant output matrix. The result
     * is what is displayed on the ImageView.
     *
     * @param inputMat The matrix representation of the image (frame) captured by the camera
     * @return The processed image
     */
    public abstract Mat processImage(Mat inputMat);

    /**
     * Performs any extra functionality that needs to be done when OpenCV is loaded
     */
    public abstract void onOpenCVLoad();

    /**
     * Returns the Android log TAG used by the implementation of this class.
     *
     * @return a String
     */
    public abstract String getTag();

    /**
     * Implementation of {@code onCreate}, which accesses the frame layout in the camera_activity_layout and sets up
     * a live camera feed. It first inserts a JavaCameraView (an implementation of a SurfaceView) in order to start
     * the camera feed. In order for the video capture to be live Android requires that the SurfaceView must be visible
     * on the current view. Hence, we use a FrameLayout, so we can overlay an image view on top of it, with the right
     * orientation to fix the OpenCV bug.
     *
     * @param savedInstanceState {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.camera_activity_layout);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera_view_frame_layout);

        cameraView = new JavaCameraView(this, CameraBridgeViewBase.CAMERA_ID_FRONT);
        cameraView.setCvCameraViewListener(this);
        frameLayout.addView(cameraView);

        // The screen shouldn't go dim or go on standby while in the camera view
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        imageView = new ImageView(this);
        frameLayout.addView(imageView);
    }

    /**
     * Implementation of onResume to load the OpenCV C++ symlinks
     */
    @Override
    public void onResume() {
        super.onResume();

        // Reload the OpenCV C++ symlinks upon resume
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, openCVLoaderCallback);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(getTag(), "Started!");
    }

    @Override
    public void onCameraViewStopped() {
    }

    /**
     * This method is called on every frame update recieved by the camera. It first rotates the image to get the
     * correct orientation, then calls the abstract method {@code processImage} and sets the ImageView to the processed
     * image.
     *
     * @param cvCameraViewFrame The frame retrieved by the camera
     * @return The RGB matrix representation of the input matrix. This is the return value used for displaying on the
     * SurfaceView (JavaCameraView). However, the value stored in the ImageView is what matters.
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame cvCameraViewFrame) {
        Mat tmp = cvCameraViewFrame.rgba();

        // Pre-invert the image to the correct orientation and aspect ratio before processing
        Mat inverted = tmp.t();
        Core.flip(inverted, inverted, OPENCV_FLIP_VERTICAL);
        setImage(processImage(inverted));

        return tmp;
    }

    /**
     * An implementation of an OpenCV Loader Callback. This the {@code onManagerConnected} method is called upon
     * the OpenCV libraries loading (either successfully or unsuccessfully).
     */
    private BaseLoaderCallback openCVLoaderCallback = new BaseLoaderCallback(this) {
        /**
         * If OpenCV loads successfully, enables the camera view and calls the abstract method {@code onOpenCVLoad}.
         *
         * @param status {@inheritDoc}
         */
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(getTag(), "OpenCV loaded successfully");

                // The camera view and classifier can only be instantiated upon the success of a BaseLoader
                cameraView.enableView();

                onOpenCVLoad();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    /**
     * Sets the image displayed by the ImageView to the image stored in an OpenCV {@code Mat}.
     *
     * @param image
     */
    private void setImage(Mat image) {
        Bitmap bm = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, bm);

        final Bitmap finalBitmap = bm;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(finalBitmap);
            }
        });
    }
}
