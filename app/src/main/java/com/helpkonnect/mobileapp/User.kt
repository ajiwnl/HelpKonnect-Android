package com.helpkonnect.mobileapp
import io.getstream.chat.android.models.User

fun connectUser(userId: String, userName: String): User {
    val user = User(
        id = userId,
        name = userName,
    );

    return user;
}