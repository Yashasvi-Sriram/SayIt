#!/usr/bin/python

from __future__ import print_function
import django
django.setup()

import socket
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
print('Connected to client ', address)


data = c.recv(1024)
print(data)
if data == '1':
    print("Got correct input")
data = c.recv(1024)
print(data)

a = c.send('Hello')
print('Sent ', a, 'bytes')
a = c.send('how')
print('Sent ', a, 'bytes')
a = c.send('are')
print('Sent ', a, 'bytes')
a = c.send('you')
print('Sent ', a, 'bytes')

data = c.recv(1024)

c.close()

# try:
#     user = User.objects.get(pk=data)
#     c.sendall(str(user.handle) + ' found!\n')
# except ObjectDoesNotExist:
#     c.sendall('No such User\n')

