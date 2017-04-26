package tech.elc1798.projectpepe.activities.extras;


import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tech.elc1798.projectpepe.Constants;
import tech.elc1798.projectpepe.R;

public class ScrollableGalleryAdapter extends PagerAdapter {

    private Context contextRef;
    private LayoutInflater layoutInflater;
    private ArrayList<String> imageIDs;

    public ScrollableGalleryAdapter(Context contextRef, ArrayList<String> imageIDs) {
        this.contextRef = contextRef;
        layoutInflater = (LayoutInflater) this.contextRef.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.imageIDs = imageIDs;
    }

    @Override
    public int getCount() {
        return imageIDs.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = layoutInflater.inflate(R.layout.gallery_image_item_layout, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.gallery_image_item_image_view);
        Picasso.with(contextRef).load(getImageURL(imageIDs.get(position))).into(imageView);
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }

    private String getImageURL(String imageID) {
        return Constants.PROJECT_SERVER_URL + imageID;
    }
}
