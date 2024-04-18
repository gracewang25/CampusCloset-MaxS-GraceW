package com.example.clothingswap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
    private List<Listing> listings;

    public interface OnItemClickListener {
        void onPassButtonClick(int position);
        void onMatchButtonClick(int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public CardAdapter(List<Listing> listings) {
        this.listings = listings;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_layout, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Listing listing = listings.get(position);

        // Load item image using Glide
        Glide.with(holder.itemView.getContext())
                .load(listing.getImageUri())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.itemImageView);

        holder.itemNameTextView.setText(listing.getItemName());
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImageView;
        TextView itemNameTextView;
        Button passButtonCard;
        Button matchButtonCard;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.itemImageView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            passButtonCard = itemView.findViewById(R.id.passButtonCard);
            matchButtonCard = itemView.findViewById(R.id.matchButtonCard);

            passButtonCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onItemClickListener.onPassButtonClick(position);
                        }
                    }
                }
            });

            matchButtonCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onItemClickListener.onMatchButtonClick(position);
                        }
                    }
                }
            });
        }
    }
}