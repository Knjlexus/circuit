package com.cas.circuit.consts;

public enum IOType {
//	仅输出
	OUTPUT(1),
//	仅输入
	INPUT(2),
//	可输入输出
	BOTH(3);
	private int value;

	private IOType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static IOType parseIOType(String type) {
		if ("output".equalsIgnoreCase(type)) {
			return IOType.OUTPUT;
		}
		if ("input".equalsIgnoreCase(type)) {
			return IOType.INPUT;
		}
		return IOType.BOTH;
	}
}
