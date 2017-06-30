package com.cas.circuit;

public class ACRailElm extends RailElm {
	public ACRailElm(int xx, int yy) {
		super(xx, yy, WF_AC);
	}

	@Override
	protected Class<?> getDumpClass() {
		return RailElm.class;
	}

	@Override
	int getShortcut() {
		return 0;
	}
}
