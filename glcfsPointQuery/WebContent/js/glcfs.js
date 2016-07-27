var map=null;
var plon,plat,plake;


function textBoxError(jqid,errid,msg)
{
	if(jqid.hasClass('text-valid'))
		jqid.removeClass('text-valid');
	if(jqid.hasClass('text-error')==false)
		jqid.addClass('text-error');
	errid.html(msg);
	errid.show();
}
function textBoxResume(jqid,errid)
{
	if(jqid.hasClass('text-error'))
		jqid.removeClass('text-error');
	if(jqid.hasClass('text-valid')==false)
		jqid.addClass('text-valid');
	errid.html("");
	errid.hide();
}
var roundCnt=1000000.;
function convertLonLat2DMS(lon,lat)
{
	var signlon=lon>=0?1:-1;
	var signlat=lat>=0?1:-1;
	lon=Math.abs(Math.round(lon*roundCnt));
	lat=Math.abs(Math.round(lat*roundCnt));
	var obj=new Object();
	obj.lon=new Object();
	obj.lon.D=Math.floor(lon/roundCnt)*signlon;
	obj.lon.M=Math.floor(((lon/roundCnt)-Math.floor(lon/roundCnt))*60.);
	obj.lon.S=Math.floor(((((lon/roundCnt)-Math.floor(lon/roundCnt))*60)-Math.floor(((lon/roundCnt)-Math.floor(lon/roundCnt))*60.))*roundCnt)*60./roundCnt;
	obj.lat=new Object();
	obj.lat.D=Math.floor(lat/roundCnt)*signlat;
	obj.lat.M=Math.floor(((lat/roundCnt)-Math.floor(lat/roundCnt))*60.);
	obj.lat.S=Math.floor(((((lat/roundCnt)-Math.floor(lat/roundCnt))*60)-Math.floor(((lat/roundCnt)-Math.floor(lat/roundCnt))*60.))*roundCnt)*60./roundCnt;
	return obj;
}
function convertLonLatDMS2Dec(lonD,lonM,lonS,latD,latM,latS)
{
	var signlon=lonD>=0?1:-1;
	var signlat=latD>=0?1:-1;
	lonD=Math.abs(Math.round(lonD*roundCnt));
	latD=Math.abs(Math.round(latD*roundCnt));
	lonM=Math.abs(Math.round(lonM*roundCnt));
	latM=Math.abs(Math.round(latM*roundCnt));
	lonS=Math.abs(Math.round(lonS*roundCnt));
	latS=Math.abs(Math.round(latS*roundCnt));
	var obj=new Object();
	obj.lon=Math.round(lonD+lonM/60.+lonS/3600.)*signlon/roundCnt;
	obj.lat=Math.round(latD+latM/60.+latS/3600.)*signlat/roundCnt;
	return obj;
}
function setupLonLat(lon,lat,id)
{
	if((id!=null&&id=="rdo_loc_dec")||"rdo_loc_dec"==$("[name=locradio]:checked").attr("id"))
	{
		$("#txt_lon_dec").val(lon);
		$("#txt_lat_dec").val(lat);
		textBoxResume($("#txt_lon_dec"),$("#err_txt_lon_dec"));
		textBoxResume($("#txt_lat_dec"),$("#err_txt_lat_dec"));
	}
	else if((id!=null&&id=="rdo_loc_mm")||"rdo_loc_mm"==$("[name=locradio]:checked").attr("id"))
	{
		var ll=convertLonLat2DMS(lon,lat);
		$("#txt_lon_deg").val(ll.lon.D);
		$("#txt_lon_min").val(ll.lon.M);
		$("#txt_lon_sec").val(ll.lon.S.toFixed(2));
		$("#txt_lat_deg").val(ll.lat.D);
		$("#txt_lat_min").val(ll.lat.M);
		$("#txt_lat_sec").val(ll.lat.S.toFixed(2));
	}
}
function validateLon()
{
	var result=false;
	if("rdo_loc_dec"==$("[name=locradio]:checked").attr("id"))
	{
		var lon=parseFloat($("#txt_lon_dec").val());
		if(!isNaN(lon))
		{
			if(Math.abs(lon)>180)
			{
				textBoxError($("#txt_lon_dec"),$("#err_txt_lon_dec"),"Longitude must be in the range of -180 to 180");
		
			}
			else
			{
				textBoxResume($("#txt_lon_dec"),$("#err_txt_lon_dec"));
				result=true;
			}
		}
		else
		{
			textBoxError($("#txt_lon_dec"),$("#err_txt_lon_dec"),"Longitude must be a number and in the range of -180 to 180");
		
		}
	}
	else
	{
		var d=parseFloat($("#txt_lon_deg").val());
		var m=parseFloat($("#txt_lon_min").val());
		var s=parseFloat($("#txt_lon_sec").val());
		
		if(!(isNaN(d)&&isNaN(m)&&isNaN(s)))
		{
			d=Math.abs(d);
			if(d>180||(d==180&&(m>0||s>0)))
			{
				textBoxError($("#txt_lon_min"),$("#err_txt_lon_dms"),"");
				textBoxError($("#txt_lon_sec"),$("#err_txt_lon_dms"),"");
				textBoxError($("#txt_lon_deg"),$("#err_txt_lon_dms"),"Longitude must be in the range of -180 to 180");
			}
			else if(m<0||m>=60||s<0||s>=60)
			{
				textBoxError($("#txt_lon_min"),$("#err_txt_lon_dms"),"");
				textBoxError($("#txt_lon_sec"),$("#err_txt_lon_dms"),"Minute and Second must be in the range of 0 to 60");
			}
			else
			{
				textBoxResume($("#txt_lon_min"),$("#err_txt_lon_dms"));
				textBoxResume($("#txt_lon_sec"),$("#err_txt_lon_dms"));
				textBoxResume($("#txt_lon_deg"),$("#err_txt_lon_dms"));
				result=true;
				
			}
		}
		else
		{
			textBoxError($("#txt_lon_min"),$("#err_txt_lon_dms"),"");
			textBoxError($("#txt_lon_sec"),$("#err_txt_lon_dms"),"");
			textBoxError($("#txt_lon_deg"),$("#err_txt_lon_dms"),"Longitude must be a number and in the range of -180 to 180");
			
		}
		 
	}
	return result;
}
function validateLat()
{
	var result=false;
	if("rdo_loc_dec"==$("[name=locradio]:checked").attr("id"))
	{
		var lat=parseFloat($("#txt_lat_dec").val());
		if(!isNaN(lat))
		{
			if(Math.abs(lat)>90)
			{
				textBoxError($("#txt_lat_dec"),$("#err_txt_lat_dec"),"Latitude must be in the range of -90 to 90");
				
			}
			else
			{
				textBoxResume($("#txt_lat_dec"),$("#err_txt_lat_dec"));
				result=true;
			}
		}
		else
		{
			textBoxError($("#txt_lat_dec"),$("#err_txt_lat_dec"),"Latitude must be a number and in the range of -90 to 90");
			
		}
	}
	else
	{
		var d=parseFloat($("#txt_lat_deg").val());
		var m=parseFloat($("#txt_lat_min").val());
		var s=parseFloat($("#txt_lat_sec").val());
		
		if(!(isNaN(d)&&isNaN(m)&&isNaN(s)))
		{
			d=Math.abs(d);
			if(d>180||(d==180&&(m>0||s>0)))
			{
				textBoxError($("#txt_lat_min"),$("#err_txt_lat_dms"),"");
				textBoxError($("#txt_lat_sec"),$("#err_txt_lat_dms"),"");
				textBoxError($("#txt_lat_deg"),$("#err_txt_lat_dms"),"Latitude must be in the range of -90 to 90");
			}
			else if(m<0||m>=60||s<0||s>=60)
			{
				textBoxError($("#txt_lat_min"),$("#err_txt_lat_dms"),"");
				textBoxError($("#txt_lat_sec"),$("#err_txt_lat_dms"),"Minute and Second must be in the range of 0 to 60");
			}
			else
			{
				textBoxResume($("#txt_lat_min"),$("#err_txt_lat_dms"));
				textBoxResume($("#txt_lat_sec"),$("#err_txt_lat_dms"));
				textBoxResume($("#txt_lat_deg"),$("#err_txt_lat_dms"));
				result=true;
				
			}
		}
		else
		{
			textBoxError($("#txt_lat_min"),$("#err_txt_lat_dms"),"");
			textBoxError($("#txt_lat_sec"),$("#err_txt_lat_dms"),"");
			textBoxError($("#txt_lat_deg"),$("#err_txt_lat_dms"),"Latitude must be a number and in the range of -90 to 90");
			
		}
	}
	return result;
}
function getLonLat()
{
	var lonlat=new Object();
	var lon,lat;
	if("rdo_loc_dec"==$("[name=locradio]:checked").attr("id"))
	{
		lat=parseFloat($("#txt_lat_dec").val());
		lon=parseFloat($("#txt_lon_dec").val());
		if(!isNaN(lon)&&!isNaN(lat))
		{
			lonlat.lon=lon;
			lonlat.lat=lat;
		}
	}
	else
	{
		var dlat=parseFloat($("#txt_lat_deg").val());
		var mlat=parseFloat($("#txt_lat_min").val());
		var slat=parseFloat($("#txt_lat_sec").val());

		var dlon=parseFloat($("#txt_lon_deg").val());
		var mlon=parseFloat($("#txt_lon_min").val());
		var slon=parseFloat($("#txt_lon_sec").val());

		if(!isNaN(dlat)&&!isNaN(mlat)&&!isNaN(slat)&&!isNaN(dlon)&&!isNaN(mlon)&&!isNaN(slon))
			return convertLonLatDMS2Dec(dlon,mlon,slon,dlat,mlat,slat);
	}
	return lonlat;
}
var depLonlat=null;
var depRange=null;
function setDepth()
{
	if(!$('#rdo_mt_n3').attr('checked'))return;
	var lonlat=getLonLat();
	if(lonlat.lon==null||lonlat.lat==null||$('#sel_lname option:selected').val()=='')
	{
		$('#err_txt_depth').html('Can not retrieve depth information. Is location information in section 1 correct?');
		$('#err_txt_depth').show();
	}
	else
	{
		$('#err_txt_depth').html('');
		$('#err_txt_depth').hide();
		if(depLonlat!=null&&depRange!=null&&lonlat.lon.toFixed(4)==depLonlat.lon.toFixed(4)&&lonlat.lat.toFixed(4)==depLonlat.lat.toFixed(4))
		{
			if($('#sel_unit option:selected').val()=='m')
				$('#sp_depth_range').html("(Depth range: 0 - "+depRange.toFixed(2)+" meters)");
			else
				$('#sp_depth_range').html("(Depth range: 0 - "+(depRange*3.2808399).toFixed(2)+" feet)");
		}
		else
		{
			depLonlat=lonlat;
			$.get('glcfsps-d3d.glos',{"x":lonlat.lon,"y":lonlat.lat,"l":$('#sel_lname option:selected').val()},
				function(data){
					if(data!=null&&data.bathy!=null)
					{
						depRange=data.bathy;
						if($('#sel_unit option:selected').val()=='m')
						{
							$('#sp_depth_range').html("(Depth range: 0 -- "+depRange.toFixed(2)+" meters)");
						}
						else
						{
							$('#sp_depth_range').html("(Depth range: 0 -- "+(depRange*3.2808399).toFixed(2)+" feet)");
						}
					}
					else
					{
						$('#err_txt_depth').html('Fail to retrieve depth information.');
						$('#err_txt_depth').show();
					}
			},'json');
		}
		
	}
}

$(function() {
	$("#txt_lon_dec").val("");
	$("#txt_lat_dec").val("");
	$("#sp_lon_mm > input").each(function(){$(this).val("");});
	$("#sp_lat_mm > input").each(function(){$(this).val("");});
	$("#rdo_loc_dec").attr('checked',true);
	$("#rdo_mt_n").attr('checked',true);
	$("#rdo_dt_historic").attr('checked',true);
	$('#sel_lname option:eq(0)').attr('selected',true);
	$('#rdo_order_asc').attr('checked',true);
	//$( "#div_rdo_loc" ).buttonset();
	$("#btn_map").button();
	$('#btn_range').button();
	$('#txt_ds').val("");
	$('#txt_de').val("");
	var dc=new Date();
	var ofst=dc.getTimezoneOffset()/60*-1;
	$('#sel_tzone').val(ofst);
	var dates=$("#txt_ds, #txt_de").datepicker(
	{
		changeMonth: true,
    	changeYear: true,
		minDate:new Date(2006,0,1,1,0,0,0),
		maxDate:new Date(new Date().getTime()+1296000000),
		dateFormat:'yy-mm-dd',
		onSelect:function(selectedDate){
			var option = this.id == "txt_ds" ? "minDate" : "maxDate",
			instance = $( this ).data( "datepicker" ),
			date = $.datepicker.parseDate(
					instance.settings.dateFormat ||
					$.datepicker._defaults.dateFormat,
					selectedDate, instance.settings );
			dates.not( this ).datepicker( "option", option, date );
		}
	});

	$("#txt_lon_dec").blur(function(){
		if($.trim($("#txt_lon_dec").val())=="")return;
		validateLon();
	});
	
	$("#txt_lat_dec").blur(function(){
		if($.trim($("#txt_lat_dec").val())=="")return;
		validateLat();
	});
	
	$("[name='locradio']").change(function(){
		if('rdo_loc_dec'==$(this).attr('id'))
		{
			var bEmpty=true;
			$("#sp_lon_mm > input").each(function(){bEmpty=($.trim($(this).val())=="");});
			$("#sp_lat_mm > input").each(function(){bEmpty=($.trim($(this).val())=="");});
			$("#rdo_loc_mm").attr("checked",true);
			var bVal=false;
			if(!bEmpty)
			{
				bVal=validateLon();
				bVal=validateLat();
			}
			if((bEmpty)||(bVal))
			{
				
				if(bVal)
				{
					var obj=convertLonLatDMS2Dec(parseFloat($("#txt_lon_deg").val()),parseFloat($("#txt_lon_min").val()),parseFloat($("#txt_lon_sec").val()),
											parseFloat($("#txt_lat_deg").val()),parseFloat($("#txt_lat_min").val()),parseFloat($("#txt_lat_sec").val()));
					
					setupLonLat(obj.lon,obj.lat,"rdo_loc_dec");
				}
				$("#rdo_loc_dec").attr("checked",true);
				$("#dv_lon_dec").show();
				$("#sp_lon_mm").hide();
				$("#dv_lat_dec").show();
				$("#sp_lat_mm").hide();
			}
			else
				$("#rdo_loc_mm").attr("checked",true);
			//convertLonLatDMS2Dec
			
		}
	    else
		{
	    	var lo=parseFloat($("#txt_lon_dec").val());
			var la=parseFloat($("#txt_lat_dec").val());
			if(!isNaN(lo)&&!isNaN(la))
			{
				setupLonLat(lo,la,"rdo_loc_mm");
				
			}
			else if($.trim($("#txt_lon_dec").val())!=""||$.trim($("#txt_lat_dec").val())!="")
			{
				$("#rdo_loc_dec").attr("checked",true);
				validateLon();
				validateLat();
				return;
			}
			$("#dv_lon_dec").hide();
			$("#sp_lon_mm").show();
			$("#dv_lat_dec").hide();
			$("#sp_lat_mm").show();
		}
	});
	
	$("[name='mtradio']").change(function(){
		if('rdo_mt_n'==this.id)
		{
			$('#sel_ts1').show();
			$('#sel_ts3').hide();
			$('#sel_te1').show();
			$('#sel_te3').hide();
			$('#dv_n2').show();
			$('#dv_n3').hide();
			$('#dv_force_f').hide();
			$('#dv_force_n').hide();
		}
		else if('rdo_mt_n3'==this.id)
		{
			$('#sel_ts3').show();
			$('#sel_ts1').hide();
			$('#sel_te3').show();
			$('#sel_te1').hide();
			$('#dv_n2').hide();
			$('#dv_n3').show();
			$('#dv_force_f').hide();
			$('#dv_force_n').hide();
			setDepth();
			
		}
		else if('rdo_mt_f'==this.id)
		{
			$('#sel_ts1').show();
			$('#sel_ts3').hide();
			$('#sel_te1').show();
			$('#sel_te3').hide();
			$('#dv_n2').show();
			$('#dv_n3').hide();
			$('#dv_force_f').hide();
			$('#dv_force_n').hide();
		}
		else if('rdo_mt_force_n'==this.id)
		{
			$('#sel_ts1').show();
			$('#sel_ts3').hide();
			$('#sel_te1').show();
			$('#sel_te3').hide();
			$('#dv_n2').hide();
			$('#dv_n3').hide();
			$('#dv_force_n').show();
			$('#dv_force_f').hide();
		}
		else if('rdo_mt_force_f'==this.id)
		{
			$('#sel_ts1').show();
			$('#sel_ts3').hide();
			$('#sel_te1').show();
			$('#sel_te3').hide();
			$('#dv_n2').hide();
			$('#dv_n3').hide();
			$('#dv_force_f').show();
			$('#dv_force_n').hide();
		}
	});

	$("[name='dtradio']").change(function(){
		if('rdo_dt_historic'==$(this).attr('id'))
		{
			$("#dv_lat").hide();
			$("#tab_dt").show();
			
		}
	    else
		{
	    	$("#dv_lat").show();
			$("#tab_dt").hide();
		}
	});

	$('#sel_unit').change(function(){
		if(!$('#rdo_mt_n3').attr('checked'))return;
		setDepth();
	});

	$('#btn_range').click(function(){
		setDepth();
	});
	
	$('#btn_download').button();
	$('#btn_download').click(function(){
		var bvalid=validateLat();
		bvalid=validateLon();
		var lname=$('#sel_lname option:selected').val();
		if(lname=='')
		{
			textBoxError($('#sel_lname'),$('#err_sel_lname'),'Lake name is not valid');
			bvalid=false;
		}
		else
		{
			textBoxResume($('#sel_lname'),$('#err_sel_lname'));
		}
		var blatest=false;
		var st=null,et=null,hour;
		if($('#rdo_dt_historic').attr('checked'))
		{
			st=$('#txt_ds').val();
			if(st=='')
			{
				textBoxError($('#txt_ds'),$('#err_txt_ds'),'Start date is not valid');
				bvalid=false;
			}
			else
			{
				if($('#sel_ts1').is(':visible'))
					st=st+':'+$('#sel_ts1 option:selected').val();
				else
					st=st+':'+$('#sel_ts3 option:selected').val();
				textBoxResume($('#txt_ds'),$('#err_txt_ds'));
			}
			et=$('#txt_de').val();
			if(et=='')
			{
				textBoxError($('#txt_de'),$('#err_txt_de'),'End date is not valid');
				bvalid=false;
			}
			else
			{
				if($('#sel_te1').is(':visible'))
					et=et+':'+$('#sel_te1 option:selected').val();
				else
					et=et+':'+$('#sel_te3 option:selected').val();
				textBoxResume($('#txt_de'),$('#err_txt_de'));
			}
		}
		else
		{
			blatest=true;
			hour=$('#txt_hours').val();
			if(hour=='')
			{
				textBoxError($('#txt_hours'),$('#err_txt_hours'),'Hour is not valid');
				bvalid=false;
			}
			else
				textBoxResume($('#txt_hours'),$('#err_txt_hours'));
		}

		var bNowcast=false;
		var bInfile=false;
		
		if($('#rdo_mt_n').attr('checked')||$('#rdo_mt_n3').attr('checked')||$('#rdo_mt_force_n').attr('checked'))
		{
			bNowcast=true;
		}

		if($('#rdo_mt_force_n').attr('checked')||$('#rdo_mt_force_f').attr('checked'))
			bInfile=true;
		var vars='';
		if($('#rdo_mt_n').attr('checked')||$('#rdo_mt_f').attr('checked'))
		{
			$('#dv_n2').find('input').each(function(){
				if($(this).attr('checked'))
					vars=vars+$(this).val()+',';
			});
		}
		else if($('#rdo_mt_n3').attr('checked'))
		{
			$('#dv_n3').find('input').each(function(){
				if($(this).attr('checked'))
					vars=vars+$(this).val()+',';
			});
		}
		else if($('#rdo_mt_force_n').attr('checked'))
		{
			$('#dv_force_n').find('input').each(function(){
				if($(this).attr('checked'))
					vars=vars+$(this).val()+',';
			});
		}
		else if($('#rdo_mt_force_f').attr('checked'))
		{
			var d=new Date;
			var cy=d.getFullYear();
			if(!blatest&&
				((st!=null&&st.length>=4&&st.substr(0,4)!=cy)||
				(et!=null&&et.length>=4&&et.substr(0,4)!=cy)))
			{
				if(st!=null&&st.length>=4&&st.substr(0,4)!=cy)
					textBoxError($('#txt_ds'),$('#err_txt_ds'),'Input Data for Forecast only available for '+cy);
				if(et!=null&&et.length>=4&&et.substr(0,4)!=cy)
					textBoxError($('#txt_de'),$('#err_txt_de'),'Input Data for Forecast only available for '+cy);
				bvalid=false;
			}
			else
			{
				$('#dv_force_f').find('input').each(function(){
					if($(this).attr('checked'))
						vars=vars+$(this).val()+',';
				});
				if(bvalid)
				{
					textBoxResume($('#txt_ds'),$('#err_txt_ds'));
					textBoxResume($('#txt_de'),$('#err_txt_de'));
				}
			}
		}
		if(vars.length>0)
		{
			$('#err_vars').html('');
			$('#err_vars').hide();
			vars=vars.substr(0,vars.length-1);
		}
		else
		{
			$('#err_vars').html('Please selecat at least one paramters from the list below<br/>');
			$('#err_vars').show();
			bvalid=false;
		}
		var rdepth;
		if($('#rdo_mt_n3').attr('checked'))
		{
			rdepth=parseFloat($('#txt_rdepth').val());
			if(isNaN(rdepth)||($('#sel_unit').val()=='m'&&(rdepth<0||rdepth>depRange))||($('#sel_unit').val()=='e'&&(rdepth<0||rdepth>depRange*3.2808399)))
			{
				bvalid=false;
				textBoxError($('#txt_rdepth'),$('#err_txt_depth'),'Requested depth is not valid');
			}
			else
				textBoxResume($('#txt_rdepth'),$('#err_txt_depth'));
		}
		if(bvalid)
		{
			var lonlat=getLonLat();
			var url='glcfsps.glos?lake='+lname+'&i='+lonlat.lon+'&j='+lonlat.lat+'&v='+vars;
			if(!bNowcast)
				url=url+'&t=forecast';
			if(bInfile)
				url=url+'&in=1';
			if(blatest)
				url=url+'&latest=1&h='+hour;
			else
				url=url+'&st='+st+'&et='+et;
			if($('#rdo_mt_n3').attr('checked'))
				url=url+'&rdepth='+rdepth;
			if($('#sel_unit option:selected').val()=='e')
				url=url+'&u=e';
			if($('#rdo_order_asc').attr('checked'))
				url=url+'&order=asc';
			if($('#chk_pv').attr('checked'))
				url=url+'&pv=1';
			if($('#chk_gi').attr('checked'))
				url=url+'&gi=1';
			if($('#chk_doy').attr('checked'))
				url=url+'&doy=1';
			url=url+'&tzf='+$('#sel_tzone option:selected').val();
			url=url+'&f='+$('#sel_datafmt option:selected').val();
			
			$('#btn_download').attr('href',url);
		}
		return bvalid;
	});
	Proj4js.defs["EPSG:4269"] = "+proj=longlat +ellps=GRS80 +datum=NAD83 +no_defs";
	Proj4js.defs["EPSG:3857"]= "+title=GoogleMercator +proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +no_defs";
	var prjdist= new OpenLayers.Projection("EPSG:4269");
	var p_marker=null;
	var p_marker2=null;
	var lyr_marker=null;
	var plonlat=null;
	var pi,pj,pdist;
	function friendlyDistanceLabel(dist)
	{
		if(dist>500)
			return (dist/1000.0).toFixed(2)+" km ("+(dist/(1000.0*1.609)).toFixed(2)+" mile)";
		if(dist>100)
			return dist.toFixed(2)+" m ("+(dist*1.0936133).toFixed(2)+" yard)";
		if(dist>1)
			return dist.toFixed(2)+" m ("+(dist*3.2808399).toFixed(2)+" feet)";
		if(dist>0)
			return dist.toFixed(2)+" m ("+(dist*39.3700787).toFixed(2)+" inch)";
		return "0";
	}
	function removeMarker(lyr,marker)
	{
		if(lyr==null||marker==null)return;
		if(marker.popup!=null)
    	{
        	marker.popup.feature=null;
        	map.removePopup(marker.popup);
        	marker.popup.destroy();
        	marker.popup=null;
    	}
        lyr.removeMarker(marker);
	}
	function addPopup(marker)
	{
		var lonlat=marker.lonlat.clone().transform(map.getProjectionObject(),prjdist);
		var html="";
		if(marker.pi==null||marker.pj==null)
			html="<table><tr><td>Longitude:</td><td>"+lonlat.lon.toFixed(4)+"&nbsp;Degree</td></tr><tr><td>Latitude:</td><td>"+lonlat.lat.toFixed(4)+"&nbsp;Degree</td></tr></table>";
		else if(marker.dist==null)
			html="<table><tr><td>Longitude:</td><td>"+lonlat.lon.toFixed(4)+"&nbsp;Degree</td></tr><tr><td>Latitude:</td><td>"+lonlat.lat.toFixed(4)+"&nbsp;Degree</td></tr><tr><td>Cell X:</td><td>"+marker.pi+"</td></tr><tr><td>Cell Y:</td><td>"+marker.pj+"</td></tr></table>";
		else
			html="<table><tr><td>Longitude:</td><td>"+lonlat.lon.toFixed(4)+"&nbsp;Degree</td></tr><tr><td>Latitude:</td><td>"+lonlat.lat.toFixed(4)+"&nbsp;Degree</td></tr><tr><td>Cell X:</td><td>"+marker.pi+"</td></tr><tr><td>Cell Y:</td><td>"+marker.pj+"</td></tr><tr><td>Distance:</td><td>"+friendlyDistanceLabel(marker.dist)+"</td></tr></table>";
		var popup=new OpenLayers.Popup.FramedCloud("popup"+new Date().getTime(),marker.lonlat,new OpenLayers.Size(250,200),
													html,null,true,null);
		marker.popup=popup;
		popup.feature=marker;
		map.addPopup(popup);
	}
	function markerOnMouseDown(evt)
	{
		if(this.popup!=null)
			this.popup.show();
		else
			addPopup(this);
		OpenLayers.Event.stop(evt);
	}
	$("#btn_map").click(function(){
		$( "#dlg_map" ).dialog({
			resizable: false,
			height: 'auto',
			width:1000,
			modal: true,
			draggable:false,
			buttons:{
				"OK":function()
		    	{
	            	if(plonlat!=null)
	            	{
						setupLonLat(plonlat.lon.toFixed(4),plonlat.lat.toFixed(4));
						$('#sel_lname option').each(function(){
							if($(this).val()==plake)
								$(this).attr('selected',true);
						});
						setDepth();
	            		$(this).dialog("close");
	            	}
	            	else
	            	{
						$("#div_errmsg").html("<span>No Point Has Been Selected Yet.</span>");
		            	$("#dlg_error").dialog(
				            	{resizable:false,
					            height:'auto',
					            width:400,
					            modal:true,
					            buttons:{"OK":function(){$(this).dialog("close");}}});
	            	}
            	},
				"Cancel":function()
			    {
		            $(this).dialog("close");
	            }
	        },
		    open:function(){
		    	if(map==null)
		    	{
		    		OpenLayers.IMAGE_RELOAD_ATTEMPTS=5;
		    		

		    	var extent=new OpenLayers.Bounds(-10473707.362244375,5104770.496306875,-8272320.948025625,6229923.5524631245);
			var mext=new OpenLayers.Bounds(-10807503,4493274,-8038525,6841420)
			var options = {
		                    projection: new OpenLayers.Projection("EPSG:3857"),
		                    displayProjection: new OpenLayers.Projection("EPSG:4269"),
				    units:'m',
		                    //maxScale:433000,
		                    //minScale:4000000
		                    //units:"degree",
		                    maxExtent: mext,
		                    numZoomLevels:9,
				    fallThrough:true,
		                    maxZoomLevel:14,
				    minZoomLevel:6,
				    //maxResolution:'auto',
				    //minResolution:0.5971642833948135,
		            	    restrictedExtent: mext 
		                };
		            map=new OpenLayers.Map('map',options);
			    oldZoomTo=map.zoomTo;
			    map.zoomTo=function(zoom,xy){
				if(zoom>4)
				    oldZoomTo.apply(this,arguments);
			    }
		            map.Z_INDEX_BASE.Control=10010;
			    map.Z_INDEX_BASE.Feature=10008;
                            map.Z_INDEX_BASE.Overlay=10007;
			    map.Z_INDEX_BASE.Popup=10009;
		            //var lyr_gsstat=new OpenLayers.Layer.Google(
                            //			"Google Hybrid",
                            //			{projection:new OpenLayers.Projection("EPSG:3857"),type: google.maps.MapTypeId.HYBRID, minZoomLevel: 3,"sphericalMercator": true});
		            
			    var lyr_gsstat=new OpenLayers.Layer.OSM("OSM Map");
			    var lyr_marker=new OpenLayers.Layer.Markers("markers");
			    //lyr_gsstat.serverResolutions=osmResolutions;
		            //aliasproj = new OpenLayers.Projection("EPSG:3857");
		            //lyr_gstreet.projection=aliasproj;
		            //lyr_gsstat.projection=aliasproj;
		            //lyr_marker.projection=aliasproj;
		            //OpenLayers.Projection.addTransform("EPSG:4269", "EPSG:3857", OpenLayers.Layer.SphericalMercator.projectForward);
		            //OpenLayers.Projection.addTransform("EPSG:3857", "EPSG:4269", OpenLayers.Layer.SphericalMercator.projectInverse);
		            map.addLayers([lyr_gsstat,lyr_marker]);
		            lyr_marker.setZIndex(10001);
		            //map.zoomToExtent(extent);//(new OpenLayers.Bounds(-92.5,41,-70.5,49.5));
			    map.setCenter(new OpenLayers.LonLat(-9373014.1551348, 5667347.024385),6);

			    map.events.register("mousemove",map,function(e){
					    var pnt=this.events.getMousePosition(e);
					    var lonlat=map.getLonLatFromViewPortPx(new OpenLayers.Pixel(pnt.x,pnt.y));
					    lonlat=lonlat.transform(map.getProjectionObject(),prjdist);
		            	$("#dv_coord").html(lonlat.lon.toFixed(4)+", "+lonlat.lat.toFixed(4));
				    });
				    OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {                
		                defaultHandlerOptions: {
		                    'single': true,
		                    'double': false,
		                    'pixelTolerance': 0,
		                    'stopSingle': false,
		                    'stopDouble': false
		                },

		                initialize: function(options) {
		                    this.handlerOptions = OpenLayers.Util.extend(
		                        {}, this.defaultHandlerOptions
		                    );
		                    OpenLayers.Control.prototype.initialize.apply(
		                        this, arguments
		                    ); 
		                    this.handler = new OpenLayers.Handler.Click(
		                        this, {
		                            'click': this.trigger
		                        }, this.handlerOptions
		                    );
		                }, 

		                trigger: function(e) {
		                    var lonlat = map.getLonLatFromViewPortPx(e.xy);
		                    var size = new OpenLayers.Size(21,25);
		                    var offset = new OpenLayers.Pixel(-(size.w/2), -size.h);
		                    var icon = new OpenLayers.Icon('http://maps.glin.net/projects/lcm/icons/marker-red.png', size, offset);
		                    if(p_marker!=null)
			                    removeMarker(lyr_marker,p_marker);
		                    if(p_marker2!=null)
		                    	removeMarker(lyr_marker,p_marker2);
		                    p_marker=new OpenLayers.Marker(lonlat,icon);
		                    p_marker.events.register("mousedown",p_marker,markerOnMouseDown);
		                    lyr_marker.addMarker(p_marker);
							$("#dlg_verfiy").dialog({
								resizable: false,
								height: 'auto',
								width:350,
								modal: true,
								buttons:[
									{
										id:'btn_ver_ok',
										text:'OK',
										click:function(){
											
     var oldlon=plonlat.lon;var oldlat=plonlat.lat;
											$.get("glcfsspatial.glos",
													{x:plonlat.lon,y:plonlat.lat,f:1,l:$("#sel_m_lake option:selected").val()},
													function(data){
														if(data!=null&&data.i>=0&&data.j>=0)
														{
															plake=$("#sel_m_lake option:selected").val();
															pi=data.i;
															pj=data.j;
															pdist=data.dist;
															$("#dv_inrange").hide();
															var lonlat2=new OpenLayers.LonLat(data.lon,data.lat);
															plonlat=lonlat2;
															lonlat2=lonlat2.clone().transform(prjdist,map.getProjectionObject());
															
															var icon2 = new OpenLayers.Icon('http://maps.glin.net/projects/lcm/icons/marker-blue.png', size, offset);
															p_marker2=new OpenLayers.Marker(lonlat2,icon2);
															p_marker2.pi=pi;
															p_marker2.pj=pj;
															p_marker2.dist=pdist;
															lyr_marker.addMarker(p_marker2);
															p_marker2.events.register("mousedown",p_marker2,markerOnMouseDown);
															$("#sp_valo1").text(oldlon.toFixed(4)+" ,"+oldlat.toFixed(4));
															
															$("#sp_valo2").text(data.lon.toFixed(4)+" ,"+data.lat.toFixed(4));
															$("#sp_valo3").text(friendlyDistanceLabel(data.dist));
															$("#dv_outrange").show();
															$("#dlg_verfiy").dialog("close");
														}
											},"json");
											
										}
									}
								],
								open:function(){
									$(this).parent().children().children('.ui-dialog-titlebar-close').hide();
									lonlat=lonlat.clone().transform(map.getProjectionObject(),prjdist);
									plonlat=lonlat;
									$("#sp_v_prom").show();
									$("#btn_ver_ok").button("disable");
									$("#dv_chk2").hide();
									$.get("glcfsspatial.glos",
										{x:lonlat.lon,y:lonlat.lat},
										function(data)
										{
											if(data!=null)
											{
												if(data.i>=0&&data.j>=0)
												{
													plonlat=lonlat;
													plake=data.lake;
													pi=data.i;
													pj=data.j;
													p_marker.pi=pi;
													p_marker.pj=pj;
													$("#dv_inrange").show();
													$("#dv_outrange").hide();
													$("#sp_vali").text(lonlat.lon.toFixed(4)+" ,"+lonlat.lat.toFixed(4));
													$("#dlg_verfiy").dialog("close");
												}
												else
												{
													$("#dv_chk2").show();
													$("#sp_v_prom").hide();
													$("#btn_ver_ok").button("enable");
													
												}
											}
										},
										"json"
									);
								}
							});
		                }

		            });
				    
				    var click = new OpenLayers.Control.Click();
	                map.addControl(click);
	                $(".olLayerGooglePoweredBy").css("z-index",100);
	                $("#dv_maptools").buttonset();
	                $("#rdo_pan").attr("checked",true);
	                $("#rdo_pick").click(function(e){
	                	click.deactivate();
	                	e.stopPropagation();
	        			click.activate();
	            		
	            	});
	                $("#rdo_pick").dblclick(function(e){
	                	click.deactivate();
	                	e.stopPropagation();
	        			click.activate();
	            		
	            	});
	                $("#rdo_pan").click(function(){
	        			click.deactivate();
	            		
	            	});
	                
	                $("#dv_maptools").find('label').each(function(i,d){
	                		$(this).dblclick(function(e){e.stopPropagation();});
	                });
		    	}
		    }
		});
	});
});