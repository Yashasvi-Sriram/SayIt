package com.withjarvis.sayit.Network;

public class Flags {

    public class QueryType {
        public static final String SIGN_UP = "1";
        public static final String LOG_IN = "2";
        public static final String UPDATE_ACCOUNT = "3";
        public static final String DELETE_ACCOUNT = "4";
    }

    public class ResponseType {
        public static final String SUCCESS = "51";
        public static final String FAILURE = "52";
        public static final String HANDLE_ALREADY_EXIST = "53";
    }
}
