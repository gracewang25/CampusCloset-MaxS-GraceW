package com.example.clothingswap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.time.Instant;
import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {
    private List<Listing> listings;

    // Constructor to initialize the list of listings
    public ListingAdapter(List<Listing> listings) {
        this.listings = listings;
    }

    // ViewHolder class to hold the views for each grid item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewGrid;

        public ViewHolder(View itemView) {
            super(itemView);
            imageViewGrid = itemView.findViewById(R.id.imageViewGrid);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Listing listing = listings.get(position);

        // Load image into ImageView using Glide
        Glide.with(holder.itemView.getContext())
                .load(listing.getImageUri())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.imageViewGrid);

        // Add click listeners or other view updates as needed
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }
}
