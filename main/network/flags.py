class Flags:
    class QueryType:
        SIGN_UP = '1'
        LOG_IN = '2'
        UPDATE_ACCOUNT = '3'
        DELETE_ACCOUNT = '4'

        FILTER_USERS = '5'
        NEW_MESSAGE = '6'
        FILTER_MESSAGES = '7'

        def __init__(self):
            pass

    class ResponseType:
        SUCCESS = '51'
        FAILURE = '52'
        HANDLE_ALREADY_EXIST = '53'
        INVALID_CREDENTIALS = '54'
        INVALID_REGEX = '55'

        def __init__(self):
            pass

    def __init__(self):
        pass
