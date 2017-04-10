import json

from django.core.exceptions import ObjectDoesNotExist
from django.db.models import Q
from main.models import User, UserToUserMessage as UTUMessage
from main.network.flags import Flags
from .keys import Keys
from datetime import datetime as dt
import re

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
    4. Send success or handle already exists
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
    2. See whether such a user exists
    3. Expected Format handle, password (blocks in that order)
    4. If every thing okay send success, name
        Else if no user send invalid_credentials
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
    4. send success or invalid credentials or invalid regex,
        if success send the json string of matched users
    """
    handle = socket_station.receive()
    password = socket_station.receive()
    regex_string = socket_station.receive()
    try:
        re.compile(regex_string)
    except re.error:
        print 'Filter people failed invalid regex : ', regex_string
        socket_station.send(Flags.ResponseType.INVALID_REGEX)
        return

    if is_valid_user(handle, password):
        json_response = []

        filtered_users = User.objects.filter(name__iregex=regex_string)

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


def new_message(socket_station):
    """
    :param socket_station: SocketStation instance

    1. (done)
    2. see whether such a user exists. Expected Format handle, password (blocks in that order)
    3. Expected Format pk_of_receiver, json_string_of_messages
    4. send success or invalid credentials or invalid pk
    """
    handle = socket_station.receive()
    password = socket_station.receive()
    receiver_pk = socket_station.receive()
    new_messages = json.loads(socket_station.receive())

    try:
        sender = User.objects.get(handle=handle, password=password)
        try:
            receiver = User.objects.get(pk=receiver_pk)
            # Both sender and receiver are verified
            for new_mess in new_messages:
                user_to_user_message = UTUMessage(content=new_mess,
                                                  sender_id=sender.pk,
                                                  receiver_id=receiver.pk)
                user_to_user_message.save()

            print len(new_messages), 'new message successfully stored from handle ', sender.handle, ' to ', receiver.handle
            socket_station.send(Flags.ResponseType.SUCCESS)

        except ObjectDoesNotExist:
            socket_station.send(Flags.ResponseType.INVALID_PK)

    except ObjectDoesNotExist:
        socket_station.send(Flags.ResponseType.INVALID_CREDENTIALS)


def filter_messages(socket_station):
    """
    :param socket_station: SocketStation instance

    1. (done)
    2. see whether such a user exists. Expected Format handle, password (blocks in that order)
    3. Expected Format pk of receiver, timestamp
    4. send success or invalid credentials or invalid pk
        if success send the json string of array of message objects
            then send the latest timestamp
    """
    handle = socket_station.receive()
    password = socket_station.receive()
    receiver_pk = socket_station.receive()
    from_this_datetime = socket_station.receive()

    try:
        sender = User.objects.get(handle=handle, password=password)
        try:
            receiver = User.objects.get(pk=receiver_pk)
            # Both sender and receiver are verified

            # Get message with sender and receiver as sender or receiver
            # Ordered by time stamp
            if from_this_datetime == '':
                filtered_messages = UTUMessage.objects.filter(
                    Q(receiver_id=sender.pk) | Q(receiver_id=receiver.pk),
                    Q(sender_id=sender.pk) | Q(sender_id=receiver.pk),
                ).order_by('time_stamp')
            # Get message with sender and receiver as sender or receiver
            # Greater than given param datetime
            # Ordered by time stamp
            else:
                filtered_messages = UTUMessage.objects.filter(
                    Q(receiver_id=sender.pk) | Q(receiver_id=receiver.pk),
                    Q(sender_id=sender.pk) | Q(sender_id=receiver.pk),
                    time_stamp__gte=dt.strptime(from_this_datetime, Keys.DateTime.DEFAULT_FORMAT)
                ).order_by('time_stamp')

            fm_list = []
            for filtered_message in filtered_messages:
                fm_dict = {
                    Keys.JSON.PK: filtered_message.pk,
                    Keys.JSON.SENDER_PK: filtered_message.sender_id,
                    Keys.JSON.RECEIVER_PK: filtered_message.receiver_id,
                    Keys.JSON.CONTENT: filtered_message.content,
                    Keys.JSON.TIME_STAMP: filtered_message.time_stamp.strftime(Keys.DateTime.DEFAULT_FORMAT)
                }
                print fm_dict
                fm_list.append(fm_dict)

            print 'Messages filtered successfully asked by ', sender.pk, ' in chat with ', receiver.pk
            socket_station.send(Flags.ResponseType.SUCCESS)
            socket_station.send(json.dumps(fm_list))
            socket_station.send(dt.now().strftime(Keys.DateTime.DEFAULT_FORMAT))

        except ObjectDoesNotExist:
            socket_station.send(Flags.ResponseType.INVALID_PK)

    except ObjectDoesNotExist:
        socket_station.send(Flags.ResponseType.INVALID_CREDENTIALS)
