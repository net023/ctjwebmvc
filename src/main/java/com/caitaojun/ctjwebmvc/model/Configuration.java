package com.caitaojun.ctjwebmvc.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.caitaojun.ctjwebmvc.interceptor.InterceptorChain;

/**
 * webmvc.xml配置对象
 * @author caitaojun
 *
 */
public class Configuration {
	//是否开启注解
	private boolean useAnnotation = false;
	//注解扫描路径
	private String basepath;
	//常量
	private Map<String, String> constans = new HashMap<>();
	//拦截器
	private List<Class> interceptors = new ArrayList<>();
	//actions {url  class method}
	private Map<String, ActionMapping> actions = new HashMap<>();
	
	private InterceptorChain interceptorChain;
	
	private Set<String> statusSuffix = new HashSet<>();
	
	private boolean integratespring = false;
	
	private List<View> views = new ArrayList<>();
	
	public boolean isUseAnnotation() {
		return useAnnotation;
	}
	public void setUseAnnotation(boolean useAnnotation) {
		this.useAnnotation = useAnnotation;
	}
	public String getBasepath() {
		return basepath;
	}
	public void setBasepath(String basepath) {
		this.basepath = basepath;
	}
	public Map<String, String> getConstans() {
		return constans;
	}
	public void setConstans(Map<String, String> constans) {
		this.constans = constans;
	}
	public List<Class> getInterceptors() {
		return interceptors;
	}
	public void setInterceptors(List<Class> interceptors) {
		this.interceptors = interceptors;
	}
	public Map<String, ActionMapping> getActions() {
		return actions;
	}
	public void setActions(Map<String, ActionMapping> actions) {
		this.actions = actions;
	}
	public InterceptorChain getInterceptorChain() {
		return interceptorChain;
	}
	public void setInterceptorChain(InterceptorChain interceptorChain) {
		this.interceptorChain = interceptorChain;
	}
	public List<View> getViews() {
		return views;
	}
	public void setViews(List<View> views) {
		this.views = views;
	}
	public Set<String> getStatusSuffix() {
		return statusSuffix;
	}
	public void setStatusSuffix(Set<String> statusSuffix) {
		this.statusSuffix = statusSuffix;
	}
	public boolean isIntegratespring() {
		return integratespring;
	}
	public void setIntegratespring(boolean integratespring) {
		this.integratespring = integratespring;
	}
	
}
