#!/usr/bin/python

import socket

from network.socket_station import SocketStation
from network.config import Config
from network.flags import Flags

import django

# Initialize django services
# This should be before any import related to django
django.setup()

from . import actions

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
"""


def start_talking(socket_station):
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
