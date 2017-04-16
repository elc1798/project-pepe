package tech.elc1798.projectpepe.activities.extras;


import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tech.elc1798.projectpepe.Constants;
import tech.elc1798.projectpepe.R;

public class MemeViewAdapter extends RecyclerView.Adapter {

    private AppCompatActivity parentActivityRef;
    private ArrayList<String> imageIDs;

    public MemeViewAdapter(AppCompatActivity parentActivityRef, ArrayList<String> imageIDs) {
        this.parentActivityRef = parentActivityRef;
        this.imageIDs = imageIDs;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View meme = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.tinder_view_meme_view_holder_layout,
                parent,
                false
        );

        return new MemeViewHolder(parentActivityRef, meme);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MemeViewHolder viewHolder = (MemeViewHolder) holder;
        String imageID = imageIDs.get(position);

        String imageURL = Constants.PROJECT_SERVER_URL + imageID;

        Picasso.with(parentActivityRef).load(imageURL).into(viewHolder.imageView);

        viewHolder.setImageID(imageID);
    }

    @Override
    public int getItemCount() {
        return imageIDs.size();
    }
}
