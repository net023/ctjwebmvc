package com.caitaojun.ctjwebmvc.interceptor;

public abstract class Interceptor {
	public abstract boolean intercept(InterceptorChain interceptorChain) throws Exception;
	public Interceptor nextInterceptor;
}
