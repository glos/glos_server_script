### 50 Days Data processing###
1. Download data to GLOS Storage at /var/local/glos/storage/GLCFS/Archive/YYYY
  An example of a filename is "c201630100.in1.nc" 
    The first letter represents a great lake. For example "h" stands for Lake Huron.
    The firt four numbers are the year.
    Numbers 5-7 are the day of year that starts the dataset.
    Numbers 8-9 is the hour that starts the dataset.
    There should be three files for each lake that have the extensions ".in1", ".out1", and ".out3."
2. Check 6 hours file at /var/local/glos/storage/GLCFS/Nowcast
3. Remove 6 hours files from the /Nowcast that fall into the time span of 50 days file based upon the naming convention of the file name
4. Restart THREDDS server (optional)
