copy (select  extract (epoch from m_date) as epoch,m_value,m_value_2 from mtri_nkw g
inner join sensor s on s.row_id=g.sensor_id
inner join platform p on p.row_id=s.platform_id
where p.other_id=upper('mtri-nkw')
and s.short_name='TTAD'
order by epoch asc) to '/tmp/mtri-nkw_sea_water_temp' with CSV;
