package com.example.e_commerce.Model;

public class Users {
    private String password, phone, username;
    // Default constructor
    public Users() {

    }
    // Parametarized constructor

    public Users(String password, String phone, String username) {
        this.password = password;
        this.phone = phone;
        this.username = username;
    }

    // Getters and Setters

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
