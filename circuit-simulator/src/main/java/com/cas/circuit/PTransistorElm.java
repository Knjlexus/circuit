package com.cas.circuit;

class PTransistorElm extends TransistorElm {
	public PTransistorElm(int xx, int yy) {
		super(xx, yy, true);
	}

	@Override
	protected Class getDumpClass() {
		return TransistorElm.class;
	}
}
