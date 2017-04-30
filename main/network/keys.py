class Keys:
    class DateTime:
        """
        Container for keys used in parsing datetime strings
        """
        DEFAULT_FORMAT = '%Y-%m-%d %H:%M:%S:%f'
        READABLE_FORMAT = '%I:%M %p %d %b %y'

        def __init__(self):
            pass

    class JSON:
        """
        Container for keys used in creating json objects
        """
        PK = 'pk'
        NAME = 'name'
        HANDLE = 'handle'
        PASSWORD = 'password'

        SENDER_PK = 'sender'
        RECEIVER_PK = 'receiver'
        CONTENT = 'content'
        TIME_STAMP = 'time_stamp'

        FRIENDS_LIST = 'friends_list'
        OTHERS_LIST = 'others_list'

        MESSAGE = 'message'
        STATUS = 'status'

        ACTIVE_STATUS = 'active_status'

        def __init__(self):
            pass

    def __init__(self):
        pass
