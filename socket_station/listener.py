#!/usr/bin/python

from __future__ import print_function

import django
import time
import socket

from .socket_station import SocketStation

django.setup()

from django.core.exceptions import ObjectDoesNotExist
from .models import User

s = socket.socket()
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
host = "localhost"
port = 8036
s.bind((host, port))

s.listen(5)

print('Listening')
c, address = s.accept()  # Establish connection with client.
print(c.gettimeout())
print(c.getpeername())
print(c.getsockname())

ss = SocketStation(c)

# time.sleep(5)
# data = ss.receive()
# print(data)
# data = ss.receive()
# print(data)
# data = ss.receive()
# print(data)
# data = ss.receive()
# print(data)
# data = ss.receive()
# print(data)
#
# ss.send("first one\n")
# ss.send("some here")
# ss.send("")
# ss.send("some THERE")
# ss.send("\n")
# ss.send("This is a second packet should be heard differently")
# ss.send("Hello from yashasvi")

c.close()

# try:
#     user = User.objects.get(pk=data)
#     c.sendall(str(user.handle) + ' found!\n')
# except ObjectDoesNotExist:
#     c.sendall('No such User\n')

