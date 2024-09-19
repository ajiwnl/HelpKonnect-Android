package com.helpkonnect.mobileapp;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.checkerframework.checker.nullness.qual.Nullable;

public class LogoutDialogFragment extends DialogFragment {

    private OnLogoutConfirmationListener listener;

    public interface OnLogoutConfirmationListener {
        void onConfirmLogout();
    }

    public void setOnLogoutConfirmationListener(OnLogoutConfirmationListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Use the custom layout
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_logout_confirmation, null);

        // Get references to the buttons
        Button yesButton = dialogView.findViewById(R.id.yesButton);
        Button noButton = dialogView.findViewById(R.id.noButton);

        // Set click listeners for Yes and No buttons
        yesButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirmLogout();
            }
            dismiss(); // Close the dialog
        });

        noButton.setOnClickListener(v -> {
            dismiss(); // Just dismiss the dialog
        });

        // Build and return the dialog
        builder.setView(dialogView);
        return builder.create();
    }
}
