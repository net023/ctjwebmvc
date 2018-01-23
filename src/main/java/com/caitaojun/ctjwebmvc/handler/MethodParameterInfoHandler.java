package com.caitaojun.ctjwebmvc.handler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

/**
 * 获取方法的参数名称和类型
 * @author caitaojun
 *
 */
public class MethodParameterInfoHandler {
	private Map<String,Class> parameterInfo = new LinkedHashMap<>();
	private Class targetClass;
	private Method targetMethod;
	public MethodParameterInfoHandler() {
	}
	public MethodParameterInfoHandler(Class targetClass, Method targetMethod) {
		super();
		this.targetClass = targetClass;
		this.targetMethod = targetMethod;
	}
	
	public Map<String, Class> parseMethodParameter() throws Exception{
		ClassPool classPool = ClassPool.getDefault();
		ClassClassPath ccpath = new ClassClassPath(targetClass);
		classPool.insertClassPath(ccpath);
		CtClass ctClass = classPool.get(targetClass.getName());
		CtMethod ctMethod = ctClass.getDeclaredMethod(targetMethod.getName());
//		CtMethod ctMethod = classPool.getMethod(targetClass.getName(), targetMethod.getName());
		MethodInfo methodInfo = ctMethod.getMethodInfo();
		CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
		LocalVariableAttribute attribute = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
		Class<?>[] parameterTypes = targetMethod.getParameterTypes();
		int post = Modifier.isStatic(targetMethod.getModifiers())?0:1;
		for (int i = 0; i < parameterTypes.length; i++) {
			parameterInfo.put(attribute.variableName(post+i), parameterTypes[i]);
		}
		return parameterInfo;
	}
	
	public void setTargetClass(Class targetClass) {
		this.targetClass = targetClass;
	}
	public void setTargetMethod(Method targetMethod) {
		this.targetMethod = targetMethod;
	}
}
