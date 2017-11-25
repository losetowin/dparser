package com.dutycode.dparser.htmlparser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
import org.jsoup.select.Elements;

import com.dutycode.dparser.config.HtmlParserConfig;
import com.dutycode.dparser.config.HtmlParserConfigEnum;

/**
 * Html代码转换成实体的工具类 1、初始化配置文件 2、维护配置数据信息 3、提供转化实体方法
 *
 * 使用时,请保留此注释
 * 
 * @website https://www.dutycode.com
 * @author zzh
 * @email dutycode@gmail.com
 * @version 0.0.1
 *
 */
public class HtmlParser {

	private static Logger logger = Logger.getLogger(HtmlParser.class);

	private static Map<String, Map<String, HtmlParserConfig>> map = new HashMap<String, Map<String, HtmlParserConfig>>();

	private static final String TRANSFER_CACHE_PREFIX = "transfer_";

	private static final String TRANSFERLIST_CACHE_PREFIX = "transferlist_";
	
	private static final String DEFAULT_LIST_MAPKEY = "list_key";//因为list只有一个层级，为了配合tranfer的column添加这个

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

				// 转换实体数据
				String qname = child.getQualifiedName();
				if (HtmlParserConfigEnum.PARSER_TYPE_ENTITY.getName().equals(qname)) {
					// 单实体处理
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
							map.put(TRANSFER_CACHE_PREFIX + key, columnMap);
						}

					} else {
						map.put(TRANSFER_CACHE_PREFIX + key, columnMap);
					}

				} else if (HtmlParserConfigEnum.PARSER_TYPE_LIST.getName().equals(qname)) {
					// 列表类型数据
					//获取type类型
					String type = child.attributeValue("type");
					String entityType = child.attributeValue("entityType");
					if (StringUtils.isBlank(entityType)){
						entityType = DEFAULT_LIST_MAPKEY;
					}
					
					//获取属性配置
					String path = child.elementText("path");
					String startpos = child.elementText("startpos");
					String endpos = child.elementText("endpos");
					
					int startposition = Integer.parseInt(startpos);
					int endpostition = Integer.parseInt(endpos);
					
					HtmlParserConfig config = new HtmlParserConfig(path, startposition, endpostition);
					Map<String, HtmlParserConfig> m = new HashMap<String, HtmlParserConfig>();
					m.put(entityType, config);
					
					
					if (entityType.contains(",")){
						String[] typeArr = entityType.split(",");
						for (String tmpType : typeArr) {
							m.put(tmpType, config);
						}
					}
					//写入缓存
					map.put(TRANSFERLIST_CACHE_PREFIX+type, m);
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
			URL u = new URL(url);
			org.jsoup.nodes.Document doc = Jsoup.parse(u, timeoutMills);
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
				Elements ems = htmlelem.select(config.getPath());
				elem = ems.get(config.getPostion());
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

	/**
	 * 转换List类型的数据(xml中配置为list节点的数据)
	 * 
	 * @param html
	 *            待转换的html代码
	 * @param clazz
	 *            待转换到的实体对象
	 * @param listType
	 *            类型,对DTO时有效.
	 * @param entityType 
	 * 			要转换的实体，tranfer中的type
	 * @param <T>
	 * @return
	 */
	public static <T> List<T> transferHtmlList(org.jsoup.nodes.Element elems, Class<T> clazz, String listType, String entityType) {
		
		List<T> resList = new ArrayList<T>();
		//检查是否存在List相应的配置
		String listmapKey = TRANSFERLIST_CACHE_PREFIX + listType;
		if (!map.containsKey(listmapKey)){
			logger.error(String.format("当前不存在type=%s的list节点", listType));
			return null;
		}
		
		if (StringUtils.isBlank(entityType)){
			entityType = DEFAULT_LIST_MAPKEY;
		}
		//获取List配置
		HtmlParserConfig listConfig = map.get(listmapKey).get(entityType);
		
		Elements es = elems.select(listConfig.getPath());
		//检查配置的start和end是否异常
		if (es.size() < listConfig.getEndpos() || es.size() < listConfig.getStartpos()){
			logger.error("html中的最大元素个数不足endpos ， type=" + listType );
			return null;
		}
		
		for (int i = listConfig.getStartpos(); i < listConfig.getEndpos(); i++){
			org.jsoup.nodes.Element e = es.get(i);
			//获取实体
			T entity = transferHtml(e, clazz, entityType);
			resList.add(entity);
		}
		return resList;
	}
	
	public static <T> List<T> transferHtmlList(String html, Class<T> clazz, String listType, String entityType) {
		org.jsoup.nodes.Document doc = Jsoup.parse(html);
		return transferHtmlList(doc, clazz, listType, entityType);
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
