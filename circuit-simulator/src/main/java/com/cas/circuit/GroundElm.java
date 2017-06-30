package com.cas.circuit;

import java.awt.Graphics;
import java.util.StringTokenizer;

import com.cas.circuit.util.CircuitUtil;

class GroundElm extends CircuitElm {
	public GroundElm(int xx, int yy) {
		super(xx, yy);
	}

	public GroundElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
		super(xa, ya, xb, yb, f);
	}

	@Override
	void draw(Graphics g) {
		setVoltageColor(g, 0);
		CircuitUtil.drawThickLine(g, point1, point2);
		int i;
		for (i = 0; i != 3; i++) {
			int a = 10 - i * 4;
			int b = i * 5; // -10;
			CircuitUtil.interpPoint2(point1, point2, ps1, ps2, 1 + b / dn, a);
			CircuitUtil.drawThickLine(g, ps1, ps2);
		}
		doDots(g);
		CircuitUtil.interpPoint(point1, point2, ps2, 1 + 11. / dn);
		setBbox(point1, ps2, 11);
		drawPost(g, x, y, nodes[0]);
	}

	@Override
	int getDumpType() {
		return 'g';
	}

	@Override
	void getInfo(String arr[]) {
		arr[0] = "ground";
		arr[1] = "I = " + CircuitUtil.getCurrentText(getCurrent());
	}

	@Override
	int getPostCount() {
		return 1;
	}

	@Override
	int getShortcut() {
		return 'g';
	}

	@Override
	double getVoltageDiff() {
		return 0;
	}

	@Override
	int getVoltageSourceCount() {
		return 1;
	}

	@Override
	boolean hasGroundConnection(int n1) {
		return true;
	}

	@Override
	void setCurrent(int x, double c) {
		current = -c;
	}

	@Override
	void stamp() {
		sim.stampVoltageSource(0, nodes[0], voltSource, 0);
	}
}
