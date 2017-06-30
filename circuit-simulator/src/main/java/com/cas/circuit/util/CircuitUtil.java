package com.cas.circuit.util;

import static java.lang.Math.PI;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.text.NumberFormat;

import com.cas.circuit.CirSim;

public class CircuitUtil {
	public static NumberFormat showFormat, shortFormat, noCommaFormat;

	static {
		showFormat = NumberFormat.getInstance();
		showFormat.setMaximumFractionDigits(2);
		shortFormat = NumberFormat.getInstance();
		shortFormat.setMaximumFractionDigits(1);
		noCommaFormat = NumberFormat.getInstance();
		noCommaFormat.setMaximumFractionDigits(10);
		noCommaFormat.setGroupingUsed(false);
	}

	public static String getCurrentDText(double i) {
		return getUnitText(Math.abs(i), "A");
	}

	public static String getCurrentText(double i) {
		return getUnitText(i, "A");
	}

	public static String getShortUnitText(double v, String u) {
		double va = Math.abs(v);
		if (va < 1e-13)
			return null;
		if (va < 1e-9)
			return shortFormat.format(v * 1e12) + "p" + u;
		if (va < 1e-6)
			return shortFormat.format(v * 1e9) + "n" + u;
		if (va < 1e-3)
			return shortFormat.format(v * 1e6) + CirSim.muString + u;
		if (va < 1)
			return shortFormat.format(v * 1e3) + "m" + u;
		if (va < 1e3)
			return shortFormat.format(v) + u;
		if (va < 1e6)
			return shortFormat.format(v * 1e-3) + "k" + u;
		if (va < 1e9)
			return shortFormat.format(v * 1e-6) + "M" + u;
		return shortFormat.format(v * 1e-9) + "G" + u;
	}

	public static String getUnitText(double v, String u) {
		double va = Math.abs(v);
		if (va < 1e-14)
			return "0 " + u;
		if (va < 1e-9)
			return showFormat.format(v * 1e12) + " p" + u;
		if (va < 1e-6)
			return showFormat.format(v * 1e9) + " n" + u;
		if (va < 1e-3)
			return showFormat.format(v * 1e6) + " " + CirSim.muString + u;
		if (va < 1)
			return showFormat.format(v * 1e3) + " m" + u;
		if (va < 1e3)
			return showFormat.format(v) + " " + u;
		if (va < 1e6)
			return showFormat.format(v * 1e-3) + " k" + u;
		if (va < 1e9)
			return showFormat.format(v * 1e-6) + " M" + u;
		return showFormat.format(v * 1e-9) + " G" + u;
	}

	public static String getVoltageDText(double v) {
		return getUnitText(Math.abs(v), "V");
	}

	public static String getVoltageText(double v) {
		return getUnitText(v, "V");
	}

	// public static int abs(int x) {
	// return x < 0 ? -x : x;
	// }

	// static int max(int a, int b) {
	// return (a > b) ? a : b;
	// }

	// static int min(int a, int b) {
	// return (a < b) ? a : b;
	// }

	public static int sign(int x) {
		return (x < 0) ? -1 : (x == 0) ? 0 : 1;
	}

	public static double distance(Point p1, Point p2) {
		double x = p1.x - p2.x;
		double y = p1.y - p2.y;
		return Math.sqrt(x * x + y * y);
	}

	public static boolean comparePair(int x1, int x2, int y1, int y2) {
		return ((x1 == y1 && x2 == y2) || (x1 == y2 && x2 == y1));
	}

	public static void drawThickCircle(Graphics g, int cx, int cy, int ri) {
		int a;
		double m = PI / 180;
		double r = ri * .98;
		for (a = 0; a != 360; a += 20) {
			double ax = Math.cos(a * m) * r + cx;
			double ay = Math.sin(a * m) * r + cy;
			double bx = Math.cos((a + 20) * m) * r + cx;
			double by = Math.sin((a + 20) * m) * r + cy;
			drawThickLine(g, (int) ax, (int) ay, (int) bx, (int) by);
		}
	}

	public static void drawThickLine(Graphics g, int x, int y, int x2, int y2) {
		g.drawLine(x, y, x2, y2);
		g.drawLine(x + 1, y, x2 + 1, y2);
		g.drawLine(x, y + 1, x2, y2 + 1);
		g.drawLine(x + 1, y + 1, x2 + 1, y2 + 1);
	}

	public static void drawThickLine(Graphics g, Point pa, Point pb) {
		g.drawLine(pa.x, pa.y, pb.x, pb.y);
		g.drawLine(pa.x + 1, pa.y, pb.x + 1, pb.y);
		g.drawLine(pa.x, pa.y + 1, pb.x, pb.y + 1);
		g.drawLine(pa.x + 1, pa.y + 1, pb.x + 1, pb.y + 1);
	}

	public static void drawThickPolygon(Graphics g, int xs[], int ys[], int c) {
		int i;
		for (i = 0; i != c - 1; i++)
			drawThickLine(g, xs[i], ys[i], xs[i + 1], ys[i + 1]);
		drawThickLine(g, xs[i], ys[i], xs[0], ys[0]);
	}

	public static void drawThickPolygon(Graphics g, Polygon p) {
		drawThickPolygon(g, p.xpoints, p.ypoints, p.npoints);
	}

	public static Polygon createPolygon(Point a[]) {
		Polygon p = new Polygon();
		int i;
		for (i = 0; i != a.length; i++)
			p.addPoint(a[i].x, a[i].y);
		return p;
	}

	public static Polygon createPolygon(Point a, Point b, Point c) {
		Polygon p = new Polygon();
		p.addPoint(a.x, a.y);
		p.addPoint(b.x, b.y);
		p.addPoint(c.x, c.y);
		return p;
	}

	public static Polygon createPolygon(Point a, Point b, Point c, Point d) {
		Polygon p = new Polygon();
		p.addPoint(a.x, a.y);
		p.addPoint(b.x, b.y);
		p.addPoint(c.x, c.y);
		p.addPoint(d.x, d.y);
		return p;
	}

	public static Point interpPoint(Point a, Point b, double f) {
		Point p = new Point();
		interpPoint(a, b, p, f);
		return p;
	}

	public static Point interpPoint(Point a, Point b, double f, double g) {
		Point p = new Point();
		interpPoint(a, b, p, f, g);
		return p;
	}

	public static void interpPoint(Point a, Point b, Point c, double f) {
		int xpd = b.x - a.x;
		int ypd = b.y - a.y;
		/*
		 * double q = (a.x*(1-f)+b.x*f+.48); System.out.println(q + " " + (int)
		 * q);
		 */
		c.x = (int) Math.floor(a.x * (1 - f) + b.x * f + .48);
		c.y = (int) Math.floor(a.y * (1 - f) + b.y * f + .48);
	}

	public static void interpPoint(Point a, Point b, Point c, double f, double g) {
		int xpd = b.x - a.x;
		int ypd = b.y - a.y;
		int gx = b.y - a.y;
		int gy = a.x - b.x;
		g /= Math.sqrt(gx * gx + gy * gy);
		c.x = (int) Math.floor(a.x * (1 - f) + b.x * f + g * gx + .48);
		c.y = (int) Math.floor(a.y * (1 - f) + b.y * f + g * gy + .48);
	}

	public static void interpPoint2(Point a, Point b, Point c, Point d, double f, double g) {
		int xpd = b.x - a.x;
		int ypd = b.y - a.y;
		int gx = b.y - a.y;
		int gy = a.x - b.x;
		g /= Math.sqrt(gx * gx + gy * gy);
		c.x = (int) Math.floor(a.x * (1 - f) + b.x * f + g * gx + .48);
		c.y = (int) Math.floor(a.y * (1 - f) + b.y * f + g * gy + .48);
		d.x = (int) Math.floor(a.x * (1 - f) + b.x * f - g * gx + .48);
		d.y = (int) Math.floor(a.y * (1 - f) + b.y * f - g * gy + .48);
	}

}
