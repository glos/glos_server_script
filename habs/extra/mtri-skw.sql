copy (select  extract (epoch from m_date) as epoch,m_value,m_value_2 from mtri_skw g
inner join sensor s on s.row_id=g.sensor_id
inner join platform p on p.row_id=s.platform_id
where p.other_id=upper('mtri-skw')
and s.short_name='TTAD'
order by epoch asc) to '/tmp/mtri-skw_sea_water_temp' with CSV;
