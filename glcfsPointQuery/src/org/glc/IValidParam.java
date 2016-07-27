package org.glc;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public interface IValidParam {
	boolean IsParamValid(ServletRequest request,ServletResponse response);
	void dispose();
}
