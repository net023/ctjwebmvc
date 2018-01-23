package com.caitaojun.ctjwebmvc.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.beanutils.Converter;

/**
 * 支持多种格式的日期转换器
 * @author caitaojun
 *
 */
public class MyDateConverter implements Converter {
	
	 // 实现 将日期串转成日期类型(格式是yyyy-MM-dd HH:mm:ss)
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    public final static String pattern1 = "EEE MMM dd HH:mm:ss zzz yyyy";
    public final static String pattern2 = "yyyy-MM-dd HH:mm:ss";
    public final static String pattern3 = "yyyy.MM.dd HH:mm:ss";
    public final static String pattern4 = "yyyy MM dd HH:mm:ss";
    public final static String pattern5 = "yyyy/MM/dd HH:mm:ss";
    public final static String pattern6 = "yyyyMMddHHmmss";
    public final static String pattern7 = "yyyy-MM-dd";
    public final static String pattern8 = "yyyy/MM/dd";
    public final static String pattern9 = "yyyyMMdd";

	@Override
	public Object convert(Class c, Object value) {
		String source = (String) value;
		try {
            Long dateL = Long.valueOf(source);
            return new Date(dateL);
        } catch (NumberFormatException e) {
            //是字符串
            //换个方式
            sdf = new SimpleDateFormat(pattern1, Locale.US);
            try {
                return sdf.parse(source);
            } catch (ParseException e1) {
                sdf.applyPattern(pattern2);
                try {
                    return sdf.parse(source);
                } catch (ParseException e2) {
                    sdf.applyPattern(pattern3);
                    try {
                        return sdf.parse(source);
                    } catch (ParseException e3) {
                        sdf.applyPattern(pattern4);
                        try {
                            return sdf.parse(source);
                        } catch (ParseException e4) {
                            sdf.applyPattern(pattern5);
                            try {
                                return sdf.parse(source);
                            } catch (ParseException e5) {
                                sdf.applyPattern(pattern6);
                                try {
                                    return sdf.parse(source);
                                } catch (ParseException e6) {
                                    sdf.applyPattern(pattern7);
                                    try {
                                        return sdf.parse(source);
                                    } catch (ParseException e7) {
                                        sdf.applyPattern(pattern8);
                                        try {
                                            return sdf.parse(source);
                                        } catch (Exception e8) {
                                            sdf.applyPattern(pattern9);
                                            try {
                                                return sdf.parse(source);
                                            } catch (Exception e9) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

		return null;
	}

	
	 public static Date parse(String source){
	        try {
	            Long dateL = Long.valueOf(source);
	            return new Date(dateL);
	        } catch (NumberFormatException e) {
	            //是字符串
	            //换个方式
	            sdf = new SimpleDateFormat(pattern1, Locale.US);
	            try {
	                return sdf.parse(source);
	            } catch (ParseException e1) {
	                sdf.applyPattern(pattern2);
	                try {
	                    return sdf.parse(source);
	                } catch (ParseException e2) {
	                    sdf.applyPattern(pattern3);
	                    try {
	                        return sdf.parse(source);
	                    } catch (ParseException e3) {
	                        sdf.applyPattern(pattern4);
	                        try {
	                            return sdf.parse(source);
	                        } catch (ParseException e4) {
	                            sdf.applyPattern(pattern5);
	                            try {
	                                return sdf.parse(source);
	                            } catch (ParseException e5) {
	                                sdf.applyPattern(pattern6);
	                                try {
	                                    return sdf.parse(source);
	                                } catch (ParseException e6) {
	                                    sdf.applyPattern(pattern7);
	                                    try {
	                                        return sdf.parse(source);
	                                    } catch (ParseException e7) {
	                                        sdf.applyPattern(pattern8);
	                                        try {
	                                            return sdf.parse(source);
	                                        } catch (Exception e8) {
	                                            sdf.applyPattern(pattern9);
	                                            try {
	                                                return sdf.parse(source);
	                                            } catch (Exception e9) {
	                                                e.printStackTrace();
	                                            }
	                                        }
	                                    }
	                                }
	                            }
	                        }
	                    }
	                }
	            }
	        }
	        return null;
	    }
	
}
