package com.cas.circuit;

import java.util.StringTokenizer;

class NandGateElm extends AndGateElm {
	public NandGateElm(int xx, int yy) {
		super(xx, yy);
	}

	public NandGateElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
		super(xa, ya, xb, yb, f, st);
	}

	@Override
	int getDumpType() {
		return 151;
	}

	@Override
	String getGateName() {
		return "NAND gate";
	}

	@Override
	int getShortcut() {
		return '@';
	}

	@Override
	boolean isInverting() {
		return true;
	}
}
