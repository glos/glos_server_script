#!/bin/bash
PY=glcfs.py
PREFIX=/usr/local/glos
DIR=glcfs
VIRTUALENV_NAME=.pyvirtualenvs/harvesting
APP_ROOT=${PREFIX}/${DIR}
LOCAL_OUTPUT=/var/local/glos/glcfs
FILE_PAT=*.gz
REMOTE_HOST=storage.glos.us
REMOTE_BASE_NOWCAST=/var/local/glos/storage/GLCFS/Nowcast
REMOTE_BASE_FORECAST=/var/local/glos/storage/GLCFS/Forecast

pushd $APP_ROOT
source $PREFIX/$VIRTUALENV_NAME/bin/activate
python $APP_ROOT/$PY $LOCAL_OUTPUT $1 $2
if [ $? -eq 0 ];then
    if [ ${1,,} == nowcast ]; then
	REMOTE_BASE=$REMOTE_BASE_NOWCAST
    else
	REMOTE_BASE=$REMOTE_BASE_FORECAST
    fi
    ret=0
    for f in $(ls $LOCAL_OUTPUT/$FILE_PAT); do
	#echo "scp $f $USER@$REMOTE_HOST:$REMOTE_BASE" 
        scp $f $USER@$REMOTE_HOST:$REMOTE_BASE
	ret=$?
        rf=$(basename $f)
        ssh $USER@$REMOTE_HOST "gunzip -fqd $REMOTE_BASE/$rf"
	ret=$?
        rm $f
    done
    if [ $ret -ne 0 ]; then
	echo Oops!
	amqp-publish -e glos_notify_data_status -r glcfs --url=amqp://mon.glos.us:5672 -b "GLCFS data trasnfer to storage may fail"
    fi
else
    #clean up the mess so the next successful download will not copy these files over
    for f in $(ls $LOCAL_OUTPUT/$FILE_PAT); do
	rm $f
    done
fi
popd
