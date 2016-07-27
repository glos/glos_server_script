package org.glc.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HTMLUtilities {
	/** Replaces characters that have special HTML meanings
	   *  with their corresponding HTML character entities.
	   */
	  // Note that Javadoc is not used for the more detailed
	  // documentation due to the difficulty of making the
	  // special chars readable in both plain text and HTML.
	  //
	  //  Given a string, this method replaces all occurrences of
	  //  '<' with '&lt;', all occurrences of '>' with
	  //  '&gt;', and (to handle cases that occur inside attribute
	  //  values), all occurrences of double quotes with
	  //  '&quot;' and all occurrences of '&' with '&amp;'.
	  //  Without such filtering, an arbitrary string
	  //  could not safely be inserted in a Web page.

	  public static String filterDisplay(String input) {
		if(input==null||input.equals(""))return "";
	    if (!hasSpecialChars(input)) {
	      return(input);
	    }
	    StringBuffer filtered = new StringBuffer(input.length());
	    char c;
	    for(int i=0; i<input.length(); i++) {
	      c = input.charAt(i);
	      switch(c) {
	        case '<': filtered.append("&lt;"); break;
	        case '>': filtered.append("&gt;"); break;
	        case '"': filtered.append("&quot;"); break;
	        case '&': filtered.append("&amp;"); break;
	        default: filtered.append(c);
	      }
	    }
	    return(filtered.toString());
	  }

	  private static boolean hasSpecialChars(String input) {
	    boolean flag = false;
	    if ((input != null) && (input.length() > 0)) {
	      char c;
	      for(int i=0; i<input.length(); i++) {
	        c = input.charAt(i);
	        switch(c) {
	          case '<': flag = true; break;
	          case '>': flag = true; break;
	          case '"': flag = true; break;
	          case '&': flag = true; break;
	        }
	      }
	    }
	    return(flag);
	  }
	  /**
       * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000 through U+001F).
       * @param input
       * @return
       */

	  public static String JSONStringify(String input)
	  {
		  if(input==null)return null;
		  StringBuilder sb=new StringBuilder();
		  for(int i=0;i<input.length();i++){
			  char ch=input.charAt(i);
			  switch(ch){
			  case '"':
				  sb.append("\\\"");
				  break;
			  case '\\':
				  sb.append("\\\\");
				  break;
			  case '\b':
				  sb.append("\\b");
				  break;
			  case '\f':
				  sb.append("\\f");
				  break;
			  case '\n':
				  sb.append("\\n");
				  break;
			  case '\r':
				  sb.append("\\r");
				  break;
			  case '\t':
				  sb.append("\\t");
				  break;
			  case '/':
				  sb.append("\\/");
				  break;
			  default:
				  //Reference: http://www.unicode.org/versions/Unicode5.1.0/
				  if((ch>='\u0000' && ch<='\u001F') || (ch>='\u007F' && ch<='\u009F') || (ch>='\u2000' && ch<='\u20FF')){
					  String ss=Integer.toHexString(ch);
					  sb.append("\\u");
					  for(int k=0;k<4-ss.length();k++){
						  sb.append('0');
					  }
					  sb.append(ss.toUpperCase());
				  }
				  else{
					  sb.append(ch);
				  }
			  }
		  }//for
		  return sb.toString();
	  }
	  public static boolean SetCookie(HttpServletRequest request,HttpServletResponse response,int expiry,String path,String name,String value)
		{
			Cookie cookie=null;
			if(request!=null&&response!=null&&expiry>0&&name!=null&&value!=null)
			{
				cookie=new Cookie(name,value);
				cookie.setMaxAge(expiry);
				if(path!=null)
					cookie.setPath(path);
				else
					cookie.setPath(request.getContextPath());
				response.addCookie(cookie);
				return true;
			}
			return false;
		}
		public static boolean RemoveCookie(HttpServletRequest request,HttpServletResponse response,String path,String name,String value)
		{
			Cookie cookie=null;
			if(request!=null&&response!=null&&name!=null&&value!=null)
			{
				cookie=new Cookie(name,value);
				cookie.setMaxAge(0);
				if(path!=null)
					cookie.setPath(path);
				else
					cookie.setPath(request.getContextPath());
				response.addCookie(cookie);
				return true;
			}
			return false;
		}
	  

}
