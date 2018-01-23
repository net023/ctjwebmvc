package com.caitaojun.ctjwebmvc.constants;

public enum MimeType {
	JSON(MimeTypes.JSON),HTML(MimeTypes.JSON);
	private String mimeType;
	private MimeType(String mimeType){
		this.mimeType = mimeType;
	}
	public String getMimeType(){
		return this.mimeType;
	}
}
