package com.example.nearby_chat.utils;


public class DataValidator {

    public static boolean isUsernameValid(String username) {
        return username.length() < 25;
    }

    public static boolean isBioValid(String bio) {
        return bio.length() < 40;
    }

    public static boolean isAnggotaValid(String anggota) {
        return anggota.length() < 25;
    }

    public static boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPasswordValid(String password) {
        return password.length() > 6;
    }

}
