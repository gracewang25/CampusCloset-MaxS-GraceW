package com.example.clothingswap;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ListingViewHolder> {
    private List<Listing> listings;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public ListingAdapter(List<Listing> listings) {
        this.listings = listings;
    }

    @NonNull
    @Override
    public ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_layout, parent, false);
        return new ListingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingViewHolder holder, int position) {
        Listing listing = listings.get(position);
        Glide.with(context)
                .load(listing.getImageUri())
                .into(holder.imageViewGrid);

        holder.trashIcon.setVisibility(listing.isSelected() ? View.VISIBLE : View.GONE);
        holder.imageViewGrid.setColorFilter(listing.isSelected() ? Color.argb(150, 0, 0, 0) : Color.TRANSPARENT);
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    public class ListingViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewGrid;
        ImageView trashIcon;

        public ListingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewGrid = itemView.findViewById(R.id.imageViewGrid);
            trashIcon = itemView.findViewById(R.id.trashIcon);

            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(position);
                    }
                }
            });
        }
    }
}