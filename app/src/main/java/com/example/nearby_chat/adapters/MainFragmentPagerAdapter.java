package com.example.nearby_chat.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.nearby_chat.fragments.LoginFragment;
import com.example.nearby_chat.fragments.RegisterFragment;

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {

    public MainFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        if (position == 0) {
            return LoginFragment.newInstance();
        } else {
            return RegisterFragment.newInstance();
        }

    }

    @Override
    public int getCount() {
        return 2;
    }
}