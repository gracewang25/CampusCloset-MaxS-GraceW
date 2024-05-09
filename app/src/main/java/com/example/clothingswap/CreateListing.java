package com.example.clothingswap;
import com.example.clothingswap.Listing;


import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateListing extends AppCompatActivity {

    EditText editTextItemName, editTextTags;
    Button buttonUpload, buttonSelectImage, buttonTakePhoto;
    DatabaseReference databaseReference;
    ImageView imageView;
    Uri selectedImageUri;
    String userCity;

    public static final int CAMERA_ACTION = 1;
    private static final int PICK_IMAGE_REQUEST = 1;

    private static final int REQUEST_IMAGE_PERMISSION = 1;

    private void requestImagePermission() {
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION}, REQUEST_IMAGE_PERMISSION);
            } else {
                // Permission already granted
                selectImage();
            }
        } else {
            // On older Android versions, continue using READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION}, REQUEST_IMAGE_PERMISSION);
            } else {
                // Permission already granted
                selectImage();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_IMAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Permission is required to access images", Toast.LENGTH_SHORT).show();
            }
        }
    }


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

        if ((requestCode == PICK_IMAGE_REQUEST || requestCode == CAMERA_ACTION) && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
            uploadImageToImagga(selectedImageUri);  // Trigger the upload and tagging process
        } else {
            Toast.makeText(this, "Failed to get the image.", Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            selectedImageUri = data.getData();
//            imageView.setImageURI(selectedImageUri);
//        } else if (requestCode == CAMERA_ACTION && resultCode == RESULT_OK && data != null) {
//            // Get the photo as a Bitmap
//            selectedImageUri = data.getData();
//            imageView.setImageURI(selectedImageUri);
//        }
//    }

    private void uploadImageToImagga(Uri imageUri) {
        File file = new File(getPathFromUri(this, imageUri)); // Convert Uri to file path
        String credentials = "acc_your_account_id:your_api_key";
        String basicAuth = Base64.encodeToString(credentials.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("image/jpeg");
        RequestBody fileBody = RequestBody.create(mediaType, file);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url("https://api.imagga.com/v2/uploads")
                .post(requestBody)
                .addHeader("Authorization", "Basic " + basicAuth)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseString = response.body().string();
                    String uploadId = extractUploadId(responseString); // Implement this method to parse JSON and extract upload_id
                    fetchTagsFromImagga(uploadId);
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(CreateListing.this, "API request failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void fetchTagsFromImagga(String uploadId) {
        String credentialsToEncode = "acc_your_account_id:your_api_key";
        String basicAuth = android.util.Base64.encodeToString(credentialsToEncode.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

        HttpUrl url = HttpUrl.parse("https://api.imagga.com/v2/tags").newBuilder()
                .addQueryParameter("image_upload_id", uploadId)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Basic " + basicAuth)
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    parseAndDisplayTags(responseData);
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> Toast.makeText(CreateListing.this, "API request failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void parseAndDisplayTags(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray tagsArray = jsonObject.getJSONObject("result").getJSONArray("tags");
            StringBuilder tagsBuilder = new StringBuilder();
            double confidenceThreshold = 40.0; // Set the confidence threshold

            for (int i = 0; i < tagsArray.length(); i++) {
                JSONObject tagObject = tagsArray.getJSONObject(i);
                double confidence = tagObject.getDouble("confidence");
                if (confidence > confidenceThreshold) {
                    String tag = tagObject.getJSONObject("tag").getString("en");
                    tagsBuilder.append(tag).append(", ");
                }
            }

            final String tagsResult = tagsBuilder.length() > 2 ? tagsBuilder.substring(0, tagsBuilder.length() - 2) : "";
            runOnUiThread(() -> editTextTags.setText(tagsResult));

        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(CreateListing.this, "Error parsing tags: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }



    private void handleErrorResponse(Response response) throws IOException {
        String errorMessage = response.body() != null ? response.body().string() : "Unknown error";
        runOnUiThread(() -> Toast.makeText(CreateListing.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show());
    }

    private String getPathFromUri(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    result = cursor.getString(idx);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (uri.getScheme().equals("file")) {
            result = uri.getPath();
        }
        return result;
    }

    private String extractUploadId(String jsonResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        return jsonObject.getJSONObject("result").getString("upload_id");
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
