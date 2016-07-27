package org.glc;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public interface IRequestHandler {
	boolean isValid(ServletRequest request,ServletResponse response);
	void dispose();
}
