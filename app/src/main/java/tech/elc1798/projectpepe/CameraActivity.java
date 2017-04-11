package tech.elc1798.projectpepe;

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
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import tech.elc1798.projectpepe.imgprocessing.HaarCascade;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "PROJECT_PEPE::";
    private static final String FACE_CLASSIFIER_XML_FILE = "frontalfacecascade.xml";
    private static final double CLASSIFIER_SCALE_FACTOR = 1.1;
    private static final int MIN_SIZE_WIDTH = 250;
    private static final int MIN_SIZE_HEIGHT = 150;
    private static final int MAX_SIZE_WIDTH = 2000;
    private static final int MAX_SIZE_HEIGHT = 2000;
    private static final int RECTANGLE_THICKNESS = 3;
    private static final int CIRCLE_RADIUS = 1;
    private static final int CIRCLE_THICKNESS = 5;
    private static final int OPENCV_FLIP_VERTICAL = 1;
    private static final Scalar COLOR_GREEN = new Scalar(0, 255, 0);
    private static final Scalar COLOR_RED = new Scalar(255, 0, 0);
    public static final int FRAME_PROCESS_RATE = 8;

    private JavaCameraView cameraView;
    private ImageView imageView;
    private HaarCascade classifier;
    private Rect cached;
    private int frameCount;

    private BaseLoaderCallback openCVLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");

                // The camera view and classifier can only be instantiated upon the success of a BaseLoader
                cameraView.enableView();

                if (classifier == null) {
                    classifier = new HaarCascade(this.mAppContext, FACE_CLASSIFIER_XML_FILE);
                }
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.camera_activity_layout);

        // OpenCV requires a really dumb workaround: The camera feed by default will be in rotated, and the internal
        // surface view MUST be visible for it to capture camera feed. Hence, we use a FrameLayout to overlap the
        // surface view with an ImageView with the corrected image.
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera_view_frame_layout);

        cameraView = new JavaCameraView(this, CameraBridgeViewBase.CAMERA_ID_FRONT);
        cameraView.setCvCameraViewListener(this);
        frameLayout.addView(cameraView);

        // The screen shouldn't go dim or go on standby while in the camera view
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        imageView = new ImageView(this);
        frameLayout.addView(imageView);

        classifier = null;
        cached = new Rect(0, 0, 0, 0);
        frameCount = 0;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Reload the OpenCV C++ symlinks upon resume
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, openCVLoaderCallback);
    }

    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "Started!");
    }

    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame cvCameraViewFrame) {
        Mat tmp = cvCameraViewFrame.rgba();
        processImage(tmp);
        return tmp;
    }

    private void setImage(Bitmap bm) {
        imageView.setImageBitmap(bm);
    }

    private void processImage(Mat input) {
        Mat inverted = input.t();
        Core.flip(inverted, inverted, OPENCV_FLIP_VERTICAL);

        Mat grayscaleNormalized = inverted.clone();
        Imgproc.cvtColor(inverted, grayscaleNormalized, Imgproc.COLOR_RGB2GRAY);
        Imgproc.equalizeHist(grayscaleNormalized, grayscaleNormalized);

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

        Imgproc.rectangle(inverted, diagonalEndpointUpper, diagonalEndpointLower, COLOR_GREEN, RECTANGLE_THICKNESS);
        Imgproc.circle(inverted, diagonalMidpoint, CIRCLE_RADIUS, COLOR_RED, CIRCLE_THICKNESS);

        Bitmap bm = Bitmap.createBitmap(input.rows(), input.cols(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(inverted, bm);

        final Bitmap finalBitmap = bm;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setImage(finalBitmap);
            }
        });

        frameCount = (frameCount + 1) % FRAME_PROCESS_RATE;
    }
}
