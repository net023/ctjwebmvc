package com.caitaojun.ctjwebmvc.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截器链   责任链设计模式实现
 * @author caitaojun
 *
 */
public class InterceptorChain {
	private Interceptor currentInterceptor;
	private HttpServletRequest request ;
	private HttpServletResponse response ;
	public boolean firstDoIntercept() throws Exception{
		if(null!=currentInterceptor){
			return currentInterceptor.intercept(this);
		}
		return true;
	}
	public boolean doIntercept() throws Exception{
		currentInterceptor = currentInterceptor.nextInterceptor;
		if(null!=currentInterceptor){
			return currentInterceptor.intercept(this);
		}
		return true;
	}
	public HttpServletRequest getRequest() {
		return request;
	}
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
	public HttpServletResponse getResponse() {
		return response;
	}
	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}
	public void setCurrentInterceptor(Interceptor currentInterceptor) {
		this.currentInterceptor = currentInterceptor;
	}
	public Interceptor getCurrentInterceptor() {
		return currentInterceptor;
	}
}
