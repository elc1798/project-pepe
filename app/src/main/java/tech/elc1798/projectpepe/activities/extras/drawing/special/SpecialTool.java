package tech.elc1798.projectpepe.activities.extras.drawing.special;

import android.content.Context;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import tech.elc1798.projectpepe.imgprocessing.OpenCVLibLoader;

/**
 * Abstract class for a DrawingSession operation that performs an automatic built-in operation that is too complicated
 * or impossible to do using standard free-draw and text boxes
 */
public abstract class SpecialTool {

    public SpecialTool(final Context context, String tag) {
        OpenCVLibLoader.loadOpenCV(context, new OpenCVLibLoader.Callback(context, tag) {
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
