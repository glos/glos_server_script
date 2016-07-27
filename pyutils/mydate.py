#!/usr/bin/env python
"""
USAGE:


"""
from datetime import timedelta
from datetime import datetime
__mydate_noleap=(0,31,28,31,30,31,30,31,31,30,31,30,31)
__mydate_leap=(0,31,29,31,30,31,30,31,31,30,31,30,31)
def isLeapYear(year):
        if isinstance(year,int) and year>0:
                return year%400==0 or (year%4==0 and year%100!=0)
        else:
                return False
def getYearMonthDay(dayofyear,year):
        if isinstance(dayofyear,int) and isinstance(year,int) and year>0  and dayofyear>0 and dayofyear<=366:
                if isLeapYear(year)==True:
                        darray=__mydate_leap
                else:
                        darray=__mydate_noleap
                month=1
                while dayofyear>darray[month] and month<=12:
                        dayofyear-=darray[month]
                        month+=1
                else:
                        if month<=12:
                                return (year,month,dayofyear)
                        else:#i will be surprised if this actually happens
                                return None
        else:
                return None
def getDateTimePlus(year,month,day,hour,minute,second,plus):
        return datetime(year,month,day,hour,minute,second,plus)+timedelta(hours=plus)
