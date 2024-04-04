package com.example.clothingswap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;

    BottomNavigationView bottomNav;
    RecyclerView photoGrid;

    DatabaseReference databaseReference; // Firebase Database reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("listings");

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            TextView textView = findViewById(R.id.user_details);
            textView.setText(user.getEmail());
        }

        // Setup for logout button - now using the logoutUser() method

        // Setup BottomNavigationView with the new method for logout
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_add) {
                // Navigate to AddItemActivity
                Intent intent = new Intent(MainActivity.this, CreateListing.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_logout) {
                logoutUser(); // Use the logout method here as well
                return true;
            }
            return false;
        });

        // Setup RecyclerView
        photoGrid = findViewById(R.id.photoGrid);
        photoGrid.setLayoutManager(new GridLayoutManager(this, 3)); // 3 columns in the grid
        // TODO: Set the adapter for the RecyclerView with your data
    }

    // The centralized logout method
    private void logoutUser() {
        auth.signOut(); // Firebase sign out
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        finish(); // Close the current activity
    }
}