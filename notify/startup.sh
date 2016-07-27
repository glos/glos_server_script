#!/bin/bash

source ../.pyvirtualenvs/notify/bin/activate
#replace current shell with the program below
#no new process will be created
#supervisord can't kill child process
exec python notify.py
