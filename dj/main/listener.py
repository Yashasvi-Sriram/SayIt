"""
Main server init script

High Level Protocol (SayMTP):
=======================================
When a client opens a connection with a socket

    1.What do you want? What is the query?
        - Format expected : Integer Flag, mutually agreed upon by client and server

    2.Who are you?
        - If SignUp then skip this step
        - Else ask for credentials (for any query other than sign up credentials are required)
            - Format expected : Handle, Password blocks sent in this order

The below steps are specific to query    

    3.Tell me more ...
        - More information transferred as required by the query

    4.So your answer is ...
        - Send the response to the client

Implementation:
==============
Creates a socket, sets options and binds it to IP address and Port

1.Then calls accept() function on the socket created and waits for client to connect

2.When accepted gets the new socket formed "c"
3.and creates a SocketStation class(written by us) instance "ss" using that new socket

4.Then start_talking function is called with "ss" as argument, from where the SayMTP protocol is implemented

5.Finally when control returns back from some action, the socket "c" is closed

The above 5 steps are executed in an infinite loop
"""
# !/usr/bin/python

import socket

import django

from network.config import Config
from network.flags import Flags
from network.socket_station import SocketStation

# Initialize django services
# This should be before any import related to django
django.setup()

from main.network import actions


def start_talking(socket_station):
    """
    Implements the first step of the SayMTP protocol
    i.e. 
    1. asks for the query type
    2. based on received query type 
        redirects to one of the actions where the rest of the SayMTP is implemented
    """
    # What do you want?
    query_type = socket_station.receive()
    print 'Received query : ', query_type

    if query_type == Flags.QueryType.SIGN_UP:
        # Pass control to action
        actions.sign_up(socket_station)

    elif query_type == Flags.QueryType.LOG_IN:
        # Pass control to action
        actions.log_in(socket_station)

    elif query_type == Flags.QueryType.UPDATE_ACCOUNT:
        # Pass control to action
        actions.update_account(socket_station)

    elif query_type == Flags.QueryType.DELETE_ACCOUNT:
        # Pass control to action
        actions.delete_account(socket_station)

    elif query_type == Flags.QueryType.FILTER_USERS:
        # Pass control to action
        actions.filter_people(socket_station)

    elif query_type == Flags.QueryType.NEW_MESSAGE:
        # Pass control to action
        actions.new_messages(socket_station)

    elif query_type == Flags.QueryType.FILTER_MESSAGES:
        # Pass control to action
        actions.filter_messages(socket_station)

    elif query_type == Flags.QueryType.PLACE_FRIEND_REQUEST:
        # Pass control to action
        actions.place_friend_request(socket_station)

    elif query_type == Flags.QueryType.ANSWER_FRIEND_REQUEST:
        # Pass control to action
        actions.answer_friend_request(socket_station)

    elif query_type == Flags.QueryType.GET_STATUS_OF_FRIEND_REQUEST:
        # Pass control to action
        actions.get_status_of_friend_request(socket_station)

    elif query_type == Flags.QueryType.LOG_OUT:
        # Pass control to action
        actions.log_out(socket_station)
    elif query_type == Flags.QueryType.LDAP_LOGIN:
        # Pass control to action
        actions.ldap_login(socket_station)


# Setup a Server Socket
s = socket.socket()
# Reuse the address after closing
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
# Bind to a IP and Port using the Config class
s.bind((Config.IP, Config.PORT))
# Limit of waiting queue
s.listen(5)

while True:
    print('Listening ...')
    # Establish connection with client
    c, address = s.accept()
    # Set timeout for socket
    c.settimeout(20)
    print('Connected to ', c.getpeername())
    # Create a SocketStation instance using the socket returned
    ss = SocketStation(c)
    # Start talking to client
    start_talking(ss)
    # Close the socket
    c.close()
