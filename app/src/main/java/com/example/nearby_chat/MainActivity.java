package com.example.nearby_chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import com.example.nearby_chat.activities.OnlineActivity;
import com.example.nearby_chat.adapters.MainFragmentPagerAdapter;
import com.example.nearby_chat.constants.Constant;
import com.example.nearby_chat.constants.Database;
import com.example.nearby_chat.fragments.LoginFragment;
import com.example.nearby_chat.fragments.RegisterFragment;
import com.example.nearby_chat.models.UserConversations;
import com.example.nearby_chat.utils.DatabaseUtils;
import com.example.nearby_chat.utils.NetworkUtils;


public class MainActivity extends AppCompatActivity
        implements LoginFragment.OnFragmentInteractionListener,
        RegisterFragment.OnFragmentInteractionListener {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference database;

    private ViewPager viewPager;
    private ProgressBar progressBar;

    private MainFragmentPagerAdapter mainFragmentPagerAdapter;
    private long BackPressedTime;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        AppBarLayout.LayoutParams toolParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();

        toolParams.setScrollFlags(0);
        toolbar.setLayoutParams(toolParams);

        mainFragmentPagerAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.container_main);
        viewPager.setAdapter(mainFragmentPagerAdapter);

        progressBar = (ProgressBar) findViewById(R.id.main_spinner);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs_main);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();




    }

    @Override
    public void onStart() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (NetworkUtils.isAvailable(connectivityManager)) {
            super.onStart();

            user = auth.getCurrentUser();

            if (user == null) {
                Toast.makeText(MainActivity.this, "Please login or create a new account", Toast.LENGTH_LONG).show();
            } else {
                mountOnlineActivity();
            }

        } else {
            Toast.makeText(MainActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }


    }


    //fungsi double backPressed untuk keluar
    @Override
    public void onBackPressed() {

            if (BackPressedTime + 2000 > System.currentTimeMillis()){
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }else{
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

            }
            BackPressedTime = System.currentTimeMillis();
        }


    // app logic

    @Override
    public void requestLogin(String email, String password) {

        progressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(Constant.NEARBY_CHAT, "signInWithEmail:success");
                        user = auth.getCurrentUser();

                        mountOnlineActivity();

                        Log.d(Constant.NEARBY_CHAT, user.getEmail() != null ? user.getEmail() : "EMPTY");
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(Constant.NEARBY_CHAT, "signInWithEmail:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }



                    progressBar.setVisibility(View.GONE);
                });
    }

    private void initProfileImage() {
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);
        DatabaseUtils.saveProfilePicture(bitmap, null, null);
    }

    @Override
    public void requestRegister(String username, String email, String password) {

        progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(Constant.NEARBY_CHAT, "createUserWithEmail:success");
                        user = auth.getCurrentUser();
                        Log.d(Constant.NEARBY_CHAT, user.getEmail() != null ? user.getEmail() : "EMPTY");
                        initProfileImage();
                        createUserConversationEntry();
                        mountOnlineActivity();

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(Constant.NEARBY_CHAT, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }

                    progressBar.setVisibility(View.GONE);
                });
    }


    public void mountOnlineActivity() {
        Intent intent = new Intent(this, OnlineActivity.class);
        startActivity(intent);
    }

    private void createUserConversationEntry() {

        String key = database
                .child(Database.userConversations)
                .push()
                .getKey();

        UserConversations userConversations = new UserConversations();
        userConversations.setOwnerId(DatabaseUtils.getCurrentUUID());
        userConversations.setId(key);
        userConversations.setConversations(new HashMap<>());

        DatabaseUtils.getCurrentDatabaseReference()
                .child(Database.userConversations)
                .child(user.getUid())
                .setValue(userConversations);
    }

}