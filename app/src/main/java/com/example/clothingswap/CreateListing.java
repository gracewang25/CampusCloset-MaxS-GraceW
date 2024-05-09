package com.example.clothingswap;
import com.example.clothingswap.Listing;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateListing extends AppCompatActivity {

    EditText editTextItemName, editTextTags;
    Button buttonUpload, buttonSelectImage;
    DatabaseReference databaseReference;
    ImageView imageView;
    Uri selectedImageUri;
    String userCity;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_listing);

        editTextItemName = findViewById(R.id.editTextItemName);
        editTextTags = findViewById(R.id.editTextTags);
        buttonUpload = findViewById(R.id.buttonUpload);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        imageView = findViewById(R.id.imageView);

        databaseReference = FirebaseDatabase.getInstance().getReference("listings");

        TextView textViewCityName = findViewById(R.id.textViewCityName);


        // Retrieve the city passed from MainActivity
        userCity = getIntent().getStringExtra("userCity");
        if (userCity != null && !userCity.isEmpty()) {
            textViewCityName.setText(userCity); // Display the city name
        } else {
            textViewCityName.setText("City not available"); // Default text if city is not provided
        }

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadListing();
            }
        });

        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
        }
    }

    private void uploadListing() {
        // Get the item name and tags from EditText fields
        String itemName = editTextItemName.getText().toString().trim();
        String tags = editTextTags.getText().toString().trim();

        // Validate input fields
        if (itemName.isEmpty() || tags.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Please fill in all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Upload the listing details, image URI, and city to Firebase
        String listingId = databaseReference.push().getKey();
        Listing listing = new Listing(listingId, itemName, tags, selectedImageUri.toString(), userCity); // Include city
        databaseReference.child(listingId).setValue(listing);

        Toast.makeText(this, "Listing uploaded successfully", Toast.LENGTH_SHORT).show();
        finish(); // Close the activity after uploading
    }
}
