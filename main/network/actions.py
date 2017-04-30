"""
This module contains functions called actions

The control is passed to an action
    1. from start_talking function in listener module
    2. after tallying the query type received with the list of actions available

An action is a function which implements steps 2, 3, 4 of SayMTP
Therefore each action has three specifications
    Credentials Format
    Further info
    Response

Then query received is answered in the action it is passed into

The answer may be a flag(mutually agreed upon by client and server) or raw text
"""
import json

from django.core.exceptions import ObjectDoesNotExist
from django.db.models import Q
from main.models import User, UserToUserMessage as UTUMessage, FriendRequest
from main.network.flags import Flags
from .keys import Keys
from datetime import datetime as dt, timedelta as td
import re
from suggestions import give_suggestions
from authenticate import authentication_ldap
from namespace import Namespace

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


def sign_up(socket_station):
    """
    :param socket_station: SocketStation instance
    
    Credentials Format  : not required
    Further info        : name, handle, password in that order
    Response            : If sign up is success : SUCCESS
                          If handle's namespace is invalid : INVALID_NAME_SPACE
                          If handle already exists : HANDLE_ALREADY_EXIST
    
    If SUCCESS:
    Creates a new user in local db
    """
    name = socket_station.receive()
    handle = socket_station.receive()
    password = socket_station.receive()
    # Restricts handle to exclude ldap name space
    if len(handle) >= len(Namespace.LDAP) and handle[:len(Namespace.LDAP)] == Namespace.LDAP:
        print 'Sign up with handle ', handle, ' : Fail - invalid name space'
        socket_station.send(Flags.ResponseType.INVALID_NAME_SPACE)

    else:
        try:
            User.objects.get(handle=handle)
            print 'Sign up with handle ', handle, ' : Fail - Handle already exists'
            socket_station.send(Flags.ResponseType.HANDLE_ALREADY_EXIST)

        except ObjectDoesNotExist:
            new_user = User(name=name, handle=handle, password=password, last_active="")
            new_user.save()
            # Every user is a friend of himself
            new_user.friends.add(new_user)
            print 'Sign up with handle ', handle, ' : Success'
            socket_station.send(Flags.ResponseType.SUCCESS)


def log_in(socket_station):
    """
    :param socket_station: SocketStation instance
    
    Credentials Format  : handle, password in that order
    Further info        : none
    Response            : If log in is success : SUCCESS, name in that order
                          If invalid credentials : INVALID_CREDENTIALS
    
    Validates user using the handle and password received from local db
    """
    handle = socket_station.receive()
    password = socket_station.receive()
    try:
        user = User.objects.get(handle=handle, password=password)
        print 'Login successful for handle : ', handle
        user.last_active = ""
        user.save()
        socket_station.send(Flags.ResponseType.SUCCESS)
        socket_station.send(user.name)
    except ObjectDoesNotExist:
        print 'Login failed for handle : ', handle
        socket_station.send(Flags.ResponseType.INVALID_CREDENTIALS)


def update_account(socket_station):
    """
    :param socket_station: SocketStation instance
    
    Credentials Format  : handle, password in that order
    Further info        : new_name, new_password in that order
    Response            : If update is success : SUCCESS
                          If invalid credentials : INVALID_CREDENTIALS
    
    If SUCCESS:
    Updates existing user with new password and name in local db
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
    
    Credentials Format  : handle, password in that order
    Further info        : none
    Response            : If delete is success : SUCCESS
                          If invalid credentials : INVALID_CREDENTIALS
    
    If SUCCESS:
    Deletes user and all their related data in local db
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


def log_out(socket_station):
    """
    :param socket_station: SocketStation instance
    
    Credentials Format  : handle, password in that order
    Further info        : none
    Response            : If log out is success : SUCCESS
                          If invalid credentials : INVALID_CREDENTIALS
    
    If SUCCESS:
    Flags the user inactive
    """
    handle = socket_station.receive()
    password = socket_station.receive()
    try:
        user = User.objects.get(handle=handle, password=password)
        user.last_active = dt.now().strftime(Keys.DateTime.DEFAULT_FORMAT)
        user.save()
        print 'Logout successful for handle : ', handle
        socket_station.send(Flags.ResponseType.SUCCESS)

    except ObjectDoesNotExist:
        print 'Logout failed for handle : ', handle
        socket_station.send(Flags.ResponseType.INVALID_CREDENTIALS)


def filter_people(socket_station):
    """
    :param socket_station: SocketStation instance
    
    Credentials Format  : handle, password in that order
    Further info        : regex_string
    Response            : If filter is success    : SUCCESS, json_response in that order
                          If invalid credentials  : INVALID_CREDENTIALS
                          If invalid regex_string : INVALID_REGEX
    
    If SUCCESS:
    Divides the users into friends and strangers
    Sorts strangers according to give_suggestions() function in suggestion module
    Filters them with to regex_string received
    Sends this data as json string
    """
    handle = socket_station.receive()
    password = socket_station.receive()
    regex_string = socket_station.receive()
    pattern = None
    try:
        pattern = re.compile('.*' + regex_string)
    except re.error:
        print 'Filter people failed invalid regex : ', regex_string
        socket_station.send(Flags.ResponseType.INVALID_REGEX)
        return

    try:
        user = User.objects.get(handle=handle, password=password)

        total_list = {}
        friends_list = []
        others_list = []

        suggested_users = give_suggestions(user.pk, User.objects.all())
        sorted_friends = suggested_users[0]
        sorted_others = suggested_users[1]

        for friend in sorted_friends:
            if pattern.match(friend.name) is not None:
                friend_dict = {
                    Keys.JSON.PK: friend.pk,
                    Keys.JSON.NAME: friend.name,
                    Keys.JSON.HANDLE: friend.handle
                }
                if friend.last_active == '':
                    active_status = 'active now'
                else:
                    last_active_time = dt.strptime(friend.last_active, Keys.DateTime.DEFAULT_FORMAT)
                    present = dt.now()
                    diff = present - last_active_time
                    if diff.days > 0:
                        active_status = 'active ' + str(diff.days) + 'd' + ' ago'
                    elif diff.seconds > 0:
                        if diff.seconds >= 3600:
                            active_status = 'active ' + str(diff.seconds / 3600) + 'h' + ' ago'
                        elif diff.seconds >= 60:
                            active_status = 'active ' + str(diff.seconds / 60) + 'm' + ' ago'
                        else:
                            active_status = 'active few seconds ago'
                    else:
                        active_status = 'active few seconds ago'

                friend_dict[Keys.JSON.ACTIVE_STATUS] = active_status
                friends_list.append(friend_dict)

        for stranger in sorted_others:
            if pattern.match(stranger.name) is not None:
                stranger_dict = {
                    Keys.JSON.PK: stranger.pk,
                    Keys.JSON.NAME: stranger.name,
                    Keys.JSON.HANDLE: stranger.handle
                }
                others_list.append(stranger_dict)

        total_list[Keys.JSON.FRIENDS_LIST] = friends_list
        total_list[Keys.JSON.OTHERS_LIST] = others_list

        print 'Filter people success for the regex : ', regex_string
        socket_station.send(Flags.ResponseType.SUCCESS)
        socket_station.send(json.dumps(total_list))

    except ObjectDoesNotExist:
        print 'Filter people failed [Invalid Credentials] for the handle : ', handle
        socket_station.send(Flags.ResponseType.INVALID_CREDENTIALS)


def new_messages(socket_station):
    """
    :param socket_station: SocketStation instance
    
    Credentials Format  : handle, password in that order
    Further info        : pk_of_receiver, json_string_of_messages in that order
    Response            : If filter is success    : SUCCESS
                          If invalid credentials  : INVALID_CREDENTIALS
                          If invalid receiver pk  : INVALID_PK
                          If receiver not friend  : NOT_FRIEND
    If SUCCESS:
    Stores all new messages received in local  db
    """
    handle = socket_station.receive()
    password = socket_station.receive()
    receiver_pk = socket_station.receive()
    _new_messages = json.loads(socket_station.receive())

    try:
        sender = User.objects.get(handle=handle, password=password)
        try:
            receiver = User.objects.get(pk=receiver_pk)
            # Both sender and receiver are verified
            # If friends
            if sender.friends.get(pk=receiver.pk) == receiver:
                for new_mess in _new_messages:
                    user_to_user_message = UTUMessage(content=new_mess,
                                                      sender_id=sender.pk,
                                                      receiver_id=receiver.pk)
                    user_to_user_message.save()

                print len(_new_messages), 'New message successfully stored from handle ', \
                    sender.handle, ' to ', receiver.handle
                socket_station.send(Flags.ResponseType.SUCCESS)
            else:
                socket_station.send(Flags.ResponseType.NOT_FRIENDS)

        except ObjectDoesNotExist:
            socket_station.send(Flags.ResponseType.INVALID_PK)

    except ObjectDoesNotExist:
        socket_station.send(Flags.ResponseType.INVALID_CREDENTIALS)


def filter_messages(socket_station):
    """
    :param socket_station: SocketStation instance
    
    Credentials Format  : handle, password in that order
    Further info        : pk_of_receiver, timestamp in that order
    Response            : If filter is success    : SUCCESS, json_response, new_timestamp in that order
                          If invalid credentials  : INVALID_CREDENTIALS
    
    If SUCCESS:
    Filters messages sent by sender, received by receiver
                     received by sender,sent by receiver
                     after the given timestamp 
                     (if empty timestamp, no restriction of timestamp)
    Sends these messages as json string
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
            # If friends
            if sender.friends.get(pk=receiver.pk) == receiver:

                # Get message with sender and receiver as sender or receiver
                # Ordered by time stamp
                if from_this_datetime == '':
                    if sender.pk == receiver.pk:
                        filtered_messages = UTUMessage.objects.filter(
                            sender_id=sender.pk,
                            receiver_id=sender.pk,
                        ).order_by('time_stamp')
                    else:
                        filtered_messages = UTUMessage.objects.filter(
                            Q(sender_id=sender.pk, receiver_id=receiver.pk) |
                            Q(sender_id=receiver.pk, receiver_id=sender.pk),
                        ).order_by('time_stamp')
                # Get message with sender and receiver as sender or receiver
                # Greater than given param datetime
                # Ordered by time stamp
                else:
                    if sender.pk == receiver.pk:
                        filtered_messages = UTUMessage.objects.filter(
                            sender_id=sender.pk,
                            receiver_id=sender.pk,
                            time_stamp__gte=dt.strptime(from_this_datetime, Keys.DateTime.DEFAULT_FORMAT)
                        ).order_by('time_stamp')
                    else:

                        filtered_messages = UTUMessage.objects.filter(
                            Q(sender_id=sender.pk, receiver_id=receiver.pk) |
                            Q(sender_id=receiver.pk, receiver_id=sender.pk),
                            time_stamp__gte=dt.strptime(from_this_datetime, Keys.DateTime.DEFAULT_FORMAT)
                        ).order_by('time_stamp')

                fm_list = []

                for filtered_message in filtered_messages:
                    corrected_time_stamp = filtered_message.time_stamp + td(seconds=19800)
                    fm_dict = {
                        Keys.JSON.PK: filtered_message.pk,
                        Keys.JSON.SENDER_PK: filtered_message.sender_id,
                        Keys.JSON.RECEIVER_PK: filtered_message.receiver_id,
                        Keys.JSON.CONTENT: filtered_message.content,
                        Keys.JSON.TIME_STAMP: corrected_time_stamp.strftime(Keys.DateTime.READABLE_FORMAT)
                    }
                    fm_list.append(fm_dict)

                print 'Messages filtered successfully asked by ', sender.pk, ' in chat with ', receiver.pk
                socket_station.send(Flags.ResponseType.SUCCESS)
                socket_station.send(json.dumps(fm_list))
                socket_station.send(dt.now().strftime(Keys.DateTime.DEFAULT_FORMAT))

            else:
                socket_station.send(Flags.ResponseType.NOT_FRIENDS)

        except ObjectDoesNotExist:
            socket_station.send(Flags.ResponseType.INVALID_PK)

    except ObjectDoesNotExist:
        socket_station.send(Flags.ResponseType.INVALID_CREDENTIALS)


def place_friend_request(socket_station):
    """
    :param socket_station: SocketStation instance
    
    Credentials Format  : handle, password in that order
    Further info        : pk_of_fr_receiver, message_in_fr in that order (fr = friend request)
    Response            : If filter is success    : SUCCESS
                          If invalid receiver pk  : INVALID_PK
                          If invalid credentials  : INVALID_CREDENTIALS
                          If receiver and sender same  : IDENTICAL_PKS
                          If fr already placed    : ALREADY_PLACED
    
    If SUCCESS:
    Creates a friend request object in local db
    """
    handle = socket_station.receive()
    password = socket_station.receive()
    receiver_pk = socket_station.receive()
    message = socket_station.receive()

    try:
        sender = User.objects.get(handle=handle, password=password)
        try:
            receiver = User.objects.get(pk=receiver_pk)
            if receiver.pk == sender.pk:
                socket_station.send(Flags.ResponseType.IDENTICAL_PKS)
            else:
                # Both sender and receiver are verified
                try:
                    FriendRequest.objects.get(Q(sender_id=sender.pk, receiver_id=receiver.pk) |
                                              Q(sender_id=receiver.pk, receiver_id=sender.pk))
                    socket_station.send(Flags.ResponseType.FriendRequest.REQUEST_ALREADY_PLACED)

                except ObjectDoesNotExist:
                    # No friend request already placed
                    friend_request = FriendRequest(sender=sender, receiver=receiver,
                                                   status=FriendRequest.Status.PENDING,
                                                   message=message)
                    friend_request.save()
                    print 'Friend request successfully placed from ', sender.pk, ' to ', receiver.pk
                    socket_station.send(Flags.ResponseType.SUCCESS)

        except ObjectDoesNotExist:
            socket_station.send(Flags.ResponseType.INVALID_PK)

    except ObjectDoesNotExist:
        socket_station.send(Flags.ResponseType.INVALID_CREDENTIALS)


def answer_friend_request(socket_station):
    """
    :param socket_station: SocketStation instance
    
    Credentials Format  : handle, password in that order
    Further info        : pk_of_fr_sender, answer_for_fr in that order (fr = friend request)
    Response            : If filter is success    : SUCCESS
                          If invalid sender pk    : INVALID_PK
                          If invalid credentials  : INVALID_CREDENTIALS
                          If receiver and sender same  : IDENTICAL_PKS
                          If fr already answered    : ALREADY_ANSWERED
    
    If SUCCESS:
    Changes the status of existing friend request according to "answer_to_fr" received
    """
    handle = socket_station.receive()
    password = socket_station.receive()
    sender_pk = socket_station.receive()
    answer = socket_station.receive()

    try:
        receiver = User.objects.get(handle=handle, password=password)
        try:
            sender = User.objects.get(pk=sender_pk)
            if sender.pk == receiver.pk:
                socket_station.send(Flags.ResponseType.IDENTICAL_PKS)
            else:
                # Both sender and receiver are verified
                try:
                    fr = FriendRequest.objects.get(sender_id=sender.pk, receiver_id=receiver.pk)

                    if fr.status == FriendRequest.Status.PENDING:
                        # Storing answer
                        fr.status = answer
                        # Making friends
                        if answer == FriendRequest.Status.ACCEPTED:
                            sender.friends.add(receiver)
                            receiver.friends.add(sender)
                        fr.save()

                        print 'Friend request ', answer, '  by ', receiver.pk, ' to ', sender.pk
                        socket_station.send(Flags.ResponseType.SUCCESS)
                    elif fr.status == FriendRequest.Status.REJECTED:
                        # Changed status to friends
                        if answer == FriendRequest.Status.ACCEPTED:
                            # Storing answer
                            fr.status = answer
                            # Making friends
                            sender.friends.add(receiver)
                            receiver.friends.add(sender)
                            fr.save()

                        print 'Friend request ', answer, '  by ', receiver.pk, ' to ', sender.pk
                        socket_station.send(Flags.ResponseType.SUCCESS)
                    elif fr.status == FriendRequest.Status.ACCEPTED:
                        # Changed status to blocked
                        if answer == FriendRequest.Status.REJECTED:
                            # Storing answer
                            fr.status = answer
                            # Blocking the friendship edge
                            sender.friends.remove(receiver)
                            receiver.friends.remove(sender)
                            fr.save()

                        print 'Friend request ', answer, '  by ', receiver.pk, ' to ', sender.pk
                        socket_station.send(Flags.ResponseType.SUCCESS)

                except ObjectDoesNotExist:
                    # No friend request already placed
                    socket_station.send(Flags.ResponseType.FriendRequest.NO_SUCH_FRIEND_REQUEST)

        except ObjectDoesNotExist:
            socket_station.send(Flags.ResponseType.INVALID_PK)

    except ObjectDoesNotExist:
        socket_station.send(Flags.ResponseType.INVALID_CREDENTIALS)


def get_status_of_friend_request(socket_station):
    """
    :param socket_station: SocketStation instance
    
    Credentials Format  : handle, password in that order
    Further info        : pk_of_other_friend
    Response            : If filter is success          : SUCCESS, json_response in that order
                          If invalid other friend pk    : INVALID_PK
                          If invalid credentials        : INVALID_CREDENTIALS
                          If receiver and sender same   : IDENTICAL_PKS
                          If no such fr                 : NO_SUCH_FRIEND_REQUEST (fr = friend request)
    
    If SUCCESS:
    Sends the status of existing friend request
    """
    handle = socket_station.receive()
    password = socket_station.receive()
    receiver_pk = socket_station.receive()
    try:
        sender = User.objects.get(handle=handle, password=password)
        try:
            receiver = User.objects.get(pk=receiver_pk)
            if receiver.pk == sender.pk:
                socket_station.send(Flags.ResponseType.IDENTICAL_PKS)
            else:
                # Both sender and receiver are verified
                try:
                    fr = FriendRequest.objects.get(Q(sender_id=sender.pk, receiver_id=receiver.pk) |
                                                   Q(sender_id=receiver.pk, receiver_id=sender.pk))
                    json_response = {
                        Keys.JSON.SENDER_PK: fr.sender.pk,
                        Keys.JSON.RECEIVER_PK: fr.receiver.pk,
                        Keys.JSON.MESSAGE: fr.message,
                        Keys.JSON.STATUS: fr.status
                    }
                    socket_station.send(Flags.ResponseType.SUCCESS)
                    socket_station.send(json.dumps(json_response))

                except ObjectDoesNotExist:
                    # No friend request placed
                    socket_station.send(Flags.ResponseType.FriendRequest.NO_SUCH_FRIEND_REQUEST)

        except ObjectDoesNotExist:
            socket_station.send(Flags.ResponseType.INVALID_PK)

    except ObjectDoesNotExist:
        socket_station.send(Flags.ResponseType.INVALID_CREDENTIALS)


def ldap_login(socket_station):
    """
    :param socket_station: SocketStation instance
    
    Credentials Format  : handle, password in that order
    Further info        : pk_of_other_friend
    Response            : If filter is success          : SUCCESS, json_response in that order
                          If invalid credentials        : INVALID_CREDENTIALS
    
    Searches for the user in local db first
        If found then SUCCESS
        Else searches in ldap db
            If found then create user in local db, SUCCESS
            Else INVALID_CREDENTIALS
    """
    handle = socket_station.receive()
    password = socket_station.receive()
    try:
        user = User.objects.get(handle=Namespace.LDAP + handle, password=password)
        user.last_active = ""
        user.save()
        print 'Ldap Login successful for handle : ', handle
        socket_station.send(Flags.ResponseType.SUCCESS)
        socket_station.send(user.name)

    except ObjectDoesNotExist:
        ldap_uid = authentication_ldap(handle, password)
        if ldap_uid is not False:
            new_user = User(name=ldap_uid, handle=Namespace.LDAP + handle, password=password, last_active="")
            new_user.save()
            # Every user is a friend of himself
            new_user.friends.add(new_user)
            print 'Ldap Signup with handle ', handle, ' : Success'
            socket_station.send(Flags.ResponseType.SUCCESS)
            socket_station.send(ldap_uid)
        else:
            print 'Ldap Login failed for handle : ', handle
            socket_station.send(Flags.ResponseType.INVALID_CREDENTIALS)
