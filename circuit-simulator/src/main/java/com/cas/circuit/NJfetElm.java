package com.cas.circuit;
class NJfetElm extends JfetElm {
	public NJfetElm(int xx, int yy) {
		super(xx, yy, false);
	}

	protected Class getDumpClass() {
		return JfetElm.class;
	}
}
