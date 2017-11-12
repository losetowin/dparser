package com.dutycode.dparser.config;

public class HtmlParserConfig {

	private String columnName;

	private String path;

	private int postion;

	private String valType;

	private String attributeName;

	private String colunmType;

	public HtmlParserConfig(){}
	public HtmlParserConfig(String columName, String path, int pos,
			String valType, String attributeName, String columnType) {
		this.attributeName = attributeName;
		this.columnName = columName;
		this.colunmType = columnType;
		this.path = path;
		this.postion = pos;
		this.valType = valType;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getPostion() {
		return postion;
	}

	public void setPostion(int postion) {
		this.postion = postion;
	}

	public String getValType() {
		return valType;
	}

	public void setValType(String valType) {
		this.valType = valType;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getColunmType() {
		return colunmType;
	}

	public void setColunmType(String colunmType) {
		this.colunmType = colunmType;
	}
	@Override
	public String toString() {
		return "HtmlParserConfig [columnName=" + columnName + ", path="
				+ path + ", postion=" + postion + ", valType=" + valType
				+ ", attributeName=" + attributeName + ", colunmType="
				+ colunmType + "]";
	}

}
