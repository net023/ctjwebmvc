package com.caitaojun.ctjwebmvc.model;

import com.caitaojun.ctjwebmvc.constants.ResultType;

/**
 * 模型视图
 * @author caitaojun
 *
 */
public class ModelAndView {
	private Object model;
	private String view;
	private ResultType ResultType;
	public ModelAndView() {
	}
	public ModelAndView(Object model, String view, ResultType ResultType) {
		super();
		this.model = model;
		this.view = view;
		this.ResultType = ResultType;
	}
	public Object getModel() {
		return model;
	}
	public void setModel(Object model) {
		this.model = model;
	}
	public String getView() {
		return view;
	}
	public void setView(String view) {
		this.view = view;
	}
	public ResultType getResultType() {
		return ResultType;
	}
	public void setResultType(ResultType ResultType) {
		this.ResultType = ResultType;
	}
	
}
