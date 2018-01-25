package com.caitaojun.ctjwebmvc.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class MethodParameterAnnotationUtil {
	//获取方法指定位置参数的注解
	public static List<Annotation> getMethodParameterAnnotations(Method method,int idx) {  
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();  
        if (parameterAnnotations == null || parameterAnnotations.length == 0) {  
            return null;  
        }  
        return Arrays.asList(parameterAnnotations[idx]);
    }
	//判断方法指定参数是否有指定注解
	public static Annotation getMethodParameterAnnotation(Method method,int idx,Class annotationType){
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();  
        if (parameterAnnotations == null || parameterAnnotations.length == 0) {  
            return null;  
        }else{
        	Annotation[] annotations = parameterAnnotations[idx];
        	for (Annotation annotation : annotations) {
				if(annotation.annotationType().equals(annotationType)){
					return annotation;
				}
			}
        }  
		return null;
	}
	
	//获取方法指定位置参数的类型
	public static Class getMethodParameterType(Method method,int idx){
		return method.getParameterTypes()[idx];
	}
	//获取方法指定位置参数的类型中的泛型
	public static Class getMethodParameterGenericType(Method method,int idx) throws ClassNotFoundException{
//		String typeName = method.getGenericParameterTypes()[idx].getTypeName();
		String typeName = method.getGenericParameterTypes()[idx].toString();
		String parameterClassName = getMethodParameterType(method,idx).getName();
		String genericClassName = typeName.substring(parameterClassName.length()+1);
		genericClassName = genericClassName.substring(0, genericClassName.lastIndexOf(">"));
		//java.util.List<java.util.Map<java.lang.String, java.lang.Object>>
		//java.util.Map<java.lang.String, java.lang.Object>
		if(genericClassName.indexOf("<")!=-1){
			genericClassName = genericClassName.substring(0, genericClassName.indexOf("<"));
		}
		return Class.forName(genericClassName);
	}
}
