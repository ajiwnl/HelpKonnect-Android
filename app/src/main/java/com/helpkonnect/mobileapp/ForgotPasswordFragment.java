package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class ForgotPasswordFragment extends Fragment {

    private TextView signupTextView;
    private Button forgotPasswordButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        signupTextView = rootView.findViewById(R.id.tosignintextview);
        forgotPasswordButton = rootView.findViewById(R.id.forgotpasswordButton);

        forgotPasswordButton.setOnClickListener(v ->
                Toast.makeText(rootView.getContext(), "Email sent", Toast.LENGTH_SHORT).show()
        );

        signupTextView.setOnClickListener(v -> {
            // Use FragmentManager to switch to RegisterFragment
            FragmentManager fragmentManager = requireFragmentManager();
            FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new RegisterFragment());
        });

        return rootView;
    }
}
