package com.cas.circuit;

import java.awt.Checkbox;
import java.awt.Graphics;
import java.util.StringTokenizer;

import com.cas.circuit.util.CircuitUtil;

class InductorElm extends CircuitElm {
	Inductor ind;
	double inductance;

	public InductorElm(int xx, int yy) {
		super(xx, yy);
		ind = new Inductor(sim);
		inductance = 1;
		ind.setup(inductance, current, flags);
	}

	public InductorElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		ind = new Inductor(sim);
		inductance = new Double(st.nextToken()).doubleValue();
		current = new Double(st.nextToken()).doubleValue();
		ind.setup(inductance, current, flags);
	}

	@Override
	void calculateCurrent() {
		double voltdiff = volts[0] - volts[1];
		current = ind.calculateCurrent(voltdiff);
	}

	@Override
	void doStep() {
		double voltdiff = volts[0] - volts[1];
		ind.doStep(voltdiff);
	}

	@Override
	void draw(Graphics g) {
		double v1 = volts[0];
		double v2 = volts[1];
		int i;
		int hs = 8;
		setBbox(point1, point2, hs);
		draw2Leads(g);
		setPowerColor(g, false);
		drawCoil(g, 8, lead1, lead2, v1, v2);
		if (sim.showValuesCheckItem.getState()) {
			String s = CircuitUtil.getShortUnitText(inductance, "H");
			drawValues(g, s, hs);
		}
		doDots(g);
		drawPosts(g);
	}

	@Override
	String dump() {
		return super.dump() + " " + inductance + " " + current;
	}

	@Override
	int getDumpType() {
		return 'l';
	}

	@Override
	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Inductance (H)", inductance, 0, 0);
		if (n == 1) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Trapezoidal Approximation", ind.isTrapezoidal());
			return ei;
		}
		return null;
	}

	@Override
	void getInfo(String arr[]) {
		arr[0] = "inductor";
		getBasicInfo(arr);
		arr[3] = "L = " + CircuitUtil.getUnitText(inductance, "H");
		arr[4] = "P = " + CircuitUtil.getUnitText(getPower(), "W");
	}

	@Override
	boolean nonLinear() {
		return ind.nonLinear();
	}

	@Override
	void reset() {
		current = volts[0] = volts[1] = curcount = 0;
		ind.reset();
	}

	@Override
	public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
			inductance = ei.value;
		if (n == 1) {
			if (ei.checkbox.getState())
				flags &= ~Inductor.FLAG_BACK_EULER;
			else
				flags |= Inductor.FLAG_BACK_EULER;
		}
		ind.setup(inductance, current, flags);
	}

	@Override
	void setPoints() {
		super.setPoints();
		calcLeads(32);
	}

	@Override
	void stamp() {
		ind.stamp(nodes[0], nodes[1]);
	}

	@Override
	void startIteration() {
		ind.startIteration(volts[0] - volts[1]);
	}
}
