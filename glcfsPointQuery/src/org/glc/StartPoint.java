package org.glc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.glc.xmlconfig.ConfigManager;
import org.glc.ISecurity;
import org.glc.IValidParam;
import org.glc.IRequestHandler;
import org.glc.IErrorHandler;

public class StartPoint implements Filter {

	private static final String SECURITY_CLASS_KEY="SECURITY";
	private static final String VALIDATOR_CLASS_KEY="VALIDATOR";
	private static final String REQUEST_HANDLER_KEY="CUSTOM_REQUEST_HANDLER";
	private static final String ERROR_HANDLER_KEY="CUSTOM_ERROR_HANDLER";
	private int i=0;
	private ISecurity guard=null;
	private IValidParam validator=null;
	private IRequestHandler handler=null;
	private IErrorHandler errHandler=null;
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if(guard!=null)
			guard.dispose();
		if(validator!=null)
			validator.dispose();
		if(handler!=null)
			handler.dispose();
		if(errHandler!=null)
			errHandler.dispose();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		// TODO Auto-generated method stub
		boolean result=true;
		if(guard!=null&&false==guard.IsAuthenticated(request, response))
			result=false;
		else if(validator!=null&&false==validator.IsParamValid(request, response))
			result=false;
		else if(handler!=null&&false==handler.isValid(request, response))
			result=false;
		if(result)
		    chain.doFilter(request, response);
		else
			if(errHandler!=null)
				errHandler.processError(request, response);
			else 
				return;
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		// TODO Auto-generated method stub
		ConfigManager.init(config.getServletContext(), false);
		String temp=config.getInitParameter(SECURITY_CLASS_KEY);
		try
		{
			if(temp!=null)
			{
				Class<ISecurity> secClass=(Class<ISecurity>)Class.forName(temp);
				this.guard=secClass.getConstructor().newInstance(new Object[0]);
				temp=null;
			}
			temp=config.getInitParameter(VALIDATOR_CLASS_KEY);
			if(temp!=null)
			{
				Class<IValidParam> vpmClass=(Class<IValidParam>)Class.forName(temp);
				this.validator=vpmClass.getConstructor().newInstance(new Object[0]);
				temp=null;
			}
			temp=config.getInitParameter(REQUEST_HANDLER_KEY);
			if(temp!=null)
			{
				Class<IRequestHandler> rhClass=(Class<IRequestHandler>)Class.forName(temp);
				this.handler=rhClass.getConstructor().newInstance(new Object[0]);
				temp=null;
			}
			
			temp=config.getInitParameter(ERROR_HANDLER_KEY);
			if(temp!=null)
			{
				Class<IErrorHandler> rhClass=(Class<IErrorHandler>)Class.forName(temp);
				this.errHandler=rhClass.getConstructor().newInstance(new Object[0]);
				temp=null;
			}
		}
		catch(ClassNotFoundException cfe)
		{
			throw new ServletException(cfe.getMessage());
		}
		catch(IllegalAccessException iae)
		{
			throw new ServletException(iae.getMessage());
		}
		catch(InvocationTargetException ite)
		{
			throw new ServletException(ite.getMessage());
		}
		catch(InstantiationException ie)
		{
			throw new ServletException(ie.getMessage());
		}
		catch(NoSuchMethodException nse)
		{
			throw new ServletException(nse.getMessage());
		}
	}
	
}
