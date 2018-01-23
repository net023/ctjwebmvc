package com.caitaojun.ctjwebmvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.caitaojun.ctjwebmvc.constants.MimeType;
import com.caitaojun.ctjwebmvc.constants.RequestMethod;

/**
 * 请求路径
 * @author caitaojun
 *
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReqPath {
	String[] value();
	RequestMethod[] method() default RequestMethod.ANY;
	MimeType[] acceptType() default {};
	MimeType[] produceType()  default {};
}
