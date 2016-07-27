import os
import psycopg2

_prefix="leoc_"
_sqls={
"sea_surface_water_temperature":"select extract (epoch from m_date) as epoch,m_value from glos_leoc g inner join sensor s on s.row_id=g.sensor_id inner join platform p on p.row_id=s.platform_id where p.other_id='leoc' and s.short_name='WTMP' and g.m_date>= timestamp with time zone '{0} 00:00:00+00' and g.m_date<= timestamp with time zone '{1} 23:59:59+00' order by epoch asc",
"ph":"select extract (epoch from m_date) as epoch,m_value from glos_leoc g inner join sensor s on s.row_id=g.sensor_id inner join platform p on p.row_id=s.platform_id where p.other_id='leoc' and s.short_name='PH' and g.m_date>= timestamp with time zone '{0} 00:00:00+00' and g.m_date<= timestamp with time zone '{1} 23:59:59+00' order by epoch asc",
"water_conductivity":"select extract (epoch from m_date) as epoch,m_value from glos_leoc g inner join sensor s on s.row_id=g.sensor_id inner join platform p on p.row_id=s.platform_id where p.other_id='leoc' and s.short_name='SPCOND' and g.m_date>= timestamp with time zone '{0} 00:00:00+00' and g.m_date<= timestamp with time zone '{1} 23:59:59+00' order by epoch asc",
"ysi_blue_green_algae":"select  extract (epoch from m_date) as epoch,m_value from glos_leoc g inner join sensor s on s.row_id=g.sensor_id inner join platform p on p.row_id=s.platform_id where p.other_id='leoc' and s.short_name='YBGALG' and g.m_date>= timestamp with time zone '{0} 00:00:00+00' and g.m_date<= timestamp with time zone '{1} 23:59:59+00' order by epoch asc",
"ysi_chlorophyll":"select  extract (epoch from m_date) as epoch,m_value from glos_leoc g inner join sensor s on s.row_id=g.sensor_id inner join platform p on p.row_id=s.platform_id where p.other_id='leoc' and s.short_name='YCHLOR' and g.m_date>= timestamp with time zone '{0} 00:00:00+00' and g.m_date<= timestamp with time zone '{1} 23:59:59+00' order by epoch asc",
"dissolved_oxygen_saturation":"select  extract (epoch from m_date) as epoch,m_value from glos_leoc g inner join sensor s on s.row_id=g.sensor_id inner join platform p on p.row_id=s.platform_id where p.other_id='leoc' and s.short_name='DIOSAT' and g.m_date>= timestamp with time zone '{0} 00:00:00+00' and g.m_date<= timestamp with time zone '{1} 23:59:59+00' order by epoch asc",
"dissolved_oxygen":"select  extract (epoch from m_date) as epoch,m_value from glos_leoc g inner join sensor s on s.row_id=g.sensor_id inner join platform p on p.row_id=s.platform_id where p.other_id='leoc' and s.short_name='DISOXY' and g.m_date>= timestamp with time zone '{0} 00:00:00+00' and g.m_date<= timestamp with time zone '{1} 23:59:59+00' order by epoch asc"
}

def fetch(connstr,date,register):
    if not connstr:
	print("A valid connection string is required for psycopg2")
	return
    try:
	basedir=os.path.dirname(os.path.realpath(__file__))
	conn=psycopg2.connect(connstr)
	for k in _sqls:
	    cur=conn.cursor()
	    cur.execute(_sqls[k].format(date,date))
	    rows=cur.fetchall()
	    f=open(os.path.join(basedir,"{0}{1}".format(_prefix,k)),"w")
	    for row in rows:
	        f.write("{0},{1}\n".format(int(row[0]),row[1]))
	    f.close()
	    register[k]="{0}{1}.nc".format(_prefix,k)
    except Exception,e:
	print("Have trouble to fetch from {0}".format(connstr))
	print e 
    else:
	conn.close()
	return register
