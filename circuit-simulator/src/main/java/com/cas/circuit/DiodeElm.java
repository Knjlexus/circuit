package com.cas.circuit;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

import com.cas.circuit.util.CircuitUtil;

class DiodeElm extends CircuitElm {
	static final int FLAG_FWDROP = 1;
	Diode diode;
	final double defaultdrop = .805904783;
	double fwdrop, zvoltage;

	final int hs = 8;

	Polygon poly;

	Point cathode[];

	public DiodeElm(int xx, int yy) {
		super(xx, yy);
		diode = new Diode(sim);
		fwdrop = defaultdrop;
		zvoltage = 0;
		setup();
	}

	public DiodeElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		diode = new Diode(sim);
		fwdrop = defaultdrop;
		zvoltage = 0;
		if ((f & FLAG_FWDROP) > 0) {
			try {
				fwdrop = new Double(st.nextToken()).doubleValue();
			} catch (Exception e) {
			}
		}
		setup();
	}

	@Override
	void calculateCurrent() {
		current = diode.calculateCurrent(volts[0] - volts[1]);
	}

	@Override
	void doStep() {
		diode.doStep(volts[0] - volts[1]);
	}

	@Override
	void draw(Graphics g) {
		drawDiode(g);
		doDots(g);
		drawPosts(g);
	}

	void drawDiode(Graphics g) {
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
	}

	@Override
	String dump() {
		flags |= FLAG_FWDROP;
		return super.dump() + " " + fwdrop;
	}

	@Override
	int getDumpType() {
		return 'd';
	}

	@Override
	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Fwd Voltage @ 1A", fwdrop, 10, 1000);
		return null;
	}

	@Override
	void getInfo(String arr[]) {
		arr[0] = "diode";
		arr[1] = "I = " + CircuitUtil.getCurrentText(getCurrent());
		arr[2] = "Vd = " + CircuitUtil.getVoltageText(getVoltageDiff());
		arr[3] = "P = " + CircuitUtil.getUnitText(getPower(), "W");
		arr[4] = "Vf = " + CircuitUtil.getVoltageText(fwdrop);
	}

	@Override
	int getShortcut() {
		return 'd';
	}

	@Override
	boolean nonLinear() {
		return true;
	}

	@Override
	void reset() {
		diode.reset();
		volts[0] = volts[1] = curcount = 0;
	}

	@Override
	public void setEditValue(int n, EditInfo ei) {
		fwdrop = ei.value;
		setup();
	}

	@Override
	void setPoints() {
		super.setPoints();
		calcLeads(16);
		cathode = newPointArray(2);
		Point pa[] = newPointArray(2);
		CircuitUtil.interpPoint2(lead1, lead2, pa[0], pa[1], 0, hs);
		CircuitUtil.interpPoint2(lead1, lead2, cathode[0], cathode[1], 1, hs);
		poly = CircuitUtil.createPolygon(pa[0], pa[1], lead2);
	}

	void setup() {
		diode.setup(fwdrop, zvoltage);
	}

	@Override
	void stamp() {
		diode.stamp(nodes[0], nodes[1]);
	}
}
