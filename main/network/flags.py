class Flags:
    """
    Container for flags used in SayMTP protocol
    """
    class QueryType:
        """
        Container for Query Type flags used in SayMTP protocol
        """
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
        LOG_OUT = '11'
        LDAP_LOGIN = '12'

        def __init__(self):
            pass

    class ResponseType:
        """
        Container for Response Type flags used in SayMTP protocol
        """
        SUCCESS = '51'
        FAILURE = '52'
        HANDLE_ALREADY_EXIST = '53'
        INVALID_CREDENTIALS = '54'
        INVALID_REGEX = '55'
        INVALID_PK = '56'
        IDENTICAL_PKS = '57'
        INVALID_NAME_SPACE = '58'

        class FriendRequest:
            """
            Container for special Friend Request Response Type flags used in SayMTP protocol
            """
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
