#!/bin/bash
PREFIX=/usr/local/glos
DIR=habs
VIRTUALENV_NAME=.pyvirtualenvs/habs
LOCAL_OUTPUT=${PREFIX}/${DIR}
REMOTE_BASE=/var/local/glos/storage/HABS/lakes/

source $PREFIX/$VIRTUALENV_NAME/bin/activate
#export PYTHONPATH=$PYTHONPATH:${LOCAL_OUTPUT}/platform
echo "Fetch data for the platforms listed under "${LOCAL_OUTPUT}"/platform/"
python $PREFIX/$DIR/habs.py ${LOCAL_OUTPUT}/platform ${LOCAL_OUTPUT}/platform-config
