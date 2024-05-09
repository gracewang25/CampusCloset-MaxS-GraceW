package com.example.clothingswap;

// Listing.java
public class Listing {
    private String listingId;
    private String itemName;
    private String tags;
    private String imageUri;
    private String city; // Field for storing city

    // Default constructor (required for Firebase)
    public Listing() {
    }

    // Constructor with parameters
    public Listing(String listingId, String itemName, String tags, String imageUri, String city) { // Include city in parameters
        this.listingId = listingId;
        this.itemName = itemName;
        this.tags = tags;
        this.imageUri = imageUri;
        this.city = city; // Initialize city
    }

    // Getters and setters
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

    public String getCity() { // Getter for city
        return city;
    }

    public void setCity(String city) { // Setter for city
        this.city = city;
    }
}