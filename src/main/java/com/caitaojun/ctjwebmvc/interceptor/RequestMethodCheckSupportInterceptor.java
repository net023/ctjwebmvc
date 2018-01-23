package com.caitaojun.ctjwebmvc.interceptor;

import javax.servlet.http.HttpServletResponse;

import com.caitaojun.ctjwebmvc.constants.RequestMethod;

/**
 * 请求方法类型检查拦截器
 * @author caitaojun
 *
 */
public class RequestMethodCheckSupportInterceptor extends Interceptor {
	
	private RequestMethod[] methods;
	
	public RequestMethodCheckSupportInterceptor() {
	}
	
	public RequestMethodCheckSupportInterceptor(RequestMethod[] reqMethods) {
		super();
		this.methods = reqMethods;
	}

	@Override
	public boolean intercept(InterceptorChain interceptorChain) throws Exception {
		String method = interceptorChain.getRequest().getMethod();
		for (RequestMethod requestMethod : methods) {
			if(requestMethod.equals(RequestMethod.ANY)){
				return interceptorChain.doIntercept();
			}
			if(requestMethod.name().toLowerCase().equals(method.toLowerCase())){
				return interceptorChain.doIntercept();
			}
		}
		HttpServletResponse response = interceptorChain.getResponse();
		response.setContentType("application/json;charset=utf-8");
		response.getWriter().write("{\"msg\":\"不支持请求方式\"}");
		return false;
	}

}
