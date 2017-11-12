package com.dutycode.dparser.config;

/**
 * 
 * Html解析配置常量类
 * @author zhangzhonghua
 * @date 2017年11月12日
 * @version 0.0.1
 * @website https://www.dutycode.com
 * 
 */
public enum HtmlParserConfigEnum {

	PARSER_HTML_CONFIG_TYPE_VALUE(0, "取值为dom属性值"), PARSER_HTML_CONFIG_TYPE_CONTENT(1, "取值为content值");

	private int code;
	private String msg;

	HtmlParserConfigEnum(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}
}
