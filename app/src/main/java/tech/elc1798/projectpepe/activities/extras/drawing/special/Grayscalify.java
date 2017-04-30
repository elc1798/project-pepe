package tech.elc1798.projectpepe.activities.extras.drawing.special;


import android.content.Context;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Special tool that makes the image grayscale.
 */
public class Grayscalify extends SpecialTool {

    public Grayscalify(Context context, String tag) {
        super(context, tag);
    }

    private static final String NAME = "Grayscale Image";

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Makes the image grayscale using color range conversions.
     *
     * @param inputImage The image to perform the action on
     */
    @Override
    public void doAction(Mat inputImage) {
        Mat grayscale = inputImage.clone();
        Imgproc.cvtColor(inputImage, grayscale, Imgproc.COLOR_RGB2GRAY);
        Imgproc.cvtColor(grayscale, inputImage, Imgproc.COLOR_GRAY2RGB);
    }

    @Override
    public void onOpenCVLoad(Context context) {
    }
}
