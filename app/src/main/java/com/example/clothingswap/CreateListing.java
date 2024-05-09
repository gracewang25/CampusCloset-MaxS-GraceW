package com.example.clothingswap;
import com.example.clothingswap.Listing;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
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
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public class CreateListing extends AppCompatActivity {

    private static final String API_KEY = "acc_382bef3758b23c2";
    private static final String API_SECRET = "792e181bda7d9bcf014607680f2c23c3";
    private static final String BASIC_AUTH = "Basic YWNjXzM4MmJlZjM3NThiMjNjMjo3OTJlMTgxYmRhN2Q5YmNmMDE0NjA3NjgwZjJjMjNjMw==";
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.clothingswap.fileprovider";

    private EditText editTextItemName, editTextTags;
    private Button buttonUpload, buttonSelectImage, buttonTakePhoto;
    private DatabaseReference databaseReference;
    private ImageView imageView;
    private Uri selectedImageUri;
    private String userCity;
    private Uri photoUri;

    private static final int CAMERA_ACTION = 1;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 1;



    private ImaggaService imaggaService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_listing);

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.imagga.com/v2/tags/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        imaggaService = retrofit.create(ImaggaService.class);

        // Initialize views and components
        editTextItemName = findViewById(R.id.editTextItemName);
        editTextTags = findViewById(R.id.editTextTags);
        buttonUpload = findViewById(R.id.buttonUpload);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        imageView = findViewById(R.id.imageView);
        TextView textViewCity = findViewById(R.id.textViewCity);

        databaseReference = FirebaseDatabase.getInstance().getReference("listings");

        // Retrieve the city passed from MainActivity
        userCity = getIntent().getStringExtra("userCity");

        // Set the city text
        if (userCity != null && !userCity.isEmpty()) {
            textViewCity.setText("This item will be listed in: " + userCity);
        } else {
            textViewCity.setText("This item will be listed in: City not available");
        }

        buttonUpload.setOnClickListener(v -> uploadListing());


        buttonSelectImage.setOnClickListener(v -> selectImage());
    }


    private File getPhotoFile() {
        // Create a unique file name based on timestamp
        String fileName = "photo_" + System.currentTimeMillis() + ".jpg";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (storageDir != null) {
            return new File(storageDir, fileName);
        } else {
            Toast.makeText(this, "Unable to access storage", Toast.LENGTH_SHORT).show();
            return null;
        }
    }



    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
            uploadImageToImagga(selectedImageUri);
        }else{
                Toast.makeText(this, "Error capturing image.", Toast.LENGTH_SHORT).show();
            }

        }










    private void uploadImageToImagga(Uri imageUri) {
        File file = new File(getPathFromUri(this, imageUri));
        if (!file.exists()) {
            Toast.makeText(this, "File does not exist.", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        String credentialsToEncode = API_KEY + ":" + API_SECRET;
        String basicAuth = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            basicAuth = Base64.getEncoder().encodeToString(credentialsToEncode.getBytes(StandardCharsets.UTF_8));
        }

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(body)
                .build();

        Request request = new Request.Builder()
                .url("https://api.imagga.com/v2/uploads")
                .post(requestBody)
                .addHeader("Authorization", "Basic " + basicAuth)
                .build();

        new OkHttpClient().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    String uploadId = extractUploadId(responseData);
                    fetchTagsFromImagga(uploadId);
                } else {
                    handleErrorResponse(response.code(), response.message());
                }
            }

            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                runOnUiThread(() -> Toast.makeText(CreateListing.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private String extractUploadId(String responseData) {
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            return jsonObject.getJSONObject("result").getString("upload_id");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void fetchTagsFromImagga(String uploadId) {
        String credentialsToEncode = API_KEY + ":" + API_SECRET;
        String basicAuth = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            basicAuth = Base64.getEncoder().encodeToString(credentialsToEncode.getBytes(StandardCharsets.UTF_8));
        }

        String endpointUrl = "https://api.imagga.com/v2/tags";
        String url = endpointUrl + "?image_upload_id=" + uploadId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Basic " + basicAuth)
                .build();

        new OkHttpClient().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    parseAndDisplayTags(responseData);
                } else {
                    handleErrorResponse(response.code(), response.message());
                }
            }

            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                runOnUiThread(() -> Toast.makeText(CreateListing.this, "API request failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void handleErrorResponse(int code, String message) {
        String errorMessage = "Error: " + code + " " + message;
        runOnUiThread(() -> Toast.makeText(CreateListing.this, errorMessage, Toast.LENGTH_LONG).show());
    }

    private void parseAndDisplayTags(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray tagsArray = jsonObject.getJSONObject("result").getJSONArray("tags");
            StringBuilder tagsBuilder = new StringBuilder();
            double confidenceThreshold = 40.0;

            // Define an array of tags to exclude
            String[] excludedTags = {"apparel", "clothing", "garment"};

            for (int i = 0; i < tagsArray.length(); i++) {
                JSONObject tagObject = tagsArray.getJSONObject(i);
                double confidence = tagObject.getDouble("confidence");
                String tag = tagObject.getJSONObject("tag").getString("en");

                // Check if the tag is not in the excludedTags array
                if (confidence > confidenceThreshold && !Arrays.asList(excludedTags).contains(tag)) {
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

    private void handleErrorResponse(Response<?> response) {
        String errorMessage = "Error: " + response.code();
        if (response.errorBody() != null) {
            try {
                errorMessage += " " + response.errorBody().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String finalErrorMessage = errorMessage;
        runOnUiThread(() -> Toast.makeText(CreateListing.this, finalErrorMessage, Toast.LENGTH_LONG).show());
    }

    private String getPathFromUri(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { MediaStore.Images.Media.DATA };
            try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                Log.e("UploadImage", "Failed to get path from URI", e);
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CreateListing.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
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

        // Get the currently logged-in user's email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = currentUser != null ? currentUser.getEmail() : "";

        // Upload the listing details, image URI, city, and user email to Firebase
        String listingId = databaseReference.push().getKey();
        Listing listing = new Listing(listingId, itemName, tags, selectedImageUri.toString(), userCity, userEmail);
        databaseReference.child(listingId).setValue(listing);

        Toast.makeText(this, "Listing uploaded successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(CreateListing.this, MainActivity.class));
        finish();
    }

    // Retrofit interfaces
    public interface ImaggaService {
        // Existing method for uploading images
        @Multipart
        @POST("uploads")
        Call<UploadResponse> uploadImage(@Header("Authorization") String authorization, @Part MultipartBody.Part image);

        // Method to get tags
        @GET("tags")
        Call<TagResponse> getTags(@Header("Authorization") String authorization, @Query("image_upload_id") String uploadId);
    }

    // Retrofit response classes
    public static class UploadResponse {
        private UploadResult result;

        public UploadResult getResult() {
            return result;
        }
    }

    public static class UploadResult {
        private String uploadId;

        public String getUploadId() {
            return uploadId;
        }
    }

    public static class TagResponse {
        private TagResult result;

        public TagResult getResult() {
            return result;
        }
    }

    public static class TagResult {
        private Tag[] tags;

        public Tag[] getTags() {
            return tags;
        }
    }

    public static class Tag {
        private double confidence;
        private TagInfo tag;

        public double getConfidence() {
            return confidence;
        }

        public TagInfo getTag() {
            return tag;
        }
    }

    public static class TagInfo {
        private String en;

        public String getEn() {
            return en;
        }
    }
}
//
//import android.Manifest;
//
//import android.content.Intent;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.database.Cursor;
//import android.net.Uri;
//import android.os.Build;
//import android.provider.MediaStore;
//import android.graphics.Bitmap;
//import android.net.Uri;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.util.Base64;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.annotation.RequiresApi;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.annotations.NotNull;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.DataOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//import java.util.Map;
//
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.HttpUrl;
//import okhttp3.MediaType;
//import okhttp3.MultipartBody;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;
//
//public class CreateListing extends AppCompatActivity {
//
//    private static final String API_KEY = "acc_382bef3758b23c2";
//    private static final String API_SECRET = "792e181bda7d9bcf014607680f2c23c3";
//
//
////    private static final String BASIC_AUTH = "Basic " + Base64.encodeToString((API_KEY + ":" + API_SECRET).getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
//
//    EditText editTextItemName, editTextTags;
//    Button buttonUpload, buttonSelectImage, buttonTakePhoto;
//    DatabaseReference databaseReference;
//    ImageView imageView;
//    Uri selectedImageUri;
//    String userCity;
//
//    public static final int CAMERA_ACTION = 1;
//    private static final int PICK_IMAGE_REQUEST = 1;
//
//    private static final int REQUEST_IMAGE_PERMISSION = 1;
//
//    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
//
//    private void requestImagePermission() {
//        String requiredPermission;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            requiredPermission = Manifest.permission.READ_MEDIA_IMAGES;
//        } else {
//            requiredPermission = Manifest.permission.READ_EXTERNAL_STORAGE;
//        }
//
//        if (ContextCompat.checkSelfPermission(this, requiredPermission) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{requiredPermission}, REQUEST_IMAGE_PERMISSION);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_IMAGE_PERMISSION) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission was granted, handle the camera or file access functionality
//            } else {
//                Toast.makeText(this, "Permission denied to access your External storage", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_create_listing);
//
//        // Request necessary permissions
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//            requestImagePermission();  // Ensure this method is correctly implemented as discussed
//        }
//
//        // Initialize components
//        editTextItemName = findViewById(R.id.editTextItemName);
//        editTextTags = findViewById(R.id.editTextTags);
//        buttonUpload = findViewById(R.id.buttonUpload);
//        buttonSelectImage = findViewById(R.id.buttonSelectImage);
//        imageView = findViewById(R.id.imageView);
//        buttonTakePhoto = findViewById(R.id.buttonTakePhoto);
//        TextView textViewCity = findViewById(R.id.textViewCity);  // Reference to the TextView
//
//        databaseReference = FirebaseDatabase.getInstance().getReference("listings");
//
//        // Retrieve the city passed from MainActivity
//        userCity = getIntent().getStringExtra("userCity");
//
//        // Check if the city is received properly
//        if (userCity != null && !userCity.isEmpty()) {
//            textViewCity.setText("This item will be listed in: " + userCity);  // Set full text dynamically
//        } else {
//            textViewCity.setText("This item will be listed in: City not available");  // Fallback text
//        }
//
//        buttonUpload.setOnClickListener(v -> uploadListing());
//
//        buttonTakePhoto.setOnClickListener(v -> {
//            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            if (intent.resolveActivity(getPackageManager()) != null) {
//                startActivityForResult(intent, CAMERA_ACTION);
//            } else {
//                Toast.makeText(CreateListing.this, "There is no app that supports this action", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        buttonSelectImage.setOnClickListener(v -> selectImage());
//    }
//
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_create_listing);
////
////        // Initialize components
////        editTextItemName = findViewById(R.id.editTextItemName);
////        editTextTags = findViewById(R.id.editTextTags);
////        buttonUpload = findViewById(R.id.buttonUpload);
////        buttonSelectImage = findViewById(R.id.buttonSelectImage);
////        imageView = findViewById(R.id.imageView);
////        buttonTakePhoto = findViewById(R.id.buttonTakePhoto);
////        TextView textViewCity = findViewById(R.id.textViewCity);  // Reference to the TextView
////
////        databaseReference = FirebaseDatabase.getInstance().getReference("listings");
////
////        // Retrieve the city passed from MainActivity
////        userCity = getIntent().getStringExtra("userCity");
////
////        // Check if the city is received properly
////        if (userCity != null && !userCity.isEmpty()) {
////            textViewCity.setText("This item will be listed in: " + userCity);  // Set full text dynamically
////        } else {
////            textViewCity.setText("This item will be listed in: City not available");  // Fallback text
////        }
////
////        buttonUpload.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                uploadListing();
////            }
////        });
////
////        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
////                if(intent.resolveActivity(getPackageManager()) != null){
////                    startActivityForResult(intent, CAMERA_ACTION);
////
////                }else{
////                    Toast.makeText(CreateListing.this, "There is no app that supports this action", Toast.LENGTH_SHORT).show();
////                }
////            }
////        });
////
////
////        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                selectImage();
////            }
////        });
////    }
//
//    private void selectImage() {
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType("image/*");
//        startActivityForResult(intent, PICK_IMAGE_REQUEST);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if ((requestCode == PICK_IMAGE_REQUEST || requestCode == CAMERA_ACTION) && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            selectedImageUri = data.getData();
//            imageView.setImageURI(selectedImageUri);
//            uploadImageToImagga(selectedImageUri);  // Trigger the upload and tagging process
//        } else {
//            Toast.makeText(this, "Failed to get the image.", Toast.LENGTH_SHORT).show();
//        }
//    }
//
////    @Override
////    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
////        super.onActivityResult(requestCode, resultCode, data);
////
////        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
////            selectedImageUri = data.getData();
////            imageView.setImageURI(selectedImageUri);
////        } else if (requestCode == CAMERA_ACTION && resultCode == RESULT_OK && data != null) {
////            // Get the photo as a Bitmap
////            selectedImageUri = data.getData();
////            imageView.setImageURI(selectedImageUri);
////        }
////    }
//
//    private static final String BASIC_AUTH = "Basic " + "YWNjXzM4MmJlZjM3NThiMjNjMjo3OTJlMTgxYmRhN2Q5YmNmMDE0NjA3NjgwZjJjMjNjMw==";
//
//    private void uploadImageToImagga(Uri imageUri) {
//        new Thread(() -> {
//            try {
//                File file = new File(getPathFromUri(this, imageUri));
//                if (!file.exists()) {
//                    runOnUiThread(() -> Toast.makeText(CreateListing.this, "File does not exist.", Toast.LENGTH_SHORT).show());
//                    return;
//                }
//
//                FileInputStream fileInputStream = new FileInputStream(file);
//                byte[] bytesArray = new byte[(int) file.length()];
//                fileInputStream.read(bytesArray);
//                fileInputStream.close();
//
//                URL url = new URL("https://api.imagga.com/v2/uploads");
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("POST");
//                connection.setDoOutput(true);
//                connection.setRequestProperty("Authorization", BASIC_AUTH);
//                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
//
//                DataOutputStream request = new DataOutputStream(connection.getOutputStream());
//                request.writeBytes("------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n");
//                request.writeBytes("Content-Disposition: form-data; name=\"image\";filename=\"" +
//                        file.getName() + "\"\r\nContent-Type: image/jpeg\r\n\r\n");
//                request.write(bytesArray);
//                request.writeBytes("\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--\r\n");
//                request.flush();
//                request.close();
//
//                int responseCode = connection.getResponseCode();
//                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    String response = convertStreamToString(connection.getInputStream());
//                    String uploadId = extractUploadId(response);
//                    fetchTagsFromImagga(uploadId);
//                } else {
//                    String errorResponse = convertStreamToString(connection.getErrorStream());
//                    Log.e("UploadImage", "Failed response from server: " + responseCode + " " + errorResponse);
//                    runOnUiThread(() -> Toast.makeText(CreateListing.this, "Error: " + responseCode + " " + errorResponse, Toast.LENGTH_LONG).show());
//                }
//            } catch (Exception e) {
//                Log.e("UploadImage", "Exception in making HTTP request: " + e.getMessage(), e);
//                runOnUiThread(() -> Toast.makeText(CreateListing.this, "Exception in making HTTP request: " + e.getMessage(), Toast.LENGTH_LONG).show());
//            }
//        }).start();
//    }
//
//    private String convertStreamToString(InputStream is) throws IOException {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//        StringBuilder sb = new StringBuilder();
//        String line;
//        while ((line = reader.readLine()) != null) {
//            sb.append(line).append('\n');
//        }
//        return sb.toString();
//    }
//
//    private String extractUploadId(String jsonResponse) throws JSONException {
//        JSONObject jsonObject = new JSONObject(jsonResponse);
//        return jsonObject.getJSONObject("result").getString("upload_id");
//    }
//
//    private void fetchTagsFromImagga(String uploadId) {
//        String credentialsToEncode = "acc_your_account_id:your_api_key";
//        String basicAuth = android.util.Base64.encodeToString(credentialsToEncode.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
//
//        HttpUrl url = HttpUrl.parse("https://api.imagga.com/v2/tags").newBuilder()
//                .addQueryParameter("image_upload_id", uploadId)
//                .build();
//
//        Request request = new Request.Builder()
//                .url(url)
//                .get()
//                .addHeader("Authorization", "Basic " + basicAuth)
//                .build();
//
//        new OkHttpClient().newCall(request).enqueue(new Callback() {
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                if (response.isSuccessful() && response.body() != null) {
//                    String responseData = response.body().string();
//                    parseAndDisplayTags(responseData);
//                } else {
//                    handleErrorResponse(response);
//                }
//            }
//
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                runOnUiThread(() -> Toast.makeText(CreateListing.this, "API request failed 2: " + e.getMessage(), Toast.LENGTH_LONG).show());
//            }
//        });
//    }
//
//    private void parseAndDisplayTags(String jsonResponse) {
//        try {
//            JSONObject jsonObject = new JSONObject(jsonResponse);
//            JSONArray tagsArray = jsonObject.getJSONObject("result").getJSONArray("tags");
//            StringBuilder tagsBuilder = new StringBuilder();
//            double confidenceThreshold = 40.0; // Set the confidence threshold
//
//            for (int i = 0; i < tagsArray.length(); i++) {
//                JSONObject tagObject = tagsArray.getJSONObject(i);
//                double confidence = tagObject.getDouble("confidence");
//                if (confidence > confidenceThreshold) {
//                    String tag = tagObject.getJSONObject("tag").getString("en");
//                    tagsBuilder.append(tag).append(", ");
//                }
//            }
//
//            final String tagsResult = tagsBuilder.length() > 2 ? tagsBuilder.substring(0, tagsBuilder.length() - 2) : "";
//            runOnUiThread(() -> editTextTags.setText(tagsResult));
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//            runOnUiThread(() -> Toast.makeText(CreateListing.this, "Error parsing tags: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//        }
//    }
//
//
//
//    private void handleErrorResponse(Response response) throws IOException {
//        String responseBody = response.body() != null ? response.body().string() : "No response body";
//        Log.e("UploadImage", "Failed response from server: " + response.code() + " " + responseBody);
//        runOnUiThread(() -> Toast.makeText(CreateListing.this, "Error: " + response.code() + " " + responseBody, Toast.LENGTH_LONG).show());
//    }
//
//
//
////    private String getPathFromUri(Context context, Uri uri) {
////        String result = null;
////        if (uri.getScheme().equals("content")) {
////            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
////            try {
////                if (cursor != null && cursor.moveToFirst()) {
////                    int idx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
////                    result = cursor.getString(idx);
////                }
////            } finally {
////                if (cursor != null) {
////                    cursor.close();
////                }
////            }
////        } else if (uri.getScheme().equals("file")) {
////            result = uri.getPath();
////        }
////        return result;
////    }
//
//    private void logConnectionDetails(HttpURLConnection connection) {
//        Map<String, List<String>> headers = connection.getRequestProperties();
//        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
//            Log.d("UploadImage", "Header field: " + entry.getKey() + " - " + entry.getValue());
//        }
//        Log.d("UploadImage", "Request Method: " + connection.getRequestMethod());
//        Log.d("UploadImage", "Request URL: " + connection.getURL().toString());
//    }
//
//    private String getPathFromUri(Context context, Uri uri) {
//        if ("content".equalsIgnoreCase(uri.getScheme())) {
//            String[] projection = { MediaStore.Images.Media.DATA };
//            try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
//                if (cursor != null && cursor.moveToFirst()) {
//                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                    return cursor.getString(column_index);
//                }
//            } catch (Exception e) {
//                Log.e("UploadImage", "Failed to get path from URI", e);
//            }
//        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
//            return uri.getPath();
//        }
//        return null;
//    }
//
//
//
//    @Override
//    public void onBackPressed() {
//        // Create an Intent to start MainActivity
//        super.onBackPressed();
//        Intent intent = new Intent(CreateListing.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // This flag ensures all other activities on top are cleared.
//        startActivity(intent);
//        finish(); // Finish CreateListing to remove it from the stack
//    }
//
//
//
//    private void uploadListing() {
//        // Get the item name and tags from EditText fields
//        String itemName = editTextItemName.getText().toString().trim();
//        String tags = editTextTags.getText().toString().trim();
//
//        // Validate input fields
//        if (itemName.isEmpty() || tags.isEmpty() || selectedImageUri == null) {
//            Toast.makeText(this, "Please fill in all fields and select an image", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Upload the listing details, image URI, and city to Firebase
//        String listingId = databaseReference.push().getKey();
//        Listing listing = new Listing(listingId, itemName, tags, selectedImageUri.toString(), userCity); // Include city
//        databaseReference.child(listingId).setValue(listing);
//
//        Toast.makeText(this, "Listing uploaded successfully", Toast.LENGTH_SHORT).show();
//        finish(); // Close the activity after uploading
//    }
//}
