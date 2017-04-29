package com.withjarvis.sayit.Network;

public class Flags {

    public class QueryType {
        public static final String SIGN_UP = "1";
        public static final String LOG_IN = "2";
        public static final String UPDATE_ACCOUNT = "3";
        public static final String DELETE_ACCOUNT = "4";
        public static final String FILTER_USERS = "5";
        public static final String NEW_MESSAGE = "6";
        public static final String FILTER_MESSAGES = "7";
        public static final String PLACE_FRIEND_REQUEST = "8";
        public static final String ANSWER_FRIEND_REQUEST = "9";
        public static final String GET_STATUS_OF_FRIEND_REQUEST = "10";
        public static final String LOG_OUT = "11";
        public static final String LDAP_LOGIN = "12";
    }

    public class ResponseType {
        public static final String SUCCESS = "51";
        public static final String FAILURE = "52";
        public static final String HANDLE_ALREADY_EXIST = "53";
        public static final String INVALID_CREDENTIALS = "54";
        public static final String INVALID_REGEX = "55";
        public static final String INVALID_PK = "56";
        public static final String IDENTICAL_PKS = "57";
        public static final String INVALID_NAME_SPACE = "58";

        public class FriendRequest {

            public static final String REQUEST_ALREADY_PLACED = "58";
            public static final String NO_SUCH_FRIEND_REQUEST = "59";
            public static final String REQUEST_ALREADY_ANSWERED = "60";
        }

        public static final String NOT_FRIENDS = "61";

    }

    public class Local {
        public static final String NO_NEW_MESSAGES = "101";
    }
}
