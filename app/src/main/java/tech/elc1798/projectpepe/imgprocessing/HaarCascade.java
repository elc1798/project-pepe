package tech.elc1798.projectpepe.imgprocessing;


import android.content.Context;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import tech.elc1798.projectpepe.R;

/**
 * Class that abstracts away the overhead required on Android for loading a HaarCascade XML file and processing it
 */
public class HaarCascade {
    private static final String TAG = "imgproc/HaarCascade::";
    private static final String CASCADE_STORAGE_DIRECTORY = "haarcascades";
    private static final String CASCADE_DIRECTORY_LOG_FORMAT = "Cascade directory: %s";
    private static final String CASCADE_CONTENTS_LOG_FORMAT = "Cascade contents: %s";
    private static final int SIZE_4KB = 4096;

    private Context referenceContext;
    private CascadeClassifier classifier;

    public HaarCascade(Context referenceContext, String cascadeXMLFileName) {
        this.referenceContext = referenceContext;
        this.classifier = getClassifier(cascadeXMLFileName);
    }

    /**
     * Returns true if the internal classifier is non-null and empty
     *
     * @return A boolean
     */
    public boolean isEmpty() {
        return (classifier != null) && classifier.empty();
    }

    /**
     * Wrapper function for OpenCV's CascadeClassifier.detectMultiScale method. Removes the requirement of the
     * {@code minNeighbors} and {@code flags} parameters.
     *
     * @param inputMat The matrix to processes using the classifier
     * @param scaleFactor The scale factor used when using the classifier. Trades performance for accuracy.
     * @param minSize The minimum size of a positive detection
     * @param maxSize The maximum size of a positive detection
     * @return An OpenCV {@code MatOfRect} containing all the detections for our classifier
     */
    public MatOfRect detect(Mat inputMat, double scaleFactor, Size minSize, Size maxSize) {
        MatOfRect detections = new MatOfRect();
        classifier.detectMultiScale(inputMat, detections, scaleFactor, 0, 0, minSize, maxSize);
        return detections;
    }

    /**
     * Loading a File object in Android requires some overhead: we have to copy the contents of the file in
     * {@code res.raw} and write it to an "App-access-only" directory for temporary storage.
     *
     * @param cascadeXMLFileName The filename of the XML file in {@code res/raw}
     * @return An OpenCV {@code CascadeClassifier} loaded from the specified XML file
     */
    private CascadeClassifier getClassifier(String cascadeXMLFileName) {
        File cascadeDirectory = referenceContext.getDir(CASCADE_STORAGE_DIRECTORY, Context.MODE_PRIVATE);
        Log.d(TAG, String.format(CASCADE_DIRECTORY_LOG_FORMAT, Boolean.toString(cascadeDirectory.exists())));

        File frontalFaceHaarCascade = new File(cascadeDirectory, cascadeXMLFileName);

        InputStream inputStream = referenceContext.getResources().openRawResource(R.raw.frontalfacecascade);
        try {
            FileOutputStream outputStream = new FileOutputStream(frontalFaceHaarCascade);
            byte[] buffer = new byte[SIZE_4KB];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            CascadeClassifier classifier = new CascadeClassifier(frontalFaceHaarCascade.getAbsolutePath());
            classifier.load(frontalFaceHaarCascade.getAbsolutePath());
            Log.d(TAG, String.format(CASCADE_CONTENTS_LOG_FORMAT, Boolean.toString(!classifier.empty())));

            cascadeDirectory.deleteOnExit();
            return classifier;
        } catch (IOException e) {
            return null;
        }
    }
}
