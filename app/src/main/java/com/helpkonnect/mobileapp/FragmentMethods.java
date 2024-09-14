package com.helpkonnect.mobileapp;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class FragmentMethods {

    public static void displayFragment(FragmentManager fragmentManager, int contentDisplayId, Fragment displayFragment) {
        fragmentManager.beginTransaction()
                .replace(contentDisplayId, displayFragment)
                .addToBackStack(null)
                .commit();
    }
}
