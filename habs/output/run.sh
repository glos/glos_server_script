#!/bin/bash
if [ $# -ne 1 ] ; then
    echo 'need buoy id'
    exit -1
fi
../obs2nc $1
if [ $? -eq 0 ]; then
for f in ./*.nc; do
    /usr/bin/md5sum $f > ${f}.md5
done
base=$(basename $1)
tar -zcf glos_${base}_$(date +%Y_%m_%d).tar.gz ${base}*.nc*
/usr/bin/md5sum glos_${base}_$(date +%Y_%m_%d).tar.gz > glos_${base}_$(date +%Y_%m_%d).tar.gz.md5
rm ./${base}*.nc*
else
    echo "Error occurred!Abort!"
fi
