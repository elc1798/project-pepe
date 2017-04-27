package tech.elc1798.projectpepe.activities.extras.drawing.special;

import android.content.Context;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Sharpen extends SpecialTool {

    private static final String NAME = "Sharpen Image";
    private static final Size GAUSSIAN_KERNEL_SIZE = new Size(9, 9);
    private static final int GAUSSIAN_KERNEL_STD_DEV = 10;
    private static final int SHARPEN_GAMMA = 0;
    private static final int NUM_ITERATIONS = 3;
    private static final double SHARPEN_BIAS = 3.0;

    public Sharpen(Context context, String tag) {
        super(context, tag);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void doAction(Mat inputImage) {
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            Mat sharpenDeltas = inputImage.clone();
            Imgproc.GaussianBlur(inputImage, sharpenDeltas, GAUSSIAN_KERNEL_SIZE, GAUSSIAN_KERNEL_STD_DEV);
            Core.addWeighted(inputImage, 1.0 + SHARPEN_BIAS, sharpenDeltas, -SHARPEN_BIAS, SHARPEN_GAMMA, inputImage);
        }
    }

    @Override
    public void onOpenCVLoad(Context context) {
    }
}
