package tech.elc1798.projectpepe.imgprocessing;

import android.content.Context;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

/**
 * Utility class to abstract away loading the OpenCV libraries for a given context
 */
public class OpenCVLibLoader {

    private static final String CV_LOAD_SUCCESS_MESSAGE = "OpenCV loaded successfully";

    public static void loadOpenCV(Context context, Callback callback) {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, context, callback);
    }

    /**
     * Child class of OpenCVLibLoader that acts as a callback, so we can execute extra operations when OpenCV is loaded
     * successfully (such as instantiating OpenCV objects)
     */
    public abstract static class Callback extends BaseLoaderCallback {

        private String tag;

        protected Callback(Context context, String tag) {
            super(context);

            this.tag = tag;
        }

        /**
         * Called when OpenCV is successfully loaded
         */
        public abstract void onOpenCVLoadSuccess();

        /**
         * If OpenCV loads successfully, enables the camera view and calls the abstract method
         * {@code onOpenCVLoadSuccess}.
         *
         * @param status {@inheritDoc}
         */
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(tag, CV_LOAD_SUCCESS_MESSAGE);

                onOpenCVLoadSuccess();
            } else {
                super.onManagerConnected(status);
            }
        }
    }
}
