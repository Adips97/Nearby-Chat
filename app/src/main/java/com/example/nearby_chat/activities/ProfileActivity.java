package com.example.nearby_chat.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;

import com.example.nearby_chat.R;
import com.example.nearby_chat.constants.Constant;
import com.example.nearby_chat.models.UserProfile;
import com.example.nearby_chat.utils.DataValidator;
import com.example.nearby_chat.utils.DatabaseUtils;
import com.example.nearby_chat.utils.NetworkUtils;
import com.example.nearby_chat.utils.PermissionUtils;

public class ProfileActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final String[] READ_STORAGE_PERMISSION =
            {Manifest.permission.READ_EXTERNAL_STORAGE};
    private final Activity activity = this;
    private AutoCompleteTextView userNameView;
    private EditText userBioView;
    private EditText userAnggota1View,userAnggota2View,userAnggota3View,userAnggota4View,userAnggota5View,userAnggota6View,userrataUsiaView;
    private Button updateProfileButton;
    private ImageView profileImage;
    private ImageView profileImageIcon;
    private ProgressBar imageProgressBar;
    private ProgressBar progressBar;
    private UserProfile userProfile;

    private final ValueEventListener userProfileListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(Constant.NEARBY_CHAT, "onDataChange: dataSnapshot = [" + dataSnapshot + "]");
            UserProfile userProfileLocal = dataSnapshot.getValue(UserProfile.class);

            if (userProfileLocal != null) {
                userProfile = userProfileLocal;
                Log.w(Constant.NEARBY_CHAT, "Online profile loaded for id " + ProfileActivity.this.userProfile.getId());
                initProfileView();
            } else {
                Log.w(Constant.NEARBY_CHAT, "Error while loading the online profile");
            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(Constant.NEARBY_CHAT, "Error database");
        }
    };


    private String userId;
    private DatabaseReference userProfileDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        userProfile = new UserProfile();
        userId = DatabaseUtils.getCurrentUUID();
        userProfileDatabaseReference = DatabaseUtils.getUserProfileReferenceById(userId);

        userNameView = (AutoCompleteTextView) findViewById(R.id.username);
        userNameView.requestFocus();
        userBioView = (EditText) findViewById(R.id.bio);
        userrataUsiaView = (EditText) findViewById(R.id.rataUsia);
        userAnggota1View = (EditText) findViewById(R.id.anggota1);
        userAnggota2View = (EditText) findViewById(R.id.anggota2);
        userAnggota3View = (EditText) findViewById(R.id.anggota3);
        userAnggota4View = (EditText) findViewById(R.id.anggota4);
        userAnggota5View = (EditText) findViewById(R.id.anggota5);
        userAnggota6View = (EditText) findViewById(R.id.anggota6);


        progressBar = (ProgressBar) findViewById(R.id.profile_spinner);
        imageProgressBar = (ProgressBar) findViewById(R.id.image_spinner);

        updateProfileButton = (Button) findViewById(R.id.update_profile_button);
        updateProfileButton.setOnClickListener(v -> {
            saveProfileData();
            Toast.makeText(activity, getString(R.string.profile_updated_text), Toast.LENGTH_LONG).show();
        });

        profileImage = (ImageView) findViewById(R.id.profile_image);
        profileImage.setDrawingCacheEnabled(true);
        profileImage.buildDrawingCache();

        profileImageIcon = (ImageView) findViewById(R.id.profile_image_icon);

        profileImageIcon.setOnClickListener(v -> {
            Log.d(Constant.NEARBY_CHAT, "profileImage setOnClickListener: ");
            boolean isAndroidVersionNew = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
            if (isAndroidVersionNew) {
                if (!PermissionUtils.hasReadPermission(this)) {
                    ActivityCompat.requestPermissions(activity, READ_STORAGE_PERMISSION
                            , 1);
                }
            }
            if (!isAndroidVersionNew || PermissionUtils.hasReadPermission(this)) {
                pickProfileImage();
            }
        });

        loadProfileData();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickProfileImage();

                } else {
                    Toast.makeText(activity, R.string.profile_image_permission, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    /**
     * Sets a new profile picture for the user
     */
    private void pickProfileImage() {
        Log.d(Constant.NEARBY_CHAT, "pickProfileImage: ");
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = null;
            if (selectedImage != null) {
                cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
            }
            String picturePath = "";
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                picturePath = cursor.getString(columnIndex);
                cursor.close();
            }

            profileImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            imageProgressBar.setVisibility(View.GONE);
            profileImage.setVisibility(View.VISIBLE);

        }
    }

    /**
     * Loads the user profile and fills the image and user information
     */
    private void loadProfileData() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (NetworkUtils.isAvailable(connectivityManager)) {
            //load online information
            loadProfileOnline();
        } else {
            initProfileView();
        }

        progressBar.setVisibility(View.GONE);
    }


    private void initProfileView() {
        userNameView.setText(userProfile.getUserName());
        userBioView.setText(userProfile.getBio());
        userrataUsiaView.setText(userProfile.getRataUsia());
        userAnggota1View.setText(userProfile.getAnggota1());
        userAnggota2View.setText(userProfile.getAnggota2());
        userAnggota3View.setText(userProfile.getAnggota3());
        userAnggota4View.setText(userProfile.getAnggota4());
        userAnggota5View.setText(userProfile.getAnggota5());
        userAnggota6View.setText(userProfile.getAnggota6());
        if (userProfile.getAvatar() != null) {
            profileImage.setImageBitmap(userProfile.getAvatar());
        }
    }

    private void saveProfileData() {

        progressBar.setVisibility(View.VISIBLE);

        // Reset errors.
        userNameView.setError(null);
        userBioView.setError(null);
        userrataUsiaView.setError(null);
        userAnggota1View.setError(null);
        userAnggota2View.setError(null);
        userAnggota3View.setError(null);
        userAnggota4View.setError(null);
        userAnggota5View.setError(null);
        userAnggota6View.setError(null);

        // Store values at the time of the profile update attempt.
        String userName = userNameView.getText().toString();
        String userBio = userBioView.getText().toString();
        String userUsia = userrataUsiaView.getText().toString();
        String userAnggota1 = userAnggota1View.getText().toString();
        String userAnggota2 = userAnggota2View.getText().toString();
        String userAnggota3 = userAnggota3View.getText().toString();
        String userAnggota4 = userAnggota4View.getText().toString();
        String userAnggota5 = userAnggota5View.getText().toString();
        String userAnggota6 = userAnggota6View.getText().toString();


        View errorView = null;

        // Check for a valid username.
        if (TextUtils.isEmpty(userName)) {
            userNameView.setError(getString(R.string.error_field_required));
            errorView = userNameView;
        } else if (!DataValidator.isUsernameValid(userName)) {
            userNameView.setError(getString(R.string.error_invalid_username));
            errorView = userNameView;
        }

        // Check for a valid bio, if the user entered one.
        if (!TextUtils.isEmpty(userBio) && !DataValidator.isBioValid(userBio)) {
            userBioView.setError(getString(R.string.error_invalid_bio));
            errorView = userBioView;
        }


        // Check for a valid members
        if (TextUtils.isEmpty(userUsia)) {
            userrataUsiaView.setError(getString(R.string.error_field_required));
            errorView = userrataUsiaView;
        } else if (!DataValidator.isAnggotaValid(userAnggota1)) {
            userAnggota1View.setError(getString(R.string.error_invalid_anggota));
            errorView = userrataUsiaView;
        }

        // Check for a valid members
        if (TextUtils.isEmpty(userAnggota1)) {
            userAnggota1View.setError(getString(R.string.error_field_required));
            errorView = userAnggota1View;
        } else if (!DataValidator.isAnggotaValid(userAnggota1)) {
            userAnggota1View.setError(getString(R.string.error_invalid_anggota));
            errorView = userAnggota1View;
        }

        if (TextUtils.isEmpty(userAnggota2)) {
            userAnggota2View.setError(getString(R.string.error_field_required));
            errorView = userAnggota2View;
        } else if (!DataValidator.isAnggotaValid(userAnggota2)) {
            userAnggota2View.setError(getString(R.string.error_invalid_anggota));
            errorView = userAnggota2View;
        }

        if (TextUtils.isEmpty(userAnggota3)) {
            userAnggota3View.setError(getString(R.string.error_field_required));
            errorView = userAnggota3View;
        } else if (!DataValidator.isAnggotaValid(userAnggota3)) {
            userAnggota3View.setError(getString(R.string.error_invalid_anggota));
            errorView = userAnggota3View;
        }

        if (TextUtils.isEmpty(userAnggota4)) {
            userAnggota4View.setError(getString(R.string.error_field_required));
            errorView = userAnggota4View;
        } else if (!DataValidator.isAnggotaValid(userAnggota4)) {
            userAnggota4View.setError(getString(R.string.error_invalid_anggota));
            errorView = userAnggota4View;
        }

        if (TextUtils.isEmpty(userAnggota5)) {
            userAnggota5View.setError(getString(R.string.error_field_required));
            errorView = userAnggota5View;
        } else if (!DataValidator.isAnggotaValid(userAnggota5)) {
            userAnggota5View.setError(getString(R.string.error_invalid_anggota));
            errorView = userAnggota5View;
        }

        if (TextUtils.isEmpty(userAnggota6)) {
            userAnggota6View.setError(getString(R.string.error_field_required));
            errorView = userAnggota6View;
        } else if (!DataValidator.isAnggotaValid(userAnggota6)) {
            userAnggota6View.setError(getString(R.string.error_invalid_anggota));
            errorView = userAnggota6View;
        }





        if (errorView != null) {
            errorView.requestFocus();
        } else {

            userProfile.setId(userId);
            userProfile.setUserName(userName);
            userProfile.setAnggota1(userAnggota1);
            userProfile.setAnggota2(userAnggota2);
            userProfile.setAnggota3(userAnggota3);
            userProfile.setAnggota4(userAnggota4);
            userProfile.setAnggota5(userAnggota5);
            userProfile.setAnggota6(userAnggota6);
            userProfile.setBio(userBio);
            userProfile.setRataUsia(userUsia);
            //for the moment we don't store the bitmap
            profileImage.setDrawingCacheEnabled(true);
            profileImage.buildDrawingCache();
            userProfile.setAvatar(profileImage.getDrawingCache());

            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if (NetworkUtils.isAvailable(connectivityManager)) {
                saveProfileOnline();

            }else {
                Toast.makeText(ProfileActivity.this, "Please check your Internet Connection", Toast.LENGTH_LONG).show();


            }
        }

        progressBar.setVisibility(View.GONE);

    }


    /**
     * Save the current userProfile of the current firebaseUser online
     */
    private void saveProfileOnline() {
        Log.d(Constant.NEARBY_CHAT, "Save profile online for id  " + userId);

        OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener = taskSnapshot -> Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show();
        OnFailureListener onFailureListener = e -> Toast.makeText(this, "Upload failed miserably", Toast.LENGTH_SHORT).show();

        userProfileDatabaseReference.setValue(userProfile);
        profileImage.setDrawingCacheEnabled(true);
        profileImage.buildDrawingCache();

        DatabaseUtils.saveProfilePicture(profileImage.getDrawingCache(), onSuccessListener, onFailureListener);
    }


    /**
     * Load userProfile for the current firebaseUser with the online version
     * Async task
     */
    private void loadProfileOnline() {
        Log.d(Constant.NEARBY_CHAT, "Load profile online and add listener for id " + userId);


        userProfileDatabaseReference.addListenerForSingleValueEvent(userProfileListener);

        DatabaseUtils.loadProfileImage(userId, bitmap -> {
            if (bitmap != null) {
                userProfile.setAvatar(bitmap);
                profileImage.setImageBitmap(bitmap);

                profileImage.setVisibility(View.VISIBLE);
                profileImageIcon.setVisibility(View.VISIBLE);
            }
        }, null);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseUtils.getUserProfileReferenceById(userId).removeEventListener(userProfileListener);
    }
}

