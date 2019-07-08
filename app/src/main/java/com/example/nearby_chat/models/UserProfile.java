package com.example.nearby_chat.models;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class UserProfile implements Serializable {

    private String id;
    private String userName;
    private String bio;
    private String usia;
    private String anggota1;
    private String anggota2;
    private String anggota3;
    private String anggota4;
    private String anggota5;
    private String anggota6;

    @Exclude
    private Bitmap avatar;

    // required empty constructor for firebase loading
    public UserProfile() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserProfile that = (UserProfile) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getRataUsia() {
        return usia;
    }

    public void setRataUsia(String usia) {
        this.usia = usia;
    }

    public String getAnggota1() {
        return anggota1;
    }

    public void setAnggota1(String anggota1) {
        this.anggota1 = anggota1;
    }

    public String getAnggota2() {
        return anggota2;
    }

    public void setAnggota2(String anggota2) {
        this.anggota2 = anggota2;
    }

    public String getAnggota3() {
        return anggota3;
    }

    public void setAnggota3(String anggota3) {
        this.anggota3 = anggota3;
    }

    public String getAnggota4() {
        return anggota4;
    }

    public void setAnggota4(String anggota4) {
        this.anggota4 = anggota4;
    }

    public String getAnggota5() {
        return anggota5;
    }

    public void setAnggota5(String anggota5) {
        this.anggota5 = anggota5;
    }

    public String getAnggota6() {
        return anggota6;
    }

    public void setAnggota6(String anggota6) {
        this.anggota6 = anggota6;
    }


    @Exclude
    public Bitmap getAvatar() {
        return avatar;
    }

    @Exclude
    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }
}
