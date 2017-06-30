package com.cas.circuit;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

import com.cas.circuit.util.CircuitUtil;

abstract class GateElm extends CircuitElm {
	final int FLAG_SMALL = 1;
	int inputCount = 2;
	boolean lastOutput;

	int gsize, gwidth, gwidth2, gheight, hs2;

	Point inPosts[], inGates[];

	int ww;

	Polygon gatePoly;

	Point pcircle, linePoints[];

	public GateElm(int xx, int yy) {
		super(xx, yy);
		noDiagonal = true;
		inputCount = 2;
		setSize(sim.smallGridCheckItem.getState() ? 1 : 2);
	}

	public GateElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		inputCount = new Integer(st.nextToken()).intValue();
		lastOutput = new Double(st.nextToken()).doubleValue() > 2.5;
		noDiagonal = true;
		setSize((f & FLAG_SMALL) != 0 ? 1 : 2);
	}

	abstract boolean calcFunction();

	@Override
	void doStep() {
		int i;
		boolean f = calcFunction();
		if (isInverting())
			f = !f;
		lastOutput = f;
		double res = f ? 5 : 0;
		sim.updateVoltageSource(0, nodes[inputCount], voltSource, res);
	}

	@Override
	void draw(Graphics g) {
		int i;
		for (i = 0; i != inputCount; i++) {
			setVoltageColor(g, volts[i]);
			CircuitUtil.drawThickLine(g, inPosts[i], inGates[i]);
		}
		setVoltageColor(g, volts[inputCount]);
		CircuitUtil.drawThickLine(g, lead2, point2);
		g.setColor(needsHighlight() ? selectColor : lightGrayColor);
		CircuitUtil.drawThickPolygon(g, gatePoly);
		if (linePoints != null)
			for (i = 0; i != linePoints.length - 1; i++)
				CircuitUtil.drawThickLine(g, linePoints[i], linePoints[i + 1]);
		if (isInverting())
			CircuitUtil.drawThickCircle(g, pcircle.x, pcircle.y, 3);
		curcount = updateDotCount(current, curcount);
		drawDots(g, lead2, point2, curcount);
		drawPosts(g);
	}

	@Override
	String dump() {
		return super.dump() + " " + inputCount + " " + volts[inputCount];
	}

	// there is no current path through the gate inputs, but there
	// is an indirect path through the output to ground.
	@Override
	boolean getConnection(int n1, int n2) {
		return false;
	}

	@Override
	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("# of Inputs", inputCount, 1, 8).setDimensionless();
		return null;
	}

	abstract String getGateName();

	@Override
	void getInfo(String arr[]) {
		arr[0] = getGateName();
		arr[1] = "Vout = " + CircuitUtil.getVoltageText(volts[inputCount]);
		arr[2] = "Iout = " + CircuitUtil.getCurrentText(getCurrent());
	}

	boolean getInput(int x) {
		return volts[x] > 2.5;
	}

	@Override
	Point getPost(int n) {
		if (n == inputCount)
			return point2;
		return inPosts[n];
	}

	@Override
	int getPostCount() {
		return inputCount + 1;
	}

	@Override
	int getVoltageSourceCount() {
		return 1;
	}

	@Override
	boolean hasGroundConnection(int n1) {
		return (n1 == inputCount);
	}

	boolean isInverting() {
		return false;
	}

	@Override
	public void setEditValue(int n, EditInfo ei) {
		inputCount = (int) ei.value;
		setPoints();
	}

	@Override
	void setPoints() {
		super.setPoints();
		if (dn > 150 && this == sim.dragElm)
			setSize(2);
		int hs = gheight;
		int i;
		ww = gwidth2; // was 24
		if (ww > dn / 2)
			ww = (int) (dn / 2);
		if (isInverting() && ww + 8 > dn / 2)
			ww = (int) (dn / 2 - 8);
		calcLeads(ww * 2);
		inPosts = new Point[inputCount];
		inGates = new Point[inputCount];
		allocNodes();
		int i0 = -inputCount / 2;
		for (i = 0; i != inputCount; i++, i0++) {
			if (i0 == 0 && (inputCount & 1) == 0)
				i0++;
			inPosts[i] = CircuitUtil.interpPoint(point1, point2, 0, hs * i0);
			inGates[i] = CircuitUtil.interpPoint(lead1, lead2, 0, hs * i0);
			volts[i] = (lastOutput ^ isInverting()) ? 5 : 0;
		}
		hs2 = gwidth * (inputCount / 2 + 1);
		setBbox(point1, point2, hs2);
	}

	void setSize(int s) {
		gsize = s;
		gwidth = 7 * s;
		gwidth2 = 14 * s;
		gheight = 8 * s;
		flags = (s == 1) ? FLAG_SMALL : 0;
	}

	@Override
	void stamp() {
		sim.stampVoltageSource(0, nodes[inputCount], voltSource);
	}
}
