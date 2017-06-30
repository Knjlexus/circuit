package com.cas.circuit;

class PMosfetElm extends MosfetElm {
	public PMosfetElm(int xx, int yy) {
		super(xx, yy, true);
	}

	@Override
	protected Class getDumpClass() {
		return MosfetElm.class;
	}
}
