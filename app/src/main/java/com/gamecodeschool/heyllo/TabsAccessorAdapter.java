package com.gamecodeschool.heyllo;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter {
    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                Chats chats = new Chats();
                return chats;
            case 1:
                GroupsFragment groupsFragment = new GroupsFragment();
                return  groupsFragment;
            case 2:
                ContactsFragment contactsFragment = new ContactsFragment();
                return  contactsFragment;

        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
             return "Chats";
            case 1:
               return "Groups";
            case 2:
                return "Friends";

            default:
                return null;
        }

    }
}
