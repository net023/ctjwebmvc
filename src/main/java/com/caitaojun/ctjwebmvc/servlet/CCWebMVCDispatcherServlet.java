package com.caitaojun.ctjwebmvc.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.alibaba.fastjson.JSONObject;
import com.caitaojun.ctjwebmvc.annotation.ReqJson;
import com.caitaojun.ctjwebmvc.annotation.ReqPath;
import com.caitaojun.ctjwebmvc.annotation.RespJson;
import com.caitaojun.ctjwebmvc.constants.MimeType;
import com.caitaojun.ctjwebmvc.constants.RequestMethod;
import com.caitaojun.ctjwebmvc.constants.ResultType;
import com.caitaojun.ctjwebmvc.constants.ViewType;
import com.caitaojun.ctjwebmvc.handler.MethodParameterInfoHandler;
import com.caitaojun.ctjwebmvc.handler.ProxyPostHandler;
import com.caitaojun.ctjwebmvc.handler.RequestUploadFileHandler;
import com.caitaojun.ctjwebmvc.interceptor.Interceptor;
import com.caitaojun.ctjwebmvc.interceptor.InterceptorChain;
import com.caitaojun.ctjwebmvc.interceptor.RequestMethodCheckSupportInterceptor;
import com.caitaojun.ctjwebmvc.model.ActionMapping;
import com.caitaojun.ctjwebmvc.model.Configuration;
import com.caitaojun.ctjwebmvc.model.Result;
import com.caitaojun.ctjwebmvc.model.UploadFile;
import com.caitaojun.ctjwebmvc.model.View;
import com.caitaojun.ctjwebmvc.proxy.ActionProxy;
import com.caitaojun.ctjwebmvc.proxy.ProxyMethodInterceptor;
import com.caitaojun.ctjwebmvc.utils.ClassSearchUtil;
import com.caitaojun.ctjwebmvc.utils.MyDateConverter;
import com.caitaojun.ctjwebmvc.utils.ValidataConfigurationFileUtil;

import javassist.ClassPool;
import javassist.CtMethod;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * 中央调度servlet
 * @author caitaojun
 *
 */
@SuppressWarnings("all")
public class CCWebMVCDispatcherServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		super.service(request, response);
//		request.getRequestDispatcher("").forward(arg0, arg1);
//		response.sendRedirect("");
		//1.获取请求的uri
		//2.根据uri去Configuration的action是中获取对应的action执行
		
		//  /aaa/bbb.dd
		Configuration cfg = (Configuration) request.getSession().getServletContext().getAttribute("cfg");
		
		String servletPath = request.getServletPath();
		if(servletPath.indexOf(".")!=-1){
			String suffix = servletPath.substring(servletPath.lastIndexOf("."));
			if(cfg.getStatusSuffix().contains(suffix)){
//				super.service(request, response);
//				request.getRequestDispatcher(servletPath).forward(request, response);
				/**
				Tomcat, Jetty, JBoss, and GlassFish  默认 Servlet的名字 -- "default"
				Google App Engine 默认 Servlet的名字 -- "_ah_default"
				Resin 默认 Servlet的名字 -- "resin-file"
				WebLogic 默认 Servlet的名字  -- "FileServlet"
				WebSphere  默认 Servlet的名字 -- "SimpleFileServlet" 
				 */
				List<String> defaultServletNames = new ArrayList<>();
				defaultServletNames.add("default");
				defaultServletNames.add("_ah_default");
				defaultServletNames.add("resin-file");
				defaultServletNames.add("FileServlet");
				defaultServletNames.add("SimpleFileServlet");
				RequestDispatcher rd = null;
				for (String servletName : defaultServletNames) {
					if(rd==null){
						rd = request.getSession().getServletContext().getNamedDispatcher(servletName);
					}
				}
				rd.forward(request, response);
				return;
			}
		}
		
		if(servletPath.indexOf(".")!=-1){
			servletPath = servletPath.substring(0, servletPath.lastIndexOf("."));
		}
		
		ActionMapping actionMapping = cfg.getActions().get(servletPath);
		if(null==actionMapping){
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		InterceptorChain interceptorChain=cfg.getInterceptorChain();
		//添加RequestMethodCheckSupportInterceptor
		RequestMethodCheckSupportInterceptor checkSupportInterceptor = new RequestMethodCheckSupportInterceptor(actionMapping.getMethods());
		checkSupportInterceptor.nextInterceptor=interceptorChain.getCurrentInterceptor();
		interceptorChain.setCurrentInterceptor(checkSupportInterceptor);
		
		
		interceptorChain.setRequest(request);
		interceptorChain.setResponse(response);
		ProxyPostHandler postHandler =  new ProxyPostHandler(actionMapping, response, request);
		try {
			//判断是否集成spring，如果集成就用spring创建action的bean
			Object target = null;
			if(cfg.isIntegratespring()){
				String springBeanFactoryClassName = "com.caitaojun.ctjwebmvc.spring.SpringBeanFactory";
				try {
					Class<?> springBeanFactoryClass = Class.forName(springBeanFactoryClassName);
					Object springBeanFactory = springBeanFactoryClass.newInstance();
					Method method = springBeanFactoryClass.getMethod("getBean", Class.class,HttpServletRequest.class);
					target = method.invoke(springBeanFactory, actionMapping.getClazz(),request);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException("please add ctjwebmvc-spring-plugin.jar to build path!");
				} catch (Exception e){
					throw new RuntimeException(e);
				}
			}else{
				target = actionMapping.getClazz().newInstance();
			}
			ProxyMethodInterceptor proxyMethodInterceptor = new ProxyMethodInterceptor(target,interceptorChain, postHandler);
			Object proxy = ActionProxy.create(actionMapping.getClazz(), proxyMethodInterceptor);
			
			Class<?>[] parameterTypes = actionMapping.getMethod().getParameterTypes();
			List<Object> param = new LinkedList<>();
			/*Parameter[] parameters = actionMapping.getMethod().getParameters();
			for (Parameter parameter : parameters) {
				Type parameterizedType = parameter.getParameterizedType();
				Class class1 = parameter.getType();;
			}
			actionMapping.getMethod().get*/
			//可以考虑使用asm/javassist来读取metho的参数名称和类型 然后根据请求过来的数据map，通过名称获取 然后转换
			
			MethodParameterInfoHandler parameterInfoHandler = new MethodParameterInfoHandler(actionMapping.getClazz(), actionMapping.getMethod());
			Map<String, Class> parameterInfo = parameterInfoHandler.parseMethodParameter();
			//模型数据封装
			//判断是否是基本数据类型   string类型  数组  map 集合(list\set) request response 自定义类型
			Set<String> parameterNames = parameterInfo.keySet();
			int idx = 0;
			for (String parameterName : parameterNames) {
				Class parameterType = parameterInfo.get(parameterName);
				if(actionMapping.getMethod().getParameters()[idx].isAnnotationPresent(ReqJson.class)){
					System.out.println(request.getContentType());
					List<String> lines = IOUtils.readLines(request.getInputStream(), request.getCharacterEncoding());
					StringBuffer sb = new StringBuffer();
					for (String line : lines) {
						sb.append(line);
					}
					if(parameterType.equals(Set.class)||parameterType.equals(List.class)){
//						System.out.println(sb.toString());
//						param.add(JSONArray.parseObject(sb.toString(), parameterType));
//						JSONArrayDeserializer instance = JSONArrayDeserializer.instance;
//						JSONArray.parse
//						ObjectMapper om = new ObjectMapper();
//						TypeFactory.defaultInstance().constructpara
						
						//1.获取泛型
						ParameterizedType type = (ParameterizedType) actionMapping.getMethod().getParameters()[idx].getParameterizedType();
						Class innerType = (Class) type.getActualTypeArguments()[0];
						ObjectMapper om = new ObjectMapper();
						om.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
						JavaType javaType = TypeFactory.defaultInstance().constructParametricType(parameterType, innerType);
						param.add(om.readValue(sb.toString(), javaType));
					}else if(parameterType.isArray()){
						ObjectMapper om = new ObjectMapper();
						om.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
						param.add(om.readValue(sb.toString(), parameterType));
					}else{
						param.add(JSONObject.parseObject(sb.toString(), parameterType));
					}
				}else if(parameterType == HttpServletRequest.class){
					param.add(request);
				}else if(parameterType == HttpServletResponse.class){
					param.add(response);
				}else if(parameterType.equals(Map.class)){
					//主要用于存放数据到request域属性中
					param.add(request.getParameterMap());
				}else if(parameterType.equals(UploadFile.class)){
					RequestUploadFileHandler fileHandler = new RequestUploadFileHandler(request);
					param.add(fileHandler.getFiles().get(0));
				}else if(parameterType.equals(String.class)){
					param.add(request.getParameter(parameterName));
				}else if(parameterType.equals(Set.class)){
					/**
					 ====================
						ctj[0][name]:jack
						ctj[0][age]:22
						ctj[1][name]:tom
						ctj[1][age]:11
					 ====================
					 */
					String[] values = request.getParameterValues(parameterName);
					if(null==values){
						int maxSize = request.getParameterMap().size();
						//获取泛型
						Class innerType = null;
						try {
							ParameterizedType type = (ParameterizedType) actionMapping.getMethod().getParameters()[idx].getParameterizedType();
							Type genericType = type.getActualTypeArguments()[0];
							if(genericType.getClass().equals(ParameterizedTypeImpl.class)){
								innerType = (Class) ((ParameterizedType)genericType).getRawType();
							}else{
								innerType = (Class) genericType;
							}
						} catch (Exception e) {
							throw new RuntimeException(actionMapping.getClazz().getName()+" 's method "+actionMapping.getMethod().getName()+" 's parameter "+parameterName+" not have generic paradig");
						}
						Set data = new HashSet<>();
						if(innerType.equals(Set.class)||Arrays.asList(innerType.getInterfaces()).contains(Set.class)){
							Enumeration<String> names = request.getParameterNames();
							List<String> nameArr = new ArrayList<>();
							int count = 0;
							while(names.hasMoreElements()){
								String element = names.nextElement();
								element = element.substring(0, element.length()-1);
								nameArr.add(element.substring(element.lastIndexOf("[")+1));
								count++;
							}
							int size = count/nameArr.size();
							for (int i = 0; i <= size; i++) {
								Map<String, Object> obj = new HashMap<>();
								for (String attribute : nameArr) {
									String value = request.getParameter(parameterName+"["+i+"]["+attribute+"]");
									if(null!=value){
										obj.put(attribute, value);
									}
								}
								if(!obj.isEmpty()){
									data.add(obj);
								}
							}
							if(data.size()==0){
								Map<String, Object> obj = new HashMap<>();
								for (String attribute : nameArr) {
									String value = request.getParameter(parameterName+"["+attribute+"]");
									if(null!=value){
										obj.put(attribute, value);
									}
								}
								if(!obj.isEmpty()){
									data.add(obj);
								}
							}
						}else{
							Field[] fields = innerType.getDeclaredFields();
							for (int i = 0; i <= maxSize; i++) {
								Map<String, Object> obj = new HashMap<>();
								for (Field field : fields) {
									field.setAccessible(true);
									String value = request.getParameter(parameterName+"["+i+"]["+field.getName()+"]");
									if(null!=value){
										obj.put(field.getName(), value);
									}
								}
								if(!obj.isEmpty()){
									Object bean = innerType.newInstance();
									BeanUtils.populate(bean, obj);
									data.add(bean);
								}
							}
							if(data.size()==0){
								Map<String, Object> obj = new HashMap<>();
								for (Field field : fields) {
									field.setAccessible(true);
									String value = request.getParameter(parameterName+"["+field.getName()+"]");
									if(null!=value){
										obj.put(field.getName(), value);
									}
								}
								if(!obj.isEmpty()){
									Object bean = innerType.newInstance();
									BeanUtils.populate(bean, obj);
									data.add(bean);
								}
							}
						}
						param.add(data);
					}else{
						param.add(new HashSet<>(Arrays.asList(values)));
					}
				}else if(parameterType.equals(List.class)){
					/**
					 ====================
						ctj[0][name]:jack
						ctj[0][age]:22
						ctj[1][name]:tom
						ctj[1][age]:11
					 ====================
					 */
					String[] values = request.getParameterValues(parameterName);
					if(null==values){
						int maxSize = request.getParameterMap().size();
						//获取泛型
						Class innerType = null;
						try {
							ParameterizedType type = (ParameterizedType) actionMapping.getMethod().getParameters()[idx].getParameterizedType();
							Type genericType = type.getActualTypeArguments()[0];
							if(genericType.getClass().equals(ParameterizedTypeImpl.class)){
								innerType = (Class) ((ParameterizedType)genericType).getRawType();
							}else{
								innerType = (Class) genericType;
							}
						} catch (Exception e) {
							throw new RuntimeException(actionMapping.getClazz().getName()+" 's method "+actionMapping.getMethod().getName()+" 's parameter "+parameterName+" not have generic paradig");
						}
						List data = new ArrayList<>();
						if(innerType.equals(Map.class)||Arrays.asList(innerType.getInterfaces()).contains(Map.class)){
							Enumeration<String> names = request.getParameterNames();
							List<String> nameArr = new ArrayList<>();
							int count = 0;
							while(names.hasMoreElements()){
								String element = names.nextElement();
								element = element.substring(0, element.length()-1);
								nameArr.add(element.substring(element.lastIndexOf("[")+1));
								count++;
							}
							int size = count/nameArr.size();
							for (int i = 0; i <= size; i++) {
								Map<String, Object> obj = new HashMap<>();
								for (String attribute : nameArr) {
									String value = request.getParameter(parameterName+"["+i+"]["+attribute+"]");
									if(null!=value){
										obj.put(attribute, value);
									}
								}
								if(!obj.isEmpty()){
									data.add(obj);
								}
							}
							if(data.size()==0){
								Map<String, Object> obj = new HashMap<>();
								for (String attribute : nameArr) {
									String value = request.getParameter(parameterName+"["+attribute+"]");
									if(null!=value){
										obj.put(attribute, value);
									}
								}
								if(!obj.isEmpty()){
									data.add(obj);
								}
							}
						}else{
							Field[] fields = innerType.getDeclaredFields();
							for (int i = 0; i <= maxSize; i++) {
								Map<String, Object> obj = new HashMap<>();
								for (Field field : fields) {
									field.setAccessible(true);
									String value = request.getParameter(parameterName+"["+i+"]["+field.getName()+"]");
									if(null!=value){
										obj.put(field.getName(), value);
									}
								}
								if(!obj.isEmpty()){
									Object bean = innerType.newInstance();
									BeanUtils.populate(bean, obj);
									data.add(bean);
								}
							}
							if(data.size()==0){
								Map<String, Object> obj = new HashMap<>();
								for (Field field : fields) {
									field.setAccessible(true);
									String value = request.getParameter(parameterName+"["+field.getName()+"]");
									if(null!=value){
										obj.put(field.getName(), value);
									}
								}
								if(!obj.isEmpty()){
									Object bean = innerType.newInstance();
									BeanUtils.populate(bean, obj);
									data.add(bean);
								}
							}
						}
						param.add(data);
					}else{
						param.add(Arrays.asList(values));
					}
				}else if(parameterType.isPrimitive()){
					/**
					 * @see     java.lang.Boolean#TYPE
				     * @see     java.lang.Character#TYPE
				     * @see     java.lang.Byte#TYPE
				     * @see     java.lang.Short#TYPE
				     * @see     java.lang.Integer#TYPE
				     * @see     java.lang.Long#TYPE
				     * @see     java.lang.Double#TYPE
				     * @see     java.lang.Float#TYPE
				     * @see     java.lang.Void#TYPE
					 */
					if(parameterType.equals(int.class)){
						param.add(Integer.parseInt(request.getParameter(parameterName)));
					}else if(parameterType.equals(long.class)){
						param.add(Long.parseLong(request.getParameter(parameterName)));
					}else if(parameterType.equals(double.class)){
						param.add(Double.parseDouble(request.getParameter(parameterName)));
					}else if(parameterType.equals(float.class)){
						param.add(Float.parseFloat(request.getParameter(parameterName)));
					}else if(parameterType.equals(short.class)){
						param.add(Short.parseShort(request.getParameter(parameterName)));
					}else if(parameterType.equals(byte.class)){
						param.add(Byte.parseByte(request.getParameter(parameterName)));
					}else if(parameterType.equals(boolean.class)){
						param.add(Boolean.parseBoolean(request.getParameter(parameterName)));
					}else if(parameterType.equals(char.class)){
						param.add(request.getParameter(parameterName).charAt(0));
					}
				}else if(parameterType.equals(Integer.class)){
					param.add(Integer.valueOf(request.getParameter(parameterName)));
				}else if(parameterType.equals(Long.class)){
					param.add(Long.valueOf(request.getParameter(parameterName)));
				}else if(parameterType.equals(Double.class)){
					param.add(Double.valueOf(request.getParameter(parameterName)));
				}else if(parameterType.equals(Float.class)){
					param.add(Float.valueOf(request.getParameter(parameterName)));
				}else if(parameterType.equals(Short.class)){
					param.add(Short.valueOf(request.getParameter(parameterName)));
				}else if(parameterType.equals(Byte.class)){
					param.add(Byte.valueOf(request.getParameter(parameterName)));
				}else if(parameterType.equals(Boolean.class)){
					param.add(Boolean.valueOf(request.getParameter(parameterName)));
				}else if(parameterType.equals(Character.class)){
					param.add(request.getParameter(parameterName).charAt(0));
				}else if(parameterType.isArray()){
//					System.out.println(parameterType.getComponentType().getName());
					if(parameterType.isAssignableFrom(String[].class)){
						param.add(request.getParameterValues(parameterName));
					}else if(parameterType.isAssignableFrom(UploadFile[].class)){
						RequestUploadFileHandler fileHandler = new RequestUploadFileHandler(request);
						param.add(fileHandler.getFiles().toArray(new UploadFile[]{}));
					}else if(parameterType.equals(int[].class)||parameterType.equals(Integer[].class)){
						String[] values = request.getParameterValues(parameterName);
						List<Integer> valueList = new ArrayList<>();
						for (String value : values) {
							valueList.add(Integer.valueOf(value));
						}
						param.add(valueList.toArray(new Integer[]{}));
					}else if(parameterType.equals(long[].class)||parameterType.equals(Long[].class)){
						String[] values = request.getParameterValues(parameterName);
						List<Long> valueList = new ArrayList<>();
						for (String value : values) {
							valueList.add(Long.valueOf(value));
						}
						param.add(valueList.toArray(new Long[]{}));
					}else if(parameterType.equals(double[].class)||parameterType.equals(Double[].class)){
						String[] values = request.getParameterValues(parameterName);
						List<Double> valueList = new ArrayList<>();
						for (String value : values) {
							valueList.add(Double.valueOf(value));
						}
						param.add(valueList.toArray(new Double[]{}));
					}else if(parameterType.equals(float[].class)||parameterType.equals(Float[].class)){
						String[] values = request.getParameterValues(parameterName);
						List<Float> valueList = new ArrayList<>();
						for (String value : values) {
							valueList.add(Float.valueOf(value));
						}
						param.add(valueList.toArray(new Float[]{}));
					}else if(parameterType.equals(short[].class)||parameterType.equals(Short[].class)){
						String[] values = request.getParameterValues(parameterName);
						List<Short> valueList = new ArrayList<>();
						for (String value : values) {
							valueList.add(Short.valueOf(value));
						}
						param.add(valueList.toArray(new Short[]{}));
					}else if(parameterType.equals(byte[].class)||parameterType.equals(Byte[].class)){
						String[] values = request.getParameterValues(parameterName);
						List<Byte> valueList = new ArrayList<>();
						for (String value : values) {
							valueList.add(Byte.valueOf(value));
						}
						param.add(valueList.toArray(new Byte[]{}));
					}else if(parameterType.equals(boolean[].class)||parameterType.equals(Boolean[].class)){
						String[] values = request.getParameterValues(parameterName);
						List<Boolean> valueList = new ArrayList<>();
						for (String value : values) {
							valueList.add(Boolean.valueOf(value));
						}
						param.add(valueList.toArray(new Boolean[]{}));
					}else if(parameterType.equals(char[].class)||parameterType.equals(Character[].class)){
						String[] values = request.getParameterValues(parameterName);
						List<Character> valueList = new ArrayList<>();
						for (String value : values) {
							valueList.add(value.charAt(0));
						}
						param.add(valueList.toArray(new Character[]{}));
					}else{
						//自定义类型数组  User[] ctj
						/**
						 ====================
							ctj[0][name]:jack
							ctj[0][age]:22
							ctj[1][name]:tom
							ctj[1][age]:11
						 ====================
						 	ctj[name]:jack
							ctj[age]:22
						 */
						String[] values = request.getParameterValues(parameterName);
						if(null==values){
							int maxSize = request.getParameterMap().size();
							Field[] fields = actionMapping.getMethod().getParameters()[idx].getType().getComponentType().getDeclaredFields();
							List data = new ArrayList<>();
							for (int i = 0; i <= maxSize; i++) {
								Map<String, Object> obj = new HashMap<>();
								for (Field field : fields) {
									field.setAccessible(true);
									String value = request.getParameter(parameterName+"["+i+"]["+field.getName()+"]");
									if(null!=value){
										obj.put(field.getName(), value);
									}
								}
								if(!obj.isEmpty()){
									Object bean = actionMapping.getMethod().getParameters()[idx].getType().getComponentType().newInstance();
									BeanUtils.populate(bean, obj);
									data.add(bean);
								}
							}
							if(data.size()==0){
								Map<String, Object> obj = new HashMap<>();
								for (Field field : fields) {
									field.setAccessible(true);
									String value = request.getParameter(parameterName+"["+field.getName()+"]");
									if(null!=value){
										obj.put(field.getName(), value);
									}
								}
								if(!obj.isEmpty()){
									Object bean = actionMapping.getMethod().getParameters()[idx].getType().getComponentType().newInstance();
									BeanUtils.populate(bean, obj);
									data.add(bean);
								}
							}
							Object instance = Array.newInstance(actionMapping.getMethod().getParameters()[idx].getType().getComponentType(), data.size());
							for (int i = 0; i < data.size(); i++) {
								Array.set(instance, i, data.get(i));
							}
							param.add(instance);
						}
					}
				}else{
					//自定义类型
					Map<String, String[]> parameterMap = request.getParameterMap();
					Object object = parameterType.newInstance();//注意需要空构造器
					ConvertUtils.register(new MyDateConverter() , Date.class);
					BeanUtils.populate(object, parameterMap);//注意日期格式化注入
					param.add(object);
				}
				idx++;
			}
			
			/*for (Class<?> parameterType : parameterTypes) {
				if(parameterType == HttpServletRequest.class){
					param.add(request);
				}else if(parameterType == HttpServletResponse.class){
					param.add(response);
				}else if(parameterType.equals(Map.class)){
					//主要用于存放数据到request域属性中
					param.add(request.getParameterMap());
				}else if(parameterType.equals(UploadFile.class)){
					RequestUploadFileHandler fileHandler = new RequestUploadFileHandler(request);
					param.add(fileHandler.getFiles().get(0));
				}
//				else if(parameterType.isArray()){
//					System.out.println(parameterType.getComponentType().getName());
//				}
				else if(parameterType.isAssignableFrom(UploadFile [].class)){
					RequestUploadFileHandler fileHandler = new RequestUploadFileHandler(request);
					param.add(fileHandler.getFiles().toArray(new UploadFile[]{}));
				}else if(parameterType.isPrimitive()){
					System.out.println(parameterType);
					param.add(null);
				}else{
					//自定义类型
					Map<String, String[]> parameterMap = request.getParameterMap();
					Object object = parameterType.newInstance();//注意需要空构造器
					ConvertUtils.register(new MyDateConverter() , Date.class);
					BeanUtils.populate(object, parameterMap);//注意日期格式化注入
					param.add(object);
				}
			}*/
			
			actionMapping.getMethod().invoke(proxy, param.toArray());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void init() throws ServletException {
		// 1.加载contextConfigLocation所指定的配置文件
		// 全局配置对象(是否开启注解 、扫描路径、拦截器、常量、class集合、action路由映射集合) 映射路由放入application域中
		// 代理 执行对应方法前 执行拦截器 执行后跳转还是转换json
		String contextConfigLocation = this.getServletConfig().getInitParameter("contextConfigLocation");
		if (null == contextConfigLocation || "".equals(contextConfigLocation)
				|| !contextConfigLocation.startsWith("classpath:")) {
			System.out.println("CCWebMVCFilter启动参数contextConfigLocation不能为空！格式为：classpath:xxx.xml");
			System.exit(0);
		} else {
			// 2.解析xml文件
			// 3.判断是否开启注解
			try {
				URL xml = this.getClass().getClassLoader().getResource(contextConfigLocation.substring("classpath:".length()));
				if(null==xml){
					System.out.println(contextConfigLocation.substring("classpath:".length())+"文件不存在!!");
					System.exit(0);
				}
				//验证xml文件是否正确
				InputStream is = this.getClass().getClassLoader().getResourceAsStream("ctjwebmvc.xsd");
				File destination = File.createTempFile("ctjwebmvc", ".xsd");
				FileUtils.copyInputStreamToFile(is, destination);
				boolean validata = ValidataConfigurationFileUtil.validata(new File(xml.getPath()), null);
				if(!validata){
					System.exit(0);
				}
				Configuration cfg = parseConfigurationXML(contextConfigLocation.substring("classpath:".length()));
				if(cfg.isUseAnnotation()){
					List<Class> classArray = ClassSearchUtil.search(cfg.getBasepath());
					System.out.println(classArray.size());
					parseControllerClass(cfg, classArray);
					System.out.println(cfg);
					//1.配置interceptorChain
					setInterceptorChain(cfg);
					//2.配置后置处理器
					this.getServletContext().setAttribute("cfg", cfg);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void setInterceptorChain(Configuration cfg) throws Exception{
		List<Class> interceptorsClass = cfg.getInterceptors();
		Interceptor pre = null;
		Interceptor cur = null;
		Interceptor first = null;
		for (Class clazz : interceptorsClass) {
			cur = (Interceptor) clazz.newInstance();
			if(null!=pre){
				pre.nextInterceptor = cur;
			}
			pre = cur;
			if(null==first){
				first = pre;
			}
		}
		InterceptorChain interceptorChain = new InterceptorChain();
		interceptorChain.setCurrentInterceptor(first);
		cfg.setInterceptorChain(interceptorChain);
	}
	
	private void parseControllerClass(Configuration cfg,List<Class> classArray){
		for (Class class1 : classArray) {
			//1.获取class上的ReqPath注解
			Annotation reqPath = class1.getAnnotation(ReqPath.class);
			String[] classReqPathValues=null;
			if(null!=reqPath){
				classReqPathValues = ((ReqPath)reqPath).value();
			}
			//2.获取Controller的Class中的全部方法
			Method[] methods = class1.getDeclaredMethods();
			for (Method method : methods) {
				ReqPath path = method.getAnnotation(ReqPath.class);
				if(null!=path){
					String[] methodReqPathValues = path.value();
					RequestMethod[] acceptMethod = path.method();
					MimeType[] acceptType = path.acceptType();
					MimeType[] produceType = path.produceType();
					boolean parseJson = method.isAnnotationPresent(RespJson.class);
					for (String classReqPath : classReqPathValues) {
						for (String methodReqPath : methodReqPathValues) {
							ActionMapping mapping =
								new ActionMapping(classReqPath+methodReqPath, method, class1, null, parseJson, acceptMethod, acceptType, produceType);
							mapping.setFromXMLCfg(false);
							cfg.getActions().put(mapping.getUrl(), mapping);
						}
					}
				}
				
			}
		}
	}
	
	private Configuration parseConfigurationXML(String contextConfigLocation) throws Exception{
		SAXReader reader = new SAXReader();
		Document document =null;
		try {
			document = reader
					.read(CCWebMVCDispatcherServlet.class.getClassLoader().getResourceAsStream(contextConfigLocation));
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		Configuration cfg = new Configuration();
		
		Element rootElement = document.getRootElement();
		//获取是否开启注解
		Element useAnnotation = rootElement.element("useAnnotation");
		if(null!=useAnnotation){
			String use = useAnnotation.attributeValue("use");
			Boolean used = Boolean.valueOf(use);
			cfg.setUseAnnotation(used);
		}
		//注解扫描包
		Element basepath = rootElement.element("basepath");
		if(null!=basepath){
			String packageStr = basepath.attributeValue("package");
			cfg.setBasepath(packageStr);
		}
		//静态文件类型
		Element statusFile = rootElement.element("statusFile");
		String statusSuffix = statusFile.attributeValue("suffix");
		cfg.setStatusSuffix(new HashSet<>(Arrays.asList(statusSuffix.split(","))));
		//是否集成spring
		Element integratespring = rootElement.element("integratespring");
		String switchStatus = integratespring.attributeValue("switch");
		if("on".equalsIgnoreCase(switchStatus)){
			cfg.setIntegratespring(true);
		}else{
			cfg.setIntegratespring(false);
		}
		//视图解析类型
		Element views = rootElement.element("views");
		List<Element> viewArray = views.elements("view");
		//排序
		Collections.sort(viewArray, new Comparator<Element>() {
			@Override
			public int compare(Element e1, Element e2) {
				String orderStr1 = e1.attributeValue("order");
				String orderStr2 = e2.attributeValue("order");
				int v1 = Integer.valueOf(orderStr1);
				int v2 = Integer.valueOf(orderStr2);
				return v1-v2;
			}
		});
		for (Element v : viewArray) {
			View view = new View();
			view.setOrder(Integer.valueOf(v.attributeValue("order")));
			view.setPath(v.attributeValue("path"));
			view.setSuffix(v.attributeValue("suffix"));
			String type = v.attributeValue("type");
			if("JSP".equals(type)){
				view.setType(ViewType.jsp);
			}else if("HTML".equals(type)){
				view.setType(ViewType.html);
			}else if("FREEMARKER".equals(type)){
				view.setType(ViewType.freemarker);
			}
			cfg.getViews().add(view);
		}
		
		//获取常量
		Element constants = rootElement.element("constants");
		if(null!=constants){
			List<Element> constantsArray = constants.elements();
			for (Element constant : constantsArray) {
				String key = constant.attributeValue("key");
				String value = constant.attributeValue("value");
				cfg.getConstans().put(key, value);
			}
		}
		//获取拦截器
		Element interceptors = rootElement.element("interceptors");
		if(null!=interceptors){
			List<Element> interceptorsArray = interceptors.elements();
			//排序
			Collections.sort(interceptorsArray, new Comparator<Element>() {
				
				@Override
				public int compare(Element e1, Element e2) {
					String orderStr1 = e1.attributeValue("order");
					String orderStr2 = e2.attributeValue("order");
					int v1 = Integer.valueOf(orderStr1);
					int v2 = Integer.valueOf(orderStr2);
					return v1-v2;
				}
			});
			for (Element interceptorE : interceptorsArray) {
				String clazz = interceptorE.attributeValue("class");
				cfg.getInterceptors().add(Class.forName(clazz, false, CCWebMVCDispatcherServlet.class.getClassLoader()));
			}
		}
		
		//获取actions
		Element actions = rootElement.element("actions");
		if(null!=actions){
			List<Element> actionsArray = actions.elements();
			for (Element element : actionsArray) {
				ActionMapping mapping = new ActionMapping();
				String name = element.attributeValue("name");
				String classStr = element.attributeValue("class");
				String methodStr = element.attributeValue("method");
				if(null==methodStr){
					methodStr = "execute";
				}
				mapping.setUrl(name);
				
				List<Element> params = element.elements("param");
				List<Class> paramArray = new ArrayList<>();
				for (Element param : params) {
					paramArray.add(Class.forName(param.attributeValue("class"), false, CCWebMVCDispatcherServlet.class.getClassLoader()));
				}
				
				Class<?> clazz = Class.forName(classStr, false, CCWebMVCDispatcherServlet.class.getClassLoader());
				Class[] css = new Class[10];
				for (int i = 0; i < paramArray.size(); i++) {
					css[i] = paramArray.get(i);
				}
				
				Class[] newss = Arrays.copyOf(css, params.size());
				
				Method method = null;
				try {
					method = clazz.getDeclaredMethod(methodStr, newss);
				} catch (NoSuchMethodException e) {
					System.out.println(contextConfigLocation+"中的action："+name+"对应的方法不存在，或参数不正确，请检查！");
					System.exit(0);
				}
				mapping.setClazz(clazz);
				mapping.setMethod(method);
				mapping.setFromXMLCfg(true);
				List<Element> results = element.elements("result");
				for (Element result : results) {
					String r_name = result.attributeValue("name");
					String r_type = result.attributeValue("type");
//				String view = result.getTextTrim();
					String view = result.attributeValue("path");
					Result rs = new Result();
					rs.setName(r_name);
					rs.setView(view);
					if("forward".equals(r_type)){
						rs.setType(ResultType.forward);
					}else if("forward_view".equals(r_type)){
						rs.setType(ResultType.forward_view);
					}else if("redirect".equals(r_type)){
						rs.setType(ResultType.redirect);
					}else if("redirect_view".equals(r_type)){
						rs.setType(ResultType.redirect_view);
					}else if("json".equals(r_type)){
						rs.setType(ResultType.json);
					}else if("file".equals(r_type)){
						rs.setType(ResultType.file);
					}
					mapping.getResults().put(r_name, rs);
				}
				
				//请求方法
				List<Element> reqmethods = element.elements("reqmethod");
				List<RequestMethod> reqMethodArray = new ArrayList<>();
				for (Element reqMethod : reqmethods) {
					String type = reqMethod.attributeValue("type");
					if("POST".equals(type)){
						reqMethodArray.add(RequestMethod.POST);
					}else if("GET".equals(type)){
						reqMethodArray.add(RequestMethod.GET);
					}else if("DELETE".equals(type)){
						reqMethodArray.add(RequestMethod.DELETE);
					}else if("PUT".equals(type)){
						reqMethodArray.add(RequestMethod.PUT);
					}else if("ANY".equals(type)){
						reqMethodArray.add(RequestMethod.ANY);
					}
				}
				RequestMethod[] ms = new RequestMethod[5];
				for (int i = 0; i < reqMethodArray.size(); i++) {
					ms[i] = reqMethodArray.get(i);
				}
				RequestMethod[] newms = Arrays.copyOf(ms, reqMethodArray.size());
				mapping.setMethods(newms);
				
				cfg.getActions().put(mapping.getUrl(), mapping);
			}
		}
		return cfg;
	}
public static void main(String[] args) throws Exception {
	ClassPool classPool = ClassPool.getDefault();
	CtMethod method = classPool.getMethod("com.caitaojun.app.demo1.web.controller.Bcontroller", "c1");
}
}

