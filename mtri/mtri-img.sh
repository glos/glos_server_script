#!/bin/bash
LOG=/usr/local/src/glos/glos_scripts/mtri/log/mtri.log
REMOTE_NAME=storage.glos.us
REMOTE_BASE=/var/local/glos/storage
FTP_ROOT=ftp://ftp.mtri.org/pub/DMAC/Weekly_Files/
IFS=,
FILES=Erie.zip,Huron.zip,Michigan.zip,Ontario.zip,Superior.zip
echo "******************************************" >> $LOG
for file in $FILES;do
    echo [`/bin/date '+%Y-%m-%d %H:%M:%S'`] "work on $file" >> $LOG
    wget ${FTP_ROOT}$file -O /tmp/$file > /dev/null 2>&1
    if [ -s /tmp/$file ]; then
        unzip /tmp/$file -d /tmp > /dev/null 2>>$LOG
        base=${file%.zip}
        lake=$(echo $base|tr '[A-Z'] '[a-z]')
        scp /tmp/$base/CHL/*.* $USER@${REMOTE_NAME}:${REMOTE_BASE}/MTRI-CHL/${lake}
        scp /tmp/$base/LST/*.* $USER@${REMOTE_NAME}:${REMOTE_BASE}/MTRI-LST/${lake}_current_year/converted
        scp /tmp/$base/NCI/*.* $USER@${REMOTE_NAME}:${REMOTE_BASE}/MTRI-NC/${lake}
        scp /tmp/$base/CDOM/*.* $USER@${REMOTE_NAME}:${REMOTE_BASE}/MTRI-CDOM/${lake}
        scp /tmp/$base/DOC/*.* $USER@${REMOTE_NAME}:${REMOTE_BASE}/MTRI-DOC/${lake}
        scp /tmp/$base/SM/*.* $USER@${REMOTE_NAME}:${REMOTE_BASE}/MTRI-SM/${lake}
        rm -r /tmp/$base/
        rm /tmp/$file
    else
        echo "No $file available for today?" >> $LOG
    fi
    echo [`/bin/date '+%Y-%m-%d %H:%M:%S'`] "done on $file" >>$LOG
done
echo "******************************************" >> $LOG
