package com.example.nearby_chat.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.example.nearby_chat.R;
import com.example.nearby_chat.adapters.ActiveConversationsAdapter;
import com.example.nearby_chat.adapters.SkilAdapter;
import com.example.nearby_chat.constants.Constant;
import com.example.nearby_chat.models.Conversation;
import com.example.nearby_chat.models.UserProfile;
import com.example.nearby_chat.utils.DatabaseUtils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserSkil extends AppCompatActivity {

    ArrayList<UserProfile> list;
    RecyclerView recyclerView;
    DatabaseReference ref;
    SkilAdapter skilAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_skil);

        recyclerView = (RecyclerView) findViewById(R.id.userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<UserProfile>();

        ref = DatabaseUtils.getCurrentDatabaseReference().child("userProfiles");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot sn: dataSnapshot.getChildren()){

                    UserProfile usrProfile = sn.getValue(UserProfile.class);

                    if (usrProfile != null) {
                    list.add(usrProfile);
                    DatabaseUtils.loadProfileImage(usrProfile.getId(), bitmap -> {
                        usrProfile.setAvatar(bitmap);

                        }, null);
                        Log.w(Constant.NEARBY_CHAT, "id " + usrProfile.getId());
                    }
                }

                skilAdapter = new SkilAdapter(UserSkil.this,list);
                recyclerView.setAdapter(skilAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
