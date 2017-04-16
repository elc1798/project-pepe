package tech.elc1798.projectpepe.activities.extras;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import tech.elc1798.projectpepe.R;

public class MemeViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageView;

    private String imageID;

    public MemeViewHolder(final AppCompatActivity activityContextRef, View itemView) {
        super(itemView);

        /* This will be used later
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activityContextRef, GalleryViewActivity.class);
                intent.putExtra(GalleryViewActivity.GALLERY_VIEW_ACTIVITY_IMAGE_ID_INTENT_EXTRA_ID, imageID);

                activityContextRef.startActivity(intent);
            }
        });
        */

        imageView = (ImageView) itemView.findViewById(R.id.tinder_view_memevh_image_view);
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }
}
