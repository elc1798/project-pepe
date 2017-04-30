package tech.elc1798.projectpepe.activities.extras.drawing.special;

import android.content.Context;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import tech.elc1798.projectpepe.imgprocessing.HaarCascade;

/**
 * Special tool that flips faces upside down
 */
public class FaceFlipper extends SpecialTool {

    private static final String FACE_CLASSIFIER_XML_FILE = "frontalfacecascade.xml";
    private static final String NAME = "Face Flipper";
    private static final double CLASSIFIER_SCALE_FACTOR = 1.1;
    private static final int MIN_SIZE_WIDTH = 250;
    private static final int MIN_SIZE_HEIGHT = 150;
    private static final int MAX_SIZE_WIDTH = 2000;
    private static final int MAX_SIZE_HEIGHT = 2000;
    private static final int OPENCV_FLIP_HORIZONTAL = 0;

    private HaarCascade faceClassifier;

    public FaceFlipper(Context context, String tag) {
        super(context, tag);
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * This implementation will scan through the input image for matches for a "Frontal Face" classifier. The largest
     * detected match (by size of the bounding rectangle) will be flipped over the horizontal axis, making it upside
     * down.
     *
     * @param inputImage The image to perform the action on
     */
    @Override
    public void doAction(Mat inputImage) {
        if (faceClassifier.isEmpty()) {
            return;
        }

        // Create a grayscaled version of our image
        Mat grayscaleNormalized = inputImage.clone();
        Imgproc.cvtColor(inputImage, grayscaleNormalized, Imgproc.COLOR_RGB2GRAY);

        // Use equalizeHist to normalize the lighting (brightness) of the image
        Imgproc.equalizeHist(grayscaleNormalized, grayscaleNormalized);

        MatOfRect detections = faceClassifier.detect(
                grayscaleNormalized,
                CLASSIFIER_SCALE_FACTOR,
                new Size(MIN_SIZE_WIDTH, MIN_SIZE_HEIGHT),
                new Size(MAX_SIZE_WIDTH, MAX_SIZE_HEIGHT)
        );

        // Get the largest rectangle
        Rect largestRect = null;
        for (Rect rect : detections.toArray()) {
            if (largestRect == null || rect.area() > largestRect.area()) {
                largestRect = rect;
            }
        }

        // Flip the largest face upside down :D
        if (largestRect != null) {
            Mat extracted = inputImage.submat(largestRect.y, largestRect.y + largestRect.height,
                    largestRect.x, largestRect.x + largestRect.width);
            Core.flip(extracted, extracted, OPENCV_FLIP_HORIZONTAL);
        }
    }

    /**
     * Loads the Haar Cascade classifier when OpenCV is loaded.
     *
     * @param context The app context to load OpenCV for
     */
    @Override
    public void onOpenCVLoad(Context context) {
        faceClassifier = new HaarCascade(context, FACE_CLASSIFIER_XML_FILE);
    }
}
