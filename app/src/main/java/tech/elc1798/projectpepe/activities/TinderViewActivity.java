package tech.elc1798.projectpepe.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import java.util.ArrayList;

import tech.elc1798.projectpepe.Constants;
import tech.elc1798.projectpepe.R;
import tech.elc1798.projectpepe.activities.extras.DepthPageTransformer;
import tech.elc1798.projectpepe.activities.extras.ScrollableGalleryAdapter;
import tech.elc1798.projectpepe.net.NetworkOperationCallback;
import tech.elc1798.projectpepe.net.NetworkRequestAsyncTask;

public class TinderViewActivity extends AppCompatActivity {

    private static final int CAMERA_ACTIVITY_FINISH_REQUEST_CODE = 42;
    private static final String TAG = "PEPE_TINDER_VIEW:";

    private ProgressBar progressBar;
    private ArrayList<String> imageIDs;
    private GetImageURLCallback callback;
    private ViewPager viewPager;
    private ScrollableGalleryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tinder_view_layout);

        imageIDs = new ArrayList<>();
        callback = new GetImageURLCallback();

        progressBar = (ProgressBar) this.findViewById(R.id.tinder_view_meme_load_progress_bar);
        viewPager = (ViewPager) this.findViewById(R.id.tinder_view_view_pager);
        adapter = new ScrollableGalleryAdapter(this, imageIDs);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                // If position = count - 1, then we're at the end
                Log.d(TAG, position + " " + adapter.getCount());
                if (position == adapter.getCount() - 1) {
                    getNextBatchOfImages();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setPageTransformer(true, new DepthPageTransformer());

        getNextBatchOfImages();
        setUpCameraButton();
        setUpSyncButton();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_ACTIVITY_FINISH_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_CANCELED) {
                getNextBatchOfImages();
            }
        }
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


    private void setUpCameraButton() {
        ImageButton cameraButton = (ImageButton) this.findViewById(R.id.tinder_view_go_to_camera_button);
        final AppCompatActivity activityRef = this;

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activityRef, CameraActivity.class);
                activityRef.startActivityForResult(intent, CAMERA_ACTIVITY_FINISH_REQUEST_CODE);
            }
        });
    }

    private void setUpSyncButton() {
        ImageButton syncButton = (ImageButton) this.findViewById(R.id.tinder_view_sync_button);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNextBatchOfImages();
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
                    if (imageIDs.size() == 0) {
                        imageIDs.add(imagePath);
                    } else {
                        // Insert the image BEFORE the last image (which is "Pepe has no more memes")
                        imageIDs.add(imageIDs.size() - 1, imagePath);
                    }
                }
            }

            currentOffset = imageIDs.size();
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
