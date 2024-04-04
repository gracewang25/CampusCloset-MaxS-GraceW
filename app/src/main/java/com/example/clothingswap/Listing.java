package com.example.clothingswap;

// Listing.java
public class Listing {
    private String listingId;
    private String itemName;
    private String tags;
    private String imageUri;

    // Default constructor (required for Firebase)
    public Listing() {
    }

    // Constructor with parameters
    public Listing(String listingId, String itemName, String tags, String imageUri) {
        this.listingId = listingId;
        this.itemName = itemName;
        this.tags = tags;
        this.imageUri = imageUri;
    }

    // Getters and setters
    // Make sure to include getters and setters for all fields
    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
