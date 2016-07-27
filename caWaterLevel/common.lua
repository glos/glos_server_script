Workspace="/usr/local/glos/dfo-waterlevel"
Output="/usr/local/glos/dfo-waterlevel/tmp"
GlobalAttributes={
    nodc_template_version = "NODC_NetCDF_TimeSeries_Orthogonal_Template_v1.1",
    sea_name = "Great Lakes",
    Conventions="CF-1.6",
    Metadata_Conventions="Unidata Dataset Discovery v1.0",
    featureType=nil,
    cmd_data_type=nil,
    standard_name_vocabulary="NetCDF Climate and Forecast (CF) Metadata Convention Standard Name Table v26",
    platform="platform",
    instrument="instrument",
    title=nil,
    summary=nil,
    processing_level="none",
    data_created=nil,
    license="Freely Distributed",
    uuid="",
    project="",
    id=nil,
    naming_authority="GLOS",
    keywords=nil,
    keywords_vocabulary = "",
    institution="GLOS",
    institution_url="http://glos.us",
    institution_dods_url="",
    acknowledgement=nil,
    contributor_role="",
    contributor_name="GLOS",
    publisher_name="GLOS",
    publisher_email="dmac@glos.us",
    publisher_url="http://glos.us",
    creator_name="",
    creator_email="",
    creator_url="",
    geospatial_lat_min=nil,
    geospatial_lat_max=nil,
    geospatial_lon_min=nil,
    geospatial_lon_max=nil,
    geospatial_lat_units = "degrees_north",
    geospatial_lat_resolution = "",
    geospatial_lon_units = "degrees_east",
    geospatial_lon_resolution = "",
    geospatial_vertical_min="",
    geospatial_vertical_max="",
    geospatial_vertical_units="",
    geospatial_vertical_resolution="",
    geospatial_vertical_positive="",
    missing_value=-9999.0,
    date_created=nil,
    date_issued=nil,
    date_modified=nil,
    time_coverage_start="",
    time_coverage_end="",
    time_coverage_duration="",
    time_coverage_resolution="",
    comment = "",
    acknowledgment = "",
    cdm_data_type = "station",
    history = "",
    source = "",
    references = "",
    metadata_link = ""
}

Variables={
    feature_type_instance={
	attributes={
	    long_name="Identifier for each feature type instance",
	    cf_role="timeseries_id"
	},
	value=nil
    },
    time={
	attributes={
	    standard_name="time",
	    long_name="time of measurement",
	    calendar="gregorian",
	    units="seconds since 1970-01-01T00:00:00Z",
	    time_coverage_start=nil,
	    time_coverage_end=nil,
	    axis="T"
	    --time_coverage_duration=nil,
	    --time_coverage_resolution=nil,
	},
	count=nil
    },
    latitude={
	attributes={
	    standard_name="latitude",
	    long_name="sensor latitude",
	    units="degrees_north",
	    axis="Y"
	},
	value=nil
    },
    longitude={
	attributes={
	    standard_name="longitude",
	    long_name="sensor longitude",
	    units="degrees_east",
	    axis="X"
	},
	value=nil
    },
    crs={
	attributes={
	    long_name="http://www.opengis.net/def/crs/EPSG/0/4326",
	    grid_mapping_name="latitude_longitude",
	    epsg_code="EPSG:4326",
	    semi_major_axis=6378137.0,
	    inverse_flattening=298.257223563
	},
	value=4326,
	vtype="int"
    },
    platform={
	attributes={
	    standard_name="platform_name",
	    ioos_code=nil,
	    long_name=nil,
	    imo_code="",
	    call_sign="",
	    ices_code="",
	    imo_code="",
	    wmo_code=""
	},
	value=nil
    },
    instrument={
	attributes={
	    standard_name="instrument_name",
	    definition="http://mmisw.org/ont/ioos/definition/sensorID",
	    long_name=nil,
	    make_model="",
	    serial_number="",
	    calibration_date="",
	    comment=""
	},
	value=nil
    },
    obs={
	featureType=nil,
	varname=nil,
	attributes={
	    coordinates="time latitude longitude height",
	    coverage_content_type="physicalMeasurement",
	    standard_name=nil,
	    long_name=nil,
	    units=nil,
	    scale_factor=1,
	    add_offset=0,
	    _FillValue=-9999.0,
	    valid_min=nil,
	    valid_max=nil,
	    grid_mapping="crs",
	    platform="platform",
	    instrument="instrument",
	    nodc_name=""
	}
    },
    height={
	attributes={
	    long_name="height of the sensor relative to sea surface",
	    standard_name="height",
	    positive="down",
            units="m",
            axis="Z"
	},
	depthLevel=nil
    }
}
function vardump(value, depth, key)
    local linePrefix = ""
    local spaces = ""
  
    if key ~= nil then
        linePrefix = "["..key.."] = "
    end
  
    if depth == nil then
        depth = 0
    else
        depth = depth + 1
        for i=1, depth do spaces = spaces .. "  " end
    end
  
    if type(value) == 'table' then
        mTable = getmetatable(value)
        if mTable == nil then
            print(spaces ..linePrefix.."(table) ")
        else
            print(spaces .."(metatable) ")
            value = mTable
        end		
        for tableKey, tableValue in pairs(value) do
            vardump(tableValue, depth, tableKey)
        end
    elseif type(value)	== 'function' or 
      type(value)	== 'thread' or 
      type(value)	== 'userdata' or
      value		== nil
    then
        print(spaces..tostring(value))
    else
        print(spaces..linePrefix.."("..type(value)..") "..tostring(value))
    end
end
-- Compatibility: Lua-5.1
function split(str, pat)
    local t = {}  -- NOTE: use {n = 0} in Lua-5.0
    local fpat = "(.-)" .. pat
    local last_end = 1
    local s, e, cap = str:find(fpat, 1)
    while s do
       if s ~= 1 or cap ~= "" then
          table.insert(t,cap)
       end
       last_end = e+1
       s, e, cap = str:find(fpat, last_end)
    end
    if last_end <= #str then
       cap = str:sub(last_end)
       table.insert(t, cap)
    end
    return t
end
function loopTable(obj,table)
    for k,v in pairs(table) do
	if type(v)=="table" then
	    obj[k]={}
	    loopTable(obj[k],v);
	else
	    obj[k]=v
	end
    end
end
function initPlatformInfo(Platform)
   local tf=io.open(Workspace.."/.obs2nc_workspace","w")
   if tf~=nil then
       io.close(tf)
       os.remove(Workspace.."/.obs2nc_workspace")
   else
       print("Workspace: "..Workspace.." does not exist!")
       return nil
   end
   if Platform and type(Platform) == "table" then
	local p={}
	p.GlobalAttributes={}
	for k,v in pairs(GlobalAttributes) do
	    p.GlobalAttributes[k]=v	
	end
	if Platform.attributes~=nil and type(Platform.attributes)=="table" then
	    for pk,pv in pairs(Platform.attributes) do
		p.GlobalAttributes[pk]=pv
	    end
        end
	p.GlobalAttributes.title=Platform.title
	p.GlobalAttributes.summary=Platform.summary
	p.GlobalAttributes.id=Platform.id
	p.GlobalAttributes.keywords=Platform.keywords
	p.GlobalAttributes.geospatial_lat_min=Platform.lat
	p.GlobalAttributes.geospatial_lat_max=Platform.lat
	p.GlobalAttributes.geospatial_lon_min=Platform.lon
	p.GlobalAttributes.geospatial_lon_max=Platform.lon
	local cdate=os.date("*t")
        local datestr=cdate.year..cdate.month..cdate.day
	p.GlobalAttributes.date_created=datestr
	p.GlobalAttributes.date_issued=datestr
	p.GlobalAttributes.date_modified=datestr
	p.NCs={}
	for k,v in pairs(Platform.sensors) do -- k:sensor name,v:sensor def table
	    v.vars={}
	    for kk,vv in pairs(Variables) do -- kk:variable name,vv:variable def table
	        v.vars[kk]={}
		loopTable(v.vars[kk],vv)
	        --for kkk,vvv in pairs(vv) do --copy variable def item vvv with name kkk into vars.kk.kkk
	            --if type(vvv)=="table" then
			--v.vars[kk][kkk]={}
			--for p,j in pairs(vvv) do
			  --  v.vars[kk][kkk][p]=j;
			--end
		    --else
		      --  v.vars[kk][kkk]=vvv
		    --end
		--end
	    end
	    v.target="/tmp/"..Platform.id.."_"..k
	    v.nc_name=Output.."/"..Platform.id.."_"..k..".nc"
	    --v.GlobalAttributes=p.GlobalAttributes
	    local attrtype=nil
	    v.GlobalAttributes={}
	    for lk,lv in pairs(p.GlobalAttributes) do
	        v.GlobalAttributes[lk]=lv	
	    end
	    for obsk,obsv in pairs(v) do
		attrtype=type(obsv)
		if attrtype=="string" or attrtype=="number" or attrtype=="boolean" then
		     v.vars.obs.attributes[obsk]=obsv;
		end
	    end
	    v.GlobalAttributes.institution=v.GlobalAttributes.keywords
	    v.GlobalAttributes.keywords=v.keywords
	    v.GlobalAttributes.featureType=v.featureType
	    v.vars.obs.varname=v.habs_name
	    v.vars.obs.attributes.standard_name=v.standard_name
	    v.vars.obs.attributes.units=v.units
	    v.vars.obs.attributes.long_name=v.long_name
	    v.vars.obs.featureType=v.featureType
	    local tsstart,tsend,path
	    if string.sub(v.source,1,1)=="/" then
		if string.sub(Workspace,string.len(Workspace),1)=="/" then
		    path=string.sub(Workspace,1,string.len(Workspace)-1) .. v.source
		else
		    path=Workspace .. v.source
		end
	    else
		if string.sub(Workspace,string.len(Workspace),1)=="/" then
		    path=Workspace .. v.source
		else
		    path=Workspace .. "/" .. v.source
		end
	    end	
	    if v.featureType=="timeSeries" then
		v.vars.height.depthLevel=1
		--v.vars.isTimeSeriesProfile=false
		v.vars.height.value=v.depth
		v.vars.time.count,tsstart,tsend,v.vars.obs.valid_min,v.vars.obs.valid_max=parseFromSource(path,v.target,false,v.validator)
	    else--timeSeriesProfile
		--v.vars.height.depthLevel=#v.depths
		--v.vars.isTimeSeriesProfile=true
		--v.vars.height.value=v.depths
		v.vars.time.count,tsstart,tsend,v.vars.obs.valid_min,v.vars.obs.valid_max,v.vars.height.depthLevel=parseFromSource(path,v.target,true,v.validator)
	    end
	    v.vars.maxTextVarLength=128
	    v.vars.depthLevel=v.vars.height.depthLevel
	    --v.vars.height=v.vars.height.value
	    v.vars.time.attributes.time_coverage_start=os.date("%Y-%m-%dT%H:%M:%S%z",tsstart)
	    v.vars.time.attributes.time_coverage_end=os.date("%Y-%m-%dT%H:%M:%S%z",tsend)
	    v.vars.source=path
	    v.vars.feature_type_instance.value="urn:ioos:station:glos:"..Platform.title
	    v.vars.latitude.value=Platform.lat
	    v.vars.longitude.value=Platform.lon
	    v.vars.platform.attributes.ioos_code="urn:ioos:station:glos:"..Platform.id
	    v.vars.platform.attributes.long_name=Platform.title
	    v.vars.platform.value=Platform.title
	    v.vars.instrument.attributes.long_name="urn:ioos:sensor:cencoos:"..Platform.id..":"..k
	    v.vars.instrument.value=v.vars.instrument.attributes.long_name
	    --print("*************************")
	    --vardump(v)
	    p.NCs[k]=v
	end
	
	--vardump(p)
	return p
    else
	return nil
    end
end
function parseFromSource(path,output,is3D,validator)
    assert(type(validator)=="function","Validator must be provided")
    local f=io.open(path,"r")
    local count=0
    local dimCnt=1
    local i=nil
    local j=nil
    local v=nil
    local tmin=nil
    local tmax=nil
    local omin=nil
    local omax=nil
    local t=nil
    local tData={}
    local tTms={}
    if f then
	if is3D then
	    local tDim={}
            for line in f:lines() do
	    --data[#data+1]=line
	        t=split(line,",")
		if 3==#t then
		    i=tonumber(t[1])
		    v=tonumber(t[2])
		    j=tonumber(t[3])
		    if i and j and v then
			tTms[i]=1
			if not tDim[j] then
			    tDim[j]=1
			end
			if not tData[i] then
			    tData[i]={}
			end
			if validator(v) then
			    if not omin or not omax then
		                omin,omax=v,v
		            elseif v<omin then
		                omin=v
		            elseif v>omax then
		                omax=v
	                    end
			else
			    v=GlobalAttributes.missing_value
			end
			tData[i][j]=v
		    	--io.write(string.format("i=%f,j=%f,v=%f\n",i,j,v))
		    end
		end
	    end
	    local tDim1={}
	    local tTms1={}
	    for n in pairs(tDim) do table.insert(tDim1,n) end
	    for n in pairs(tTms) do table.insert(tTms1,n) end
	    table.sort(tDim1)
	    table.sort(tTms1)
	    of=io.open(output,"w")
	    if of then
		for _,tms in ipairs(tTms1) do
		    for _,depth in ipairs(tDim1) do
			of:write(string.format("%u,%f,%f\n",tms,tData[tms][depth]==nil and GlobalAttributes.missing_value or tData[tms][depth],depth))
		    end
		end
	        of:close()
		dimCnt=#tDim1
		count=#tTms1
		tmin=tTms1[1]
		tmax=tTms1[#tTms1]
	    end
	else
	    of=io.open(output,"w")
	    if of then
		for line in f:lines() do
	            t=split(line,",")
	            if 2==#t then
		        i=tonumber(t[1])
			v=tonumber(t[2])
		        if i then
			    table.insert(tTms,i)
	                    if validator(v) then
			        if not omin or not omax then
		                    omin,omax=v,v
		                elseif v<omin then
		                    omin=v
		                elseif v>omax then
		                    omax=v
	                        end
			        --of:write(string.format("%u,%f\n",i,v))
				tData[i]=v
			    else
			        --of:write(string.format("%u,%f\n",i,GlobalAttributes.missing_value))
				tData[i]=GlobalAttributes.missing_value
			    end
	                    
		        end
	            end
	        end
		table.sort(tTms)
		for _,tms in ipairs(tTms) do
		    of:write(string.format("%u,%f\n",tms,tData[tms]))
		end
		of:close()
		count=#tTms
		tmin=tTms[1]
		tmax=tTms[#tTms]
	    end
	end
	f:close()
    end
   
    return count,tmin,tmax,omin,omax,dimCnt
end
--for testing purpose:
--dofile("platform-config/13030")
--initPlatformInfo(Platform)


