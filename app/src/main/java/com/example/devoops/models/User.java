package com.example.devoops.models;

public class User {
    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    private UserRole role;

    public User(String userId, String name, String email, String phoneNumber, UserRole role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public User(){}

    public String getUserId(){
        return userId;
    }
    public String getName(){
        return name;
    }
    public String getEmail(){
        return email;
    }
    public String getPhoneNumber(){
        return phoneNumber;
    }
    public UserRole getRole(){
        return role;
    }
}
