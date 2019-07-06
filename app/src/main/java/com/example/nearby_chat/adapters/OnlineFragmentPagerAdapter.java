package com.example.nearby_chat.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.nearby_chat.activities.ProfileActivity;
import com.example.nearby_chat.fragments.ConversationsFragment;
import com.example.nearby_chat.fragments.LapanganFragment;
import com.example.nearby_chat.fragments.MapFragment;
import com.example.nearby_chat.fragments.ProfileFragment;

public class OnlineFragmentPagerAdapter extends FragmentPagerAdapter {

    public OnlineFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {

            case 0:
                return MapFragment.newInstance();
            case 1:
                return ConversationsFragment.newInstance();
            case 2:
                return LapanganFragment.newInstance();
            case 3:
                return ProfileFragment.newInstance();
            default:
                return MapFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 4;
    }
}