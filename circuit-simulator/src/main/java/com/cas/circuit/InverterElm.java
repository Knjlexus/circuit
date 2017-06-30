package com.cas.circuit;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

import com.cas.circuit.util.CircuitUtil;

class InverterElm extends CircuitElm {
	double slewRate; // V/ns

	Polygon gatePoly;

	Point pcircle;

	public InverterElm(int xx, int yy) {
		super(xx, yy);
		noDiagonal = true;
		slewRate = .5;
	}

	public InverterElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		noDiagonal = true;
		try {
			slewRate = new Double(st.nextToken()).doubleValue();
		} catch (Exception e) {
			slewRate = .5;
		}
	}

	@Override
	void doStep() {
		double v0 = volts[1];
		double out = volts[0] > 2.5 ? 0 : 5;
		double maxStep = slewRate * sim.timeStep * 1e9;
		out = Math.max(Math.min(v0 + maxStep, out), v0 - maxStep);
		sim.updateVoltageSource(0, nodes[1], voltSource, out);
	}

	@Override
	void draw(Graphics g) {
		drawPosts(g);
		draw2Leads(g);
		g.setColor(needsHighlight() ? selectColor : lightGrayColor);
		CircuitUtil.drawThickPolygon(g, gatePoly);
		CircuitUtil.drawThickCircle(g, pcircle.x, pcircle.y, 3);
		curcount = updateDotCount(current, curcount);
		drawDots(g, lead2, point2, curcount);
	}

	@Override
	String dump() {
		return super.dump() + " " + slewRate;
	}

	// there is no current path through the inverter input, but there
	// is an indirect path through the output to ground.
	@Override
	boolean getConnection(int n1, int n2) {
		return false;
	}

	@Override
	int getDumpType() {
		return 'I';
	}

	@Override
	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Slew Rate (V/ns)", slewRate, 0, 0);
		return null;
	}

	@Override
	void getInfo(String arr[]) {
		arr[0] = "inverter";
		arr[1] = "Vi = " + CircuitUtil.getVoltageText(volts[0]);
		arr[2] = "Vo = " + CircuitUtil.getVoltageText(volts[1]);
	}

	@Override
	int getShortcut() {
		return '1';
	}

	@Override
	double getVoltageDiff() {
		return volts[0];
	}

	@Override
	int getVoltageSourceCount() {
		return 1;
	}

	@Override
	boolean hasGroundConnection(int n1) {
		return (n1 == 1);
	}

	@Override
	public void setEditValue(int n, EditInfo ei) {
		slewRate = ei.value;
	}

	@Override
	void setPoints() {
		super.setPoints();
		int hs = 16;
		int ww = 16;
		if (ww > dn / 2) {
			ww = (int) (dn / 2);
		}
		lead1 = CircuitUtil.interpPoint(point1, point2, .5 - ww / dn);
		lead2 = CircuitUtil.interpPoint(point1, point2, .5 + (ww + 2) / dn);
		pcircle = CircuitUtil.interpPoint(point1, point2, .5 + (ww - 2) / dn);
		Point triPoints[] = newPointArray(3);
		CircuitUtil.interpPoint2(lead1, lead2, triPoints[0], triPoints[1], 0, hs);
		triPoints[2] = CircuitUtil.interpPoint(point1, point2, .5 + (ww - 5) / dn);
		gatePoly = CircuitUtil.createPolygon(triPoints);
		setBbox(point1, point2, hs);
	}

	@Override
	void stamp() {
		sim.stampVoltageSource(0, nodes[1], voltSource);
	}
}
