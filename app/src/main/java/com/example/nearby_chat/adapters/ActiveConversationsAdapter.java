package com.example.nearby_chat.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.example.nearby_chat.R;
import com.example.nearby_chat.constants.Database;
import com.example.nearby_chat.models.Message;
import com.example.nearby_chat.models.UserMessages;
import com.example.nearby_chat.models.UserProfile;
import com.example.nearby_chat.utils.DatabaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ActiveConversationsAdapter extends ArrayAdapter<UserProfile> {

    private final int layoutResource;
    private final List<UserProfile> conversationUsers;
    private OnAdapterInteractionListener activity;


    public ActiveConversationsAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<UserProfile> conversationUsers) {
        super(context, resource, conversationUsers);

        this.layoutResource = resource;
        this.conversationUsers = conversationUsers;

        if (context instanceof OnAdapterInteractionListener) {
            activity = (OnAdapterInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAdapterInteractionListener");
        }
    }

    @Override @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layoutResource, null);
        }

        final UserProfile userProfile = conversationUsers.get(position);

        TextView userName = (TextView) convertView.findViewById(R.id.active_user_name);
        TextView userBio = (TextView) convertView.findViewById(R.id.active_user_bio);
        ImageView userAvatar = (ImageView) convertView.findViewById(R.id.active_user_avatar);

        userName.setText(userProfile.getUserName());
        userBio.setText(userProfile.getBio());
        userAvatar.setImageBitmap(userProfile.getAvatar());

        convertView.setOnClickListener(v -> activity.mountChatActivity(userProfile));

        return convertView;
    }

    public interface OnAdapterInteractionListener {

        void mountChatActivity(UserProfile partnerUserProfile);

    }


}

