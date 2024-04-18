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
    private OnItemClickListener listener;


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
        Button passButton;
        Button matchButton;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.itemImageView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            passButton = (Button) itemView.findViewById(R.id.passButtonCard);
            matchButton = (Button) itemView.findViewById(R.id.matchButtonCard);

            passButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onPassButtonClick(getAdapterPosition());
                    }
                }
            });

            matchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onMatchButtonClick(getAdapterPosition());
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onPassButtonClick(int position);
        void onMatchButtonClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}