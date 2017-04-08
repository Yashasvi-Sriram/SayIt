import json

from django.core.exceptions import ObjectDoesNotExist
from main.models import User
from main.network.flags import Flags
from .keys import Keys

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
    4. send success or handle already exists
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
    2. see whether such a user exists
    3. Expected Format handle, password (blocks in that order)
    4. send success or invalid credentials, if success send user's name also
    """
    handle = socket_station.receive()
    password = socket_station.receive()
    try:
        user = User.objects.get(handle=handle, password=password)
        print 'Login successful for handle : ', handle
        socket_station.send(Flags.ResponseType.SUCCESS)
        socket_station.send(user.name)
    except ObjectDoesNotExist:
        print 'Login failed for handle : ', handle
        socket_station.send(Flags.ResponseType.INVALID_CREDENTIALS)


def update_account(socket_station):
    """
    :param socket_station: SocketStation instance

    1. (done)
    2. see whether such a user exists
    3. Expected Format handle, old_password, new_name, new_password (blocks in that order)
    4. send success or invalid credentials
    """
    handle = socket_station.receive()
    old_password = socket_station.receive()
    new_name = socket_station.receive()
    new_password = socket_station.receive()
    try:
        user = User.objects.get(handle=handle, password=old_password)
        user.name = new_name
        user.password = new_password
        user.save()
        print 'Update account successful for handle : ', handle
        socket_station.send(Flags.ResponseType.SUCCESS)
    except ObjectDoesNotExist:
        print 'Update account failed for handle : ', handle
        socket_station.send(Flags.ResponseType.INVALID_CREDENTIALS)


def delete_account(socket_station):
    """
    :param socket_station: SocketStation instance

    1. (done)
    2. see whether such a user exists
    3. Expected Format handle, password (blocks in that order)
    4. send success or invalid credentials
    """
    handle = socket_station.receive()
    password = socket_station.receive()
    try:
        user = User.objects.get(handle=handle, password=password)
        user.delete()
        print 'Delete account successful for handle : ', handle
        socket_station.send(Flags.ResponseType.SUCCESS)
    except ObjectDoesNotExist:
        print 'Delete account failed for handle : ', handle
        socket_station.send(Flags.ResponseType.INVALID_CREDENTIALS)


def filter_people(socket_station):
    """
    :param socket_station: SocketStation instance

    1. (done)
    2. see whether such a user exists. Expected Format handle, password (blocks in that order)
    3. Expected Format regex_string
    4. send success or invalid credentials
    """
    handle = socket_station.receive()
    password = socket_station.receive()
    regex_string = socket_station.receive()
    if is_valid_user(handle, password):
        json_response = []
        filtered_users = User.objects.filter(name__regex=regex_string)
        for filtered_user in filtered_users:
            filtered_user_dict = {
                Keys.JSON.PK: filtered_user.pk,
                Keys.JSON.NAME: filtered_user.name,
                Keys.JSON.HANDLE: filtered_user.handle
            }
            json_response.append(filtered_user_dict)

        print 'Filter people success for the regex : ', regex_string
        socket_station.send(Flags.ResponseType.SUCCESS)
        socket_station.send(json.dumps(json_response))
    else:
        print 'Filter people failed [Invalid Credentials] for the handle : ', handle
        socket_station.send(Flags.ResponseType.INVALID_CREDENTIALS)
