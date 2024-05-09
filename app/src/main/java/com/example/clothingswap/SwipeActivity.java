package com.example.clothingswap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.FirebaseApp;

import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SwipeActivity extends AppCompatActivity implements CardAdapter.OnItemClickListener {
    BottomNavigationView bottomNav;
    private RecyclerView recyclerView;
    private CardAdapter cardAdapter;
    private List<Listing> listings;
    private int currentIndex = 0;
    private int totalListings = 0;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe);

        recyclerView = findViewById(R.id.recyclerView);
        bottomNav = findViewById(R.id.bottom_navigation);

        listings = new ArrayList<>();
//        bottomNavigationView.setBackgroundColor(getResources().getColor(R.color.blue));  // Ensure this color is defined in your colors.xml

        databaseReference = FirebaseDatabase.getInstance().getReference("listings");

        //setting the appropriate state for buttons + initializing bottom nav styling
//        bottomNav.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_LABELED);
        bottomNav.setSelectedItemId(R.id.nav_swipe);


        retrieveListings();

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                // Navigate to Home
                startActivity(new Intent(SwipeActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_add) {
                // Navigate to AddItemActivity
                Intent intent = new Intent(SwipeActivity.this, CreateListing.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(SwipeActivity.this, Profile.class));
                finish();
//                logoutUser();
                return true;
            }
            return false;
        });
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
                totalListings = listings.size();
                setupRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    private void setupRecyclerView() {
        cardAdapter = new CardAdapter(listings);
        cardAdapter.setOnItemClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(cardAdapter);
        recyclerView.scrollToPosition(currentIndex);
    }

    private void showEndOfCardsMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No More Items")
                .setMessage("You have gone through all the currently available items.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the OK button click if needed
                    }
                })
                .show();
    }
    @Override
    public void onPassButtonClick(int position) {
        currentIndex++;
        if (currentIndex >= totalListings) {
            showEndOfCardsMessage();
        } else {
            recyclerView.smoothScrollToPosition(currentIndex);
        }
    }

    @Override
    public void onMatchButtonClick(int position) {
        // Get the current listing
        Listing currentListing = listings.get(position);

        // TODO: Implement the logic to handle the match, e.g., send a notification to the item owner

        // Move to the next listing
        currentIndex++;
        if (currentIndex >= totalListings) {
            showEndOfCardsMessage();
        } else {
            recyclerView.smoothScrollToPosition(currentIndex);
        }
    }
}