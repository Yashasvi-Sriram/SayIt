from __future__ import unicode_literals

from django.db import models


class User(models.Model):
    name = models.CharField(max_length=50)
    handle = models.CharField(max_length=50)
    password = models.CharField(max_length=50)

    def __str__(self):
        return self.handle
