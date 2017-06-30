package com.cas.circuit;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

import com.cas.circuit.util.CircuitUtil;

// Zener code contributed by J. Mike Rollins
// http://www.camotruck.net/rollins/simulator.html
class ZenerElm extends DiodeElm {
	final int hs = 8;

	Polygon poly;

	Point cathode[];

	Point wing[];

	final double default_zvoltage = 5.6;

	public ZenerElm(int xx, int yy) {
		super(xx, yy);
		zvoltage = default_zvoltage;
		setup();
	}

	public ZenerElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
		super(xa, ya, xb, yb, f, st);
		zvoltage = new Double(st.nextToken()).doubleValue();
		setup();
	}

	@Override
	void draw(Graphics g) {
		setBbox(point1, point2, hs);

		double v1 = volts[0];
		double v2 = volts[1];

		draw2Leads(g);

		// draw arrow thingy
		setPowerColor(g, true);
		setVoltageColor(g, v1);
		g.fillPolygon(poly);

		// draw thing arrow is pointing to
		setVoltageColor(g, v2);
		CircuitUtil.drawThickLine(g, cathode[0], cathode[1]);

		// draw wings on cathode
		CircuitUtil.drawThickLine(g, wing[0], cathode[0]);
		CircuitUtil.drawThickLine(g, wing[1], cathode[1]);

		doDots(g);
		drawPosts(g);
	}

	@Override
	String dump() {
		return super.dump() + " " + zvoltage;
	}

	@Override
	int getDumpType() {
		return 'z';
	}

	@Override
	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Fwd Voltage @ 1A", fwdrop, 10, 1000);
		if (n == 1)
			return new EditInfo("Zener Voltage @ 5mA", zvoltage, 1, 25);
		return null;
	}

	@Override
	void getInfo(String arr[]) {
		super.getInfo(arr);
		arr[0] = "Zener diode";
		arr[5] = "Vz = " + CircuitUtil.getVoltageText(zvoltage);
	}

	@Override
	int getShortcut() {
		return 0;
	}

	@Override
	public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
			fwdrop = ei.value;
		if (n == 1)
			zvoltage = ei.value;
		setup();
	}

	@Override
	void setPoints() {
		super.setPoints();
		calcLeads(16);
		cathode = newPointArray(2);
		wing = newPointArray(2);
		Point pa[] = newPointArray(2);
		CircuitUtil.interpPoint2(lead1, lead2, pa[0], pa[1], 0, hs);
		CircuitUtil.interpPoint2(lead1, lead2, cathode[0], cathode[1], 1, hs);
		CircuitUtil.interpPoint(cathode[0], cathode[1], wing[0], -0.2, -hs);
		CircuitUtil.interpPoint(cathode[1], cathode[0], wing[1], -0.2, -hs);
		poly = CircuitUtil.createPolygon(pa[0], pa[1], lead2);
	}

	@Override
	void setup() {
		diode.leakage = 5e-6; // 1N4004 is 5.0 uAmp
		super.setup();
	}
}
