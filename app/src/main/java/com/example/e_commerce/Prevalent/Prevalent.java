package com.example.e_commerce.Prevalent;

import com.example.e_commerce.Model.Users;

/***
 * Working on the forget password and remember me features
 */
public class Prevalent {
    public static Users currentOnlineUser;

    // for the remember me
    public static final String userPhoneKey = "UserPhone";
    public static final String userPasswordKey = "UserPassword";

    // getters and setters


    public static Users getCurrentOnlineUser() {
        return currentOnlineUser;
    }

    public static void setCurrentOnlineUser(Users currentOnlineUser) {
        Prevalent.currentOnlineUser = currentOnlineUser;
    }
}
