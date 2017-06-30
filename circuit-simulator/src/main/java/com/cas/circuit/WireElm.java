package com.cas.circuit;

import java.awt.Checkbox;
import java.awt.Graphics;
import java.util.StringTokenizer;

import com.cas.circuit.util.CircuitUtil;

class WireElm extends ResistorElm {
	public static boolean ideal = false;
	private static final double defaultResistance = 1E-06;

	static final int FLAG_SHOWCURRENT = 1;

	static final int FLAG_SHOWVOLTAGE = 2;

	public WireElm(int xx, int yy) {
		super(xx, yy);
		resistance = defaultResistance;
	}

	public WireElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
		super(xa, ya, xb, yb, f, new StringTokenizer("0.0"));
		resistance = defaultResistance;
	}

	@Override
	void calculateCurrent() {
		if (!ideal) {
			super.calculateCurrent();
		}
	}

	@Override
	void draw(Graphics g) {
		setVoltageColor(g, volts[0]);
		CircuitUtil.drawThickLine(g, point1, point2);
		doDots(g);
		setBbox(point1, point2, 3);
		if (mustShowCurrent()) {
			String s = CircuitUtil.getShortUnitText(Math.abs(getCurrent()), "A");
			drawValues(g, s, 4);
		} else if (mustShowVoltage()) {
			String s = CircuitUtil.getShortUnitText(volts[0], "V");
			drawValues(g, s, 4);
		}
		drawPosts(g);
	}

	@Override
	String dump() {
		int t = getDumpType();
		return (t < 127 ? ((char) t) + " " : t + " ") + x + " " + y + " " + x2 + " " + y2 + " " + flags;
	}

	@Override
	int getDumpType() {
		return 'w';
	}

	@Override
	public EditInfo getEditInfo(int n) {
		if (n == 0) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Show Current", mustShowCurrent());
			return ei;
		}
		if (n == 1) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Show Voltage", mustShowVoltage());
			return ei;
		}
		return null;
	}

	@Override
	void getInfo(String arr[]) {
		arr[0] = "wire";
		arr[1] = "I = " + CircuitUtil.getCurrentDText(getCurrent());
		arr[2] = "V = " + CircuitUtil.getVoltageText(volts[0]);
	}

	@Override
	double getPower() {
		if (ideal) {
			return 0;
		} else {
			return super.getPower();
		}
	}

	@Override
	int getShortcut() {
		return 'w';
	}

	@Override
	double getVoltageDiff() {
		if (ideal) {
			return volts[0];
		} else {
			return super.getVoltageDiff();
		}
	}

	@Override
	int getVoltageSourceCount() {
		if (ideal) {
			return 1;
		} else {
			return super.getVoltageSourceCount();
		}
	}

	@Override
	boolean isWire() {
		return ideal;
	}

	boolean mustShowCurrent() {
		return (flags & FLAG_SHOWCURRENT) != 0;
	}

	boolean mustShowVoltage() {
		return (flags & FLAG_SHOWVOLTAGE) != 0;
	}

	@Override
	public void setEditValue(int n, EditInfo ei) {
		if (n == 0) {
			if (ei.checkbox.getState())
				flags = FLAG_SHOWCURRENT;
			else
				flags &= ~FLAG_SHOWCURRENT;
		}
		if (n == 1) {
			if (ei.checkbox.getState())
				flags = FLAG_SHOWVOLTAGE;
			else
				flags &= ~FLAG_SHOWVOLTAGE;
		}
	}

	@Override
	void stamp() {
		if (ideal) {
			sim.stampVoltageSource(nodes[0], nodes[1], voltSource, 0);
		} else {
			sim.stampResistor(nodes[0], nodes[1], resistance);
		}
	}
}
