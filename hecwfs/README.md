You can find HECWFS scripts at two places:
1. At data.glos.us: /var/local/glos/hecwfs
2. At process1.glos.us: /var/local/glos/glos_script/hecwfs

1 is for running HECWFS app at http://data.glos.us/hecwfs
2 is for populating GLOS NFS and then serve at THREDDS

Content of the hecwfs.py at 1: harvesting hecwfs NC from GLERL and then populating the glos_model database using shp file
               hecwfs.py at 2: harvesting hecwfs NC from GLERL and then scp to GLOS NFS

log file is at HECWFS_SCRIPT_HOME/logs

Database: on data.glos.us and constantly suffer disk space issue.
