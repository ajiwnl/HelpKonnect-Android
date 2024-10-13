package com.helpkonnect.mobileapp;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class FragmentMethods {

    public static void displayFragment(FragmentManager fragmentManager, int contentDisplayId, Fragment displayFragment) {
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                )
                .replace(contentDisplayId, displayFragment)
                .commit();
    }
}
