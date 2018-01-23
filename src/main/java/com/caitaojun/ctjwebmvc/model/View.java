package com.caitaojun.ctjwebmvc.model;

import com.caitaojun.ctjwebmvc.constants.ViewType;

/**
 * xml视图配置
 * @author caitaojun
 *
 */
public class View {
	private ViewType type;
	private String suffix;
	private String path;
	private int order;
	public ViewType getType() {
		return type;
	}
	public void setType(ViewType type) {
		this.type = type;
	}
	public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
}
