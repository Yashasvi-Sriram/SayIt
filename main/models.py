from __future__ import unicode_literals

from django.db import models


class User(models.Model):
    """
    ORM for User objects
    Fields :
        1. name     -> name, not unique, not constant
        2. handle   -> user_name, login credential, unique, constant
        3. password -> password, not unique, not constant
    """
    name = models.CharField(max_length=50)
    handle = models.CharField(max_length=50)
    password = models.CharField(max_length=50)
    friends = models.ManyToManyField('User')
    # Empty string if currently active
    # Last active time stamp if currently inactive
    last_active = models.CharField(max_length=50)

    def __str__(self):
        """
        :return: string representation of current object
        """
        return str(self.pk) + ' : ' + self.name + '@' + self.handle


class UserToUserMessage(models.Model):
    """
    ORM for User to User message objects
    Fields : 
        1. sender       -> fk of the sender of this message
        2. receiver     -> fk of the receiver of this message
        3. content      -> actual content of the message
        3. time_stamp   -> the time when message is stored in db in UTC time
        
        fk : foreign key
    """
    sender = models.ForeignKey(User, on_delete=models.CASCADE, null=True, related_name='sender')
    receiver = models.ForeignKey(User, on_delete=models.CASCADE, null=True, related_name='receiver')
    content = models.TextField()
    time_stamp = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        """
        :return: string representation of current object
        """
        return str(self.pk) + ' From ' + str(self.sender_id) + ' to ' + str(self.receiver_id) + ' : ' + self.content


class FriendRequest(models.Model):
    """
    ORM for Friend request objects
    Fields : 
        1. sender       -> fk of the sender of this friend request
        2. receiver     -> fk of the receiver of this friend request
        3. message      -> an intro of sender given to receiver
        3. status       -> the status of the friend request, can be one among the statuses in Status class
        
        fk : foreign key
    """
    class Status:
        """
        Container for statuses of a Friend request
        """
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
        """
        :return: string representation of current object
        """
        return str(self.pk) + ' From ' + str(self.sender_id) + ' to ' + str(self.receiver_id) + ' status ' \
            + self.status
