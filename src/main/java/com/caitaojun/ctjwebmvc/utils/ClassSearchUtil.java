package com.caitaojun.ctjwebmvc.utils;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.caitaojun.ctjwebmvc.annotation.Ctrl;

/**
 * Class扫描工具
 * @author caitaojun
 *
 */
@SuppressWarnings("all")
public class ClassSearchUtil {
	
	private final static String classPath = ClassSearchUtil.class.getClassLoader().getResource("").getPath().substring(1);
	private final static int idx = classPath.length();
	
	public static List<Class> search(String basePath) throws Exception{
		basePath = basePath.replace(".", "/");
		Enumeration<URL> resources = ClassSearchUtil.class.getClassLoader().getResources(basePath);
		List<Class> classArray = new ArrayList<>();
		while(resources.hasMoreElements()){
			URL element = resources.nextElement();
			if(element.getProtocol().equals("jar")){
				String path = element.getPath();
				
				//file:/D:/ProgramFiles/workspace/ssh/ccwebmvc/WebContent/WEB-INF/lib/jfinal-3.0.jar!/com/jfinal
				path = path.substring("file:/".length());
				path = path.substring(0,path.length()-basePath.length()-2);
				JarFile entry = new JarFile(path);
				Enumeration<JarEntry> entries = entry.entries();
				while(entries.hasMoreElements()){
					JarEntry jarEntry = entries.nextElement();
					if(!jarEntry.isDirectory()){
						if(jarEntry.getName().endsWith("class")){
							String className = jarEntry.getName();
							className = className.substring(0, className.length()-6);
							className = className.replace("/", ".");
							Class<?> clazz = Class.forName(className, false, ClassSearchUtil.class.getClassLoader());
							Ctrl ctrl = clazz.getAnnotation(Ctrl.class);
							if(null!=ctrl){
								System.out.println("扫描jar："+className);
								classArray.add(clazz);
							}
						}
					}
				}
			}else{
				File rootPath = new File(element.getPath());
				deepSearchClass(rootPath,classArray);
			}
		}
		return classArray;
	}
	
	private static void deepSearchClass(File path,List<Class> classArray) throws Exception{
		//0.判断path是否存在
		if(path.exists()){
			//1.判断path是否是目录
			if(path.isDirectory()){
				//2.获取当前目录的文件和子目录
				File[] listFiles = path.listFiles(new FileFilter() {
					
					@Override
					public boolean accept(File file) {
						if(file.isDirectory()){
							return true;
						}
						if(file.getName().endsWith("class")){
							return true;
						}
						return false;
					}
				});
				for (File file : listFiles) {
					deepSearchClass(file, classArray);
				}
			}else{
				//D:\ProgramFiles\workspace\ssh\ccwebmvc\build\classes\com\caitaojun\User.class
				String classFileString = path.getCanonicalPath().substring(idx);
				classFileString = classFileString.substring(0, classFileString.length()-6);
				classFileString = classFileString.replace(File.separator, ".");
				Class<?> clazz = Class.forName(classFileString, false, ClassSearchUtil.class.getClassLoader());
				Ctrl ctrl = clazz.getAnnotation(Ctrl.class);
				if(null!=ctrl){
					System.out.println("扫描："+classFileString);
					classArray.add(clazz);
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
//		List<Class> classArray = search("com.caitaojun");
		/*List<Class> classArray = search("com.jfinal");
		for (Class class1 : classArray) {
			int modifiers = class1.getModifiers();
			System.out.println(modifiers);
			System.out.println(class1.getName());
		}*/
		/*JarFile ff  = new JarFile("d:/jfinal-3.0.jar");
		Enumeration<JarEntry> entries = ff.entries();
		while(entries.hasMoreElements()){
			JarEntry jarEntry = entries.nextElement();
			if(!jarEntry.isDirectory()){
				if(jarEntry.getName().endsWith("class")){
					System.out.println(jarEntry.getName());
				}
			}
		}*/
		
//		File ff = new File("d:/jfinal-3.0.jar");
//		URL uu = ff.toURL();
//		System.out.println(uu.getProtocol());
		
		/*Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("com/jfinal");
		while(resources.hasMoreElements()){
			System.out.println(resources.nextElement());
		}*/
		
//		System.out.println(ClassSearchUtil.class.getClassLoader().getResource("com/caitaojun").getProtocol());
		
	}
	
}
