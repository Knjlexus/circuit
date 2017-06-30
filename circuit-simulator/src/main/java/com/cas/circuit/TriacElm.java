package com.cas.circuit;

// stub implementation of TriacElm, based on SCRElm
// FIXME need to add TriacElm to srclist
// FIXME need to uncomment TriacElm line from CirSim.java

import static java.lang.Math.abs;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

import com.cas.circuit.util.CircuitUtil;

// Silicon-Controlled Rectifier
// 3 nodes, 1 internal node
// 0 = anode, 1 = cathode, 2 = gate
// 0, 3 = variable resistor
// 3, 2 = diode
// 2, 1 = 50 ohm resistor

class TriacElm extends CircuitElm {
	final int anode = 0;
	final int cnode = 1;
	final int gnode = 2;
	final int inode = 3;
	Diode diode;

	double ia, ic, ig, curcount_a, curcount_c, curcount_g;

	double lastvac, lastvag;

	double cresistance, triggerI, holdingI;

	final int hs = 8;

	Polygon poly;

	Point cathode[], gate[];

	double aresistance;

	public TriacElm(int xx, int yy) {
		super(xx, yy);
		setDefaults();
		setup();
	}

	public TriacElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		setDefaults();
		try {
			lastvac = new Double(st.nextToken()).doubleValue();
			lastvag = new Double(st.nextToken()).doubleValue();
			volts[anode] = 0;
			volts[cnode] = -lastvac;
			volts[gnode] = -lastvag;
			triggerI = new Double(st.nextToken()).doubleValue();
			holdingI = new Double(st.nextToken()).doubleValue();
			cresistance = new Double(st.nextToken()).doubleValue();
		} catch (Exception e) {
		}
		setup();
	}

	@Override
	void calculateCurrent() {
		ic = (volts[cnode] - volts[gnode]) / cresistance;
		ia = (volts[anode] - volts[inode]) / aresistance;
		ig = -ic - ia;
	}

	@Override
	void doStep() {
		double vac = volts[anode] - volts[cnode]; // typically negative
		double vag = volts[anode] - volts[gnode]; // typically positive
		if (Math.abs(vac - lastvac) > .01 || Math.abs(vag - lastvag) > .01)
			sim.converged = false;
		lastvac = vac;
		lastvag = vag;
		diode.doStep(volts[inode] - volts[gnode]);
		double icmult = 1 / triggerI;
		double iamult = 1 / holdingI - icmult;
		// System.out.println(icmult + " " + iamult);
		aresistance = (-icmult * ic + ia * iamult > 1) ? .0105 : 10e5;
		// System.out.println(vac + " " + vag + " " + sim.converged + " " + ic +
		// " " + ia + " " + aresistance + " " + volts[inode] + " " +
		// volts[gnode] + " " + volts[anode]);
		sim.stampResistor(nodes[anode], nodes[inode], aresistance);
	}

	@Override
	void draw(Graphics g) {
		setBbox(point1, point2, hs);
		adjustBbox(gate[0], gate[1]);

		double v1 = volts[anode];
		double v2 = volts[cnode];

		draw2Leads(g);

		// draw arrow thingy
		setPowerColor(g, true);
		setVoltageColor(g, v1);
		g.fillPolygon(poly);

		// draw thing arrow is pointing to
		setVoltageColor(g, v2);
		CircuitUtil.drawThickLine(g, cathode[0], cathode[1]);

		CircuitUtil.drawThickLine(g, lead2, gate[0]);
		CircuitUtil.drawThickLine(g, gate[0], gate[1]);

		curcount_a = updateDotCount(ia, curcount_a);
		curcount_c = updateDotCount(ic, curcount_c);
		curcount_g = updateDotCount(ig, curcount_g);
		if (sim.dragElm != this) {
			drawDots(g, point1, lead2, curcount_a);
			drawDots(g, point2, lead2, curcount_c);
			drawDots(g, gate[1], gate[0], curcount_g);
			drawDots(g, gate[0], lead2, curcount_g + CircuitUtil.distance(gate[1], gate[0]));
		}
		drawPosts(g);
	}

	@Override
	String dump() {
		return super.dump() + " " + (volts[anode] - volts[cnode]) + " " + (volts[anode] - volts[gnode]) + " " + triggerI + " " + holdingI + " " + cresistance;
	}

	@Override
	int getDumpType() {
		return 183;
	}

	@Override
	public EditInfo getEditInfo(int n) {
		// ohmString doesn't work here on linux
		if (n == 0)
			return new EditInfo("Trigger Current (A)", triggerI, 0, 0);
		if (n == 1)
			return new EditInfo("Holding Current (A)", holdingI, 0, 0);
		if (n == 2)
			return new EditInfo("Gate-Cathode Resistance (ohms)", cresistance, 0, 0);
		return null;
	}

	@Override
	void getInfo(String arr[]) {
		arr[0] = "SCR";
		double vac = volts[anode] - volts[cnode];
		double vag = volts[anode] - volts[gnode];
		double vgc = volts[gnode] - volts[cnode];
		arr[1] = "Ia = " + CircuitUtil.getCurrentText(ia);
		arr[2] = "Ig = " + CircuitUtil.getCurrentText(ig);
		arr[3] = "Vac = " + CircuitUtil.getVoltageText(vac);
		arr[4] = "Vag = " + CircuitUtil.getVoltageText(vag);
		arr[5] = "Vgc = " + CircuitUtil.getVoltageText(vgc);
	}

	@Override
	int getInternalNodeCount() {
		return 1;
	}

	@Override
	Point getPost(int n) {
		return (n == 0) ? point1 : (n == 1) ? point2 : gate[1];
	}

	@Override
	int getPostCount() {
		return 3;
	}

	@Override
	double getPower() {
		return (volts[anode] - volts[gnode]) * ia + (volts[cnode] - volts[gnode]) * ic;
	}

	@Override
	boolean nonLinear() {
		return true;
	}

	@Override
	void reset() {
		volts[anode] = volts[cnode] = volts[gnode] = 0;
		diode.reset();
		lastvag = lastvac = curcount_a = curcount_c = curcount_g = 0;
	}

	void setDefaults() {
		cresistance = 50;
		holdingI = .0082;
		triggerI = .01;
	}

	@Override
	public void setEditValue(int n, EditInfo ei) {
		if (n == 0 && ei.value > 0)
			triggerI = ei.value;
		if (n == 1 && ei.value > 0)
			holdingI = ei.value;
		if (n == 2 && ei.value > 0)
			cresistance = ei.value;
	}

	@Override
	void setPoints() {
		super.setPoints();
		int dir = 0;
		if (abs(dx) > abs(dy)) {
			dir = -CircuitUtil.sign(dx) * CircuitUtil.sign(dy);
			point2.y = point1.y;
		} else {
			dir = CircuitUtil.sign(dy) * CircuitUtil.sign(dx);
			point2.x = point1.x;
		}
		if (dir == 0)
			dir = 1;
		calcLeads(16);
		cathode = newPointArray(2);
		Point pa[] = newPointArray(2);
		CircuitUtil.interpPoint2(lead1, lead2, pa[0], pa[1], 0, hs);
		CircuitUtil.interpPoint2(lead1, lead2, cathode[0], cathode[1], 1, hs);
		poly = CircuitUtil.createPolygon(pa[0], pa[1], lead2);

		gate = newPointArray(2);
		double leadlen = (dn - 16) / 2;
		int gatelen = sim.gridSize;
		gatelen += leadlen % sim.gridSize;
		if (leadlen < gatelen) {
			x2 = x;
			y2 = y;
			return;
		}
		CircuitUtil.interpPoint(lead2, point2, gate[0], gatelen / leadlen, gatelen * dir);
		CircuitUtil.interpPoint(lead2, point2, gate[1], gatelen / leadlen, sim.gridSize * 2 * dir);
	}

	void setup() {
		diode = new Diode(sim);
		diode.setup(.8, 0);
	}

	@Override
	void stamp() {
		sim.stampNonLinear(nodes[anode]);
		sim.stampNonLinear(nodes[cnode]);
		sim.stampNonLinear(nodes[gnode]);
		sim.stampNonLinear(nodes[inode]);
		sim.stampResistor(nodes[gnode], nodes[cnode], cresistance);
		diode.stamp(nodes[inode], nodes[gnode]);
	}
}
