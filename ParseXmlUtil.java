package com.fh.util.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.fh.entity.consts.RegisterMenuModuleInfo;

public class ParseXmlUtil {
	
	private List<RegisterMenuModuleInfo> registerMenuModuleInfoList = new ArrayList<RegisterMenuModuleInfo>();
	private static ParseXmlUtil parseXmlUtil = new ParseXmlUtil();
	private ParseXmlUtil() {

	}
	public static synchronized ParseXmlUtil getInstance() {
		return parseXmlUtil;
	}
	
	@SuppressWarnings("unchecked")
	public List<RegisterMenuModuleInfo> getRegisterMenuModuleInfo(String xmlPath){
		registerMenuModuleInfoList.clear();
		String realXmlPath = ParseXmlUtil.class.getResource(xmlPath).getPath();
		// 创建SAXReader的对象reader
        SAXReader reader = new SAXReader();
        try {
        	// 通过reader对象的read方法加载xml文件,获取docuemnt对象
        	Document document = reader.read(new File(realXmlPath));
        	// 通过document对象获取根节点projects
            Element projects = document.getRootElement();
            // 通过element对象的elementIterator方法获取迭代器
            Iterator<?> firstIterator = projects.elementIterator();
            // 遍历迭代器，获取根节点中的信息
            while (firstIterator.hasNext()) {
                Element project = (Element) firstIterator.next();
                // 获取节点的属性名以及属性值
                List<Attribute> projectAttributes = project.attributes();
                for (Attribute projectAttribute : projectAttributes) {
                	System.err.println("属性名:" + projectAttribute.getName() + "<<<<<<>>>>>>属性值:" + projectAttribute.getValue());
                }
                RegisterMenuModuleInfo registerMenuModuleInfo = new RegisterMenuModuleInfo();
                Iterator<?> secondIterator = project.elementIterator();
                while (secondIterator.hasNext()) {
                    Element moduleElement = (Element) secondIterator.next();
                    String moduleElementName = moduleElement.getName();
                    String moduleElementValue = moduleElement.getStringValue();
                    System.err.println("节点名:" + moduleElementName + "<<<<<<>>>>>>节点值:" + moduleElementValue);
                    if("projectName".equals(moduleElementName)){
                    	registerMenuModuleInfo.setProjectName(moduleElementValue);
                    }
                    if("databaseName".equals(moduleElementName)){
                    	registerMenuModuleInfo.setDatabaseName(moduleElementValue);
                    }
                    if("databaseUrl".equals(moduleElementName)){
                    	registerMenuModuleInfo.setDatabaseUrl(moduleElementValue);
                    }
                    if("databaseUserName".equals(moduleElementName)){
                    	registerMenuModuleInfo.setDatabaseUserName(moduleElementValue);
                    }
                    if("databasePassWord".equals(moduleElementName)){
                    	registerMenuModuleInfo.setDatabasePassWord(moduleElementValue);
                    }
                    if("projectSwitch".equals(moduleElementName)){
                    	registerMenuModuleInfo.setProjectSwitch(Integer.valueOf(moduleElementValue));
                    }
                    if("projectOrder".equals(moduleElementName)){
                    	registerMenuModuleInfo.setProjectOrder(Integer.valueOf(moduleElementValue));
                    }
                }
                //1表示开启  0表示关闭
                Integer projectSwitch = registerMenuModuleInfo.getProjectSwitch();
                if(projectSwitch==1){
                	registerMenuModuleInfoList.add(registerMenuModuleInfo);
                }
            }
            return registerMenuModuleInfoList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
