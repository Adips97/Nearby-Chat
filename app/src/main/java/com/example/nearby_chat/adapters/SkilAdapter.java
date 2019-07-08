package com.example.nearby_chat.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.nearby_chat.R;
import com.example.nearby_chat.models.UserProfile;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.example.nearby_chat.activities.ChatActivity.PARTNER_USER_PROFILE;
import static com.example.nearby_chat.constants.Constant.FIREBASE_STORAGE_REFERENCE;
import static com.example.nearby_chat.constants.Constant.NEARBY_CHAT;

public class SkilAdapter extends RecyclerView.Adapter<SkilAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<UserProfile> skillUser;
    private FirebaseStorage firebaseStorage;
    private UserProfile usrProfile;

    public SkilAdapter(Context c, ArrayList<UserProfile> s) {

        this.context = c;
        this.skillUser = s;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.user, viewGroup, false));
        //

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

        //UserProfile usrProfile = skillUser.get(i);
        usrProfile = (UserProfile) skillUser.get(i);

        myViewHolder.user_name.setText(usrProfile.getUserName());
        myViewHolder.user_bio.setText(usrProfile.getBio());
        myViewHolder.user_usia.setText(usrProfile.getRataUsia());
        //Picasso.get().load(skillUser.get(i).getAvatar()).into(myViewHolder.avatar);
        myViewHolder.loadProfileImage();
        //myViewHolder.avatar.setImageBitmap(usrProfile.getAvatar());


    }

    @Override
    public int getItemCount() {
        return skillUser.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView user_name, user_usia, user_bio;
        ImageView avatar;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            user_name = (TextView) itemView.findViewById(R.id.user_name);
            user_bio = (TextView) itemView.findViewById(R.id.user_bio);
            user_usia = (TextView) itemView.findViewById(R.id.user_usia);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);

            // itemView.setOnClickListener(v -> activity.mountChatActivity(usrProfile));

            firebaseStorage = FirebaseStorage.getInstance(FIREBASE_STORAGE_REFERENCE);
        }



        @NonNull
        private StorageReference getStorageReference() {
            return firebaseStorage.getReference("profile/" + usrProfile.getId() + ".jpeg");
        }

        private void loadProfileImage() {
            ImageView profileImage = (ImageView) itemView.findViewById(R.id.avatar);
            StorageReference reference = getStorageReference();

            final long ONE_MEGABYTE = 1024 * 1024;
            reference.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                // Data for "profile" is returns, use this as needed
                Bitmap avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                usrProfile.setAvatar(avatar);
                profileImage.setImageBitmap(avatar);
            }).addOnFailureListener(exception -> {
                // Handle any errors
                Log.w(NEARBY_CHAT, "loadProfileImage: ", exception);
            });

        }
    }
}




