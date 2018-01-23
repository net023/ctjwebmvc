package com.caitaojun.ctjwebmvc.model;

import java.io.File;

/**
 * 上传文件对象
 * @author caitaojun
 *
 */
public class UploadFile {
	private String fileName;//文件名称
	private String fileFiledName;//表单中的名字
	private String fileContentType;//文件类型
	private File file;
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileFiledName() {
		return fileFiledName;
	}
	public void setFileFiledName(String fileFiledName) {
		this.fileFiledName = fileFiledName;
	}
	public String getFileContentType() {
		return fileContentType;
	}
	public void setFileContentType(String fileContentType) {
		this.fileContentType = fileContentType;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	
}
