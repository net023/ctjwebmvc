package com.caitaojun.ctjwebmvc.spring;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
/**
 * 通过spring容器获取bean
 * @author caitaojun
 *
 */
@SuppressWarnings("all")
public class SpringBeanFactory {
	public Object getBean(Class clazz,HttpServletRequest request) {
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(request.getSession().getServletContext());
		if(null==applicationContext){
			throw new RuntimeException("spring content is not found，please check spring Configuration！");
		}else{
			return applicationContext.getBean(clazz);
		}
	}
}
