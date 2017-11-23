package com.dutycode.dparser.config;

/**
 * Html解析配置常量类
 *
 * @author zhangzhonghua
 * @version 0.0.1
 * @date 2017年11月12日
 * @website https://www.dutycode.com
 */
public enum HtmlParserConfigEnum {

    PARSER_HTML_CONFIG_TYPE_VALUE(0, "取值为dom属性值"), PARSER_HTML_CONFIG_TYPE_CONTENT(1, "取值为content值"),

    PARSER_TYPE_ENTITY(100, "transfer","类型,将被转换为实体"), PARSER_TYPE_LIST(101, "list","类型,将被转换为List类型");

    /**代码*/
    private int code;
    /**名称*/
    private String name;
    /**含义**/
    private String msg;

    HtmlParserConfigEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    HtmlParserConfigEnum(int code, String name, String msg){
        this.code = code;
        this.name = name;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getMsg() {
        return msg;
    }
}
