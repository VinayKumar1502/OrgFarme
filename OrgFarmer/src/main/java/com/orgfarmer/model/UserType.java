package com.orgfarmer.model;

public enum UserType {
    ADMIN("Admin"),
    SELLER("Seller/Farmer"),
    CUSTOMER("Customer");
    
    private String displayName;
    
    UserType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}