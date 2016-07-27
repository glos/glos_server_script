package us.glos.glcfs.serlvet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glc.utils.Validation;
import org.glc.domain.Coordinate;

import us.glos.glcfs.domain.NearestCoordinate;
import us.glos.glcfs.helpers.GLCFSGrid;
import us.glos.glcfs.helpers.SqlHelper;
/**
 * Servlet implementation class GLCFSSpatialServlet
 */
public class GLCFSSpatialServlet extends HttpServlet {
	
    private static String[] Lakes={"erie","huron","michigan","ontario","superior"};   
    /**
	 * 
	 */
	private static final long serialVersionUID = 1971991906885446214L;

	/**
     * @see HttpServlet#HttpServlet()
     */
    public GLCFSSpatialServlet() {
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
		Coordinate coord=null;
		String lake=null;
		double distance=0;
		double lon=-1.,lat=-1.;
		int ci=-1,cj=-1;
		if(Validation.basicValidation(request, "x")&&Validation.basicValidation(request, "y"))
		{
			try
			{
				lon=Double.parseDouble(request.getParameter("x"));
				lat=Double.parseDouble(request.getParameter("y"));
				lake=request.getParameter("l");
				if(false==Validation.basicValidation(request, "f"))
				{
					if(lake!=null)
						coord=GLCFSGrid.GetCoordInXY(true, lon, lat, String.format("%s",Calendar.getInstance(TimeZone.getTimeZone("UTC")).get(Calendar.YEAR)), lake.toLowerCase());
					else
						for(int i=0;i<Lakes.length;++i)
						{
							coord=GLCFSGrid.GetCoordInXY(true, lon, lat, String.format("%s",Calendar.getInstance(TimeZone.getTimeZone("UTC")).get(Calendar.YEAR)), Lakes[i]);
							if(coord==null||coord.getLat()==-1.||coord.getLon()==-1.)
								continue;
							else
							{
								if(SqlHelper.isInLake(new Coordinate(lon,lat), Lakes[i]))
								{
									lake=Lakes[i];
									ci=(int)Math.floor(coord.getLon());
									cj=(int)Math.floor(coord.getLat());
									break;
								}
								
							}
						}
				}
				else
				{
					if(lake!=null)
					{
						ArrayList<Coordinate> coords=SqlHelper.getNearestCell(new Coordinate(lon,lat), lake.toLowerCase(), 1);
						if(coords!=null&&coords.size()>0)
						{
							coord=coords.get(0);
							lon=coord.getLon();
							lat=coord.getLat();
							coord=GLCFSGrid.GetCoordInXY(true, lon, lat, String.format("%s",Calendar.getInstance(TimeZone.getTimeZone("UTC")).get(Calendar.YEAR)), lake.toLowerCase());
							ci=(int)Math.floor(coord.getLon());
							cj=(int)Math.floor(coord.getLat());
							distance=((NearestCoordinate)coords.get(0)).getDistance();
						}
					}
				}
			}
			catch(NumberFormatException nfe)
			{
				
			}
		}
		if(lake!=null)
		{
			//response.setContentType("application/json");
			response.getWriter().print(String.format("{\"lon\":%f,\"lat\":%f,\"i\":%d,\"j\":%d,\"lake\":\"%s\",\"dist\":%f}", lon,lat,ci,cj,lake,distance));
		}
		else
			response.getWriter().print("{}");
		response.getWriter().flush();
	}

}
