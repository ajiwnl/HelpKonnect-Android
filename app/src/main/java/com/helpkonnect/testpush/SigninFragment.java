package com.helpkonnect.testpush;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SigninFragment extends Fragment {

    private TextView forgotPasswordTextView;
    private TextView signupTextView;
    private Button login;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_signin, container, false);

        login = rootView.findViewById(R.id.signupbutton);
        forgotPasswordTextView = rootView.findViewById(R.id.forgotpasswordtextView);
        signupTextView = rootView.findViewById(R.id.tosignintextview);

        // Handle login button click
        login.setOnClickListener(v -> {
            Intent intent = new Intent(rootView.getContext(), MainScreenActivity.class);
            startActivity(intent);
        });

        // Handle forgot password click
        forgotPasswordTextView.setOnClickListener(v -> {
            androidx.fragment.app.FragmentManager fragmentManager = requireFragmentManager();
            FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new ForgotPasswordFragment());
        });

        // Handle signup click
        signupTextView.setOnClickListener(v -> {
            androidx.fragment.app.FragmentManager fragmentManager = requireFragmentManager();
            FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new RegisterFragment());
        });

        return rootView;
    }
}
