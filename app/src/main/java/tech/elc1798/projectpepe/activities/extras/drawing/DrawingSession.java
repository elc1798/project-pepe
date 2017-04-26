package tech.elc1798.projectpepe.activities.extras.drawing;


import android.content.Context;
import android.graphics.Bitmap;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import tech.elc1798.projectpepe.imgprocessing.OpenCVLoaderCallback;

public class DrawingSession {

    private static final String TAG = "PEPE_DRAWING_SESSION";
    private static final Scalar COLOR = new Scalar(255, 255, 0);
    private static final int THICKNESS = 5;
    private static final int CIRCLE_RADIUS = 2;

    private Mat image;
    private Point previousPoint;

    private OpenCVLoaderCallback openCVLoaderCallback;

    public DrawingSession(Context context, final Bitmap inputImage) {
        openCVLoaderCallback = new OpenCVLoaderCallback(context, TAG) {
            @Override
            public void onOpenCVLoadSuccess() {
                image = new Mat(inputImage.getHeight(), inputImage.getWidth(), CvType.CV_8UC1);
                Utils.bitmapToMat(inputImage, image);

                previousPoint = new Point(-1, -1);
            }
        };

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, context, openCVLoaderCallback);
    }

    public void resetCursor() {
        previousPoint = new Point(-1, -1);
    }

    public void drawPath(int x, int y) {
        Point newPoint = new Point(x, y);

        if ((int) previousPoint.x < 0 || (int) previousPoint.y < 0) {
            Imgproc.circle(image, newPoint, CIRCLE_RADIUS, COLOR, THICKNESS);
        } else {
            Imgproc.line(image, previousPoint, newPoint, COLOR, THICKNESS);
        }

        previousPoint = newPoint;
    }

    public Bitmap getBitmap() {
        Bitmap bm = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, bm);

        return bm;
    }
}
