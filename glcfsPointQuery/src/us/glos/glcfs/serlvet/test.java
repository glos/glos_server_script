package us.glos.glcfs.serlvet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glc.domain.Coordinate;
import org.glc.xmlconfig.ConfigManager;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDataset.Enhance;
import us.glos.glcfs.helpers.SqlHelper;

import org.joda.time.*;

/**
 * Servlet implementation class test
 */
public class test extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public test() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		/*org.joda.time.format.DateTimeFormatter dfter=org.joda.time.format.DateTimeFormat.forPattern("YYYY-MM-dd:HH:mm:ss Z");
		DateTime baseD=new DateTime(2011,1,1,0,0,0,0,DateTimeZone.UTC);
		DateTime dt1=dfter.parseDateTime("2011-04-12:16:19:00 -0400");
		DateTime dt2=dfter.withZone(DateTimeZone.UTC).parseDateTime("2011-04-12:16:19:00 -0400");
		response.getWriter().println(ConfigManager.getThreddsDatasetURL("michigan", "2010"));
		Set<Enhance> DATASET_ENHANCEMENTS =EnumSet.of(Enhance.ScaleMissingDefer, Enhance.CoordSystems);
		NetcdfDataset nc=NetcdfDataset.openDataset(
				//"http://michigan.glin.net:8080/thredds/dodsC/glos/glcfs/michigan/ncas_his2d.dods", 
				"http://michigan.glin.net:8080/thredds/dodsC/glos/glcfs/archive2010/michigan/ncas_his2d",				
				DATASET_ENHANCEMENTS,
								-1, // use default buffer size
									null, // no CancelTask
									null // no iospMessage
									);
		List<Variable> l=nc.getVariables();
		List<Dimension> d=nc.getDimensions();
		Variable vv=null;
		Variable vvv=null;
		for(Variable v:l)
		{
			if(v.getName().equals("time"))
			{
				vv=v;
				
			}
			else if(v.getName().equals("eta"))
				vvv=v;
		}
		Range r;
		Range r1;
		Range r2;
		Array arr;
		long la;
		List<Range> ranges=new ArrayList<Range>();
		try {
			
			r = new Range(8759,8759);
			r1=new Range(250,250);
			r2=new Range(130,130);
			
			ranges.add(r);
			arr=vv.read(ranges);
			la=arr.getSize();
			DateTime dt=new DateTime(arr.getLong(0)*1000,DateTimeZone.UTC);
			int m=dt.getMonthOfYear();
			int da=dt.getDayOfMonth();
			
			ranges.add(r1);ranges.add(r2);
			arr=vvv.read(ranges);
			la=arr.getSize();
			
		} catch (InvalidRangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(nc!=null)
			nc.close();
		try {
			ranges.remove(1);
			ranges.remove(1);
			arr=vv.read(ranges);
			
			la=arr.getSize();
			
		} catch (InvalidRangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//org.glc.domain.Coordinate org=us.glos.glcfs.helpers.GLCFSGrid.GetCoordInXY(87.9898071, 41.5976372, 2011, "michigan");
		//org.glc.domain.Coordinate coord=us.glos.glcfs.helpers.GLCFSGrid.GetCoordInXY(87.1777, 43.0693, 2011, "michigan");
		org.glc.domain.Coordinate coord=us.glos.glcfs.helpers.GLCFSGrid.GetCoordInXY(true,87.1529, 43.0515, null, "michigan");
		org.glc.domain.Coordinate ecoord=us.glos.glcfs.helpers.GLCFSGrid.GetCoordInXY(true,81.5, 42.5, null, "erie");
		org.glc.domain.Coordinate hcoord=us.glos.glcfs.helpers.GLCFSGrid.GetCoordInXY(true,83.188, 43.893, null, "huron");
		org.glc.domain.Coordinate h2006coord=us.glos.glcfs.helpers.GLCFSGrid.GetCoordInXY(true,80.9484, 45.240276, null, "huron2006");
		org.glc.domain.Coordinate ocoord=us.glos.glcfs.helpers.GLCFSGrid.GetCoordInXY(true,76.696, 44.05, null, "ontario");
		org.glc.domain.Coordinate scoord=us.glos.glcfs.helpers.GLCFSGrid.GetCoordInXY(true,85.593, 48.210, null, "superior");
		if(coord!=null)
		{
			response.getWriter().println(String.format("(%f,%f)", (coord.getLon()),(coord.getLat())));
			response.getWriter().println(String.format("(%f,%f)", (ecoord.getLon()),(ecoord.getLat())));
			response.getWriter().println(String.format("(%f,%f)", (hcoord.getLon()),(hcoord.getLat())));
			response.getWriter().println(String.format("(%f,%f)", (h2006coord.getLon()),(h2006coord.getLat())));
			response.getWriter().println(String.format("(%f,%f)", (ocoord.getLon()),(ocoord.getLat())));
			response.getWriter().println(String.format("(%f,%f)", (scoord.getLon()),(scoord.getLat())));
		}
		response.getWriter().println((int)(0.5));
		
		ArrayList<Coordinate> coord1=SqlHelper.getNearestCell(new Coordinate(-83.70, 44.16),"erie",3);
		if(coord1!=null)
			response.getWriter().println(String.format("Nearest:%f,%f", coord1.get(0).getLon(),coord1.get(0).getLat()));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
