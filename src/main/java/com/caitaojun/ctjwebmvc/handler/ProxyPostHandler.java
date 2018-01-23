package com.caitaojun.ctjwebmvc.handler;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.caitaojun.ctjwebmvc.annotation.ReqPath;
import com.caitaojun.ctjwebmvc.annotation.RespJson;
import com.caitaojun.ctjwebmvc.constants.MimeType;
import com.caitaojun.ctjwebmvc.constants.ResultType;
import com.caitaojun.ctjwebmvc.constants.ViewType;
import com.caitaojun.ctjwebmvc.model.ActionMapping;
import com.caitaojun.ctjwebmvc.model.Configuration;
import com.caitaojun.ctjwebmvc.model.ModelAndView;
import com.caitaojun.ctjwebmvc.model.Result;
import com.caitaojun.ctjwebmvc.model.View;
import com.caitaojun.ctjwebmvc.utils.ResultThreadLocalData;

/**
 * action后置处理器
 * @author caitaojun
 *
 */
public class ProxyPostHandler {
	
	private HttpServletResponse response;
	
	private HttpServletRequest request;
	
	private ActionMapping actionMapping;
	
	public ProxyPostHandler() {
	}
	
	public ProxyPostHandler(ActionMapping actionMapping,HttpServletResponse response,HttpServletRequest request) {
		this.response = response;
		this.request = request;
		this.actionMapping = actionMapping;
	}

	public void handler(Object result,Method method) throws Exception {
		//0.判断这个actionMapping是否来自于注解还是xml配置
		boolean fromXMLCfg = actionMapping.isFromXMLCfg();
		if(fromXMLCfg){
			//来自于xml的配置
			handlerXMLActionMapping(result);
		}else{
			//来自于注解的配置
			handlerAnnotationActionMapping(result,method);
		}
	}
	
	private void handlerAnnotationActionMapping(Object result,Method method) throws Exception{
		if(null==result){
			return;
		}else if(method.isAnnotationPresent(RespJson.class)){
			//0.方法上是否有@RespJson
			String jsonStr = null;
			if(null!=result){
				if(result instanceof Collection){
					jsonStr = JSONArray.toJSONString(result);
				}else{
					jsonStr = JSONObject.toJSONString(result);
				}
			}
			response.setContentType("application/json;charset=utf-8");
			response.getWriter().write(jsonStr);
		}else if(result instanceof File){
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + ((File)result).getName());
			response.getOutputStream().write(FileUtils.readFileToByteArray((File)result));
			response.getOutputStream().flush();
			response.getOutputStream().close();
			return;
		}else if(result instanceof ModelAndView){
			ModelAndView mv = ((ModelAndView)result);
			Object model = mv.getModel();
			String viewPath = mv.getView();
			ResultType resultType = mv.getResultType();
			//判断resultType的几种情况
			if(resultType==ResultType.json){
				String jsonStr = null;
				if(null!=result){
					if(model instanceof Collection){
						jsonStr = JSONArray.toJSONString(model);
					}else{
						jsonStr = JSONObject.toJSONString(model);
					}
				}
				response.setContentType("application/json;charset=utf-8");
				response.getWriter().write(jsonStr);
			}else if(resultType==ResultType.file){
				response.setContentType("application/octet-stream; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=" + ((File)model).getName());
				response.getOutputStream().write(FileUtils.readFileToByteArray((File)model));
				response.getOutputStream().flush();
				response.getOutputStream().close();
				return;
			}else if(resultType==ResultType.forward){
				if(null!=model){
					request.setAttribute("model", model);
				}
				request.getRequestDispatcher(viewPath).forward(request, response);
			}else if(resultType==ResultType.forward_view){
				if(null!=model){
					request.setAttribute("model", model);
				}
				//找到views下的进行遍历获取
				Configuration cfg = (Configuration) request.getSession().getServletContext().getAttribute("cfg");
				List<View> views = cfg.getViews();
				for (View view : views) {
					if(view.getType()==ViewType.jsp){
						String path = view.getPath();
						String suffix = view.getSuffix();
						String jspFileStr = request.getSession().getServletContext().getRealPath(path+viewPath+suffix);
						File jspFile = new File(jspFileStr);
						if(jspFile.exists()){
							request.getRequestDispatcher(path+viewPath+suffix).forward(request, response);
							return;
						}else{
							continue;
						}
					}else if(view.getType()==ViewType.html){
						String path = view.getPath();
						String suffix = view.getSuffix();
						String htmlFileStr = request.getSession().getServletContext().getRealPath(path+viewPath+suffix);
						File htmlFile = new File(htmlFileStr);
						if(htmlFile.exists()){
							request.getRequestDispatcher(path+viewPath+suffix).forward(request, response);
							return;
						}else{
							continue;
						}
					}else if(view.getType()==ViewType.freemarker){
						//TODO freemarker处理待做...
					}
				}
				response.getWriter().write("not found result view!");
			}else if(resultType==ResultType.redirect){
				if(null!=model){
					viewPath+="?";
					if(model instanceof Map){
						Set keySet = ((Map)model).keySet();
						for (Object key : keySet) {
							Object value = ((Map)model).get(key);
							if(value.getClass().isArray()){
								Object[] arr = (Object[]) value;
								for (Object obj : arr) {
									viewPath+=(key+"="+obj+"&");
								}
							}else{
								viewPath+=(key+"="+value+"&");
							}
						}
					}else{
						Field[] declaredFields = model.getClass().getDeclaredFields();
						for (Field field : declaredFields) {
							field.setAccessible(true);
							String key = field.getName();
							Object value = field.get(model);
							viewPath+=(key+"="+value+"&");
						}
					}
					viewPath = viewPath.substring(0, viewPath.length()-1);
				}
				response.sendRedirect(request.getContextPath()+viewPath);
			}else if(resultType==ResultType.redirect_view){
				String oldViewPath = viewPath;
				if(null!=model){
					viewPath+="?";
					if(model instanceof Map){
						Set keySet = ((Map)model).keySet();
						for (Object key : keySet) {
							Object value = ((Map)model).get(key);
							if(value.getClass().isArray()){
								Object[] arr = (Object[]) value;
								for (Object obj : arr) {
									viewPath+=(key+"="+obj+"&");
								}
							}else{
								viewPath+=(key+"="+value+"&");
							}
						}
					}else{
						Field[] declaredFields = model.getClass().getDeclaredFields();
						for (Field field : declaredFields) {
							field.setAccessible(true);
							String key = field.getName();
							Object value = field.get(model);
							viewPath+=(key+"="+value+"&");
						}
					}
					viewPath = viewPath.substring(0, viewPath.length()-1);
				}
				//找到views下的进行遍历获取
				Configuration cfg = (Configuration) request.getSession().getServletContext().getAttribute("cfg");
				List<View> views = cfg.getViews();
				for (View view : views) {
					if(view.getType()==ViewType.jsp){
						String path = view.getPath();
						String suffix = view.getSuffix();
						String jspFileStr = request.getSession().getServletContext().getRealPath(path+oldViewPath+suffix);
						File jspFile = new File(jspFileStr);
						if(jspFile.exists()){
							if(viewPath.contains("?")){
								String[] split = viewPath.split("\\?");
								viewPath = split[0]+suffix+"?"+split[1];
							}else{
								viewPath = oldViewPath+suffix;
							}
							response.sendRedirect(request.getContextPath()+path+viewPath);
							return;
						}else{
							continue;
						}
					}else if(view.getType()==ViewType.html){
						String path = view.getPath();
						String suffix = view.getSuffix();
						String htmlFileStr = request.getSession().getServletContext().getRealPath(path+oldViewPath+suffix);
						File htmlFile = new File(htmlFileStr);
						if(htmlFile.exists()){
							if(viewPath.contains("?")){
								String[] split = viewPath.split("\\?");
								viewPath = split[0]+suffix+"?"+split[1];
							}else{
								viewPath = oldViewPath+suffix;
							}
							response.sendRedirect(request.getContextPath()+path+viewPath);
							return;
						}else{
							continue;
						}
					}else if(view.getType()==ViewType.freemarker){
						//TODO freemarker处理待做...
					}
				}
				response.getWriter().write("not found result view!");
			}
		}else if(result instanceof String){
			//返回的是不是string
			//是不是以forward:开头的
			if(((String) result).startsWith("forward:")){
				String viewPath = ((String) result).substring("forward:".length());
				request.getRequestDispatcher(viewPath).forward(request, response);
			}else if(((String) result).startsWith("redirect:")){
				String viewPath = ((String) result).substring("redirect:".length());
				response.sendRedirect(request.getContextPath()+viewPath);
			}else if(((String) result).startsWith("forward_view:")){
				String viewPath = ((String) result).substring("forward_view:/".length());
				//找到views下的进行遍历获取
				Configuration cfg = (Configuration) request.getSession().getServletContext().getAttribute("cfg");
				List<View> views = cfg.getViews();
				for (View view : views) {
					if(view.getType()==ViewType.jsp){
						String path = view.getPath();
						String suffix = view.getSuffix();
						String jspFileStr = request.getSession().getServletContext().getRealPath(path+viewPath+suffix);
						File jspFile = new File(jspFileStr);
						if(jspFile.exists()){
							request.getRequestDispatcher(path+viewPath+suffix).forward(request, response);
							return;
						}else{
							continue;
						}
					}else if(view.getType()==ViewType.html){
						String path = view.getPath();
						String suffix = view.getSuffix();
						String htmlFileStr = request.getSession().getServletContext().getRealPath(path+viewPath+suffix);
						File htmlFile = new File(htmlFileStr);
						if(htmlFile.exists()){
							request.getRequestDispatcher(path+viewPath+suffix).forward(request, response);
							return;
						}else{
							continue;
						}
					}else if(view.getType()==ViewType.freemarker){
						//TODO freemarker处理待做...
					}
				}
				response.getWriter().write("not found result view!");
			}else if(((String) result).startsWith("redirect_view:")){
				String viewPath = ((String) result).substring("redirect_view:/".length());
				//找到views下的进行遍历获取
				Configuration cfg = (Configuration) request.getSession().getServletContext().getAttribute("cfg");
				List<View> views = cfg.getViews();
				for (View view : views) {
					if(view.getType()==ViewType.jsp){
						String path = view.getPath();
						String suffix = view.getSuffix();
						String jspFileStr = request.getSession().getServletContext().getRealPath(path+viewPath+suffix);
						File jspFile = new File(jspFileStr);
						if(jspFile.exists()){
							response.sendRedirect(request.getContextPath()+path+viewPath+suffix);
							return;
						}else{
							continue;
						}
					}else if(view.getType()==ViewType.html){
						String path = view.getPath();
						String suffix = view.getSuffix();
						String htmlFileStr = request.getSession().getServletContext().getRealPath(path+viewPath+suffix);
						File htmlFile = new File(htmlFileStr);
						if(htmlFile.exists()){
							response.sendRedirect(request.getContextPath()+path+viewPath+suffix);
							return;
						}else{
							continue;
						}
					}else if(view.getType()==ViewType.freemarker){
						//TODO freemarker处理待做...
					}
				}
				response.getWriter().write("not found result view!");
			}else{
				//找到views下的进行遍历获取
				Configuration cfg = (Configuration) request.getSession().getServletContext().getAttribute("cfg");
				List<View> views = cfg.getViews();
				for (View view : views) {
					if(view.getType()==ViewType.jsp){
						String path = view.getPath();
						String suffix = view.getSuffix();
						String jspFileStr = request.getSession().getServletContext().getRealPath(path+result+suffix);
						File jspFile = new File(jspFileStr);
						if(jspFile.exists()){
							request.getRequestDispatcher(path+result+suffix).forward(request, response);
							return;
						}else{
							continue;
						}
					}else if(view.getType()==ViewType.html){
						String path = view.getPath();
						String suffix = view.getSuffix();
						String htmlFileStr = request.getSession().getServletContext().getRealPath(path+result+suffix);
						File htmlFile = new File(htmlFileStr);
						if(htmlFile.exists()){
							request.getRequestDispatcher(path+result+suffix).forward(request, response);
							return;
						}else{
							continue;
						}
					}else if(view.getType()==ViewType.freemarker){
						//TODO freemarker处理待做...
					}
				}
				response.getWriter().write("not found result view!");
			}
		}
	}
	
	private void handlerXMLActionMapping(Object result) throws Exception{
		if(null==result){
			return;
		}
		if(!(result instanceof String)){
			response.getWriter().write("not found result view!");
			return;
		}
		//0.获取actionmapping的result集合
		Result rs = actionMapping.getResults().get(result);
		if(rs==null){
			response.getWriter().write("not found result view!");
			return;
		}
		Object data = ResultThreadLocalData.getData();
		ResultType type = rs.getType();
		String viewPath = rs.getView();
		if(type==ResultType.json){
			//判断是集合还是对象
			String jsonStr = null;
			if(null!=data){
				if(data instanceof Collection){
					jsonStr = JSONArray.toJSONString(data);
				}else{
					jsonStr = JSONObject.toJSONString(data);
				}
			}
			response.setContentType("application/json;charset=utf-8");
			response.getWriter().write(jsonStr);
		}else if(type==ResultType.file){
			if(null!=data){
				response.setContentType("application/octet-stream; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=" + ((File)data).getName());
				response.getOutputStream().write(FileUtils.readFileToByteArray((File)data));
				response.getOutputStream().flush();
				response.getOutputStream().close();
			}
		}else if(type==ResultType.forward){
			if(null!=data){
				request.setAttribute("model", data);
			}
			request.getRequestDispatcher(viewPath).forward(request, response);
		}else if(type==ResultType.forward_view){
			if(null!=data){
				request.setAttribute("model", data);
			}
			//找到views下的进行遍历获取
			Configuration cfg = (Configuration) request.getSession().getServletContext().getAttribute("cfg");
			List<View> views = cfg.getViews();
			for (View view : views) {
				if(view.getType()==ViewType.jsp){
					String path = view.getPath();
					String suffix = view.getSuffix();
					String jspFileStr = request.getSession().getServletContext().getRealPath(path+viewPath+suffix);
					File jspFile = new File(jspFileStr);
					if(jspFile.exists()){
						request.getRequestDispatcher(path+viewPath+suffix).forward(request, response);
						return;
					}else{
						continue;
					}
				}else if(view.getType()==ViewType.html){
					String path = view.getPath();
					String suffix = view.getSuffix();
					String htmlFileStr = request.getSession().getServletContext().getRealPath(path+viewPath+suffix);
					File htmlFile = new File(htmlFileStr);
					if(htmlFile.exists()){
						request.getRequestDispatcher(path+viewPath+suffix).forward(request, response);
						return;
					}else{
						continue;
					}
				}else if(view.getType()==ViewType.freemarker){
					//TODO freemarker处理待做...
				}
			}
			response.getWriter().write("not found result view!");
		}else if(type==ResultType.redirect){
			if(null!=data){
				viewPath+="?";
				if(data instanceof Map){
					Set keySet = ((Map)data).keySet();
					for (Object key : keySet) {
						Object value = ((Map)data).get(key);
						if(value.getClass().isArray()){
							Object[] arr = (Object[]) value;
							for (Object obj : arr) {
								viewPath+=(key+"="+obj+"&");
							}
						}else{
							viewPath+=(key+"="+value+"&");
						}
					}
				}else{
					Field[] declaredFields = data.getClass().getDeclaredFields();
					for (Field field : declaredFields) {
						field.setAccessible(true);
						String key = field.getName();
						Object value = field.get(data);
						viewPath+=(key+"="+value+"&");
					}
				}
				viewPath = viewPath.substring(0, viewPath.length()-1);
			}
			response.sendRedirect(request.getContextPath()+viewPath);
		}else if(type==ResultType.redirect_view){
			String oldViewPath = viewPath;
			if(null!=data){
				viewPath+="?";
				if(data instanceof Map){
					Set keySet = ((Map)data).keySet();
					for (Object key : keySet) {
						Object value = ((Map)data).get(key);
						if(value.getClass().isArray()){
							Object[] arr = (Object[]) value;
							for (Object obj : arr) {
								viewPath+=(key+"="+obj+"&");
							}
						}else{
							viewPath+=(key+"="+value+"&");
						}
					}
				}else{
					Field[] declaredFields = data.getClass().getDeclaredFields();
					for (Field field : declaredFields) {
						field.setAccessible(true);
						String key = field.getName();
						Object value = field.get(data);
						viewPath+=(key+"="+value+"&");
					}
				}
				viewPath = viewPath.substring(0, viewPath.length()-1);
			}
			//找到views下的进行遍历获取
			Configuration cfg = (Configuration) request.getSession().getServletContext().getAttribute("cfg");
			List<View> views = cfg.getViews();
			for (View view : views) {
				if(view.getType()==ViewType.jsp){
					String path = view.getPath();
					String suffix = view.getSuffix();
					String jspFileStr = request.getSession().getServletContext().getRealPath(path+oldViewPath+suffix);
					File jspFile = new File(jspFileStr);
					if(jspFile.exists()){
						if(viewPath.contains("?")){
							String[] split = viewPath.split("\\?");
							viewPath = split[0]+suffix+"?"+split[1];
						}else{
							viewPath = oldViewPath+suffix;
						}
						response.sendRedirect(request.getContextPath()+path+viewPath);
						return;
					}else{
						continue;
					}
				}else if(view.getType()==ViewType.html){
					String path = view.getPath();
					String suffix = view.getSuffix();
					String htmlFileStr = request.getSession().getServletContext().getRealPath(path+oldViewPath+suffix);
					File htmlFile = new File(htmlFileStr);
					if(htmlFile.exists()){
						if(viewPath.contains("?")){
							String[] split = viewPath.split("\\?");
							viewPath = split[0]+suffix+"?"+split[1];
						}else{
							viewPath = oldViewPath+suffix;
						}
						response.sendRedirect(request.getContextPath()+path+viewPath);
						return;
					}else{
						continue;
					}
				}else if(view.getType()==ViewType.freemarker){
					//TODO freemarker处理待做...
				}
			}
			response.getWriter().write("not found result view!");
		}
	}

}
