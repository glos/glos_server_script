elect  extract (epoch from m_date) as epoch,m_value from limno_tollsps g
inner join sensor s on s.row_id=g.sensor_id
inner join platform p on p.row_id=s.platform_id
where p.other_id='tollsps'
and s.short_name='WTMP'
order by epoch asc) to '/tmp/tollsps_sea_surface_water_temp' with CSV;
