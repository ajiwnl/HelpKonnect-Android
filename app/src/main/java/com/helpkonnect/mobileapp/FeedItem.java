package com.helpkonnect.mobileapp;

import java.io.Serializable;

public class FeedItem implements Serializable {
    public static final int TYPE_EVENT = 0;
    public static final int TYPE_COMMUNITY_POST = 1;

    private int type; // 0 for event, 1 for community post
    private Event event;
    private CommunityListAdapter.CommunityPost communityPost;

    public FeedItem(Event event) {
        this.type = TYPE_EVENT;
        this.event = event;
    }

    public FeedItem(CommunityListAdapter.CommunityPost communityPost) {
        this.type = TYPE_COMMUNITY_POST;
        this.communityPost = communityPost;
    }

    public int getType() {
        return type;
    }

    public Event getEvent() {
        return event;
    }

    public CommunityListAdapter.CommunityPost getCommunityPost() {
        return communityPost;
    }
}