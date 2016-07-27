package org.glc.listeners;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * 
 * @author gwang
 * you need to put the following in the web.xml
 * 
 *<listener>
 *<listener-class>
 *coreservlets.listeners.SessionCounter
 *</listener-class>
 *</listener>
 * 
 */
public class SessionCounter implements HttpSessionListener {
	
	private static final String SESSION_COUNTER_NAME=SessionCounter.class.getName();
	private volatile boolean isStored;
	private int counter=0;
	private synchronized void storeInContext(HttpSessionEvent e)
	{
		if(this.isStored==false&&e!=null)
		{
			e.getSession().getServletContext().setAttribute(SESSION_COUNTER_NAME, this);
		}
	}
	protected void setPredefinedValues()
	{
		
	}
	public int getSessionCounter()
	{
		return this.counter;
	}
	@Override
	public void sessionCreated(HttpSessionEvent event) {
		// TODO Auto-generated method stub
		
		if(this.isStored==false)
		{
			this.storeInContext(event);
		}
		synchronized(this)
		{
			
			this.counter++;
		}
		setPredefinedValues();
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		// TODO Auto-generated method stub
		synchronized(this)
		{
			this.counter--;
		}
	}

}
