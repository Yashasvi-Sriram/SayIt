#!/usr/bin/env bash
# Configure Django
export DJANGO_SETTINGS_MODULE=SayIt_Dj.settings
echo "Django Configured."
# Starting Listener
python -m main.listener