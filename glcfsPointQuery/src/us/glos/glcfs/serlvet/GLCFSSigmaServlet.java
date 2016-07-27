package us.glos.glcfs.serlvet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glc.xmlconfig.ConfigManager;
import org.glc.xmlconfig.LogLevel;
import org.glc.Logger;
import org.glc.domain.Coordinate;
import org.glc.utils.Validation;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDataset.Enhance;
import us.glos.glcfs.helpers.GLCFSGrid;

/**
 * Servlet implementation class GLCFSSigmaServlet
 */
public class GLCFSSigmaServlet extends HttpServlet {
	
       
    /**
	 * 
	 */
	private static final long serialVersionUID = -6746929985089588925L;

	/**
     * @see HttpServlet#HttpServlet()
     */
    public GLCFSSigmaServlet() {
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
		String lake;
		double lon,lat;
		if(Validation.basicValidation(request, "l")&&Validation.basicValidation(request, "x")&&Validation.basicValidation(request, "y"))
		{
			lake=request.getParameter("l");
			try
			{
				lon=Double.parseDouble(request.getParameter("x"));
				lat=Double.parseDouble(request.getParameter("y"));
				
				Coordinate coord=GLCFSGrid.GetCoordInXY(true, lon, lat, String.format("%s",Calendar.getInstance(TimeZone.getTimeZone("UTC")).get(Calendar.YEAR)), lake);
				if(coord==null||coord.getLat()==-1||coord.getLon()==-1)
				{
					response.getWriter().print("{}");
					return;
				}
				int y=(int)Math.floor(coord.getLat());
				int x=(int)Math.floor(coord.getLon());
				String url=ConfigManager.getThreddsDatasetURL(lake,"nowcast:current-3d");
				NetcdfDataset nc=NetcdfDataset.openDataset(url,				
						EnumSet.of(Enhance.ScaleMissingDefer),
						-1, // use default buffer size
						null, // no CancelTask
						null // no iospMessage
				);
				if(nc!=null&&nc.getVariables()!=null)
				{
					Variable var=null;
					for(Variable vv:nc.getVariables())
					{
						if(vv!=null&&vv.getName().equals("depth"))
						{
							var=vv;
							break;
						}
					}
					if(var!=null)
					{
						List<Range> dimList=new ArrayList<Range>(3);
						try
						{
							//dimList.add(new Range(0,var.getDimension(0).getLength()-1));
							dimList.add(new Range(y,y));
							dimList.add(new Range(x,x));
							Array d3dVals=var.read(dimList);
							if(d3dVals!=null&&d3dVals.getSize()>0)
							{
								StringBuffer sb=new StringBuffer();
								sb.append("{\"bathy\":");
								//for(int i=0;i<d3dVals.getSize();++i)
									//sb.append(String.format("%f,",d3dVals.getDouble(i)));
								//if(sb.charAt(sb.length()-1)==',')
								//	sb.deleteCharAt(sb.length()-1);
								sb.append(String.format("%f",d3dVals.getDouble(0)));
								sb.append('}');
								//response.setContentType("application/json");
								response.getWriter().print(sb.toString());
								response.getWriter().flush();
								return;
							}
						}
						catch (InvalidRangeException e)
						{
							Logger.writeLog(this.getClass().getName()+": "+e.getMessage(), LogLevel.SEVERE);
							Logger.writeLog(this.getClass().getName()+": Query String -- "+request.getQueryString(), LogLevel.SEVERE);
						}
					}
					
				}
				if(nc!=null)
					nc.close();
			}
			catch(NumberFormatException nfe)
			{
				
			}
			//response.setContentType("application/json");
			response.getWriter().print("{}");
			return;
		}
	}
}
