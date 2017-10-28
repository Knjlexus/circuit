package com.cas.circuit.po;

public class SignalPO {
//	信号地址
	private String addr;
//	信号注释
	private String anno;
//	信号的符号
	private String symbol;
//	信号功能描述
	private String func;
//  信号使用范围
	private String limit;
//  信号激活条件,上升沿,下降沿
	private String active;

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getAnno() {
		return anno;
	}

	public void setAnno(String anno) {
		this.anno = anno;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getFunc() {
		return func;
	}

	public void setFunc(String func) {
		this.func = func;
	}

	public String getLimit() {
		return limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

}
