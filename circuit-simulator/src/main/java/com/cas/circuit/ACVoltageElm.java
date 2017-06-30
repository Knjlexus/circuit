package com.cas.circuit;

class ACVoltageElm extends VoltageElm {
	public ACVoltageElm(int xx, int yy) {
		super(xx, yy, WF_AC);
	}

	@Override
	protected Class<?> getDumpClass() {
		return VoltageElm.class;
	}
}
