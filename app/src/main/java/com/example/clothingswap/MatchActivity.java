package com.example.clothingswap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MatchActivity extends AppCompatActivity {
    private EditText contactInfoEditText;
    private Spinner itemSpinner;
    private Button submitButton;

    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    private DatabaseReference userListingsReference;

    private List<String> userListings;
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        contactInfoEditText = findViewById(R.id.contactInfoEditText);
        itemSpinner = findViewById(R.id.itemSpinner);
        submitButton = findViewById(R.id.submitButton);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("matches");
        userListingsReference = FirebaseDatabase.getInstance().getReference("listings");

        userListings = new ArrayList<>();
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userListings);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemSpinner.setAdapter(spinnerAdapter);

        retrieveUserListings();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMatchDetails();
            }
        });
    }

    private void retrieveUserListings() {
        userListingsReference.orderByChild("userId").equalTo(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userListings.clear();
                        for (DataSnapshot listingSnapshot : dataSnapshot.getChildren()) {
                            Listing listing = listingSnapshot.getValue(Listing.class);
                            userListings.add(listing.getItemName());
                        }
                        spinnerAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                    }
                });
    }

    private void saveMatchDetails() {
        String contactInfo = contactInfoEditText.getText().toString().trim();
        String offeredItem = itemSpinner.getSelectedItem().toString();

        if (contactInfo.isEmpty()) {
            Toast.makeText(this, "Please enter your contact information", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String matchId = databaseReference.push().getKey();

        Match match = new Match(matchId, userId, contactInfo, offeredItem);
        databaseReference.child(matchId).setValue(match);

        Toast.makeText(this, "Match details saved successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}