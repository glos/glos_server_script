var request = require('request');
var cheerio = require('cheerio');
var yaml = require('yamljs')
var fs = require('fs');
var util = require('util');
//Here is a valid station url example:
//var url = "http://www.waterlevels.gc.ca/eng/Station/Month?type=1&sid=11010&tz=UTC&pres=2&date=2016%2F02%2F29";

var newline = require('os').EOL;
var base = "/usr/local/glos/dfo-waterlevel"
var program = base + "/habs2nc";
var scp = "/usr/bin/scp %s/%s $USER@storage.glos.us:%s/%s/%s/%s.nc";
var platform_base = base + "/platform/";
var platform_config_base = base + "/platform-config/";
var output = base + "/tmp";
var datafile_suffix = "_water_level_monthly";
var remote_dir = "/var/local/glos/storage/DFO-WaterLevel/lakes/sea_surface_height";

var main = function(){
    
    objs = yaml.load(base + '/' + 'stations.yaml');
    if(null != objs && null != objs.url && null != objs.stations && objs.stations.length > 0)
    {
	var ts = new Date();
        var year = ts.getFullYear();
        var month = ts.getMonth() + 1;
 	var day = ts.getDate();
	var url = null;
	var station = null;
	for(var i = 0; i < objs.stations.length; ++i)
	{
	    station = objs.stations[i];   
            if(null != station.id && null != station.name)
	    {
	        url = objs.url.replace('#####',station.id).replace('????', year + '/' + month + '/' + day);
		if(null == url)
		{
		    console.error("Error to create url for station: " + station.name);
		    continue;
		}
		//I put a closure here to hold the station variable for every async call
		//so we could recover station id when callback is invoked.
		//Be aware this is async call
	        (function(station, year, month){
		request(url, function(err, resp, body){
		    if(!err && 200 == resp.statusCode)
        	    {
	    		$ = cheerio.load(body);
	    		var txt = '';
	    		var arr = null;
	    		var cnt = 0;
	    		var ts = 0;
			var data = null;
	    		$(".stationTextData").children("div").each(function(){
	        	    txt = $(this).text().trim().replace(/(\n|\r)+$/, '');
	        	    if(null != txt && txt.length > 0)
	        	    {
		    		arr = txt.split(";");
		    		if(null != arr && 3 == arr.length)
		    		{
				    //Date.parse Function (JavaScript) Parses a string containing a date, and returns the number of milliseconds between that date and midnight, January 1, 1970.
		        	    ts = Date.parse(arr[0] + " " + arr[1]) / 1000;
		        	    if(false == isNaN(ts) && false == isNaN(parseFloat(arr[2])))
		            	        data = data + ts + "," + arr[2] + newline;
		    		}
	        	    }
	    		});
			if(null != data)
			{
			    //console.error("write to: " + platform_base + station.id + "/" + station.id + datafile_suffix);
			    fs.writeFile(platform_base + station.id + "/" + station.id + datafile_suffix,data,function(err){
				if(err != null)
				    console.error("Failed to write data file for: " + station.id);
				//console.error(program + ' ' + platform_config_base + station.id);
				var system = require('child_process').exec;
				system(program + ' ' + platform_config_base + station.id,
					function(error, stdout, stderr){
					    if(null != error)
						console.error("Failed to generate NetCDF file for: " + station.id + " " + error);
					    else
					    {
						console.error("Created NetCDF file for: " + station.id);
						system(util.format(scp, output, station.id + "_sea_surface_height.nc", remote_dir, station.id, year, station.id + "_sea_surface_height_" + year + month),function(erro,sout,serr){
						    if(null != erro)
							console.error("Failed to copy NetCDF file: " + station.id + "_sea_surface_height.nc");
						    else
							console.error("Copy NetCDF file to remote: " + station.id + "_sea_surface_height.nc");
						});
					    }
					});
			    });
			}
        	    }       
        	});
		})(station, year, month);
	    }
	    else
	        console.error("Station name or id can't be empty.");
	}
    }
    else
	console.error('Make sure url base and station list are inside yaml file.');
}

if(module == require.main)
     main();
