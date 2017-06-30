package com.cas.circuit;

class DCVoltageElm extends VoltageElm {
	public DCVoltageElm(int xx, int yy) {
		super(xx, yy, WF_DC);
	}

	@Override
	protected Class getDumpClass() {
		return VoltageElm.class;
	}

	@Override
	int getShortcut() {
		return 'v';
	}
}
