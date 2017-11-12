package com.dutycode.dparser.htmlparser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jsoup.Jsoup;

import com.dutycode.dparser.config.HtmlParserConfig;
import com.dutycode.dparser.config.HtmlParserConfigEnum;

/**
 * Html代码转换成实体的工具类 1、初始化配置文件 2、维护配置数据信息 3、提供转化实体方法
 * 
 * @author zzh
 *
 */
public class HtmlParser {

	private static Logger logger = Logger.getLogger(HtmlParser.class);

	private static Map<String, Map<String, HtmlParserConfig>> map = new HashMap<String, Map<String, HtmlParserConfig>>();

	static {
		// 默认初始化配置文件，配置文件不存在，则不初始化
		// 配置文件路径
		String path = HtmlParser.class.getProtectionDomain().getCodeSource().getLocation().getFile()
				+ "config/parser.xml";
		System.out.println("当前配置文件路径为：" + path);
		if (new File(path).exists()) {
			initHtmlParserConfig(path);
		}
	}

	@SuppressWarnings("unchecked")
	public static void initHtmlParserConfig(String filepath) {
		try {
			SAXReader reader = new SAXReader();
			File file = new File(filepath);
			Document document;

			document = reader.read(file);

			Element root = document.getRootElement();
			List<Element> childElements = root.elements();
			for (Element child : childElements) {
				// 已知属性名情况下
				String className = child.attributeValue("className");
				String type = child.attributeValue("type");

				Map<String, HtmlParserConfig> columnMap = new HashMap<String, HtmlParserConfig>();
				List<Element> columnList = child.elements();
				for (Element column : columnList) {
					String columnName = column.elementText("columnName");
					String path = column.elementText("path");
					String pos = column.elementText("pos");
					String valType = column.elementText("valType");
					String attributeName = column.elementText("attributeName");
					String columnType = column.elementText("columnType");

					int posInt = Integer.valueOf(pos);
					HtmlParserConfig config = new HtmlParserConfig(columnName, path, posInt, valType, attributeName,
							columnType);
					columnMap.put(columnName, config);
				}

				// 添加配置信息到缓存中

				String key = className;
				if (StringUtils.isNotBlank(type)) {

					String[] typeArr = type.split(",");
					for (String tmpType : typeArr) {
						key = className + "_" + tmpType;
						map.put(key, columnMap);
					}

				} else {
					map.put(key, columnMap);
				}

			}

		} catch (DocumentException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 1、获取类的全名， 2、获取配置信息 3、解析 4、返回实体
	 * 
	 * @param htmlInfo
	 * @param clazz
	 * @return
	 */
	public static <T> T transferHtml(String htmlInfo, Class<T> clazz) {

		org.jsoup.nodes.Document doc = Jsoup.parse(htmlInfo);

		return transferHtml(doc, clazz);
	}

	/**
	 * 将html页面专程实体，
	 * 
	 * @param htmlInfo
	 *            页面代码
	 * @param clazz
	 *            需要转成的实体类
	 * @param entityType
	 *            实体属性， 一般用于解析多个页面时使用， 可为空
	 * @return
	 */
	public static <T> T transferHtml(String htmlInfo, Class<T> clazz, String entityType) {

		org.jsoup.nodes.Document doc = Jsoup.parse(htmlInfo);

		return transferHtml(doc, clazz, entityType);
	}

	/**
	 * 
	 * @param htmlelem
	 * @param clazz
	 * @return
	 */
	public static <T> T transferHtml(org.jsoup.nodes.Element htmlelem, Class<T> clazz) {
		return transferHtml(htmlelem, clazz, null);
	}

	public static <T> T transferHtml(String url, Class<T> clazz, int timeoutMills, String entityType) {
		try {
			org.jsoup.nodes.Document doc = Jsoup.parse(new URL(url), timeoutMills);
			return transferHtml(doc, clazz, entityType);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static <T> T transferHtml(String url, Class<T> clazz, int timeoutMills) {
		return transferHtml(url, clazz, timeoutMills, null);
	}

	@SuppressWarnings("unchecked")
	public static <T> T transferHtml(org.jsoup.nodes.Element htmlelem, Class<T> clazz, String entityType) {

		String className = clazz.getName();

		String mapKey = className;
		if (StringUtils.isNotBlank(entityType)) {
			mapKey = className + "_" + entityType;
		}

		// 获取配置信息
		Map<String, HtmlParserConfig> configMap = map.containsKey(mapKey) ? map.get(mapKey) : null;

		if (configMap == null) {
			logger.error("当前类尚未配置，请检查xml配置文件， className=" + mapKey);
			return null;
		}

		Class<?> clazzT;
		Object obj = null;
		try {
			clazzT = Class.forName(className);
			obj = clazzT.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		// 获取配置的字段信息
		Set<String> configKeys = configMap.keySet();
		for (String key : configKeys) {
			// 配置详情
			HtmlParserConfig config = configMap.get(key);

			org.jsoup.nodes.Element elem = null;
			try {
				elem = htmlelem.select(config.getPath()).get(config.getPostion());
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			String value = "";
			// 检查是否是value值类型的数据
			if (String.valueOf(HtmlParserConfigEnum.PARSER_HTML_CONFIG_TYPE_VALUE.getCode())
					.equals(config.getValType())) {
				// 属性名称
				String attributeName = config.getAttributeName();
				value = elem.attr(attributeName);
			} else {
				value = elem.text();
			}

			// 获取字段名称
			String columnName = config.getColumnName();

			// set方法
			String setMethodName = "set" + columnName.substring(0, 1).toUpperCase()
					+ columnName.substring(1, columnName.length());

			// 获取数值类型
			Class<?> type = getMethodType(config.getColunmType());

			try {
				// 执行set方法
				Method method = obj.getClass().getMethod(setMethodName, type);
				method.invoke(obj, value);

			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

		}

		return (T) (obj);
	}

	private static Class<?> getMethodType(String type) {
		if ("string".equals(type)) {
			return String.class;
		} else if ("int".equals(type)) {
			return Integer.class;
		} else if ("long".equals(type)) {
			return Long.class;
		} else if ("double".equals(type)) {
			return Double.class;
		} else {
			return Object.class;
		}
	}

}
