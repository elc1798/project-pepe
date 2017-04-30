package tech.elc1798.projectpepe.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
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

/**
 * A Tinder-Like Activity where users swipe through images, and click them for a "Detail" view
 */
public class TinderViewActivity extends AppCompatActivity {

    public static final String TINDER_VIEW_ACTIVITY_GALLERY_NAME_INTENT_EXTRA_ID = "tind_act_gallery_name_intent_id";

    private static final int CAMERA_ACTIVITY_FINISH_REQUEST_CODE = 42;
    private static final String TAG = "PEPE_TINDER_VIEW:";

    private ProgressBar progressBar;
    private ArrayList<String> imageIDs;
    private GetImageURLCallback callback;
    private ViewPager viewPager;
    private ScrollableGalleryAdapter adapter;
    private int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tinder_view_layout);

        imageIDs = new ArrayList<>();
        callback = new GetImageURLCallback();
        currentPage = 0;
        progressBar = (ProgressBar) this.findViewById(R.id.tinder_view_meme_load_progress_bar);

        setUpViewPagerAndAdapter();
        getNextBatchOfImages();
        setUpCameraButton();
        setUpSyncButton();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_ACTIVITY_FINISH_REQUEST_CODE) { // If we returned from the camera, refresh
            if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_CANCELED) {
                getNextBatchOfImages();
            }
        }
    }

    /**
     * Pulls another batch of 10 images starting from the end of the list of images we've already loaded
     */
    private void getNextBatchOfImages() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        new NetworkRequestAsyncTask(callback).execute(callback.getCurrentPageURL());
    }

    /**
     * Sets up the ViewPager and Adapter
     */
    private void setUpViewPagerAndAdapter() {
        viewPager = (ViewPager) this.findViewById(R.id.tinder_view_view_pager);
        adapter = new ScrollableGalleryAdapter(this, imageIDs, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TinderViewActivity.this, GalleryActivity.class);
                intent.putExtra(TINDER_VIEW_ACTIVITY_GALLERY_NAME_INTENT_EXTRA_ID, imageIDs.get(currentPage));
                TinderViewActivity.this.startActivity(intent);
            }
        });

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                // If position = count - 1, then we're at the end
                if (position == adapter.getCount() - 1) {
                    getNextBatchOfImages();
                }

                currentPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setPageTransformer(true, new DepthPageTransformer());
    }

    /**
     * Sets up the camera button to start an intent for the CameraView
     */
    private void setUpCameraButton() {
        ImageButton cameraButton = (ImageButton) this.findViewById(R.id.tinder_view_go_to_camera_button);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TinderViewActivity.this, CameraActivity.class);
                TinderViewActivity.this.startActivityForResult(intent, CAMERA_ACTIVITY_FINISH_REQUEST_CODE);
            }
        });
    }

    /**
     * Sets up the sync button. The sync button will force a hard refresh of images
     */
    private void setUpSyncButton() {
        ImageButton syncButton = (ImageButton) this.findViewById(R.id.tinder_view_sync_button);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNextBatchOfImages();
            }
        });
    }

    /**
     * Callback for getting a batch of images. Will update the dataset with all the image routes we received from the
     * server. If we did receive at least 1 image, updates the dataset and force refreshes the ViewPager
     */
    private class GetImageURLCallback extends NetworkOperationCallback {

        private int currentOffset;

        GetImageURLCallback() {
            currentOffset = 0;
        }

        /**
         * Gets the current URL for the 'Page' we are on (Each page of the ViewPager is a single image)
         *
         * @return A String containing the URL
         */
        String getCurrentPageURL() {
            String params = String.format(Constants.PEPE_ROOT_GET_PARAMETERS, currentOffset);
            return Constants.PEPE_ROOT_URL + params;
        }

        @Override
        public void parseNetworkOperationContents(String contents) {
            if (contents == null) {
                return;
            }

            // Get the new image routes the server gave us
            int originalCount = adapter.getCount();
            String[] imagePaths = contents.trim().split(Constants.PEPE_IMAGELIST_SEPARATOR);

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

            // Force reinstantiation of all the items, since there's a bug with ViewPager and updating the dataset:
            // http://stackoverflow.com/questions/7369978/how-to-force-viewpager-to-re-instantiate-its-items
            if (adapter.getCount() > originalCount) {
                viewPager.setAdapter(adapter);
            }

            // Scroll to the page we were just on, since we had to hard refresh the ViewPager
            viewPager.setCurrentItem(currentPage);
        }
    }
}
