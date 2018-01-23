package com.caitaojun.ctjwebmvc.utils;

import java.io.File;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXValidator;
import org.dom4j.util.XMLErrorHandler;

/**
 * 校验配置文件xml是否正确
 * @author caitaojun
 *
 */
public class ValidataConfigurationFileUtil {
	public static boolean validata(File xmlFile,File xsdFile){
		 try {
			//创建默认的XML错误处理器
			XMLErrorHandler errorHandler =  new XMLErrorHandler();
   
			//获取基于 SAX 的解析器的实例
			SAXParserFactory factory = SAXParserFactory.newInstance();
			//解析器在解析时验证 XML 内容。
			factory.setValidating(true);
			//指定由此代码生成的解析器将提供对 XML 名称空间的支持。
			factory.setNamespaceAware(true);
			//使用当前配置的工厂参数创建 SAXParser 的一个新实例。
			SAXParser parser = factory.newSAXParser();
			
			//创建一个读取工具
			SAXReader xmlReader = new SAXReader();
			//获取要校验xml文档实例
			Document xmlDocument = (Document) xmlReader.read(xmlFile);
			//设置 XMLReader 的基础实现中的特定属性。核心功能和属性列表可以在 [url]http://sax.sourceforge.net/?selected=get-set[/url] 中找到。
			parser.setProperty(
			        "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
			        "http://www.w3.org/2001/XMLSchema");
			parser.setProperty(
			        "http://java.sun.com/xml/jaxp/properties/schemaSource",
			        xsdFile);
			//创建一个SAXValidator校验工具，并设置校验工具的属性
			SAXValidator validator = new SAXValidator(parser.getXMLReader());
			//设置校验工具的错误处理器，当发生错误时，可以从处理器对象中得到错误信息。
			validator.setErrorHandler(errorHandler);                    
			
			//校验
			validator.validate(xmlDocument);
			//如果错误信息不为空，说明校验失败，打印错误信息
			if (errorHandler.getErrors().hasContent()) {
			    System.out.println("XML文件通过XSD文件校验失败！");
			 // ** 将document中的内容写入文件中 *//*
//            writer.write(errorHandler.getErrors()); //writer.write(parse(document));            
//            writer.close();
			    Element errors = errorHandler.getErrors();
//            System.out.println(errors.getDocument().getStringValue());
			    List<Element> elements = errors.elements("error");
			    StringBuffer sb = new StringBuffer();
			    for (Element element : elements) {
					String column = element.attributeValue("column");
					String line = element.attributeValue("line");
					String text = element.getText();
					sb.append("第").append(line).append("行,的第").append(column).append("列：").append(text).append("\r\n");
				}
			    System.out.println(sb.toString());
			    return false;
			} else {
			    System.out.println("Good! XML文件通过XSD文件校验成功！");
			    return true;
			}
		} catch (Exception e) {
			System.out.println("XML文件: " + xmlFile.getName() + " 通过XSD文件:" + xsdFile.getName() + "检验失败。/n原因： " + e.getMessage());
            e.printStackTrace();
		}
		 return false;
	}
}
