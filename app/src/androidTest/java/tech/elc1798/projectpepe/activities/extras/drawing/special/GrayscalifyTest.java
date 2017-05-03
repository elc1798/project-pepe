package tech.elc1798.projectpepe.activities.extras.drawing.special;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import tech.elc1798.projectpepe.imgprocessing.OpenCVLibLoader;

import static org.junit.Assert.*;

public class GrayscalifyTest {
    private static final int MAT_SIZE = 3;
    private static final int WAIT_TIME = 5;
    private static final String TAG = "GRAYSCALIFY_TEST:";
    private static final double[] BLUE = {255, 0, 0}; // By default, OpenCV uses BGR format

    private Mat testMat;
    private Grayscalify grayscalify;

    @Before
    public void setUp() throws Exception {
        final Context context = InstrumentationRegistry.getTargetContext();

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        OpenCVLibLoader.loadOpenCV(context, new OpenCVLibLoader.Callback(context, TAG) {
            @Override
            public void onOpenCVLoadSuccess() {
                testMat = new Mat(MAT_SIZE, MAT_SIZE, CvType.CV_8UC3); // Construct a mat with 3 8-bit channels
                testMat.setTo(new Scalar(BLUE)); // Set the test mat to blue
                grayscalify = new Grayscalify(context, TAG);
                countDownLatch.countDown();
            }
        });

        countDownLatch.await(WAIT_TIME, TimeUnit.SECONDS);
    }

    @Test
    public void getName() throws Exception {
        assertEquals(grayscalify.getName(), "Grayscale Image");
    }

    @Test
    public void doAction() throws Exception {
        for (int i = 0; i < MAT_SIZE; i++) {
            for (int j = 0; j < MAT_SIZE; j++) {
                assertArrayEquals(testMat.get(i, j), BLUE, Double.MIN_NORMAL);
            }
        }

        grayscalify.doAction(testMat);
        // Here's the hard part... we can't test the actual value of the scalar now... The algorithm for converting
        // an RGB to a grayscale intensity is non trivial. However, what we can do is hope that OpenCV has it correctly
        // implemented, and instead check if ALL ELEMENTS in the scalar are equal

        for (int i = 0; i < MAT_SIZE; i++) {
            for (int j = 0; j < MAT_SIZE; j++) {
                double[] hopefullyGray = testMat.get(i, j);

                assertEquals(hopefullyGray.length, 3);
                assertEquals(hopefullyGray[0], hopefullyGray[1], Double.MIN_NORMAL);
                assertEquals(hopefullyGray[0], hopefullyGray[2], Double.MIN_NORMAL);
                assertEquals(hopefullyGray[1], hopefullyGray[2], Double.MIN_NORMAL);
            }
        }
    }
}