package com.caitaojun.ctjwebmvc.proxy;

import java.lang.reflect.Method;

import com.caitaojun.ctjwebmvc.handler.ProxyPostHandler;
import com.caitaojun.ctjwebmvc.interceptor.InterceptorChain;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * 动态代理增强
 * @author caitaojun
 *
 */
public class ProxyMethodInterceptor implements MethodInterceptor {
	
	//拦截器
	private InterceptorChain interceptorChain;
	//后处理器：用于判断返回什么类型
	private ProxyPostHandler postHandler;
	
	private Object target;
	
	public ProxyMethodInterceptor() {
	}
	
	public ProxyMethodInterceptor(Object target,InterceptorChain interceptorChain,ProxyPostHandler postHandler){
		this.interceptorChain = interceptorChain;
		this.postHandler = postHandler;
		this.target = target;
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		//1.执行拦截器
		boolean isOk = interceptorChain.firstDoIntercept();
		if(!isOk){
			return null;
		}
		//2.执行目标方法
		Object result = method.invoke(target, args);
		//3.执行后处理器
		postHandler.handler(result,method);
		return result;
	}

}
