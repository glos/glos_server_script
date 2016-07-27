;----------------------------------------------------------------------
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

