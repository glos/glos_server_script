<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>GLCFS Point Query</title>
<link rel="shortcut icon" href="http://data.glos.us/favicon.ico">
<link rel="icon" href="http://data.glos.us/favicon.ico">
<link rel="stylesheet" href="./css/blue-green/jquery-ui-1.8.13.custom.css">
<link rel="stylesheet" href="./css/glosproducts.css">
<link rel="stylesheet" href="./css/glcfs.css">
<style>
/*html{overflow-y:scroll;}
body{margin:0;padding:0;font-size:12px;font-family:Helvetica,Arial,sans-serif;} */
.text-valid{
 	background-color: #FFFFA2;
    border: 1px solid #DDDDDD;
    -moz-border-radius: 4px 4px 4px 4px;
    color: #333333;
}
.text-error{
	background-color: #FF0000;
    border: 1px solid #CD0A0A;
    -moz-border-radius: 4px 4px 4px 4px;
    color: #FFFFFF;
}
.errmsg {
    color: #ff0000;
    padding: 4px;
    margin-left:10px;
    font-size:10px;
}

</style>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.1/jquery.min.js" type="text/javascript"></script>
<script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.13/jquery-ui.min.js" type="text/javascript"></script>
<script src="js/ol2131/OpenLayers.js" type="text/javascript"></script>
<script src="js/proj4js-compressed.js" type="text/javascript"></script>
<script src="js/glcfs.js" type="text/javascript"></script>
    <!--[if !IE 7]>
	<style type="text/css">
		#wrap {display:table;height:100%}
	</style>
<![endif]-->
</head>
<body>
<!-- Google Tag Manager -->
<noscript><iframe src="//www.googletagmanager.com/ns.html?id=GTM-W3GV39"
height="0" width="0" style="display:none;visibility:hidden"></iframe></noscript>
<script>(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
'//www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
})(window,document,'script','dataLayer','GTM-W3GV39');</script>
<!-- End Google Tag Manager -->


  <div id="wrap" align="center">

	<div id="main">
  
  

           

<!--header include -->
<jsp:include page="./header_glcfs.jsp" flush="true">
 <jsp:param name="GLOS header" value="home" />
</jsp:include>
<!--end header -->

<!--start center column -->
<div align="center">

    
    <div id="page" >




		<h1 class="title">Data Download</h1>
<!--
<p>This Query tool provides quick access to Great Lakes Coastal 
Forecasting System (GLCFS) input data and model output for a given 
location and time period.  </p>
<p>The GLCFS archive begin on Jan 1, 2006 and 
  run up to the present.  This tool and these data are useful to 
  recreational boaters, swimmers, and fisherman, and to beach managers and 
  modelers. </p>
-->
<br />
<div align="center">
	<div class="ui-dialog-content ui-widget-content" style="margin:0.1em;margin-top:0em;padding:15px;font-size:12px;width:600px;text-align:left" >
		
		<div style="font-weight:bold;font-size:14px;">1. Enter Longitude, Latitude and Lake Name</div>
		<div style="padding-left:14px;">
			<div id="div_rdo_loc" style="padding-top:6px;font-size:12px;">
				<input type="radio" id="rdo_loc_dec" name="locradio" checked="checked"/><label for="rdo_loc_dec" style="margin-left:4px;">Decimal Degree</label>
				<input type="radio" id="rdo_loc_mm" name="locradio"  /><label for="rdo_loc_mm" style="margin-left:4px;">Degrees Minutes Seconds</label>
			</div>
			<table style="padding-top:6px;width:90%;">
				<tr>
					<td style="width:70px;">Longitude:</td>
					<td align="left">
						<div id="dv_lon_dec">
							<input type="text" id="txt_lon_dec" value="" class="text-valid"/><span id="err_txt_lon_dec" class="errmsg" style="display:none;"></span>
						</div>
						<span style="display:none;" id="sp_lon_mm">
							<input type="text" value=""  id="txt_lon_deg" style="width:30px;" class="text-valid"/>
							<input type="text" value=""  id="txt_lon_min" style="width:30px;" class="text-valid"/>
							<input type="text" value=""  id="txt_lon_sec" style="width:40px;" class="text-valid"/>
							<span id="err_txt_lon_dms" class="errmsg" style="display:none;"></span>
						</span>
					</td>
				</tr>
				<tr>
					<td style="width:70px;">Latitude:</td>
					<td align="left">
						<div id="dv_lat_dec">
							<input type="text" id="txt_lat_dec" value="" class="text-valid"/><span id="err_txt_lat_dec" class="errmsg" style="display:none;"></span>
						</div>
						<span style="display:none;" id="sp_lat_mm">
							<input type="text" value=""  id="txt_lat_deg" style="width:30px;" class="text-valid"/>
							<input type="text" value=""  id="txt_lat_min" style="width:30px;" class="text-valid"/>
							<input type="text" value=""  id="txt_lat_sec" style="width:40px;" class="text-valid"/>
							<span id="err_txt_lat_dms" class="errmsg" style="display:none;"></span>
						</span>
					</td>
				</tr>
				<tr>
					<td style="width:70px;">Lake Name:</td>
					<td align="left">
						<select id="sel_lname" class="text-valid">
							<option value="">---------------</option>
							<option value="erie">Lake Erie</option>
							<option value="huron">Lake Huron</option>
							<option value="michigan">Lake Michigan</option>
							<option value="ontario">Lake Ontario</option>
							<option value="superior">Lake Superior</option>
						</select>
						<span id="err_sel_lname" class="errmsg" style="display:none;"></span>
					</td>
				</tr>
				<tr>
					<td colspan="2" style="padding-top:6px;">
						<button class="button" id="btn_map">Select Point of Interest from Map</button>
					</td>	
				</tr>
			</table>
		</div>
		
		<br/>
		
		<div style="font-weight:bold;font-size:14px;">2. Select the Model Type</div>
		<div style="padding-left:14px;padding-top:6px;">
			<input type="radio" id="rdo_mt_n" name="mtradio" checked="checked"/><label style="margin-left:4px;">Nowcast 2D</label>
			<input type="radio" id="rdo_mt_n3" name="mtradio"  /><label style="margin-left:4px;">Nowcast 3D</label>
			<input type="radio" id="rdo_mt_f" name="mtradio"  /><label style="margin-left:4px;">Forecast 2D</label>
			<input type="radio" id="rdo_mt_force_n" name="mtradio"  /><label style="margin-left:4px;">Nowcast Input Data</label>
			<input type="radio" id="rdo_mt_force_f" name="mtradio"  /><label style="margin-left:4px;">Forecast Input Data</label>
			
		</div>
		<br/>
		
		<div style="font-weight:bold;font-size:14px;">3. Select Date and Time</div>
		<div style="padding-left:14px;padding-top:6px;">
			<div style="padding-bottom:8px;">
						<span>Time Zone: </span>
					
						<select id="sel_tzone" name="sel_tzone" class="text-valid">
                             <option value="0">Greenwich Mean Time</option>
                             <option value="-6">Central Standard Time</option>
                             <option value="-5">Central Daylight Time</option>
			     <option value="-5">Eastern Standard Time</option>
                             <option value="-4">Eastern Daylight Time</option>
                             <option value="-7">Mountain Standard Time</option>
                             <option value="-6">Mountain Daylight Time</option>
                             <option value="-8">Pacific Standard Time</option>
                             <option value="-7">Pacific Daylight Time</option>
                             <option value="-9">Alaska Standard Time</option>
                             <option value="-8">Alaska Daylight Time</option>
                             <option value="-10">Hawaii-Aleutian Standard Time</option>
                        </select>
						
			</div>
			<div id="div_rdo_dt" style="font-size:12px;">
				<input type="radio" id="rdo_dt_historic" name="dtradio" checked="checked"/><label for="rdo_dt_historic" style="margin-left:4px;">Date/Time Range</label>
				<input type="radio" id="rdo_dt_latest" name="dtradio" /><label for="rdo_dt_latest" style="margin-left:4px;">Latest</label>
			</div>
			<table id="tab_dt">
				<tr>
					<td>Start Date/Time: </td>
					<td>
						<input type="text" id="txt_ds" style="width:80px;" class="text-valid" readonly="readonly"/>/
						<select id="sel_ts1" class="text-valid">
<%
for(int i=0;i<24;++i)
{
	if(i<10)
		out.println(String.format("<option value='0%d:00:00'>0%d:00:00</option>",i,i));
	else
		out.println(String.format("<option value='%d:00:00'>%d:00:00</option>",i,i));
}
%>
						</select>
						<select id="sel_ts3" style="display:none;" class="text-valid">
<%
for(int i=0;i<24;i+=3)
{
	if(i<10)
		out.println(String.format("<option value='0%d:00:00'>0%d:00:00</option>",i,i));
	else
		out.println(String.format("<option value='%d:00:00'>%d:00:00</option>",i,i));
}
%>
						</select>
						<span id="err_txt_ds" class="errmsg" style="display:none;"></span>
					</td>
				</tr>
				<tr>
					<td>End Date/Time: </td>
					<td>
						<input type="text" id="txt_de" style="width:80px;" class="text-valid" readonly="readonly"/>/
						<select id="sel_te1" class="text-valid">
<%
for(int i=0;i<24;++i)
{
	if(i<10)
		out.println(String.format("<option value='0%d:00:00'>0%d:00:00</option>",i,i));
	else
		out.println(String.format("<option value='%d:00:00'>%d:00:00</option>",i,i));
}
%>
						</select>
						<select id="sel_te3" style="display:none;" class="text-valid">
<%
for(int i=0;i<24;i+=3)
{
	if(i<10)
		out.println(String.format("<option value='0%d:00:00'>0%d:00:00</option>",i,i));
	else
		out.println(String.format("<option value='%d:00:00'>%d:00:00</option>",i,i));
}
%>
						</select>
						<span id="err_txt_de" class="errmsg" style="display:none;"></span>
					</td>
				</tr>
				
			</table>
			<div id="dv_lat" style="display:none;padding-top:4px;">
				<span style="padding-right:4px;">Hours:</span><input type="text" id="txt_hours" name="txt_hours" class="text-valid" value="" />
				<span id="err_txt_hours" class="errmsg" style="display:none;"></span>
			</div>
			
		</div>
		<br/>
		
		<div style="font-weight:bold;font-size:14px;">4. Choose Parameters for Output</div>
		<div style="padding-left:14px;padding-top:6px;">
			<div>
				<span>Unit of Measure:</span>
				<select id="sel_unit" name="sel_unit" class="text-valid">
					<option value="m">Metric</option>
					<option value="e" selected="selected">English</option>
						
				</select>
			</div><br/>
			<span id="err_vars" class="errmsg" style="display:none;"></span>
			<div id="dv_n2">
				<div>
					<input type="checkbox" value="depth" /><span> Mean Water Depth</span>
				</div>
				<div>
					<input type="checkbox" value="eta" /><span> Water Level Displacement</span>
				</div>
				<div>
					<input type="checkbox" value="uc,vc" /><span> Water Velocity at Surface</span>
				</div>
				<div>
					<input type="checkbox" value="utm,vtm" /><span> Depth-Averaged Water Velocity</span>
				</div>
				<div>
					<input type="checkbox" value="wvh" /><span> Significant Wave Height</span>					
				</div>
				<div>
					<input type="checkbox" value="wvd" /><span> Wave Direction</span>
				</div>
				<div>
					<input type="checkbox" value="wvp" /><span> Wave Period</span>
				</div>
				<div>
                                        <input type="checkbox" value="ci" /><span> Ice Concentration</span>
                                </div>
				<div>
                                        <input type="checkbox" value="hi" /><span> Ice Thickness</span>
                                </div>
			</div>
			
			<div id="dv_n3" style="display:none;">
				<div>
					<span style="padding-right:4px;">Define Requested Water Depth:</span><input type="text" value="" id="txt_rdepth" class="text-valid"/>
					<button id="btn_range" style="margin-left:8px;">Update Depth Range</button>
					<span id="sp_depth_range" style="display:block;"></span>
					<span id="err_txt_depth" class="errmsg" style="display:none;margin-left:0;"></span>
				</div>
				<div>
					<input type="checkbox" value="depth" /><span>Mean Water Depth</span>
				</div>
				<div>
					<input type="checkbox" value="d3d" /><span>3D Depth at Nodes</span>
				</div>
				<div>
					<input type="checkbox" value="temp" /><span>Water Temperature</span>
				</div>
				<div>
					<input type="checkbox" value="u,v" /><span>Water Velocity</span>					
				</div>
				
			</div>
			
			<div id="dv_force_n" style="display:none;">
				<div>
					<input type="checkbox" value="depth" /><span>Mean Water Depth</span>
				</div>
				<div>
					<input type="checkbox" value="air_u,air_v" /><span>Wind Velocity</span>
				</div>
				<div>
					<input type="checkbox" value="cl" /><span>Cloud Cover</span>
				</div>
				<div>
					<input type="checkbox" value="at" /><span>Air Temperature</span>
				</div>
				<div>
					<input type="checkbox" value="dp" /><span>Dew Point</span>					
				</div>
				
			</div>
			<div id="dv_force_f" style="display:none;">
				<div>
					<input type="checkbox" value="depth" /><span>Mean Water Depth</span>
				</div>
				<div>
					<input type="checkbox" value="air_u,air_v" /><span>Wind Velocity</span>
				</div>
				<div>
					<input type="checkbox" value="at" /><span>Air Temperature</span>
				</div>
				
				
			</div>
			
		</div>
		<br/>
		
		<div style="font-weight:bold;font-size:14px;">5. Data Format and Other Options</div>
			<div style="padding-left:14px;padding-top:6px;">
				
				<div style="padding-top:6px;">
					<span>Data Format:</span>
					<select id="sel_datafmt" name="sel_datafmt" class="text-valid">
						<option value="csv">CSV</option>
						<option value="tab">TAB</option>
						<option value="rss">GeoRSS</option>
					</select>
				</div>
				<div style="padding-top:6px;">
					<span>Order by Date/Time:</span>
					<input type="radio" id="rdo_order_asc" name="rdo_order" checked="checked"/><label for="chk_order_asc" style="margin-left:4px;">Ascending</label>
					<input type="radio" id="rdo_order_desc" name="rdo_order" /><label for="chk_order_desc" style="margin-left:4px;">Descending</label>
				</div>
				<div style="padding-top:6px;">
					<span>Display CSV/TAB in the Browser: </span><input type="checkbox" id="chk_pv" name="chk_pv" /><br/>
					<span>Include Cell Number: </span><input type="checkbox" id="chk_gi" name="chk_gi" /><br/>
					<span>Include Day of the Year: </span><input type="checkbox" id="chk_doy" name="chk_doy" />
				</div>
			</div>
		<br/>
		<hr/>
		<br/>
		<div>
			<!--<a class="button" style="font-size:16px;" id="btn_download" target="_blank" href="">Download</a>-->
			<a id="btn_download" target="_blank">Download</a>
		</div>
		<br/>
		
	</div>
</div>
<div id="dlg_map" title="Select lat/lon" style="display:none;">
	
		<div id="map" style="height:480px;width:750px;border:1px solid #cccccc;">
			<div id="dv_maptools" style="position:absolute;top:0;padding-top:20px;left:500px;margin-left:auto;margin-right:auto;text-align:center;z-index:1080;">
				<input type="radio" id="rdo_pick" name="chk_maptool" /><label for="rdo_pick">Select Point</label>
				<input type="radio" id="rdo_pan" name="chk_maptool" checked="checked"/><label for="rdo_pan">Pan Map</label>
			</div>
		</div>
		
		<div id="map_content" style="position:absolute;top:0.5em;left:770px;height:480px;width:220px;font-size:12px;">
			<div>
				<p>Use the navigation bar on the left to zoom in/out the map</p>
				<p>Use "Pan Map" tool to pan the map</p>
				<p>Use "Select Point" tool to select the point of interest</p>
			</div>
			<div id="dv_inrange" style="display:none;">
				<p>You have selected point:</p>
				<table>
					<tr>
						<td align="center">
							<img src="http://maps.glin.net/projects/lcm/icons/marker-red.png" />&nbsp;&nbsp;<span>At</span>
						</td>
					</tr>
					<tr>
						<td align="center">
							<span>[</span><span id="sp_vali"></span><span>]</span>
						</td>
					</tr>
				</table>
			</div>
			<div id="dv_outrange" style="display:none;">
				<p>The closest controid of the model cell to the point you selected:</p>
				<table style="width:100%;">
					<tr>
						<td align="center">
							<img src="http://maps.glin.net/projects/lcm/icons/marker-red.png" />&nbsp;&nbsp;<span>At</span>
						</td>
					</tr>
					<tr>
						<td align="center">
							<span>[</span><span id="sp_valo1"></span><span>]</span>
						</td>
					</tr>
					<tr>
						<td align="center">
							<span>Is</span>
						</td>
					</tr>
					<tr>
						<td align="center">
							<img src="http://maps.glin.net/projects/lcm/icons/marker-blue.png" />&nbsp;&nbsp;<span>At</span>
						</td>
					</tr>
					<tr>
						<td align="center">
							<span>[</span><span id="sp_valo2"></span><span>]</span>
						</td>
					</tr>
					<tr>
						<td align="left">
							<span>This model cell is <span id="sp_valo3"></span>&nbsp;away from your point.</span>
						</td>
					</tr>
				</table>
			</div>
		</div>
		<div style="display:block;padding-top:10px;width:100%;font-size:10px;">
			<span>Current Coordinates in lat/lon:</span><span id="dv_coord"></span>
		</div>
</div>
<div id="dlg_verfiy" title="Verfiy POI" style="display:none;">
	<div style="margin:auto;text-align:center;width:100%;"><span id="sp_v_prom">One Moment...</span></div>
	<div id="dv_chk2" style="display:none;">
		<p>It appears the point you selected is not in the valid index range of the model. Could you please let me know the name of the lake that you are interested in?</p>
		<select id="sel_m_lake">
			<option value="erie">Lake Erie</option>
			<option value="huron">Lake Huron</option>
			<option value="michigan">Lake Michigan</option>
			<option value="ontario">Lake Ontario</option>
			<option value="superior">Lake Superior</option> 
		</select>
		
	</div>
</div>
<div id="dlg_error" title="Error" style="display:none;">
	<div id="div_errmsg" style="margin-left:auto;margine-right:auto;width:100%;">
	</div>
</div>
<div style="background-color:yellow" id="debug">

</div></div>




   </div><!--end of page-->
    

</div> <!--end of centerfluid-->    


</div>

<!--footer include-->
<p>&nbsp;</p>
<jsp:include page="./footer_standalone.jsp" flush="true">
 <jsp:param name="footerinfo" value="footer" />
</jsp:include>

<!--end footer -->

</body>
</html>
