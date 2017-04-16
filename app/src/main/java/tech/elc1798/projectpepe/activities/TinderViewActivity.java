package tech.elc1798.projectpepe.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

import tech.elc1798.projectpepe.Constants;
import tech.elc1798.projectpepe.R;
import tech.elc1798.projectpepe.activities.extras.MemeViewAdapter;
import tech.elc1798.projectpepe.net.NetworkOperationCallback;
import tech.elc1798.projectpepe.net.NetworkRequestAsyncTask;

public class TinderViewActivity extends AppCompatActivity {

    private static final String TAG = "PEPE_TINDER_VIEW:";

    private RecyclerView recyclerView;
    private MemeViewAdapter adapter;
    private LinearLayoutManager layoutManager;
    private ArrayList<String> imageIDs;
    private GetImageURLCallback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tinder_view_layout);

        imageIDs = new ArrayList<>();
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        adapter = new MemeViewAdapter(this, imageIDs);

        recyclerView = (RecyclerView) this.findViewById(R.id.tinder_view_recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        callback = new GetImageURLCallback();

        new NetworkRequestAsyncTask(callback).execute(callback.getCurrentURL());

        setUpCameraButton();
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

    private class GetImageURLCallback extends NetworkOperationCallback {

        private int currentPage;

        GetImageURLCallback() {
            currentPage = 0;
        }

        String getCurrentURL() {
            String params = String.format(Constants.PROJECT_SERVER_GET_PARAMETERS, currentPage);
            return Constants.PROJECT_SERVER_URL + params;
        }

        @Override
        public void parseNetworkOperationContents(String contents) {
            if (contents == null) {
                return;
            }

            currentPage++;
            String[] imagePaths = contents.split(Constants.PROJECT_SERVER_IMAGELIST_SEPARATOR);
            for (String imagePath : imagePaths) {
                Log.d(TAG, imagePath);
                imageIDs.add(imagePath);
            }

            adapter.notifyDataSetChanged();
        }
    }
}
