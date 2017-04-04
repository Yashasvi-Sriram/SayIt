from __future__ import unicode_literals

from django.db import models


# Create your models here.
class User(models.Model):
    handle = models.CharField(max_length=50)
    password = models.CharField(max_length=30)

    def __str__(self):
        return self.handle
