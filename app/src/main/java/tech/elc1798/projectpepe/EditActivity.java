package tech.elc1798.projectpepe;

import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import tech.elc1798.projectpepe.activities.GalleryActivity;
import tech.elc1798.projectpepe.activities.extras.drawing.DrawingSession;

import static tech.elc1798.projectpepe.activities.extras.PepeUtils.getImageURL;

public class EditActivity extends AppCompatActivity {

    private ImageView imageView;
    private DrawingSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_activity_layout);

        Intent intent = getIntent();
        String originalImageID = intent.getStringExtra(GalleryActivity.GALLERY_ACTIVITY_IMG_URL_INTENT_EXTRA_ID);

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
                        float[] touchPoint = { event.getX(), event.getY() };
                        inverse.mapPoints(touchPoint);

                        // Draw the path
                        session.drawPath((int) touchPoint[0], (int) touchPoint[1]);

                        // Update the image
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(session.getBitmap());
                            }
                        });
                        break;
                    }
                }

                return true;
            }
        });
    }
}
