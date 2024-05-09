package com.example.clothingswap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ItemDetailsActivity extends AppCompatActivity {

    private ImageView imageViewItemDetails;
    private TextView textViewItemName;
    private TextView textViewItemTags;
    private Button buttonAskToSwap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        imageViewItemDetails = findViewById(R.id.imageViewItemDetails);
        textViewItemName = findViewById(R.id.textViewItemName);
        textViewItemTags = findViewById(R.id.textViewItemTags);
        buttonAskToSwap = findViewById(R.id.buttonAskToSwap);

        // Retrieve the item details from the intent extras
        Intent intent = getIntent();
        String itemName = intent.getStringExtra("itemName");
        String itemTags = intent.getStringExtra("itemTags");
        String itemImageUri = intent.getStringExtra("itemImageUri");

        // Set the item details in the views
        textViewItemName.setText(itemName);
        textViewItemTags.setText(itemTags);
        Glide.with(this).load(itemImageUri).into(imageViewItemDetails);

        buttonAskToSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the "Ask to Swap" button click
                // You can implement the swap functionality here
                Toast.makeText(ItemDetailsActivity.this, "Ask to Swap clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
