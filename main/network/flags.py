class Flags:
    class QueryType:
        SIGN_UP = '1'
        LOG_IN = '2'
        UPDATE_ACCOUNT = '3'
        DELETE_ACCOUNT = '4'

        FILTER_USERS = '5'
        NEW_MESSAGE = '6'
        FILTER_MESSAGES = '7'

        PLACE_FRIEND_REQUEST = '8'
        ANSWER_FRIEND_REQUEST = '9'
        GET_STATUS_OF_FRIEND_REQUEST = '10'

        def __init__(self):
            pass

    class ResponseType:
        SUCCESS = '51'
        FAILURE = '52'
        HANDLE_ALREADY_EXIST = '53'
        INVALID_CREDENTIALS = '54'
        INVALID_REGEX = '55'
        INVALID_PK = '56'
        IDENTICAL_PKS = '57'

        class FriendRequest:
            REQUEST_ALREADY_PLACED = '58'
            NO_SUCH_FRIEND_REQUEST = '59'
            REQUEST_ALREADY_ANSWERED = '60'

            def __init__(self):
                pass

        NOT_FRIENDS = '61'

        def __init__(self):
            pass

    def __init__(self):
        pass
