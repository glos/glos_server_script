/*
;---xdist.pro-------------------------------------------------------------------
function xdist,rlat,rlon,ip,rp
; geographic to map coordinate transformation for x coordinate

alpha=rp(6)*!dtor

; transformation for approximate polyconic projection

if(ip(4) eq 0) then begin
 dlat=rlat-rp(0)
 dlon=rp(1)-rlon
 xp=rp(7)*dlon+rp(8)*dlat+rp(9)*dlon*dlat+rp(10)*dlon*dlon
 yp=rp(11)*dlon+rp(12)*dlat+rp(13)*dlat*dlon+rp(14)*dlon*dlon
 xp=xp*1000.
 yp=yp*1000.
end

; transformation for lambert conformal conic projection

if(ip(4) eq 1) then begin
 r=(tan(!pi/4.-!dtor*rlat/2.))^rp(10)
 xp=rp(11)*r*sin(rp(10)*(rp(7)-!dtor*rlon))
 yp=-rp(11)*r*cos(rp(10)*(rp(7)-!dtor*rlon))
 xp=xp-rp(12)
 yp=yp-rp(13)
end

; transform to 'unprimed' system

;  first rotate

xd=xp*cos(alpha)+yp*sin(alpha)

; now translate

xd=xd+ip(2)*rp(2)
 
return,xd
end

;---ydist.pro-------------------------------------------------------------------
function ydist,rlat,rlon,ip,rp
; geographic to map coordinate transformation for y coordinate

alpha=rp(6)*!dtor

; transformation for approximate polyconic projection

if(ip(4) eq 0) then begin
 dlat=rlat-rp(0)
 dlon=rp(1)-rlon
 xp=rp(7)*dlon+rp(8)*dlat+rp(9)*dlon*dlat+rp(10)*dlon*dlon
 yp=rp(11)*dlon+rp(12)*dlat+rp(13)*dlat*dlon+rp(14)*dlon*dlon
 xp=xp*1000.
 yp=yp*1000.
end

; transformation for lambert conformal conic projection

if(ip(4) eq 1) then begin
 r=(tan(!pi/4.-!dtor*rlat/2.))^rp(10)
 xp=rp(11)*r*sin(rp(10)*(rp(7)-!dtor*rlon))
 yp=-rp(11)*r*cos(rp(10)*(rp(7)-!dtor*rlon))
 xp=xp-rp(12)
 yp=yp-rp(13)
end

; transform to 'unprimed' system

;  first rotate

yd=yp*cos(alpha)-xp*sin(alpha)

; now translate

yd=yd+ip(3)*rp(2)

return,yd
end



;----------------------------------------------------------------------
pro rgrid,fname,gname,dgrid,iparm,rparm

; Purpose:
;           To read a bathymetric grid data file
;           and return grid parameters and depths.
; Arguments:
;   On input:
;           fname - Full pathway name of bathymetric data file
;   On output:
;           gname - Lake name (from bathymetric data file)
;           dgrid - Depth grid
;           iparm - Integer parameters for grid description and
;                      coordinate conversion
;           rparm - Real parameters for grid description and
;                      coordinate conversion
;
;       ------------------------------------------------------------------
;                      FORMAT OF BATHYMETRIC DATA FILE
;       ------------------------------------------------------------------
;                 FIELD                            FORTRAN FORMAT  COLUMNS
;       ------------------------------------------------------------------
;       RECORD 1: LAKE NAME                            40A1          1-40
;       RECORD 2: FIRST (I) DIMENSION OF DEPTH ARRAY   I5            1-5
;                 SECOND (J) DIMENSION OF DEPTH ARRAY  I5            6-10
;                 BASE LATITUDE                        F12.7        11-22
;                 BASE LONGITUDE                       F12.7        23-34
;                 GRID SIZE                            F5.0         35-39
;                 MAXIMUM DEPTH                        F5.0         40-44
;                 MINIMUM DEPTH                        F5.0         45-49
;                 BASE ROTATION (CCW FROM E-W)         F6.2         50-55
;                 I DISPLACEMENT                       I5           56-60
;                 J DISPLACEMENT                       I5           61-65
;                 ROTATION FROM BASE (CCW)             F6.2         66-71
;                 POWER OF TEN TO CONVERT DEPTHS TO
;                                       METERS         I3           72-74
;                 MAP PROJECTION INDICATOR
;                  0=APPROXIMATE POLYCONIC
;                  1=LAMBERT CONFORMAL CONIC           I2           75-76
;       RECORDS 3-6 (FOR APPROXIMATE POLYCONIC PROJECTION):
;                 MAP PROJECTION COORDINATE CONVERSION
;                  COEFFICIENTS                       4E15.6         1-60
;       RECORD 3 (FOR LAMBERT CONFORMAL CONIC PROJECTION):
;                 CENTRAL MERIDIAN OF PROJECTION       E15.6         1-15
;                 SOUTHERNMOST STANDARD PARALLEL       E15.6        16-30
;                 NORTHERNMOST STANDARD PARALLEL       E15.6        31-45
;       FOLLOWING RECORDS:
;                 DEPTHS IN ASCENDING I, ASCENDING J
;                  SEQUENCE, 19 TO A RECORD           19F4.0         1-76
;       ------------------------------------------------------------------
;
; ON OUTPUT:
;                     D - DEPTH ARRAY. ZERO FOR LAND, AVERAGE DEPTH
;                         OF GRID BOX IN METERS FOR WATER.
;                     RPARM - ARRAY CONTAINING REAL-VALUED BATHYMETRIC
;                            GRID PARAMETERS AS FOLLOWS:
;                        1.  BASE LATITUDE
;                        2.  BASE LONGITUDE
;                        3.  GRID SIZE (M)
;                        4.  MAXIMUM DEPTH (M)
;                        5.  MINIMUM DEPTH (M)
;                        6.  BASE ROTATION (COUNTERCLOCKWISE FROM E-W)
;                        7.  ROTATION FROM BASE (COUNTERCLOCKWISE)
;
;                      FOR IPARM(45)=0 (APPROXIMATE POLYCONIC PROJECTION):
;                        8-11. GEOGRAPHIC-TO-MAP COORDINATE CONVERSION
;                             COEFFICIENTS FOR X
;                        12-15.  GEOGRAPHIC-TO-MAP COORDINATE CONVERSION
;                                COEFFICIENTS FOR Y
;                        16-19.  MAP-TO-GEOGRAPHIC COORDINATE CONVERSION
;                                COEFFICIENTS FOR LONGITUDE
;                        20-23.  MAP-TO-GEOGRAPHIC COORDINATE CONVERSION
;                                COEFFICIENTS FOR LATITUDE
;
;                      FOR IPARM(45)=1 (LAMBERT CONFORMAL CONIC PROJECTION):
;                        8.  CENTRAL MERIDIAN OF PROJECTION (RADIANS)
;                        9.  SOUTHERNMOST STANDARD PARALLEL (RADIANS)
;                        10. NORTHERNMOST STANDARD PARALLEL (RADIANS)
;                        11. LOGARITHMIC COEFFICIENT FOR TRANSFORMATIONS
;                        12. DISTANCE SCALING FACTOR FOR TRANSFORMATIONS
;                        13. X DISPLACEMENT OF BATHYMETRIC GRID ORIGIN
;                             FROM MAP PROJECTION ORIGIN
;                        14. Y DISPLACEMENT OF BATHYMETRIC GRID ORIGIN
;                             FROM MAP PROJECTION ORIGIN
;
;                    IPARM - ARRAY CONTAINING INTEGER-VALUED BATHYMETRIC
;                            GRID PARAMETERS AS FOLLOWS:
;                        1.  NUMBER OF GRID BOXES IN X DIRECTION
;                        2.  NUMBER OF GRID BOXES IN Y DIRECION
;                        3.  I DISPLACEMENT - THE NUMBER OF NEW GRID
;                            SQUARES IN THE X-DIRECTION FROM THE NEW
;                            GRID ORIGIN TO THE OLD GRID ORIGIN
;                            (USED ONLY FOR IPARM(45)=0)
;                        4.  J DISPLACEMENT - THE NUMBER OF NEW GRID
;                            SQUARES IN THE Y-DIRECTION FROM THE NEW
;                            GRID ORIGIN TO THE OLD GRID ORIGIN
;                            (USED ONLY FOR IPARM(45)=0)
;                        5. MAP PROJECTION USED FOR BATHYMETRIC GRID:
;                            0=APPROXIMATE POLYCONIC (GREAT LAKES GRIDS)
;                            1=LAMBERT CONFORMAL CONIC
gname=' '
rparm=fltarr(23)
iparm=intarr(5)
openr,1,fname
readf,1,gname
iw1=intarr(2)
iw2=intarr(2)
rw1=fltarr(6)
r1=0.
on_ioerror,next
readf,1,iw1,rw1,iw2,r1,idexp,iproj,  $
 format='(2I5,2F12.7,3F5.0,F6.2,2I5,F6.2,I3,I2)'
goto,next1
next:idexp=0
iproj=0
next1:on_ioerror,null
iparm(0:1)=iw1
rparm(0:5)=rw1
iparm(2:3)=iw2
iparm(4)=iproj
rparm(6)=r1
if(iproj eq 0) then begin	;Approximate Polyconic Projection
 rw1=fltarr(16)
 readf,1,format='(4e15.6)',rw1
 rparm(7:22)=rw1
end
if(iproj eq 1) then begin	;Lambert Conformal Conic
 rw1=fltarr(3)
 readf,1,format='(3e15.6)',rw1
 rparm(7:9)=rw1
end
im=iparm(0)
jm=iparm(1)
dgrid=fltarr(im,jm)
readf,1,dgrid,format='(19f4.0)'
close,1

; adjust depths

dfac=10.^idexp
rparm(3)=rparm(3)*dfac
rparm(4)=rparm(4)*dfac
dgrid=dgrid*dfac

; for lambert projection compute required constants

if(iparm(4) eq 1) then begin
 a45=atan(1.)
 rparm(7)=rparm(7)*!dtor
 rparm(8)=rparm(8)*!dtor
 rparm(9)=rparm(9)*!dtor
; alon0=rparm(7)
 a1=rparm(8)
 a2=rparm(9)
 rparm(10)=(alog(cos(a1))-alog(cos(a2)))/          $
    (alog(tan(a45-a1/2.))-alog(tan(a45-a2/2.)))

; set scale factor for n-s distance from a1 to a2

 aexp=rparm(10)
 y1=(tan(a45-a1/2.))^aexp
 y2=(tan(a45-a2/2.))^aexp
 rparm(11)=6378140.*(a2-a1)/(y1-y2)
 rparm(12)=0.
 rparm(13)=0.
 dx=xdist(rparm(0),rparm(1),iparm,rparm)
 dy=ydist(rparm(0),rparm(1),iparm,rparm)
 rparm(12)=dx
 rparm(13)=dy
end
return
end
;-------------------------------------------------------------------------

*/
package us.glos.glcfs.helpers;

import java.util.HashMap;

import org.glc.domain.Coordinate;

public class GLCFSGrid {
	
	private static final double Degree2Radians=0.017453293;
	private static final double PI=3.1415927;
	private static HashMap<String,int[]> IPARAM=null;
	private static HashMap<String,double[]> RPARAM=null;
	//private static HashMap<String,Coordinate> ORIGIN=null;
	static
	{
		IPARAM=new HashMap<String,int[]>(6);
		RPARAM=new HashMap<String,double[]>(6);
		//ORIGIN=new HashMap<String,Coordinate>(6);
		int[] michiganIP={131, 251, 0, 0, 1};
		
		double[] michiganRP={41.5976372, 87.9898071, 2000, 273*Math.pow(10, 0), 1*Math.pow(10, 0), 0.00, 0.00, 86.500000*Degree2Radians, 43.000000*Degree2Radians, 45.000000*Degree2Radians, 0.0, 0.0, 0.0, 0.0};
		michiganRP[10]=(Math.log(Math.cos(michiganRP[8]))-Math.log(Math.cos(michiganRP[9])))/(Math.log(Math.tan(Math.atan(1.0)-michiganRP[8]/2.0))-Math.log(Math.tan(Math.atan(1.0)-michiganRP[9]/2.0)));
		michiganRP[11]=6378140.*(michiganRP[9]-michiganRP[8])/(Math.pow(Math.tan(Math.atan(1.0)-michiganRP[8]/2.0), michiganRP[10])-Math.pow(Math.tan(Math.atan(1.0)-michiganRP[9]/2.0), michiganRP[10]));
		IPARAM.put("michigan", michiganIP);
		RPARAM.put("michigan", michiganRP);
		Coordinate origin=GetCoordInXY(false,michiganRP[1],michiganRP[0],null,"michigan");
		if(origin!=null)
		{
			michiganRP[12]=origin.getLon();
			michiganRP[13]=origin.getLat();
		}
		
		//Coordinate coord=GetCoordInXY(michiganRP[1],michiganRP[0],null,"michigan");
		//ORIGIN.put("michigan", coord);
		int[] erieIP={193, 87, 0, 0, 1};
		double[] erieRP={41.3358841, 83.4890823, 2000, 628*Math.pow(10, -1), 30*Math.pow(10, -1), 0.00, 0.00, 81.000000*Degree2Radians, 41.750000*Degree2Radians, 42.500000*Degree2Radians, 0.0, 0.0, 0.0, 0.0};
		erieRP[10]=(Math.log(Math.cos(erieRP[8]))-Math.log(Math.cos(erieRP[9])))/(Math.log(Math.tan(Math.atan(1.0)-erieRP[8]/2.0))-Math.log(Math.tan(Math.atan(1.0)-erieRP[9]/2.0)));
		erieRP[11]=6378140.*(erieRP[9]-erieRP[8])/(Math.pow(Math.tan(Math.atan(1.0)-erieRP[8]/2.0), erieRP[10])-Math.pow(Math.tan(Math.atan(1.0)-erieRP[9]/2.0), erieRP[10]));
		IPARAM.put("erie", erieIP);
		RPARAM.put("erie", erieRP);
		origin=GetCoordInXY(false,erieRP[1],erieRP[0],null,"erie");
		if(origin!=null)
		{
			erieRP[12]=origin.getLon();
			erieRP[13]=origin.getLat();
		}
		
		int[] huronIP={201, 188, 0, 0, 1};
		double[] huronRP={42.9612122, 84.6548080, 2000, 2144*Math.pow(10, -1), 20*Math.pow(10, -1), 0.00, 0.00, 82.000000*Degree2Radians, 43.000000*Degree2Radians, 45.000000*Degree2Radians, 0.0, 0.0, 0.0, 0.0};
		huronRP[10]=(Math.log(Math.cos(huronRP[8]))-Math.log(Math.cos(huronRP[9])))/(Math.log(Math.tan(Math.atan(1.0)-huronRP[8]/2.0))-Math.log(Math.tan(Math.atan(1.0)-huronRP[9]/2.0)));
		huronRP[11]=6378140.*(huronRP[9]-huronRP[8])/(Math.pow(Math.tan(Math.atan(1.0)-huronRP[8]/2.0), huronRP[10])-Math.pow(Math.tan(Math.atan(1.0)-huronRP[9]/2.0), huronRP[10]));
		IPARAM.put("huron", huronIP);
		RPARAM.put("huron", huronRP);
		origin=GetCoordInXY(false,huronRP[1],huronRP[0],null,"huron");
		if(origin!=null)
		{
			huronRP[12]=origin.getLon();
			huronRP[13]=origin.getLat();
		}
		
		int[] huron2006IP={81, 75, 1, 0, 0};
		double[] huron2006RP={42.9660301, 84.6620712, 5000, 206*Math.pow(10, 0), 1*Math.pow(10, 0), 0.0, 0.0, 
				   0.815869E+02,   0.257566E+01,  -0.134705E+01,   0.000000E+00,
				  -0.185606E+01,   0.111111E+03,   0.000000E+00,   0.485354E+00,
				   0.122482E-01,  -0.297178E-03,   0.192849E-05,   0.000000E+00,
				   0.220311E-03,   0.899372E-02,   0.000000E+00,  -0.702891E-06
					};
		IPARAM.put("huron2006", huron2006IP);
		RPARAM.put("huron2006", huron2006RP);
		
		
		int[] ontarioIP={61, 25, 0, 1, 0};
		double[] ontarioRP={43.1655426, 79.8199615, 5000, 226*Math.pow(10, 0), 7*Math.pow(10, 0), 0.0, 0.0, 
				   0.813204E+02,   0.242939E+01,  -0.133486E+01,   0.0,                          
				  -0.176688E+01,   0.111101E+03,   0.0,            0.485416E+00,                 
				   0.122965E-01,  -0.274739E-03,   0.185864E-05,   0.0,                          
				   0.199237E-03,   0.899770E-02,   0.0,           -0.674272E-06                 
					};
		IPARAM.put("ontario", ontarioIP);
		RPARAM.put("ontario", ontarioRP);
		
		int[] superiorIP={61, 30, 1, 0, 0};
		double[] superiorRP={46.3184662, 92.1027527, 10000, 326*Math.pow(10, 0), 12*Math.pow(10, 0), 0.0, 0.0, 
				   0.770215E+02,   0.584617E+01,  -0.142494E+01,   0.0,                            
				  -0.398892E+01,   0.111176E+03,   0.0,            0.486127E+00,                  
				   0.129841E-01,  -0.717624E-03,   0.226882E-05,   0.0,                            
				   0.477160E-03,   0.898120E-02,   0.0,           -0.757848E-06                                    
					};
		IPARAM.put("superior", superiorIP);
		RPARAM.put("superior", superiorRP);
		
	}
	public static Coordinate GetCoordInXY(boolean isCellNo,double lon,double lat,String year,String lake)
	{
		Coordinate coord=null;
		if(lake!=null&&year!=null&&lake.equals("huron")&&(year.equals("2006")||year.equals("2007")||year.equals("2008")))
			lake="huron2006";
		int[] iparam=IPARAM.get(lake);
		double[] rparam=RPARAM.get(lake);
		
		if(iparam!=null&&rparam!=null)
		{
			if(lon<0)lon=lon*-1.0;
			double alpha=rparam[6]*Degree2Radians;
			double xp=0.,yp=0.;
			if(iparam[4]==1)
			{
				double r=Math.pow(Math.tan(PI/4.0-Degree2Radians*lat/2.0),rparam[10]);
				xp=rparam[11]*r*Math.sin(rparam[10]*(rparam[7]-Degree2Radians*lon));
				yp=-rparam[11]*r*Math.cos(rparam[10]*(rparam[7]-Degree2Radians*lon));
				xp-=rparam[12];
				yp-=rparam[13];
				
			}
			else if(iparam[4]==0)
			{
				xp=1000.0*(rparam[7]*(rparam[1]-lon)+rparam[8]*(lat-rparam[0])+rparam[9]*(rparam[1]-lon)*(lat-rparam[0])+rparam[10]*(rparam[1]-lon)*(rparam[1]-lon));
				yp=1000.0*(rparam[11]*(rparam[1]-lon)+rparam[12]*(lat-rparam[0])+rparam[13]*(rparam[1]-lon)*(lat-rparam[0])+rparam[14]*(rparam[1]-lon)*(rparam[1]-lon));
				
			}
			double xd=xp*Math.cos(alpha)+yp*Math.sin(alpha)+iparam[2]*rparam[2];
			double yd=yp*Math.cos(alpha)-xp*Math.sin(alpha)+iparam[3]*rparam[2];
			//Coordinate ocoord=ORIGIN.get(lake);
			//if(ocoord!=null)
			//	coord=new Coordinate((xd-ocoord.getLon())/rparam[2],(yd-ocoord.getLat())/rparam[2]);
				//coord=new Coordinate((xd)/rparam[2],(yd)/rparam[2]);
			//else
			if(isCellNo)
			{
				double xr=xd/rparam[2];
				double yr=yd/rparam[2];
				if(xr>=0&&xr<iparam[0]&&yr<iparam[1]&&yr>=0)
					coord=new Coordinate(xd/rparam[2],yd/rparam[2]);
				else
					coord=new Coordinate(-1,-1);
			}
			else
				coord=new Coordinate(xd,yd);
		}
		return coord;
	}
}
