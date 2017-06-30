package com.cas.circuit;

import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

import com.cas.circuit.util.CircuitUtil;

public class AnalogSwitch2Elm extends AnalogSwitchElm {
	final int openhs = 16;

	Point swposts[], swpoles[], ctlPoint;

	public AnalogSwitch2Elm(int xx, int yy) {
		super(xx, yy);
	}

	public AnalogSwitch2Elm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
		super(xa, ya, xb, yb, f, st);
	}

	@Override
	void calculateCurrent() {
		if (open)
			current = (volts[0] - volts[2]) / r_on;
		else
			current = (volts[0] - volts[1]) / r_on;
	}

	@Override
	void doStep() {
		open = (volts[3] < 2.5);
		if ((flags & FLAG_INVERT) != 0)
			open = !open;
		if (open) {
			sim.stampResistor(nodes[0], nodes[2], r_on);
			sim.stampResistor(nodes[0], nodes[1], r_off);
		} else {
			sim.stampResistor(nodes[0], nodes[1], r_on);
			sim.stampResistor(nodes[0], nodes[2], r_off);
		}
	}

	@Override
	void draw(Graphics g) {
		setBbox(point1, point2, openhs);

		// draw first lead
		setVoltageColor(g, volts[0]);
		CircuitUtil.drawThickLine(g, point1, lead1);

		// draw second lead
		setVoltageColor(g, volts[1]);
		CircuitUtil.drawThickLine(g, swpoles[0], swposts[0]);

		// draw third lead
		setVoltageColor(g, volts[2]);
		CircuitUtil.drawThickLine(g, swpoles[1], swposts[1]);

		// draw switch
		g.setColor(lightGrayColor);
		int position = (open) ? 1 : 0;
		CircuitUtil.drawThickLine(g, lead1, swpoles[position]);

		updateDotCount();
		drawDots(g, point1, lead1, curcount);
		drawDots(g, swpoles[position], swposts[position], curcount);
		drawPosts(g);
	}

	@Override
	boolean getConnection(int n1, int n2) {
		if (n1 == 3 || n2 == 3)
			return false;
		return true;
	}

	@Override
	int getDumpType() {
		return 160;
	}

	@Override
	void getInfo(String arr[]) {
		arr[0] = "analog switch (SPDT)";
		arr[1] = "I = " + CircuitUtil.getCurrentDText(getCurrent());
	}

	@Override
	Point getPost(int n) {
		return (n == 0) ? point1 : (n == 3) ? ctlPoint : swposts[n - 1];
	}

	@Override
	int getPostCount() {
		return 4;
	}

	@Override
	void setPoints() {
		super.setPoints();
		calcLeads(32);
		swposts = newPointArray(2);
		swpoles = newPointArray(2);
		CircuitUtil.interpPoint2(lead1, lead2, swpoles[0], swpoles[1], 1, openhs);
		CircuitUtil.interpPoint2(point1, point2, swposts[0], swposts[1], 1, openhs);
		ctlPoint = CircuitUtil.interpPoint(point1, point2, .5, openhs);
	}

	@Override
	void stamp() {
		sim.stampNonLinear(nodes[0]);
		sim.stampNonLinear(nodes[1]);
		sim.stampNonLinear(nodes[2]);
	}
}
