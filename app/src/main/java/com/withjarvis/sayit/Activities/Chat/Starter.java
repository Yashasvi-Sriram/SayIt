package com.withjarvis.sayit.Activities.Chat;

import android.content.Context;
import android.content.Intent;

public class Starter {

    public class Keys {

        public static final String OTHER_PERSON_NAME = "other_person_name";
        public static final String OTHER_PERSON_HANDLE = "other_person_handle";
        public static final String OTHER_PERSON_PK = "other_person_pk";

    }

    public static void startChatWindow(String other_person_name,
                                       String other_person_handle,
                                       int other_person_pk,
                                       Context context) {
        Intent to_chat_window = new Intent(context, ChatWindow.class);
        to_chat_window.putExtra(Keys.OTHER_PERSON_NAME, other_person_name);
        to_chat_window.putExtra(Keys.OTHER_PERSON_HANDLE, other_person_handle);
        to_chat_window.putExtra(Keys.OTHER_PERSON_PK, other_person_pk);
        context.startActivity(to_chat_window);
    }

    public static void startFriendRequest(String other_person_name,
                                          String other_person_handle,
                                          int other_person_pk,
                                          Context context) {
        Intent to_friend_request = new Intent(context, FriendRequest.class);
        to_friend_request.putExtra(Keys.OTHER_PERSON_NAME, other_person_name);
        to_friend_request.putExtra(Keys.OTHER_PERSON_HANDLE, other_person_handle);
        to_friend_request.putExtra(Keys.OTHER_PERSON_PK, other_person_pk);
        context.startActivity(to_friend_request);
    }

}
