package com.cas.circuit;

class PushSwitchElm extends SwitchElm {
	public PushSwitchElm(int xx, int yy) {
		super(xx, yy, true);
	}

	@Override
	protected Class getDumpClass() {
		return SwitchElm.class;
	}

	@Override
	int getShortcut() {
		return 0;
	}
}
