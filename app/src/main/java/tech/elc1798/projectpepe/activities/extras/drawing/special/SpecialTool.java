package tech.elc1798.projectpepe.activities.extras.drawing.special;

import android.content.Context;

import org.opencv.core.Mat;

import tech.elc1798.projectpepe.imgprocessing.OpenCVLibLoader;

/**
 * Abstract class for a DrawingSession operation that performs an automatic built-in operation that is too complicated
 * or impossible to do using standard free-draw and text boxes
 */
public abstract class SpecialTool {

    /**
     * Constructs a SpecialTool object. This constructor will load OpenCV for the Context provided, and will also call
     * the implementation of the {@code onOpenCVLoad} method if OpenCV is successfully loaded.
     *
     * @param context The context to load OpenCV for
     * @param tag The Android Log Tag to use for debugging purposes
     */
    public SpecialTool(final Context context, String tag) {
        OpenCVLibLoader.loadOpenCV(context, new OpenCVLibLoader.Callback(context, tag) {
            @Override
            public void onOpenCVLoadSuccess() {
                onOpenCVLoad(context);
            }
        });
    }

    /**
     * Gets the name of the tool
     *
     * @return a String
     */
    public abstract String getName();

    /**
     * Performs the action done by the tool. This operation is done IN PLACE on the inputMat. This is to adhere to the
     * convention laid by OpenCV, since Java OpenCV is just a C / C++ wrapper using JNI
     *
     * @param inputImage The image to perform the action on
     */
    public abstract void doAction(Mat inputImage);

    /**
     * This method is called as part of the OpenCV load process as a callback when OpenCV is successfully loaded. An
     * implementation of this method should be used to initialize any OpenCV objects, such as {@code Mat}s, XML
     * files for classifiers, etc.
     *
     * @param context The app context to load OpenCV for
     */
    public abstract void onOpenCVLoad(Context context);

}
