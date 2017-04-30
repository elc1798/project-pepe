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

import tech.elc1798.projectpepe.activities.extras.drawing.special.FaceFlipper;
import tech.elc1798.projectpepe.activities.extras.drawing.special.Grayscalify;
import tech.elc1798.projectpepe.activities.extras.drawing.special.Sharpen;
import tech.elc1798.projectpepe.activities.extras.drawing.special.SpecialTool;
import tech.elc1798.projectpepe.imgprocessing.OpenCVLibLoader;

public class DrawingSession {

    private static final String TAG = "PEPE_DRAWING_SESSION";
    private static final int THICKNESS = 4;
    private static final int CIRCLE_RADIUS = 2;
    private static final int FONT_ID = Core.FONT_HERSHEY_DUPLEX;
    private static final double FONT_SCALE = 2.2;

    private Point previousPoint;
    private LinkedList<Mat> undoStack;
    private LinkedList<Mat> redoStack;
    private TextBox textBox;
    private SessionState state;
    private Scalar color;

    private SpecialTool[] specialTools;

    private enum SessionState {
        FREE_DRAW, PREVIEW_TEXT_BOX
    }
    public DrawingSession(final Context context, final Bitmap inputImage) {
        OpenCVLibLoader.loadOpenCV(context, new OpenCVLibLoader.Callback(context, TAG) {
            @Override
            public void onOpenCVLoadSuccess() {
                Mat image = new Mat(inputImage.getHeight(), inputImage.getWidth(), CvType.CV_8UC1);
                Utils.bitmapToMat(inputImage, image);
                Imgproc.cvtColor(image, image, Imgproc.COLOR_RGBA2RGB);

                undoStack = new LinkedList<>();
                undoStack.addFirst(image);
                redoStack = new LinkedList<>();

                previousPoint = new Point(-1, -1);
                textBox = new TextBox();
                state = SessionState.FREE_DRAW;
                color = new Scalar(0, 0, 0);

                specialTools = new SpecialTool[] {
                        new FaceFlipper(context, TAG),
                        new Sharpen(context, TAG),
                        new Grayscalify(context, TAG)
                };
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
        cancelTextBox();

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
                Size textSize = Imgproc.getTextSize(textBox.text, FONT_ID, FONT_SCALE, THICKNESS, null);
                textBox.x = x - (int) (textSize.width / 2);
                textBox.y = y - (int) (textSize.height / 2);
                break;
            }
        }
    }

    public void performSpecialAction(int id) {
        startNewState();

        specialTools[id].doAction(getCurrentImage());
    }

    public String[] getSpecialItemNames() {
        String[] names = new String[specialTools.length];

        for (int i = 0; i < specialTools.length; i++) {
            names[i] = specialTools[i].getName();
        }

        return names;
    }

    public void setPreviewTextBox(String text) {
        state = SessionState.PREVIEW_TEXT_BOX;

        textBox.text = text;
        textBox.x = 0;
        textBox.y = 0;
        textBox.visible = true;
    }

    public void cancelTextBox() {
        textBox.visible = false;
    }

    public void commitTextBox() {
        startNewState();

        Imgproc.putText(
                getCurrentImage(),
                textBox.text,
                new Point(textBox.x, textBox.y),
                FONT_ID,
                FONT_SCALE,
                color,
                THICKNESS
        );
    }

    public Bitmap getBitmap() {
        Mat displayed = getCurrentImage().clone();
        if (textBox.visible) {
            Imgproc.putText(
                    displayed,
                    textBox.text,
                    new Point(textBox.x, textBox.y),
                    Core.FONT_HERSHEY_DUPLEX,
                    FONT_SCALE,
                    color,
                    THICKNESS
            );
        }

        Bitmap bm = Bitmap.createBitmap(displayed.cols(), displayed.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(displayed, bm);

        return bm;
    }

    private Mat getCurrentImage() {
        return undoStack.getFirst();
    }

    private void drawPath(int x, int y) {
        Point newPoint = new Point(x, y);

        if ((int) previousPoint.x < 0 || (int) previousPoint.y < 0) {
            startNewState();
            Imgproc.circle(getCurrentImage(), newPoint, CIRCLE_RADIUS, color, THICKNESS);
        } else {
            Imgproc.line(getCurrentImage(), previousPoint, newPoint, color, THICKNESS);
        }

        previousPoint = newPoint;
    }

    private void startNewState() {
        undoStack.addFirst(getCurrentImage().clone());

        // Clear the redo stack, as we have branched away from the previous diff path
        redoStack.clear();

        // Update the state type
        state = SessionState.FREE_DRAW;

        // Make the textbox preview disappear
        textBox.visible = false;
    }
}
