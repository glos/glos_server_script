package org.glc;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.glc.IErrorHandler;
import org.glc.domain.ErrorMessage;

public class DefaultErrorHandler implements IErrorHandler {

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void processError(ServletRequest request, ServletResponse response) throws java.io.IOException,javax.servlet.ServletException{
		// TODO Auto-generated method stub
		if(request.getAttribute(ErrorMessage.getClassName())!=null)
		{
			ErrorMessage err=(ErrorMessage)request.getAttribute(ErrorMessage.getClassName());
			if(err.isAuthorized())
			{
				if(err.isAjax())
				{
					response.getWriter().print(err.getErrorJSON());
				}
				else
				{
					if(err.isForward())
						request.getRequestDispatcher(err.getForwardURL()).forward(request, response);
					else
						((HttpServletResponse)response).sendRedirect(err.getRedirectURL());
				}
				
			}
			else
			{
				((HttpServletResponse)response).sendRedirect(err.getRedirectURL());
			}
		}
	}

}
