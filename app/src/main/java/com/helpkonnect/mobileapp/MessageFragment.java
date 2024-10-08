package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Collections;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.models.FilterObject;
import io.getstream.chat.android.models.Filters;
import io.getstream.chat.android.models.querysort.QuerySortByField;
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelFactory;

public class MessageFragment extends Fragment {

    private static final String TAG = "MessageFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");


        View view = inflater.inflate(R.layout.fragment_messaging, container, false);

        // Find the ChannelListView
        ChannelListView channelListView = view.findViewById(R.id.channelListView);

        // Instantiate the ViewModel
        FilterObject filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.in("members", Collections.singletonList("user-id"))
        );

        ViewModelProvider.Factory factory = new ChannelListViewModelFactory.Builder()
                .filter(filter)
                .sort(QuerySortByField.descByName("last_updated"))
                .limit(30)
                .build();

        ChannelListViewModel viewModel = new ViewModelProvider(this, factory).get(ChannelListViewModel.class);

        // Bind the ViewModel with ChannelListView
        ChannelListViewModelBinding.bind(viewModel, channelListView, getViewLifecycleOwner());

        Log.d(TAG, "ViewModel and ChannelListView bound");

        return view;
    }
}
