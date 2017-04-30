package tech.elc1798.projectpepe.activities.extras.drawing;

import android.content.Context;
import android.graphics.Bitmap;

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

/**
 * A "Session" for drawing on an image.
 */
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

    /**
     * Creates a new DrawingSession instance. Variable setup isn't done directly in the constructor, but in the OpenCV
     * load callback. This is simply because the majority of the private variables are dependent on OpenCV, and others
     * (like the undo stack) perform operations on those variables. To handle the asynchronous nature of loading OpenCV,
     * everything is performed in the callback.
     *
     * @param context The context using a drawing session. This context is passed down to the OpenCV loader
     * @param inputImage The initial image that the DrawingSession holds
     */
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

    /**
     * Returns an OpenCV scalar representing the current color being used for free draw and text. The scalar is in RGB
     * format.
     *
     * @return a 3-value Scalar
     */
    public Scalar getColor() {
        return color;
    }

    /**
     * Sets the current color used by free draw and text.
     *
     * @param r The red value to use for the color. Should be [0, 255]
     * @param g The green value to use for the color. Should be [0, 255]
     * @param b The blue value to use for the color. Should be [0, 255]
     */
    public void setColor(int r, int g, int b) {
        this.color = new Scalar(r, g, b);
    }

    /**
     * Resets the free draw cursor / text box location to an invalid location
     */
    public void resetCursor() {
        previousPoint = new Point(-1, -1);
    }

    /**
     * Undoes an operation performed within the drawing session. If the preview text box is currently on, it is
     * cancelled. The state is reset to free draw. The original image CAN NOT be popped from the undo stack. To improve
     * runtime, rather than checking the size of the stack (O(n)), we pop the stack, check if it's empty, and re-push
     * the popped item if it is empty (O(1)).
     */
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

    /**
     * Re-applies an undone operation if available
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.addFirst(redoStack.pop());
        }
    }

    /**
     * This is called by an OnTouchListener. Will perform the appropriate action depending on the current state of the
     * DrawingSession. If the DrawingSession is in FREE_DRAW, perform action will draw a path, whereas if the session is
     * in the PREVIEW_TEXT_BOX state, it will move the preview to where x and y coordinates are.
     *
     * @param x The scaled x coordinate input sent by the touch event
     * @param y The scaled y coordinate input sent by the touch event
     */
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

    /**
     * Performs the special action indicated by the ID. Performing a special action will add a new revision onto the
     * undo stack, but will NOT change the state of the drawing session (special actions occur spontaneously)
     *
     * @param id The ID of the special action
     */
    public void performSpecialAction(int id) {
        startNewRevision();

        specialTools[id].doAction(getCurrentImage());
    }

    /**
     * Gets the names of the special tools currently loaded into this drawing session. The names will be in the same
     * order as the tool objects themselves.
     *
     * @return an array of Strings
     */
    public String[] getSpecialToolNames() {
        String[] names = new String[specialTools.length];

        for (int i = 0; i < specialTools.length; i++) {
            names[i] = specialTools[i].getName();
        }

        return names;
    }

    /**
     * Sets the text of the preview text box and makes the preview text box visible if it hasn't already
     *
     * @param text The text to display in the preview text box
     */
    public void setPreviewTextBox(String text) {
        state = SessionState.PREVIEW_TEXT_BOX;

        textBox.text = text;
        textBox.x = 0;
        textBox.y = 0;
        textBox.visible = true;
    }

    /**
     * Invalidates the preview text box, making it un-commitable and invisible
     */
    public void cancelTextBox() {
        textBox.visible = false;
    }

    /**
     * Write the preview text box to the actual image. If the preview text box is invalid (invisible), this method does
     * nothing. Otherwise, this method will start a new revision on the undo stack and write the preview text box to the
     * image.
     */
    public void commitTextBox() {
        if (!textBox.visible) {
            return;
        }

        startNewRevision();

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

    /**
     * Returns the Bitmap representation of the current image. If the preview text box is visible, will overlay it on
     * top of the image.
     *
     * @return an Android Bitmap
     */
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

    /**
     * Returns the top element of the undo stack
     *
     * @return An OpenCV Mat
     */
    private Mat getCurrentImage() {
        return undoStack.getFirst();
    }

    /**
     * Draws a path from the previous point to the new point. If the previous point is in an invalid position, then the
     * path is instead a single dot where the new point is. Doing so will start a new revision on the undo stack.
     *
     * @param x The x coordinate of the new point
     * @param y The y coordinate of the new point
     */
    private void drawPath(int x, int y) {
        Point newPoint = new Point(x, y);

        if ((int) previousPoint.x < 0 || (int) previousPoint.y < 0) {
            startNewRevision();
            Imgproc.circle(getCurrentImage(), newPoint, CIRCLE_RADIUS, color, THICKNESS);
        } else {
            Imgproc.line(getCurrentImage(), previousPoint, newPoint, color, THICKNESS);
        }

        previousPoint = newPoint;
    }

    /**
     * Starts a new revision on the undo stack.
     */
    private void startNewRevision() {
        undoStack.addFirst(getCurrentImage().clone());

        // Clear the redo stack, as we have branched away from the previous diff path
        redoStack.clear();

        // Update the state type
        state = SessionState.FREE_DRAW;

        // Make the textbox preview disappear
        textBox.visible = false;
    }
}
