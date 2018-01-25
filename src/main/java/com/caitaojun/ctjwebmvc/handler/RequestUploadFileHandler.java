package com.caitaojun.ctjwebmvc.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;

import com.caitaojun.ctjwebmvc.model.UploadFile;

/**
 * 上传文件处理器
 * @author caitaojun
 *
 */
public class RequestUploadFileHandler {
	private HttpServletRequest request;
	public RequestUploadFileHandler() {
	}
	public RequestUploadFileHandler(HttpServletRequest request) {
		super();
		this.request = request;
	}
	public HttpServletRequest getRequest() {
		return request;
	}
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
	public List<UploadFile> getFiles() throws Exception{
		DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
		ServletFileUpload fileUpload = new ServletFileUpload(diskFileItemFactory);
		fileUpload.setHeaderEncoding("UTF-8");
		List<FileItem> list = fileUpload.parseRequest(request);
		List<UploadFile> ufs = new ArrayList<>();
		for (FileItem fileItem : list) {
			if(!fileItem.isFormField()){
				String fieldName = fileItem.getFieldName();
				String fileName = fileItem.getName();
				String contentType = fileItem.getContentType();
				String suffix = "";
				if(fileName.indexOf(".")!=-1){
					suffix = fileName.substring(fileName.lastIndexOf("."));
				}
				File tempFile = File.createTempFile(fileName, suffix);
				FileUtils.copyInputStreamToFile(fileItem.getInputStream(), tempFile);
				UploadFile uf = new UploadFile();
				uf.setFile(tempFile);
				uf.setFileContentType(contentType);
				uf.setFileFiledName(fieldName);
				uf.setFileName(fileName);
				ufs.add(uf);
			}
		}
		return ufs;
	}
	
}
