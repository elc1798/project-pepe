package tech.elc1798.projectpepe.activities;

import android.content.Intent;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tech.elc1798.projectpepe.Constants;
import tech.elc1798.projectpepe.R;
import tech.elc1798.projectpepe.activities.extras.TinderViewTouchDetector;
import tech.elc1798.projectpepe.net.NetworkOperationCallback;
import tech.elc1798.projectpepe.net.NetworkRequestAsyncTask;

public class TinderViewActivity extends AppCompatActivity {

    private static final String TAG = "PEPE_TINDER_VIEW:";
    private static final float FULL_TRANSPARENCY = 0.0f;
    private static final float FULL_OPACITY = 1.0f;

    private FrameLayout imgDisplayFrame;
    private ImageView imageBelow;
    private ImageView imageAbove;
    private ProgressBar progressBar;
    private ArrayList<String> imageIDs;
    private GetImageURLCallback callback;
    private GestureDetectorCompat detector;
    private int currentImageIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tinder_view_layout);

        imageIDs = new ArrayList<>();
        callback = new GetImageURLCallback();

        imgDisplayFrame = (FrameLayout) this.findViewById(R.id.tinder_view_img_display);

        imageBelow = (ImageView) this.findViewById(R.id.tinder_view_image_below);
        imageAbove = (ImageView) this.findViewById(R.id.tinder_view_image_above);

        progressBar = (ProgressBar) this.findViewById(R.id.tinder_view_meme_load_progress_bar);

        getNextBatchOfImages();
        setUpCameraButton();
        setUpNavigationButtons();

        currentImageIndex = 0;
        detector = new GestureDetectorCompat(this, new TinderViewTouchDetector(this));

    }

    @Override
    protected void onResume() {
        super.onResume();

        loadCurrentImages();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void loadNextImage() {
        if (!atEndOfImageList()) {
            currentImageIndex++;
            loadCurrentImages();
        } else {
            getNextBatchOfImages();
        }
    }

    public void loadPreviousImage() {
        if (!atStartOfImageList()) {
            currentImageIndex--;
            loadCurrentImages();
        }
    }

    /**
     * Does **NOT** return if the currentImageIndex is the last element of the image list. Instead, returns if we are
     * 1 past the end last element. This mechanic is intended, as we want to show the "No more memes" image at the end,
     * in order to allow for us to scroll to it.
     *
     * @return a boolean
     */
    public boolean atEndOfImageList() {
        return currentImageIndex /* We don't do this: + 1 */ >= imageIDs.size();
    }

    public boolean atStartOfImageList() {
        return currentImageIndex == 0;
    }

    /**
     * Loads the images stored in our ArrayList of IDs into our image views. The current image is loaded into
     * imageAbove, while the one after is loaded into imageBelow. If the image at index is the last, imageBelow is made
     * completely transparent.
     */
    private void loadCurrentImages() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadImageAtIndexIntoImageView(currentImageIndex + 1, imageBelow);
                loadImageAtIndexIntoImageView(currentImageIndex, imageAbove);
            }
        });
    }

    private void getNextBatchOfImages() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        new NetworkRequestAsyncTask(callback).execute(callback.getCurrentPageURL());
    }

    private String getImageURL(String imageID) {
        return Constants.PROJECT_SERVER_URL + imageID;
    }

    private void loadImageAtIndexIntoImageView(int index, ImageView imageView) {
        if (index >= imageIDs.size()) {
            Log.d(TAG, "Setting transparent: Index " + index + "/" + imageIDs.size());
            imageView.setAlpha(FULL_TRANSPARENCY);
        } else {
            Log.d(TAG, "Setting opaque: Index " + index + "/" + imageIDs.size());
            imageView.setAlpha(FULL_OPACITY);
            Picasso.with(this).load(getImageURL(imageIDs.get(index))).into(imageView);
        }
    }

    private void setUpCameraButton() {
        ImageButton cameraButton = (ImageButton) this.findViewById(R.id.tinder_view_go_to_camera_button);
        final AppCompatActivity activityRef = this;

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activityRef, CameraActivity.class);
                activityRef.startActivity(intent);
            }
        });
    }

    private void setUpNavigationButtons() {
        ImageButton leftButton = (ImageButton) this.findViewById(R.id.tinder_view_go_left);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPreviousImage();
            }
        });

        ImageButton rightButton = (ImageButton) this.findViewById(R.id.tinder_view_go_right);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNextImage();
            }
        });
    }

    private class GetImageURLCallback extends NetworkOperationCallback {

        private int currentOffset;

        GetImageURLCallback() {
            currentOffset = 0;
        }

        String getCurrentPageURL() {
            String params = String.format(Constants.PROJECT_SERVER_GET_PARAMETERS, currentOffset);
            return Constants.PROJECT_SERVER_URL + params;
        }

        @Override
        public void parseNetworkOperationContents(String contents) {
            if (contents == null) {
                return;
            }

            String[] imagePaths = contents.trim().split(Constants.PROJECT_SERVER_IMAGELIST_SEPARATOR);

            for (String imagePath : imagePaths) {
                Log.d(TAG, "Image path: " + imagePath);
                if (imagePath.length() > 0) {
                    imageIDs.add(imagePath);
                }
            }

            currentOffset = imageIDs.size();
            loadCurrentImages();

            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
