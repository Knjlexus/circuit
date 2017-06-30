package com.cas.circuit;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

import com.cas.circuit.util.CircuitUtil;

class JfetElm extends MosfetElm {
	Polygon gatePoly;

	Polygon arrowPoly;

	Point gatePt;

	JfetElm(int xx, int yy, boolean pnpflag) {
		super(xx, yy, pnpflag);
		noDiagonal = true;
	}

	public JfetElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
		super(xa, ya, xb, yb, f, st);
		noDiagonal = true;
	}

	@Override
	void draw(Graphics g) {
		setBbox(point1, point2, hs);
		setVoltageColor(g, volts[1]);
		CircuitUtil.drawThickLine(g, src[0], src[1]);
		CircuitUtil.drawThickLine(g, src[1], src[2]);
		setVoltageColor(g, volts[2]);
		CircuitUtil.drawThickLine(g, drn[0], drn[1]);
		CircuitUtil.drawThickLine(g, drn[1], drn[2]);
		setVoltageColor(g, volts[0]);
		CircuitUtil.drawThickLine(g, point1, gatePt);
		g.fillPolygon(arrowPoly);
		setPowerColor(g, true);
		g.fillPolygon(gatePoly);
		curcount = updateDotCount(-ids, curcount);
		if (curcount != 0) {
			drawDots(g, src[0], src[1], curcount);
			drawDots(g, src[1], src[2], curcount + 8);
			drawDots(g, drn[0], drn[1], -curcount);
			drawDots(g, drn[1], drn[2], -(curcount + 8));
		}
		drawPosts(g);
	}

	@Override
	double getBeta() {
		return .00125;
	}

	// these values are taken from Hayes+Horowitz p155
	@Override
	double getDefaultThreshold() {
		return -4;
	}

	@Override
	int getDumpType() {
		return 'j';
	}

	@Override
	void getInfo(String arr[]) {
		getFetInfo(arr, "JFET");
	}

	@Override
	void setPoints() {
		super.setPoints();

		// find the coordinates of the various points we need to draw
		// the JFET.
		int hs2 = hs * dsign;
		src = newPointArray(3);
		drn = newPointArray(3);
		CircuitUtil.interpPoint2(point1, point2, src[0], drn[0], 1, hs2);
		CircuitUtil.interpPoint2(point1, point2, src[1], drn[1], 1, hs2 / 2);
		CircuitUtil.interpPoint2(point1, point2, src[2], drn[2], 1 - 10 / dn, hs2 / 2);

		gatePt = CircuitUtil.interpPoint(point1, point2, 1 - 14 / dn);

		Point ra[] = newPointArray(4);
		CircuitUtil.interpPoint2(point1, point2, ra[0], ra[1], 1 - 13 / dn, hs);
		CircuitUtil.interpPoint2(point1, point2, ra[2], ra[3], 1 - 10 / dn, hs);
		gatePoly = CircuitUtil.createPolygon(ra[0], ra[1], ra[3], ra[2]);
		if (pnp == -1) {
			Point x = CircuitUtil.interpPoint(gatePt, point1, 18 / dn);
			arrowPoly = calcArrow(gatePt, x, 8, 3);
		} else
			arrowPoly = calcArrow(point1, gatePt, 8, 3);
	}
}
