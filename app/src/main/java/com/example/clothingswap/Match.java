package com.example.clothingswap;

public class Match {
    private String matchId;
    private String userId;
    private String contactInfo;

    private String offeredItem;

    public Match() {
        // Default constructor required for Firebase
    }

    public Match(String matchId, String userId, String contactInfo, String offeredItem) {
        this.matchId = matchId;
        this.userId = userId;
        this.contactInfo = contactInfo;
        this.offeredItem = offeredItem;
    }
}