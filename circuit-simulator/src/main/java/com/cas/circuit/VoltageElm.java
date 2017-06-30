package com.cas.circuit;

import static java.lang.Math.PI;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Choice;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

import com.cas.circuit.util.CircuitUtil;

public class VoltageElm extends CircuitElm {
	static final int FLAG_COS = 2;
	static final int WF_DC = 0;
	static final int WF_AC = 1;
	static final int WF_SQUARE = 2;
	static final int WF_TRIANGLE = 3;
	static final int WF_SAWTOOTH = 4;
	static final int WF_PULSE = 5;
	static final int WF_VAR = 6;
	int waveform;
	double frequency, maxVoltage, freqTimeZero, bias, phaseShift, dutyCycle;

	final int circleSize = 17;

	VoltageElm(int xx, int yy, int wf) {
		super(xx, yy);
		waveform = wf;
		maxVoltage = 5;
		frequency = 40;
		dutyCycle = .5;
		reset();
	}

	public VoltageElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		maxVoltage = 5;
		frequency = 40;
		waveform = WF_DC;
		dutyCycle = .5;
		try {
			waveform = new Integer(st.nextToken()).intValue();
			frequency = new Double(st.nextToken()).doubleValue();
			maxVoltage = new Double(st.nextToken()).doubleValue();
			bias = new Double(st.nextToken()).doubleValue();
			phaseShift = new Double(st.nextToken()).doubleValue();
			dutyCycle = new Double(st.nextToken()).doubleValue();
		} catch (Exception e) {
		}
		if ((flags & FLAG_COS) != 0) {
			flags &= ~FLAG_COS;
			phaseShift = PI / 2;
		}
		reset();
	}

	@Override
	void doStep() {
		if (waveform != WF_DC)
			sim.updateVoltageSource(nodes[0], nodes[1], voltSource, getVoltage());
	}

	@Override
	void draw(Graphics g) {
		setBbox(x, y, x2, y2);
		draw2Leads(g);
		if (waveform == WF_DC) {
			setPowerColor(g, false);
			setVoltageColor(g, volts[0]);
			CircuitUtil.interpPoint2(lead1, lead2, ps1, ps2, 0, 10);
			CircuitUtil.drawThickLine(g, ps1, ps2);
			setVoltageColor(g, volts[1]);
			int hs = 16;
			setBbox(point1, point2, hs);
			CircuitUtil.interpPoint2(lead1, lead2, ps1, ps2, 1, hs);
			CircuitUtil.drawThickLine(g, ps1, ps2);
		} else {
			setBbox(point1, point2, circleSize);
			CircuitUtil.interpPoint(lead1, lead2, ps1, .5);
			drawWaveform(g, ps1);
		}
		updateDotCount();
		if (sim.dragElm != this) {
			if (waveform == WF_DC)
				drawDots(g, point1, point2, curcount);
			else {
				drawDots(g, point1, lead1, curcount);
				drawDots(g, point2, lead2, -curcount);
			}
		}
		drawPosts(g);
	}

	void drawWaveform(Graphics g, Point center) {
		g.setColor(needsHighlight() ? selectColor : Color.gray);
		setPowerColor(g, false);
		int xc = center.x;
		int yc = center.y;
		CircuitUtil.drawThickCircle(g, xc, yc, circleSize);
		int wl = 8;
		adjustBbox(xc - circleSize, yc - circleSize, xc + circleSize, yc + circleSize);
		int xc2;
		switch (waveform) {
		case WF_DC: {
			break;
		}
		case WF_SQUARE:
			xc2 = (int) (wl * 2 * dutyCycle - wl + xc);
			xc2 = max(xc - wl + 3, min(xc + wl - 3, xc2));
			CircuitUtil.drawThickLine(g, xc - wl, yc - wl, xc - wl, yc);
			CircuitUtil.drawThickLine(g, xc - wl, yc - wl, xc2, yc - wl);
			CircuitUtil.drawThickLine(g, xc2, yc - wl, xc2, yc + wl);
			CircuitUtil.drawThickLine(g, xc + wl, yc + wl, xc2, yc + wl);
			CircuitUtil.drawThickLine(g, xc + wl, yc, xc + wl, yc + wl);
			break;
		case WF_PULSE:
			yc += wl / 2;
			CircuitUtil.drawThickLine(g, xc - wl, yc - wl, xc - wl, yc);
			CircuitUtil.drawThickLine(g, xc - wl, yc - wl, xc - wl / 2, yc - wl);
			CircuitUtil.drawThickLine(g, xc - wl / 2, yc - wl, xc - wl / 2, yc);
			CircuitUtil.drawThickLine(g, xc - wl / 2, yc, xc + wl, yc);
			break;
		case WF_SAWTOOTH:
			CircuitUtil.drawThickLine(g, xc, yc - wl, xc - wl, yc);
			CircuitUtil.drawThickLine(g, xc, yc - wl, xc, yc + wl);
			CircuitUtil.drawThickLine(g, xc, yc + wl, xc + wl, yc);
			break;
		case WF_TRIANGLE: {
			int xl = 5;
			CircuitUtil.drawThickLine(g, xc - xl * 2, yc, xc - xl, yc - wl);
			CircuitUtil.drawThickLine(g, xc - xl, yc - wl, xc, yc);
			CircuitUtil.drawThickLine(g, xc, yc, xc + xl, yc + wl);
			CircuitUtil.drawThickLine(g, xc + xl, yc + wl, xc + xl * 2, yc);
			break;
		}
		case WF_AC: {
			int i;
			int xl = 10;
			int ox = -1, oy = -1;
			for (i = -xl; i <= xl; i++) {
				int yy = yc + (int) (.95 * Math.sin(i * PI / xl) * wl);
				if (ox != -1)
					CircuitUtil.drawThickLine(g, ox, oy, xc + i, yy);
				ox = xc + i;
				oy = yy;
			}
			break;
		}
		}
		if (sim.showValuesCheckItem.getState()) {
			String s = CircuitUtil.getShortUnitText(frequency, "Hz");
			if (dx == 0 || dy == 0)
				drawValues(g, s, circleSize);
		}
	}

	@Override
	String dump() {
		return super.dump() + " " + waveform + " " + frequency + " " + maxVoltage + " " + bias + " " + phaseShift + " " + dutyCycle;
	}
	/*
	 * void setCurrent(double c) { current = c;
	 * System.out.print("v current set to " + c + "\n"); }
	 */

	@Override
	int getDumpType() {
		return 'v';
	}

	@Override
	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo(waveform == WF_DC ? "Voltage" : "Max Voltage", maxVoltage, -20, 20);
		if (n == 1) {
			EditInfo ei = new EditInfo("Waveform", waveform, -1, -1);
			ei.choice = new Choice();
			ei.choice.add("D/C");
			ei.choice.add("A/C");
			ei.choice.add("Square Wave");
			ei.choice.add("Triangle");
			ei.choice.add("Sawtooth");
			ei.choice.add("Pulse");
			ei.choice.select(waveform);
			return ei;
		}
		if (waveform == WF_DC)
			return null;
		if (n == 2)
			return new EditInfo("Frequency (Hz)", frequency, 4, 500);
		if (n == 3)
			return new EditInfo("DC Offset (V)", bias, -20, 20);
		if (n == 4)
			return new EditInfo("Phase Offset (degrees)", phaseShift * 180 / PI, -180, 180).setDimensionless();
		if (n == 5 && waveform == WF_SQUARE)
			return new EditInfo("Duty Cycle", dutyCycle * 100, 0, 100).setDimensionless();
		return null;
	}

	@Override
	void getInfo(String arr[]) {
		switch (waveform) {
		case WF_DC:
		case WF_VAR:
			arr[0] = "voltage source";
			break;
		case WF_AC:
			arr[0] = "A/C source";
			break;
		case WF_SQUARE:
			arr[0] = "square wave gen";
			break;
		case WF_PULSE:
			arr[0] = "pulse gen";
			break;
		case WF_SAWTOOTH:
			arr[0] = "sawtooth gen";
			break;
		case WF_TRIANGLE:
			arr[0] = "triangle gen";
			break;
		}
		arr[1] = "I = " + CircuitUtil.getCurrentText(getCurrent());
		arr[2] = ((this instanceof RailElm) ? "V = " : "Vd = ") + CircuitUtil.getVoltageText(getVoltageDiff());
		if (waveform != WF_DC && waveform != WF_VAR) {
			arr[3] = "f = " + CircuitUtil.getUnitText(frequency, "Hz");
			arr[4] = "Vmax = " + CircuitUtil.getVoltageText(maxVoltage);
			int i = 5;
			if (bias != 0)
				arr[i++] = "Voff = " + CircuitUtil.getVoltageText(bias);
			else if (frequency > 500)
				arr[i++] = "wavelength = " + CircuitUtil.getUnitText(2.9979e8 / frequency, "m");
			arr[i++] = "P = " + CircuitUtil.getUnitText(getPower(), "W");
		}
	}

	@Override
	double getPower() {
		return -getVoltageDiff() * current;
	}

	double getVoltage() {
		double w = 2 * PI * (sim.t - freqTimeZero) * frequency + phaseShift;
		switch (waveform) {
		case WF_DC:
			return maxVoltage + bias;
		case WF_AC:
			return Math.sin(w) * maxVoltage + bias;
		case WF_SQUARE:
			return bias + ((w % (2 * PI) > (2 * PI * dutyCycle)) ? -maxVoltage : maxVoltage);
		case WF_TRIANGLE:
			return bias + triangleFunc(w % (2 * PI)) * maxVoltage;
		case WF_SAWTOOTH:
			return bias + (w % (2 * PI)) * (maxVoltage / PI) - maxVoltage;
		case WF_PULSE:
			return ((w % (2 * PI)) < 1) ? maxVoltage + bias : bias;
		default:
			return 0;
		}
	}

	@Override
	double getVoltageDiff() {
		return volts[1] - volts[0];
	}

	@Override
	int getVoltageSourceCount() {
		return 1;
	}

	@Override
	void reset() {
		freqTimeZero = 0;
		curcount = 0;
	}

	@Override
	public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
			maxVoltage = ei.value;
		if (n == 3)
			bias = ei.value;
		if (n == 2) {
			// adjust time zero to maintain continuity ind the waveform
			// even though the frequency has changed.
			double oldfreq = frequency;
			frequency = ei.value;
			double maxfreq = 1 / (8 * sim.timeStep);
			if (frequency > maxfreq)
				frequency = maxfreq;
			double adj = frequency - oldfreq;
			freqTimeZero = sim.t - oldfreq * (sim.t - freqTimeZero) / frequency;
		}
		if (n == 1) {
			int ow = waveform;
			waveform = ei.choice.getSelectedIndex();
			if (waveform == WF_DC && ow != WF_DC) {
				ei.newDialog = true;
				bias = 0;
			} else if (waveform != WF_DC && ow == WF_DC) {
				ei.newDialog = true;
			}
			if ((waveform == WF_SQUARE || ow == WF_SQUARE) && waveform != ow)
				ei.newDialog = true;
			setPoints();
		}
		if (n == 4)
			phaseShift = ei.value * PI / 180;
		if (n == 5)
			dutyCycle = ei.value * .01;
	}

	@Override
	void setPoints() {
		super.setPoints();
		calcLeads((waveform == WF_DC || waveform == WF_VAR) ? 8 : circleSize * 2);
	}

	@Override
	void stamp() {
		if (waveform == WF_DC)
			sim.stampVoltageSource(nodes[0], nodes[1], voltSource, getVoltage());
		else
			sim.stampVoltageSource(nodes[0], nodes[1], voltSource);
	}

	double triangleFunc(double x) {
		if (x < PI)
			return x * (2 / PI) - 1;
		return 1 - (x - PI) * (2 / PI);
	}
}
