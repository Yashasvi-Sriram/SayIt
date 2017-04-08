from django.core.exceptions import ObjectDoesNotExist
from .models import User
from .network.flags import Flags

"""
Protocol (as seen by server)
    1.What do you want?
        - Format : (using an query-int map identical at server and all clients)
            Integer
    
The next steps are implemented using actions

    2.Who are you?
        - Format : Blocks sent in this order (not required for sign up)
            Handle, Password
        - For other than sign up credentials are required

    3.Tell me more
        - More options transferred as required by the query

    4.So your answer is ...
        - Send the response to the client

The above steps are clearly illustrated in all the functions (actions) below
"""


def is_valid_user(handle, password):
    """
    :return: True iff a user with such handle and password exist
    """
    try:
        User.objects.get(handle=handle, password=password)
        return True
    except ObjectDoesNotExist:
        return False


def sign_up(socket_station):
    """
    :param socket_station: SocketStation instance
    
    1. (done)
    2. (no need)
    3. Expected Format
        name, handle, password (blocks in that order)
    4. send proper Response to client

    """
    name = socket_station.receive()
    handle = socket_station.receive()
    password = socket_station.receive()
    try:
        User.objects.get(handle=handle)
        print 'Sign up with handle ', handle, ' : Fail - Handle already exists'
        socket_station.send(Flags.ResponseType.HANDLE_ALREADY_EXIST)

    except ObjectDoesNotExist:
        new_user = User(name=name, handle=handle, password=password)
        new_user.save()
        print 'Sign up with handle ', handle, ' : Success'
        socket_station.send(Flags.ResponseType.SUCCESS)


def log_in(socket_station):
    """
    :param socket_station: SocketStation instance

    1. (done)
    2. done using is_valid_user() function
    3. Expected Format handle, password (blocks in that order)
        ...Actually 2 and 3 are same for this action
    4. send success or failure, if success send user's name also
    """
    handle = socket_station.receive()
    password = socket_station.receive()
    try:
        user = User.objects.get(handle=handle, password=password)
        print 'Login successful for handle ', handle
        socket_station.send(Flags.ResponseType.SUCCESS)
        socket_station.send(user.name)
    except ObjectDoesNotExist:
        print 'Login failed for handle ', handle
        socket_station.send(Flags.ResponseType.FAILURE)
