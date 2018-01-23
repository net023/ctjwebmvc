package com.caitaojun.ctjwebmvc.proxy;

import net.sf.cglib.proxy.Enhancer;

/**
 * action代理类
 * @author caitaojun
 *
 */
public class ActionProxy {
	public static Object create(Class<?> clazz,ProxyMethodInterceptor interceptor){
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(clazz);
		enhancer.setCallback(interceptor);
		return enhancer.create();
	}
}
