package com.example.clothingswap;
import com.example.clothingswap.Listing;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
    Button buttonUpload, buttonSelectImage, buttonTakePhoto;
    DatabaseReference databaseReference;
    ImageView imageView;
    Uri selectedImageUri;
    String userCity;

    public static final int CAMERA_ACTION = 1;
    private static final int PICK_IMAGE_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_listing);

        // Initialize components
        editTextItemName = findViewById(R.id.editTextItemName);
        editTextTags = findViewById(R.id.editTextTags);
        buttonUpload = findViewById(R.id.buttonUpload);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        imageView = findViewById(R.id.imageView);
        buttonTakePhoto = findViewById(R.id.buttonTakePhoto);
        TextView textViewCity = findViewById(R.id.textViewCity);  // Reference to the TextView

        databaseReference = FirebaseDatabase.getInstance().getReference("listings");

        // Retrieve the city passed from MainActivity
        userCity = getIntent().getStringExtra("userCity");

        // Check if the city is received properly
        if (userCity != null && !userCity.isEmpty()) {
            textViewCity.setText("This item will be listed in: " + userCity);  // Set full text dynamically
        } else {
            textViewCity.setText("This item will be listed in: City not available");  // Fallback text
        }

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadListing();
            }
        });

        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent, CAMERA_ACTION);

                }else{
                    Toast.makeText(CreateListing.this, "There is no app that supports this action", Toast.LENGTH_SHORT).show();
                }
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
        } else if(requestCode == CAMERA_ACTION && resultCode == RESULT_OK && data != null){
            Bundle bundle = data.getExtras();
            Bitmap finalPhoto =  (Bitmap) bundle.get("");
            imageView.setImageBitmap(finalPhoto);
        }
    }
    @Override
    public void onBackPressed() {
        // Create an Intent to start MainActivity
        super.onBackPressed();
        Intent intent = new Intent(CreateListing.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // This flag ensures all other activities on top are cleared.
        startActivity(intent);
        finish(); // Finish CreateListing to remove it from the stack
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
