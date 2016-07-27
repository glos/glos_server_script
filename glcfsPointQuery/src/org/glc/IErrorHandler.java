package org.glc;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public interface IErrorHandler {
	void processError(ServletRequest request,ServletResponse response)throws java.io.IOException,javax.servlet.ServletException;
	void dispose();
}
