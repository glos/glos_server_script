#!/bin/bash
PY=hecwfs.py
PREFIX=/usr/local/src/glos/glos_scripts
DIR=hecwfs
VIRTUALENV_NAME=/usr/local/glos/.pyvirtualenvs/harvesting
APP_ROOT=${PREFIX}/${DIR}
LOCAL_OUTPUT_NOWCAST=/var/local/glos/hecwfs/nowcast
LOCAL_OUTPUT_FORECAST=/var/local/glos/hecwfs/forecast
NC_PAT=*.nc
SHP_PAT=*.tar.gz
REMOTE_HOST=storage.glos.us
REMOTE_BASE_NOWCAST=/var/local/glos/storage/tmp/HECWFS/nowcast
REMOTE_BASE_FORECAST=/var/local/glos/storage/tmp/HECWFS/forecast

pushd $APP_ROOT
source $VIRTUALENV_NAME/bin/activate #$PREFIX/$VIRTUALENV_NAME/bin/activate
if [ ${1,,} == nowcast ]; then
    REMOTE_BASE=$REMOTE_BASE_NOWCAST
    LOCAL_BASE=$LOCAL_OUTPUT_NOWCAST
else
    REMOTE_BASE=$REMOTE_BASE_FORECAST
    LOCAL_BASE=$LOCAL_OUTPUT_FORECAST
fi
python $APP_ROOT/$PY $1
ret=$?
if [ $ret -eq 0 ];then
    ret=0
    for f in $(ls $LOCAL_BASE/$NC_PAT); do
	#echo "scp $f $USER@$REMOTE_HOST:$REMOTE_BASE" 
        scp $f $USER@$REMOTE_HOST:$REMOTE_BASE
	ret=$?
        rm $f
    done
    for f in $(ls $LOCAL_BASE/shp/$SHP_PAT); do
        scp $f $USER@$REMOTE_HOST:$REMOTE_BASE/shp/
        ret=$?
        rm $f
    done
    if [ $ret -ne 0 ]; then
	echo Oops!
	amqp-publish -e glos_notify_data_status -r hecwfs --url=amqp://mon.glos.us:5672 -b "HECWFS data trasnfer to storage may fail"
    fi
elif [ $ret -ne 3 ];then
    for f in $(ls $LOCAL_BASE/$NC_PAT); do
	rm $f
    done
fi
#py script should get these shp files removed, double check here:
#rm $LOCAL_BASE/shp/*

