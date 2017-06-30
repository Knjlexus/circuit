package com.cas.circuit;

class NMosfetElm extends MosfetElm {
	public NMosfetElm(int xx, int yy) {
		super(xx, yy, false);
	}

	@Override
	protected Class getDumpClass() {
		return MosfetElm.class;
	}
}
