package com.helpkonnect.testpush;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RegisterFragment extends Fragment {

    private Button signup;
    private TextView login;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_register, container, false);

        signup = rootView.findViewById(R.id.signupbutton);
        login = rootView.findViewById(R.id.tosignintextview);

        signup.setOnClickListener(v -> Toast.makeText(rootView.getContext(), "Register Successfully", Toast.LENGTH_SHORT).show());

        login.setOnClickListener(v -> {
            androidx.fragment.app.FragmentManager fragmentManager = requireFragmentManager();
            FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new SigninFragment());
        });

        return rootView;
    }
}
