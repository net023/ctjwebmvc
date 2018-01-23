package com.caitaojun.ctjwebmvc.model;

import com.caitaojun.ctjwebmvc.constants.ResultType;

/**
 * 返回视图
 * @author caitaojun
 *
 */
public class Result {
	private String name;
	private ResultType type;
	private String view;
	
	public Result() {
	}
	
	public Result(String name, ResultType type, String view) {
		super();
		this.name = name;
		this.type = type;
		this.view = view;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ResultType getType() {
		return type;
	}

	public void setType(ResultType type) {
		this.type = type;
	}

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}
	
	
	
}
