package tech.elc1798.projectpepe.imgprocessing;


import android.content.Context;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;

public abstract class OpenCVLoaderCallback extends BaseLoaderCallback {

    private Context context;
    private String tag;

    public OpenCVLoaderCallback(Context context, String tag) {
        super(context);

        this.context = context;
        this.tag = tag;
    }

    public abstract void onOpenCVLoadSuccess();

    /**
     * If OpenCV loads successfully, enables the camera view and calls the abstract method {@code onOpenCVLoadSuccess}.
     *
     * @param status {@inheritDoc}
     */
    @Override
    public void onManagerConnected(int status) {
        if (status == LoaderCallbackInterface.SUCCESS) {
            Log.i(tag, "OpenCV loaded successfully");

            onOpenCVLoadSuccess();
        } else {
            super.onManagerConnected(status);
        }
    }
}
