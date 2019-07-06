package com.example.nearby_chat.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.nearby_chat.R;
import com.example.nearby_chat.models.UserProfile;
import com.example.nearby_chat.utils.DatabaseUtils;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static com.example.nearby_chat.activities.ChatActivity.PARTNER_USER_PROFILE;
import static com.example.nearby_chat.constants.Constant.FIREBASE_STORAGE_REFERENCE;
import static com.example.nearby_chat.constants.Constant.NEARBY_CHAT;


public class VisitProfileActivity extends AppCompatActivity {


    UserProfile conversationPartner;
    FirebaseStorage firebaseStorage;
    ProgressBar imageProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_profile);

        conversationPartner = (UserProfile) getIntent().getSerializableExtra(PARTNER_USER_PROFILE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarVisitProfile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(conversationPartner.getUserName());

        TextView namaView = (TextView) findViewById(R.id.namaView);
        TextView teamView = (TextView) findViewById(R.id.teamView);

        TextView anggota1View = (TextView) findViewById(R.id.anggota1View);
        TextView anggota2View = (TextView) findViewById(R.id.anggota2View);
        TextView anggota3View = (TextView) findViewById(R.id.anggota3View);
        TextView anggota4View = (TextView) findViewById(R.id.anggota4View);
        TextView anggota5View = (TextView) findViewById(R.id.anggota5View);
        TextView anggota6View = (TextView) findViewById(R.id.anggota6View);


//        if (conversationPartner.getAvatar() != null) {
//            profileImage.setImageBitmap(conversationPartner.getAvatar());
//        }else {
//            Log.i(NEARBY_CHAT, "Error while loading the online sharedPreferences");
//        }

        // profileImage.setImageBitmap(conversationPartner.getAvatar());
        namaView.setText(conversationPartner.getUserName());
        teamView.setText(conversationPartner.getBio());

        anggota1View.setText(conversationPartner.getAnggota1());
        anggota2View.setText(conversationPartner.getAnggota2());
        anggota3View.setText(conversationPartner.getAnggota3());
        anggota4View.setText(conversationPartner.getAnggota4());
        anggota5View.setText(conversationPartner.getAnggota5());
        anggota6View.setText(conversationPartner.getAnggota6());

        firebaseStorage = FirebaseStorage.getInstance(FIREBASE_STORAGE_REFERENCE);

        loadProfileImage();

    }

    @NonNull
    private StorageReference getStorageReference() {
        return firebaseStorage.getReference("profile/" + conversationPartner.getId() + ".jpeg");
    }

    private void loadProfileImage() {
        ImageView profileImage = (ImageView) findViewById(R.id.profileImage);
        imageProgressBar = (ProgressBar) findViewById(R.id.image_spinner);
        StorageReference reference = getStorageReference();

        final long ONE_MEGABYTE = 1024 * 1024;
        reference.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            // Data for "profile" is returns, use this as needed
            Bitmap avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            conversationPartner.setAvatar(avatar);
            profileImage.setImageBitmap(avatar);
            imageProgressBar.setVisibility(View.GONE);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Log.w(NEARBY_CHAT, "loadProfileImage: ", exception);
        });

    }
}