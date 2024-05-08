package com.example.clothingswap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseUser user;
    BottomNavigationView bottomNav;
    RecyclerView photoGrid;
    DatabaseReference databaseReference;

    private ListingAdapter listingAdapter;
    private List<Listing> listings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            // Start the SwipeActivity instead of the explore/grid page
            Intent intent = new Intent(getApplicationContext(), SwipeActivity.class);
            startActivity(intent);
            finish();
        }

//        if (user == null) {
//            Intent intent = new Intent(getApplicationContext(), Login.class);
//            startActivity(intent);
//            finish();
//        } else {
//            TextView textView = findViewById(R.id.user_details);
//            textView.setText(user.getEmail());
//        }

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_swipe) {
                // Navigate to SwipeActivity
                Intent intent = new Intent(MainActivity.this, SwipeActivity.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_add) {
                // Navigate to AddItemActivity
                Intent intent = new Intent(MainActivity.this, CreateListing.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_logout) {
                logoutUser();
                return true;
            }
            return false;
        });

        photoGrid = findViewById(R.id.photoGrid);
        photoGrid.setLayoutManager(new GridLayoutManager(this, 3));

        listings = new ArrayList<>();
        listingAdapter = new ListingAdapter(listings);
        photoGrid.setAdapter(listingAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("listings");
        retrieveListings();
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
}