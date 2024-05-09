package com.example.clothingswap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Profile extends AppCompatActivity {

    private EditText editTextName;

    private List<Listing> listings;

    private TextView textViewEmail;
    private Button buttonEditSave;
    private boolean isEditing = false;
    private ListingAdapter listingAdapter;
    private RecyclerView recyclerViewListings;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;




    BottomNavigationView bottomNav;
    FirebaseAuth auth;
    FirebaseUser user;

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);



        databaseReference = FirebaseDatabase.getInstance().getReference("listings");
        editTextName = findViewById(R.id.editTextName);
        textViewEmail = findViewById(R.id.textViewEmail);
        buttonEditSave = findViewById(R.id.buttonEditSave);
        recyclerViewListings = findViewById(R.id.recyclerViewListings);
        bottomNav = findViewById(R.id.bottom_navigation);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        setupRecyclerView();
        loadUserProfile();
        retrieveUserListings();




        buttonEditSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEditing) {
                    // Allow editing of the EditText
                    editTextName.setEnabled(true);
                    editTextName.requestFocus();
                    buttonEditSave.setText("Save Changes");
                    isEditing = true;
                } else {
                    // Save changes to Firebase or SharedPreferences
                    editTextName.setEnabled(false);
                    buttonEditSave.setText("Edit");
                    isEditing = false;
                    Toast.makeText(Profile.this, "Changes Saved", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (user != null) {
            // If the user is signed in, display their email
            textViewEmail.setText(user.getEmail());
        }

        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                // Navigate to Home
                startActivity(new Intent(Profile.this, MainActivity.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_add) {
                // Navigate to AddItemActivity
                Intent intent = new Intent(Profile.this, CreateListing.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(Profile.this, Profile.class));
                finish();
//                logoutUser();
                return true;
            }
            return false;
        });
        if (ContextCompat.checkSelfPermission(Profile.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Profile.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }



    private void setupRecyclerView() {
        listings = new ArrayList<>();
        listingAdapter = new ListingAdapter(listings);
        recyclerViewListings.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerViewListings.setAdapter(listingAdapter);
    }

    private void retrieveUserListings() {
        if (user != null) {
            String userEmail = user.getEmail();
            DatabaseReference userListingRef = FirebaseDatabase.getInstance().getReference("listings");
            userListingRef.orderByChild("userEmail").equalTo(userEmail).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    listings.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Listing listing = snapshot.getValue(Listing.class);
                        listings.add(listing);
                    }
                    listingAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w("Profile", "loadUserListings:onCancelled", databaseError.toException());
                }
            });
        }
    }



    private void loadUserProfile() {
        // Here you would typically pull this information from Firebase or SharedPreferences
        // For now, let's assume the user is named John Doe with email placeholder
        editTextName.setText("John Doe");
//        editTextEmail.setText("john.doe@example.com");
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

     if(requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted. You can continue accessing the content provider or media files
            } else {
//                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }

    }
}

