package com.caitaojun.ctjwebmvc.model;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.caitaojun.ctjwebmvc.constants.MimeType;
import com.caitaojun.ctjwebmvc.constants.RequestMethod;

/**
 * action映射
 * @author caitaojun
 * 
 * 
 * action返回string 其他
 *
 */
public class ActionMapping {
	private String url;
	private Method method;
	private Class<?> clazz;
	private Map<String, Result> results = new HashMap<>();
	private boolean parseJson;
	private RequestMethod[] methods;
	private MimeType[] acceptType;
	private MimeType[] produceType;
	private boolean fromXMLCfg;
	
	public ActionMapping() {
	}
	public ActionMapping(String url, Method method, Class<?> clazz,Map<String, Result> results) {
		super();
		this.url = url;
		this.method = method;
		this.clazz = clazz;
		this.results = results;
	}
	
	public ActionMapping(String url, Method method, Class<?> clazz, Map<String, Result> results, boolean parseJson,
			RequestMethod[] methods, MimeType[] acceptType, MimeType[] produceType) {
		super();
		this.url = url;
		this.method = method;
		this.clazz = clazz;
		this.results = results;
		this.parseJson = parseJson;
		this.methods = methods;
		this.acceptType = acceptType;
		this.produceType = produceType;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	public Class<?> getClazz() {
		return clazz;
	}
	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
	public Map<String, Result> getResults() {
		return results;
	}
	public void setResults(Map<String, Result> results) {
		this.results = results;
	}
	public boolean isParseJson() {
		return parseJson;
	}
	public void setParseJson(boolean parseJson) {
		this.parseJson = parseJson;
	}
	public RequestMethod[] getMethods() {
		return methods;
	}
	public void setMethods(RequestMethod[] methods) {
		this.methods = methods;
	}
	public MimeType[] getAcceptType() {
		return acceptType;
	}
	public void setAcceptType(MimeType[] acceptType) {
		this.acceptType = acceptType;
	}
	public MimeType[] getProduceType() {
		return produceType;
	}
	public void setProduceType(MimeType[] produceType) {
		this.produceType = produceType;
	}
	public boolean isFromXMLCfg() {
		return fromXMLCfg;
	}
	public void setFromXMLCfg(boolean fromXMLCfg) {
		this.fromXMLCfg = fromXMLCfg;
	}
}
