package com.example.clothingswap;

import androidx.annotation.NonNull;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseUser user;
    BottomNavigationView bottomNav;
    RecyclerView photoGrid;
    DatabaseReference databaseReference;

    private ListingAdapter listingAdapter;
    private List<Listing> listings;

    private FusedLocationProviderClient fusedLocationClient;
    private final int REQUEST_LOCATION_PERMISSION = 1;

    private String userCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initial setup
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        bottomNav = findViewById(R.id.bottom_navigation);
        photoGrid = findViewById(R.id.photoGrid);
        photoGrid.setLayoutManager(new GridLayoutManager(this, 3));
        listings = new ArrayList<>();
        listingAdapter = new ListingAdapter(listings);
        photoGrid.setAdapter(listingAdapter);
        databaseReference = FirebaseDatabase.getInstance().getReference("listings");
        retrieveListings();

        // Navigation setup using if-else
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_swipe) {
                startActivity(new Intent(MainActivity.this, SwipeActivity.class));
                return true;
            } else if (id == R.id.nav_add) {
                // Check if the city is known before starting CreateListing
                if (userCity != null && !userCity.isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, CreateListing.class);
                    intent.putExtra("userCity", userCity); // Pass the city as an extra
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "City not determined yet. Please wait or try again.", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (id == R.id.nav_logout) {
                logoutUser();
                return true;
            }
            return false;
        });

        // Check user authentication and redirect if necessary
        if (user == null) {
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        } else {
            getLocation(); // Call this here to ensure it's setup after auth check
            // Optional: If you wish to start SwipeActivity and finish MainActivity
            // Uncomment the following two lines
            // startActivity(new Intent(getApplicationContext(), SwipeActivity.class));
            // finish();
        }
    }

    private void retrieveListings() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listings.clear();
                for (DataSnapshot listingSnapshot : dataSnapshot.getChildren()) {
                    Listing listing = listingSnapshot.getValue(Listing.class);
                    listings.add(listing);
                }
                listingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur during data retrieval
            }
        });
    }

    private void logoutUser() {
        auth.signOut();
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        finish();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    userCity = addresses.get(0).getLocality();  // Store the city in the member variable
                                    Toast.makeText(getApplicationContext(), "You are in " + userCity, Toast.LENGTH_LONG).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}