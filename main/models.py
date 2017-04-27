from __future__ import unicode_literals

from django.db import models


class User(models.Model):
    name = models.CharField(max_length=50)
    handle = models.CharField(max_length=50)
    password = models.CharField(max_length=50)
    friends = models.ManyToManyField('User')
    last_active = models.CharField(max_length=50)

    def __str__(self):
        return str(self.pk) + ' : ' + self.name + '@' + self.handle


class UserToUserMessage(models.Model):
    sender = models.ForeignKey(User, on_delete=models.CASCADE, null=True, related_name='sender')
    receiver = models.ForeignKey(User, on_delete=models.CASCADE, null=True, related_name='receiver')
    content = models.TextField()
    time_stamp = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return str(self.pk) + ' From ' + str(self.sender_id) + ' to ' + str(self.receiver_id) + ' : ' + self.content


class FriendRequest(models.Model):
    class Status:
        PENDING = '0'
        ACCEPTED = '1'
        REJECTED = '2'

        def __init__(self):
            pass

    sender = models.ForeignKey(User, on_delete=models.CASCADE, null=True, related_name='fr_sender')
    receiver = models.ForeignKey(User, on_delete=models.CASCADE, null=True, related_name='fr_receiver')
    message = models.TextField()
    status = models.CharField(max_length=2)

    def __str__(self):
        return str(self.pk) + ' From ' + str(self.sender_id) + ' to ' + str(self.receiver_id) + ' status ' \
               + self.status

