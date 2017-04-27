package tech.elc1798.projectpepe;

import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import tech.elc1798.projectpepe.activities.GalleryActivity;
import tech.elc1798.projectpepe.activities.extras.drawing.DrawingSession;

import static tech.elc1798.projectpepe.activities.extras.PepeUtils.getImageURL;

public class EditActivity extends AppCompatActivity {

    private ImageView imageView;
    private ImageButton confirmActionButton;
    private DrawingSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_activity_layout);

        Intent intent = getIntent();
        String originalImageID = intent.getStringExtra(GalleryActivity.GALLERY_ACTIVITY_IMG_URL_INTENT_EXTRA_ID);

        setUpImageView(originalImageID);
        setUpControlButtons();
        setUpTextBoxButton();
        setUpConfirmActionButton();
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
                Log.d("PEPE_DRAW", event.toString());

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
                updateImage();
            }
        });

        ImageButton redoButton = (ImageButton) this.findViewById(R.id.edit_view_redo_button);
        redoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.redo();
                updateImage();
            }
        });
    }

    private void setUpTextBoxButton() {
        ImageButton textBoxButton = (ImageButton) this.findViewById(R.id.edit_view_text_box_button);
        textBoxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.setPreviewTextBox("TESTING TESTING");
                setConfirmActionButtonVisibility(View.VISIBLE);
                updateImage();
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
}
