package com.cas.circuit;

class CC2NegElm extends CC2Elm {
	public CC2NegElm(int xx, int yy) {
		super(xx, yy, -1);
	}

	@Override
	protected Class<?> getDumpClass() {
		return CC2Elm.class;
	}
}
