package tech.elc1798.projectpepe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tech.elc1798.projectpepe.Constants;
import tech.elc1798.projectpepe.EditActivity;
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

    public static final String GALLERY_ACTIVITY_IMG_URL_INTENT_EXTRA_ID = "gall_act_img_url_int_ext_id";

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
        final String originalImageID = intent.getStringExtra(TINDER_VIEW_ACTIVITY_GALLERY_NAME_INTENT_EXTRA_ID);

        galleryRoute = getGalleryRouteFromImageID(originalImageID);
        imageIDs = new ArrayList<>();

        ImageView originalImage = (ImageView) this.findViewById(R.id.gallery_view_original_image);
        Picasso.with(this).load(getImageURL(originalImageID)).into(originalImage);

        ImageButton editButton = (ImageButton) this.findViewById(R.id.gallery_view_edit_image_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editIntent = new Intent(GalleryActivity.this, EditActivity.class);
                editIntent.putExtra(GALLERY_ACTIVITY_IMG_URL_INTENT_EXTRA_ID, originalImageID);
                GalleryActivity.this.startActivity(editIntent);
            }
        });

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
