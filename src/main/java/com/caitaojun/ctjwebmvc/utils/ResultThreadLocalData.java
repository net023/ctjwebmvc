package com.caitaojun.ctjwebmvc.utils;

/**
 * 用于xml配置的action在返回的时候 存储数据
 * @author caitaojun
 *
 */
public class ResultThreadLocalData {
	private static final ThreadLocal<Object> resultData = new ThreadLocal<>();
	
	public static void setData(Object obj){
		resultData.remove();
		resultData.set(obj);
	}
	
	public static Object getData(){
		return resultData.get();
	}
}
