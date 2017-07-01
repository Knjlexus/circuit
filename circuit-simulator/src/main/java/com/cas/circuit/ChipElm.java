package com.cas.circuit;

import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

import com.cas.circuit.util.CircuitUtil;

abstract class ChipElm extends CircuitElm {
	class Pin {
		Point post, stub;

		Point textloc;
		int pos, side, voltSource, bubbleX, bubbleY;
		String text;
		boolean lineOver, bubble, clock, output, value, state;
		double curcount, current;

		Pin(int p, int s, String t) {
			pos = p;
			side = s;
			text = t;
		}

		void setPoint(int px, int py, int dx, int dy, int dax, int day, int sx, int sy) {
			if ((flags & FLAG_FLIP_X) != 0) {
				dx = -dx;
				dax = -dax;
				px += cspc2 * (sizeX - 1);
				sx = -sx;
			}
			if ((flags & FLAG_FLIP_Y) != 0) {
				dy = -dy;
				day = -day;
				py += cspc2 * (sizeY - 1);
				sy = -sy;
			}
			int xa = px + cspc2 * dx * pos + sx;
			int ya = py + cspc2 * dy * pos + sy;
			post = new Point(xa + dax * cspc2, ya + day * cspc2);
			stub = new Point(xa + dax * cspc, ya + day * cspc);
			textloc = new Point(xa, ya);
			if (bubble) {
				bubbleX = xa + dax * 10 * csize;
				bubbleY = ya + day * 10 * csize;
			}
			if (clock) {
				clockPointsX = new int[3];
				clockPointsY = new int[3];
				clockPointsX[0] = xa + dax * cspc - dx * cspc / 2;
				clockPointsY[0] = ya + day * cspc - dy * cspc / 2;
				clockPointsX[1] = xa;
				clockPointsY[1] = ya;
				clockPointsX[2] = xa + dax * cspc + dx * cspc / 2;
				clockPointsY[2] = ya + day * cspc + dy * cspc / 2;
			}
		}
	}

	int csize, cspc, cspc2;
	int bits;
	final int FLAG_SMALL = 1;
	final int FLAG_FLIP_X = 1024;

	final int FLAG_FLIP_Y = 2048;

	int rectPointsX[], rectPointsY[];

	int clockPointsX[], clockPointsY[];

	Pin pins[];

	int sizeX, sizeY;

	boolean lastClock;

	final int SIDE_N = 0;

	final int SIDE_S = 1;
	final int SIDE_W = 2;
	final int SIDE_E = 3;

	public ChipElm(int xx, int yy) {
		super(xx, yy);
		if (needsBits())
			bits = (this instanceof DecadeElm) ? 10 : 4;
		noDiagonal = true;
		setupPins();
		setSize(sim.smallGridCheckItem.getState() ? 1 : 2);
	}

	public ChipElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		if (needsBits())
			bits = new Integer(st.nextToken()).intValue();
		noDiagonal = true;
		setupPins();
		setSize((f & FLAG_SMALL) != 0 ? 1 : 2);
		int i;
		for (i = 0; i != getPostCount(); i++) {
			if (pins[i].state) {
				volts[i] = new Double(st.nextToken()).doubleValue();
				pins[i].value = volts[i] > 2.5;
			}
		}
	}

	@Override
	void doStep() {
		int i;
		for (i = 0; i != getPostCount(); i++) {
			Pin p = pins[i];
			if (!p.output)
				p.value = volts[i] > 2.5;
		}
		execute();
		for (i = 0; i != getPostCount(); i++) {
			Pin p = pins[i];
			if (p.output)
				sim.updateVoltageSource(0, nodes[i], p.voltSource, p.value ? 5 : 0);
		}
	}

	@Override
	void drag(int xx, int yy) {
		yy = sim.snapGrid(yy);
		if (xx < x) {
			xx = x;
			yy = y;
		} else {
			y = y2 = yy;
			x2 = sim.snapGrid(xx);
		}
		setPoints();
	}

	@Override
	void draw(Graphics g) {
		drawChip(g);
	}

	void drawChip(Graphics g) {
		Font f = new Font("SansSerif", 0, 10 * csize);
		g.setFont(f);
		FontMetrics fm = g.getFontMetrics();
		for (int i = 0; i != getPostCount(); i++) {
			Pin p = pins[i];
			setVoltageColor(g, volts[i]);
			Point a = p.post;
			Point b = p.stub;
			CircuitUtil.drawThickLine(g, a, b);
			p.curcount = updateDotCount(p.current, p.curcount);
			drawDots(g, b, a, p.curcount);
			if (p.bubble) {
				g.setColor(sim.printableCheckItem.getState() ? Color.white : Color.black);
				CircuitUtil.drawThickCircle(g, p.bubbleX, p.bubbleY, 1);
				g.setColor(lightGrayColor);
				CircuitUtil.drawThickCircle(g, p.bubbleX, p.bubbleY, 3);
			}
			g.setColor(whiteColor);
			int sw = fm.stringWidth(p.text);
			g.drawString(p.text, p.textloc.x - sw / 2, p.textloc.y + fm.getAscent() / 2);
			if (p.lineOver) {
				int ya = p.textloc.y - fm.getAscent() / 2;
				g.drawLine(p.textloc.x - sw / 2, ya, p.textloc.x + sw / 2, ya);
			}
		}
		g.setColor(needsHighlight() ? selectColor : lightGrayColor);
		CircuitUtil.drawThickPolygon(g, rectPointsX, rectPointsY, 4);
		if (clockPointsX != null) {
			g.drawPolyline(clockPointsX, clockPointsY, 3);
		}
		for (int i = 0; i != getPostCount(); i++) {
			drawPost(g, pins[i].post.x, pins[i].post.y, nodes[i]);
		}
	}

	@Override
	String dump() {
//		int t = getDumpType();
		String s = super.dump();
		if (needsBits()) {
			s += " " + bits;
		}
		for (int i = 0; i != getPostCount(); i++) {
			if (pins[i].state) {
				s += " " + volts[i];
			}
		}
		return s;
	}

	void execute() {
	}

	String getChipName() {
		return "chip";
	}

	@Override
	boolean getConnection(int n1, int n2) {
		return false;
	}

	@Override
	public EditInfo getEditInfo(int n) {
		if (n == 0) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Flip X", (flags & FLAG_FLIP_X) != 0);
			return ei;
		}
		if (n == 1) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Flip Y", (flags & FLAG_FLIP_Y) != 0);
			return ei;
		}
		return null;
	}

	@Override
	void getInfo(String arr[]) {
		arr[0] = getChipName();
		int i, a = 1;
		for (i = 0; i != getPostCount(); i++) {
			Pin p = pins[i];
			if (arr[a] != null)
				arr[a] += "; ";
			else
				arr[a] = "";
			String t = p.text;
			if (p.lineOver)
				t += '\'';
			if (p.clock)
				t = "Clk";
			arr[a] += t + " = " + CircuitUtil.getVoltageText(volts[i]);
			if (i % 2 == 1)
				a++;
		}
	}

	@Override
	Point getPost(int n) {
		return pins[n].post;
	}

	@Override
	abstract int getVoltageSourceCount(); // output count

	@Override
	boolean hasGroundConnection(int n1) {
		return pins[n1].output;
	}

	boolean needsBits() {
		return false;
	}

	@Override
	void reset() {
		int i;
		for (i = 0; i != getPostCount(); i++) {
			pins[i].value = false;
			pins[i].curcount = 0;
			volts[i] = 0;
		}
		lastClock = false;
	}

	@Override
	void setCurrent(int x, double c) {
		int i;
		for (i = 0; i != getPostCount(); i++)
			if (pins[i].output && pins[i].voltSource == x)
				pins[i].current = c;
	}

	@Override
	public void setEditValue(int n, EditInfo ei) {
		if (n == 0) {
			if (ei.checkbox.getState())
				flags |= FLAG_FLIP_X;
			else
				flags &= ~FLAG_FLIP_X;
			setPoints();
		}
		if (n == 1) {
			if (ei.checkbox.getState())
				flags |= FLAG_FLIP_Y;
			else
				flags &= ~FLAG_FLIP_Y;
			setPoints();
		}
	}

	@Override
	void setPoints() {
		if (x2 - x > sizeX * cspc2 && this == sim.dragElm)
			setSize(2);
//		int hs = cspc;
		int x0 = x + cspc2;
		int y0 = y;
		int xr = x0 - cspc;
		int yr = y0 - cspc;
		int xs = sizeX * cspc2;
		int ys = sizeY * cspc2;
		rectPointsX = new int[] { xr, xr + xs, xr + xs, xr };
		rectPointsY = new int[] { yr, yr, yr + ys, yr + ys };
		setBbox(xr, yr, rectPointsX[2], rectPointsY[2]);
		int i;
		for (i = 0; i != getPostCount(); i++) {
			Pin p = pins[i];
			switch (p.side) {
			case SIDE_N:
				p.setPoint(x0, y0, 1, 0, 0, -1, 0, 0);
				break;
			case SIDE_S:
				p.setPoint(x0, y0, 1, 0, 0, 1, 0, ys - cspc2);
				break;
			case SIDE_W:
				p.setPoint(x0, y0, 0, 1, -1, 0, 0, 0);
				break;
			case SIDE_E:
				p.setPoint(x0, y0, 0, 1, 1, 0, xs - cspc2, 0);
				break;
			}
		}
	}

	void setSize(int s) {
		csize = s;
		cspc = 8 * s;
		cspc2 = cspc * 2;
		flags &= ~FLAG_SMALL;
		flags |= (s == 1) ? FLAG_SMALL : 0;
	}

	/**
	 * 设置元件的管脚
	 */
	abstract void setupPins();

	@Override
	protected void setVoltageSource(int j, int vs) {
		for (int i = 0; i != getPostCount(); i++) {
			Pin p = pins[i];
			if (p.output && j-- == 0) {
				p.voltSource = vs;
				return;
			}
		}
		System.out.println("setVoltageSource failed for " + this);
	}

	@Override
	void stamp() {
		int i;
		for (i = 0; i != getPostCount(); i++) {
			Pin p = pins[i];
			if (p.output)
				sim.stampVoltageSource(0, nodes[i], p.voltSource);
		}
	}
}
