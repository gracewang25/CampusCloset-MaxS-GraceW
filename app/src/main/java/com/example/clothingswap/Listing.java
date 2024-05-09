package com.example.clothingswap;

public class Listing {
    private String listingId;
    private String itemName;
    private String tags;
    private String imageUri;
    private String city;
    private String userEmail;
    private boolean isSelected;

    public Listing() {
        // Default constructor required for Firebase
    }

    public Listing(String listingId, String itemName, String tags, String imageUri, String city, String userEmail) {
        this.listingId = listingId;
        this.itemName = itemName;
        this.tags = tags;
        this.imageUri = imageUri;
        this.city = city;
        this.userEmail = userEmail;
        this.isSelected = false;
    }

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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}