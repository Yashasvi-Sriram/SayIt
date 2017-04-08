class Flags:
    class QueryType:
        SIGN_UP = '1'
        LOG_IN = '2'
        UPDATE_ACCOUNT = '3'
        DELETE_ACCOUNT = '4'

        def __init__(self):
            pass

    class ResponseType:
        SUCCESS = '51'
        FAILURE = '52'
        HANDLE_ALREADY_EXIST = '53'

        def __init__(self):
            pass

    def __init__(self):
        pass
