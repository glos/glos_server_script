package org.glc;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public interface ISecurity {
	boolean IsAuthenticated(ServletRequest request,ServletResponse response);
	void dispose();
}
