package tech.elc1798.projectpepe.activities.extras;


import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tech.elc1798.projectpepe.R;

import static tech.elc1798.projectpepe.activities.extras.PepeUtils.getImageURL;

public class ScrollableGalleryAdapter extends PagerAdapter {

    private Context contextRef;
    private LayoutInflater layoutInflater;
    private ArrayList<String> imageIDs;
    private OnClickListener onClickListener;

    public ScrollableGalleryAdapter(Context contextRef, ArrayList<String> imageIDs, OnClickListener onClickListener) {
        this.contextRef = contextRef;
        this.imageIDs = imageIDs;
        this.onClickListener = onClickListener;
        layoutInflater = (LayoutInflater) this.contextRef.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        if (onClickListener != null) {
            imageView.setOnClickListener(onClickListener);
        }
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }
}
