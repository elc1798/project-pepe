package tech.elc1798.projectpepe.activities.extras.drawing;


import android.content.Context;
import android.graphics.Bitmap;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;

import tech.elc1798.projectpepe.imgprocessing.OpenCVLoaderCallback;

public class DrawingSession {

    public enum SessionState {
        FREE_DRAW, PREVIEW_TEXT_BOX
    }

    private static final String TAG = "PEPE_DRAWING_SESSION";
    private static final int THICKNESS = 5;
    private static final int CIRCLE_RADIUS = 2;

    private Point previousPoint;
    private LinkedList<Mat> undoStack;
    private LinkedList<Mat> redoStack;
    private TextBox textBox;
    private SessionState state;
    private Scalar color;

    public DrawingSession(Context context, final Bitmap inputImage) {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, context, new OpenCVLoaderCallback(context, TAG) {
            @Override
            public void onOpenCVLoadSuccess() {
                Mat image = new Mat(inputImage.getHeight(), inputImage.getWidth(), CvType.CV_8UC1);
                Utils.bitmapToMat(inputImage, image);

                undoStack = new LinkedList<>();
                undoStack.addFirst(image);

                redoStack = new LinkedList<>();
                previousPoint = new Point(-1, -1);

                textBox = new TextBox();
                state = SessionState.FREE_DRAW;

                color = new Scalar(0, 0, 0);
            }
        });
    }

    public Scalar getColor() {
        return color;
    }

    public void setColor(int r, int g, int b) {
        this.color = new Scalar(r, g, b);
    }

    public void resetCursor() {
        previousPoint = new Point(-1, -1);
    }

    public void undo() {
        // Undo textbox visibility
        textBox.visible = false;

        // Reset state to default
        state = SessionState.FREE_DRAW;

        Mat undone = undoStack.pop();

        if (undoStack.isEmpty()) {
            undoStack.addFirst(undone);
        } else {
            redoStack.addFirst(undone);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.addFirst(redoStack.pop());
        }
    }

    public void performAction(int x, int y) {
        switch (state) {
            case FREE_DRAW: {
                drawPath(x, y);
                break;
            }
            case PREVIEW_TEXT_BOX: {
                // We want the user to appear like they're dragging the textbox from its center, so we offset the coors
                Size textSize = Imgproc.getTextSize(textBox.text, Core.FONT_HERSHEY_DUPLEX, 1.0, THICKNESS, null);
                textBox.x = x - (int) (textSize.width / 2);
                textBox.y = y - (int) (textSize.height / 2);
                break;
            }
        }
    }

    public void setPreviewTextBox(String text) {
        state = SessionState.PREVIEW_TEXT_BOX;

        textBox.text = text;
        textBox.x = 0;
        textBox.y = 0;
        textBox.visible = true;
    }

    public void commitTextBox() {
        startNewState();

        Imgproc.putText(undoStack.getFirst(), textBox.text, new Point(textBox.x, textBox.y), Core.FONT_HERSHEY_DUPLEX, 1.0, color, THICKNESS);
    }

    public Bitmap getBitmap() {
        Mat displayed = undoStack.getFirst().clone();
        if (textBox.visible) {
            Imgproc.putText(displayed, textBox.text, new Point(textBox.x, textBox.y), Core.FONT_HERSHEY_DUPLEX, 1.0, color, THICKNESS);
        }

        Bitmap bm = Bitmap.createBitmap(displayed.cols(), displayed.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(displayed, bm);

        return bm;
    }

    private void drawPath(int x, int y) {
        Point newPoint = new Point(x, y);

        if ((int) previousPoint.x < 0 || (int) previousPoint.y < 0) {
            startNewState();
            Imgproc.circle(undoStack.getFirst(), newPoint, CIRCLE_RADIUS, color, THICKNESS);
        } else {
            Imgproc.line(undoStack.getFirst(), previousPoint, newPoint, color, THICKNESS);
        }

        previousPoint = newPoint;
    }

    private void startNewState() {
        undoStack.addFirst(undoStack.getFirst().clone());

        // Clear the redo stack, as we have branched away from the previous diff path
        redoStack.clear();

        // Update the state type
        state = SessionState.FREE_DRAW;

        // Make the textbox preview disappear
        textBox.visible = false;
    }
}
