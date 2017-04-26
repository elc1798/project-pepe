package tech.elc1798.projectpepe.activities;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tech.elc1798.projectpepe.Constants;
import tech.elc1798.projectpepe.R;
import tech.elc1798.projectpepe.activities.extras.DepthPageTransformer;
import tech.elc1798.projectpepe.activities.extras.ScrollableGalleryAdapter;
import tech.elc1798.projectpepe.net.NetworkOperationCallback;
import tech.elc1798.projectpepe.net.NetworkRequestAsyncTask;

import static tech.elc1798.projectpepe.activities.TinderViewActivity.TINDER_VIEW_ACTIVITY_GALLERY_NAME_INTENT_EXTRA_ID;
import static tech.elc1798.projectpepe.activities.extras.PepeUtils.getGalleryIDFromRoute;
import static tech.elc1798.projectpepe.activities.extras.PepeUtils.getGalleryImageURL;
import static tech.elc1798.projectpepe.activities.extras.PepeUtils.getGalleryRouteFromImageID;
import static tech.elc1798.projectpepe.activities.extras.PepeUtils.getImageURL;

public class GalleryActivity extends AppCompatActivity {

    private static final String TAG = "PEPE_GALLERY:";

    private ViewPager viewPager;
    private ScrollableGalleryAdapter adapter;
    private String galleryRoute;
    private ArrayList<String> imageIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity_layout);

        Intent intent = getIntent();
        String originalImageID = intent.getStringExtra(TINDER_VIEW_ACTIVITY_GALLERY_NAME_INTENT_EXTRA_ID);

        galleryRoute = getGalleryRouteFromImageID(originalImageID);
        imageIDs = new ArrayList<>();

        ImageView originalImage = (ImageView) this.findViewById(R.id.gallery_view_original_image);
        Picasso.with(this).load(getImageURL(originalImageID)).into(originalImage);

        viewPager = (ViewPager) this.findViewById(R.id.gallery_view_view_pager);
        adapter = new ScrollableGalleryAdapter(this, imageIDs, null);
        viewPager.setPageTransformer(true, new DepthPageTransformer());

        String galleryID = getGalleryIDFromRoute(galleryRoute);
        String gallerySizeURL = Constants.PEPE_GALLERY_SIZE_URL + String.format(
                Constants.PEPE_GALLERY_SIZE_GET_PARAMETERS, galleryID
        );

        Log.d(TAG, gallerySizeURL);
        new NetworkRequestAsyncTask(new GetNumGalleryImagesCallback()).execute(gallerySizeURL);
    }

    private class GetNumGalleryImagesCallback extends NetworkOperationCallback {

        @Override
        public void parseNetworkOperationContents(String contents) {
            int numImages;

            try {
                numImages = Integer.parseInt(contents);
            } catch (NumberFormatException e) {
                Log.d(TAG, "BAD GALLERY SIZE");
                return;
            }

            for (int i = 0; i < numImages; i++) {
                imageIDs.add(getGalleryImageURL(galleryRoute, i));
            }

            Log.d(TAG, imageIDs.toString());
            viewPager.setAdapter(adapter);
        }
    }
}
