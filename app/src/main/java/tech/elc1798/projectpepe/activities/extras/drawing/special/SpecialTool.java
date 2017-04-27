package tech.elc1798.projectpepe.activities.extras.drawing.special;

import android.content.Context;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import tech.elc1798.projectpepe.imgprocessing.OpenCVLoaderCallback;

public abstract class SpecialTool {

    public SpecialTool(final Context context, String tag) {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, context, new OpenCVLoaderCallback(context, tag) {
            @Override
            public void onOpenCVLoadSuccess() {
                onOpenCVLoad(context);
            }
        });
    }

    public abstract String getName();
    public abstract void doAction(Mat inputImage);
    public abstract void onOpenCVLoad(Context context);

}
