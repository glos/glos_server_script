package org.glc.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.glc.listeners.SessionCounter;
import org.glc.xmlconfig.ConfigManager;
import org.glc.xmlconfig.LogLevel;
import org.glc.xmlconfig.Action.Scope;
import org.glc.ILiteralProvider;
import org.glc.DefaultLiteralProvider;
import org.glc.Logger;


public class ServerUtilities {
	private static Properties MailConfig=null;
	private static String MailFrom=null;
	static
	{
		if(ConfigManager.getAppSetting("mail.smtp.host")!=null)
		{
			MailConfig=System.getProperties();
			MailConfig.setProperty("mail.smtp.host", ConfigManager.getAppSetting("mail.smtp.host"));
			MailFrom=ConfigManager.getAppSetting("mail.from");
			if(MailFrom==null)
				MailFrom="localhost@localhost";
		}
	}
	public static boolean sendMail(String subject,String content,String from,String[] to)
	{
		if(MailConfig!=null&&to!=null&&to.length>0)
		{
			Session session=Session.getDefaultInstance(MailConfig);
			try
			{
				MimeMessage message=new MimeMessage(session);
				if(from==null)
					message.setFrom(new InternetAddress(MailFrom));
				else
					message.setFrom(new InternetAddress(from));
				InternetAddress[] tos=new InternetAddress[to.length];
				for(int i=0;i<tos.length;++i)
				{
					tos[i]=new InternetAddress(to[i]);
				}
				message.setRecipients(RecipientType.TO,tos);
				message.setSubject(subject);
				message.setContent(content, "text/plain");
				Transport.send(message);
				return true;
			}
			catch(MessagingException mex)
			{
				Logger.writeLog("ServerUtilities:sendMail "+mex.getMessage(), LogLevel.SEVERE);
			}
		}
		return false;
	}
	public static int getCurrentSessionCount(ServletContext context)
	{
		if(context!=null)
		{
			if(context.getAttribute(SessionCounter.class.getName())!=null)
			{
				Object o=context.getAttribute(SessionCounter.class.getName());
				if(o instanceof SessionCounter)
				{
					return ((SessionCounter)o).getSessionCounter();
				}
			}
		}
		return -1;
	}
	public static void GoTo(ServletContext context,HttpServletRequest request,HttpServletResponse response,String className,String action) throws IOException,ServletException
	{
		if(context==null||request==null||response==null||className==null||action==null)return;
		String path=ConfigManager.getActionForward(className, action);
		if(path!=null&&path.equals("")==false)
		{
			if(ConfigManager.isForwardRedirect(className, action))
				response.sendRedirect(String.format("%s%s", context.getContextPath(),path));
			else
			{
				RequestDispatcher dispatcher = request.getRequestDispatcher(path);
				dispatcher.forward(request, response);
			}
		}
	}
	public static void SetKeyValueOnAction(HttpServletRequest request,String className,String action,String key,Object obj) throws ServletException
	{
		if(request==null||className==null||action==null||key==null)return;
		if(ConfigManager.isForwardRedirect(className, action))//redirect
		{
			HttpSession session=request.getSession(false);
			if(session!=null)
				session.setAttribute(key, obj);
			else
			{
				Logger.writeLog(ServerUtilities.class.getName()+" SetKeyValueOnAction: Session does not exist.", LogLevel.SEVERE);
				throw new javax.servlet.ServletException("Session does not exist.");
			}
		}
		else
		{
			request.setAttribute(key, obj);
		}
	}
	public static void SetValueOnScope(HttpServletRequest request,HttpSession session,String className,String key,Object obj)
	{
		if(request==null||className==null||session==null||key==null)return;
		Scope scope=ConfigManager.getActionScope(className);
		if(scope==Scope.REQUEST&&request!=null&&key!=null&&key.equals("")==false)
			request.setAttribute(key, obj);
		else if(scope==Scope.SESSION&&session!=null&&key!=null&&key.equals("")==false)
			session.setAttribute(key, obj);
	}
	public static ILiteralProvider GetLiteralProvider()
	{
		return DefaultLiteralProvider.getInstance();
	}
	public static Object ReadObjectFromBinary(byte[] bytes)
	{
		Object obj=null;
		if(bytes!=null)
		{
			ByteArrayInputStream instream=new ByteArrayInputStream(bytes);
    		ObjectInputStream iobj=null;
    		try {
				iobj=new ObjectInputStream(instream);
				obj=iobj.readObject();
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Logger.writeLog("ServerUtilities:"+e.getMessage(), LogLevel.SEVERE);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				Logger.writeLog("ServerUtilities:"+e.getMessage(), LogLevel.SEVERE);
			}
			finally
			{
				try
				{
				    if(iobj!=null)
					    iobj.close();
				    if(instream!=null)
				    	instream.close();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					Logger.writeLog("ServerUtilities:"+e.getMessage(), LogLevel.SEVERE);
				}
			}
		}
		return obj;
	}
	public static byte[] WriteObjectToBinary(Object obj) {
		// TODO Auto-generated method stub
		byte[] bytes=null;
		if(obj!=null&&obj instanceof java.io.Serializable)
		{
			ByteArrayOutputStream ostream=new ByteArrayOutputStream();
			ObjectOutputStream oobj=null;
			try {
				oobj=new ObjectOutputStream(ostream);
				oobj.writeObject(obj);
				oobj.flush();
				bytes=ostream.toByteArray();
							
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Logger.writeLog("ServerUtilities:"+e.getMessage(), LogLevel.SEVERE);
			}
			finally
			{
				try
				{
				    if(oobj!=null)
					    oobj.close();
				    if(ostream!=null)
				    	ostream.close();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					Logger.writeLog("ServerUtilities:"+e.getMessage(), LogLevel.SEVERE);
				}
			}
		}
		return bytes;
	}
}
