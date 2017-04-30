package tech.elc1798.projectpepe.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import tech.elc1798.projectpepe.Constants;
import tech.elc1798.projectpepe.R;
import tech.elc1798.projectpepe.activities.extras.drawing.DrawingSession;
import tech.elc1798.projectpepe.net.FileUploader;

import static tech.elc1798.projectpepe.Constants.COMPRESSION_RATE;
import static tech.elc1798.projectpepe.Constants.IMG_CACHE_FILENAME_FORMAT;
import static tech.elc1798.projectpepe.Constants.IMG_CACHE_STORAGE_DIRECTORY;
import static tech.elc1798.projectpepe.activities.extras.PepeUtils.getGalleryIDFromRoute;
import static tech.elc1798.projectpepe.activities.extras.PepeUtils.getGalleryRouteFromImageID;
import static tech.elc1798.projectpepe.activities.extras.PepeUtils.getImageURL;

public class EditActivity extends AppCompatActivity {

    private static final String TAG = "PEPE_EDITOR:";
    private static final String TEXT_BOX_TITLE = "New text box";
    private static final String TEXT_BOX_DEFAULT_TEXT = "Your text here.";
    private static final String TEXT_BOX_POSITIVE_BUTTON_LABEL = "Confirm";
    private static final String TEXT_BOX_NEGATIVE_BUTTON_LABEL = "Cancel";
    private static final String SPECIAL_TOOLS_TITLE = "Special Actions";
    private static final int RED_CHANNEL_INDEX = 0;
    private static final int GREEN_CHANNEL_INDEX = 1;
    private static final int BLUE_CHANNEL_INDEX = 2;
    private static final int RED_SHIFT_AMOUNT = 16;
    private static final int GREEN_SHIFT_AMOUNT = 8;
    private static final int BLUE_SHIFT_AMOUNT = 0;
    private static final int COLOR_CHANNEL_BITMASK = 0xFF;
    private static final int COUNTDOWN_LATCH_WAIT_TIME = 0;

    private String originalImageID;
    private ImageView imageView;
    private ImageButton confirmActionButton;
    private ImageButton colorWheelButton;
    private ProgressBar progressBar;
    private DrawingSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_activity_layout);

        Intent intent = getIntent();
        originalImageID = intent.getStringExtra(GalleryActivity.GALLERY_ACTIVITY_IMG_URL_INTENT_EXTRA_ID);

        progressBar = (ProgressBar) this.findViewById(R.id.edit_view_progress_bar);

        setUpImageView(originalImageID);
        setUpControlButtons();
        setUpTextBoxButton();
        setUpConfirmActionButton();
        setUpColorWheelButton();
        setUpSpecialButton();
        setUpUploadButton();
    }

    /**
     * Sets up the image view and creates a drawing sesion bound to it.
     *
     * @param originalImageID The ID of the original image so we can load it with Picasso
     */
    private void setUpImageView(String originalImageID) {
        imageView = (ImageView) this.findViewById(R.id.edit_view_image_view);
        Picasso.with(this).load(getImageURL(originalImageID)).into(imageView);

        session = new DrawingSession(this, ((BitmapDrawable) imageView.getDrawable()).getBitmap());
        imageView.setOnTouchListener(new View.OnTouchListener() {
            Matrix inverse = new Matrix();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                switch (action) {
                    case MotionEvent.ACTION_UP: {
                        session.resetCursor();
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        // Calculate the actual coordinates of the bitmap:
                        // http://stackoverflow.com/questions/33391196/how-to-take-real-coordinatesx-y-of-bitmap-from-imageview-android
                        imageView.getImageMatrix().invert(inverse);
                        float[] touchPoint = {event.getX(), event.getY()};
                        inverse.mapPoints(touchPoint);

                        // Draw the path
                        session.performAction((int) touchPoint[0], (int) touchPoint[1]);

                        // Update the image
                        updateImage();
                        break;
                    }
                }

                return true;
            }
        });
    }

    private void setUpControlButtons() {
        ImageButton undoButton = (ImageButton) this.findViewById(R.id.edit_view_undo_button);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.undo();
                setConfirmActionButtonVisibility(View.INVISIBLE);
                updateImage();
            }
        });

        ImageButton redoButton = (ImageButton) this.findViewById(R.id.edit_view_redo_button);
        redoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.redo();
                setConfirmActionButtonVisibility(View.INVISIBLE);
                updateImage();
            }
        });
    }

    private void setUpTextBoxButton() {
        final ImageButton textBoxButton = (ImageButton) this.findViewById(R.id.edit_view_text_box_button);
        textBoxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText textBoxText = new EditText(EditActivity.this);
                textBoxText.setHint(TEXT_BOX_DEFAULT_TEXT);

                // Spawn alert dialog with text input "JavaScript cascade" style
                new AlertDialog.Builder(EditActivity.this)
                        .setTitle(TEXT_BOX_TITLE)
                        .setView(textBoxText)
                        .setPositiveButton(TEXT_BOX_POSITIVE_BUTTON_LABEL, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                session.setPreviewTextBox(textBoxText.getText().toString());
                                setConfirmActionButtonVisibility(View.VISIBLE);
                                updateImage();
                            }
                        })
                        .setNegativeButton(TEXT_BOX_NEGATIVE_BUTTON_LABEL, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });
    }

    private void setUpConfirmActionButton() {
        confirmActionButton = (ImageButton) this.findViewById(R.id.edit_view_confirm_action_button);
        confirmActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.commitTextBox();
                updateImage();
                setConfirmActionButtonVisibility(View.INVISIBLE);
            }
        });
    }

    public void setUpColorWheelButton() {
        colorWheelButton = (ImageButton) this.findViewById(R.id.edit_view_color_wheel_button);
        colorWheelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a dialog with the current color value as default
                double[] currentColor = session.getColor().val;
                final ColorPicker cp = new ColorPicker(
                        EditActivity.this,
                        (int) currentColor[RED_CHANNEL_INDEX],
                        (int) currentColor[GREEN_CHANNEL_INDEX],
                        (int) currentColor[BLUE_CHANNEL_INDEX]
                );

                // Set the callback for the dialog
                cp.setCallback(new ColorPickerCallback() {
                    @Override
                    public void onColorChosen(@ColorInt int color) {
                        session.setColor(
                                (color >> RED_SHIFT_AMOUNT) & COLOR_CHANNEL_BITMASK,
                                (color >> GREEN_SHIFT_AMOUNT) & COLOR_CHANNEL_BITMASK,
                                (color >> BLUE_SHIFT_AMOUNT) & COLOR_CHANNEL_BITMASK
                        );

                        // Update the background of the button to reflect the color we chose
                        colorWheelButton.setBackgroundColor(color);

                        // Update image to reflect color changes
                        updateImage();

                        cp.dismiss();
                    }
                });

                // Show the dialog
                cp.show();
            }
        });
    }

    private void setUpSpecialButton() {
        ImageButton specialButton = (ImageButton) this.findViewById(R.id.edit_view_special_tools_button);
        specialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.cancelTextBox();
                new AlertDialog.Builder(EditActivity.this)
                        .setTitle(SPECIAL_TOOLS_TITLE)
                        .setItems(session.getSpecialItemNames(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                session.performSpecialAction(which);
                                updateImage();
                            }
                        })
                        .show();
            }
        });
    }

    private void setUpUploadButton() {
        ImageButton uploadButton = (ImageButton) this.findViewById(R.id.edit_view_upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.cancelTextBox();
                saveImageAndUpload();
            }
        });
    }

    private void updateImage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(session.getBitmap());
            }
        });
    }

    private void setConfirmActionButtonVisibility(final int visible) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                confirmActionButton.setVisibility(visible);
            }
        });
    }

    private void setProgressBarVisibility(final int visibility) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(visibility);
            }
        });
    }

    private void saveImageAndUpload() {
        File imgDirectory = this.getDir(
                IMG_CACHE_STORAGE_DIRECTORY,
                Context.MODE_PRIVATE
        );

        String uniqueFileName = String.format(
                IMG_CACHE_FILENAME_FORMAT,
                Long.toString(System.currentTimeMillis())
        );

        final File imgFile = new File(imgDirectory, uniqueFileName);

        final Thread ioThread = new Thread(new Runnable() {
            @Override
            public void run() {
                setProgressBarVisibility(View.VISIBLE);

                // Write to file
                try {
                    FileOutputStream outputStream = new FileOutputStream(imgFile);
                    session.getBitmap().compress(Bitmap.CompressFormat.PNG, COMPRESSION_RATE, outputStream);
                    outputStream.close();
                } catch (Exception e) {
                    Log.d(TAG, "File saving failed!");
                    return;
                }

                String galleryUploadURL = Constants.PEPE_FILE_UPLOAD_URL + String.format(
                        Constants.PEPE_GALLERY_ID_GET_PARAMETER,
                        getGalleryIDFromRoute(getGalleryRouteFromImageID(originalImageID))
                );

                CountDownLatch latch = new CountDownLatch(1);
                FileUploader.uploadFile(EditActivity.this, imgFile, galleryUploadURL, latch);

                try {
                    latch.await(COUNTDOWN_LATCH_WAIT_TIME, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Log.d(TAG, "CountDownLatch interrupted!");
                }

                EditActivity.this.finish();
            }
        });
        ioThread.start();
    }
}
