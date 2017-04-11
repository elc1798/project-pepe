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

public abstract class CameraStreamingActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final int OPENCV_FLIP_VERTICAL = 1;

    private JavaCameraView cameraView;
    private ImageView imageView;

    public abstract Mat processImage(Mat inputMat);
    public abstract void onOpenCVLoad();
    public abstract String getTag();

    private BaseLoaderCallback openCVLoaderCallback = new BaseLoaderCallback(this) {
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
    }

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

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame cvCameraViewFrame) {
        Mat tmp = cvCameraViewFrame.rgba();

        // Pre-invert the image to the correct orientation and aspect ratio before processing
        Mat inverted = tmp.t();
        Core.flip(inverted, inverted, OPENCV_FLIP_VERTICAL);
        setImage(processImage(inverted));

        return tmp;
    }
}
