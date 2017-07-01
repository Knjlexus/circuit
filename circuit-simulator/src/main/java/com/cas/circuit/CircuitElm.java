package com.cas.circuit;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

import com.cas.circuit.util.CircuitUtil;

/**
 * 电路元件的基类
 * @author admin
 */
public abstract class CircuitElm implements Editable {
	static double voltageRange = 5;
//	彩色数
	public static final int COLOR_SCALE_COUNT = 32;

	static Color colorScale[];
	static double currentMult, powerMult;
	static Point ps1, ps2;
	static CirSim sim;
	static Color whiteColor, selectColor, lightGrayColor;
	static Font unitsFont;

	public static void initClass(CirSim s) {
		unitsFont = new Font("SansSerif", 0, 10);
		sim = s;

		colorScale = new Color[COLOR_SCALE_COUNT];
		for (int i = 0; i != COLOR_SCALE_COUNT; i++) {
			double v = i * 2. / COLOR_SCALE_COUNT - 1;
			if (v < 0) {
				int n1 = (int) (128 * -v) + 127;
				int n2 = (int) (127 * (1 + v));
				colorScale[i] = new Color(n1, n2, n2);
			} else {
				int n1 = (int) (128 * v) + 127;
				int n2 = (int) (127 * (1 - v));
				colorScale[i] = new Color(n2, n1, n2);
			}
			System.out.println("CircuitElm.initClass()" +colorScale[i]);
		}

		ps1 = new Point();
		ps2 = new Point();
	}

	int x, y, x2, y2, flags, nodes[], voltSource;

	int dx, dy, dsign;

	double dn, dpx1, dpy1;

	Point point1, point2, lead1, lead2;

	double volts[];

	double current, curcount;

	Rectangle boundingBox;

	// noDiagonal
	// n. 斜线; [数] 对角线; 斜列; 斜纹布;
	// adj. 对角线的; 斜的; 斜线的; 斜纹的;
	boolean noDiagonal;

	public boolean selected;

	CircuitElm(int xx, int yy) {
		x = x2 = xx;
		y = y2 = yy;
		flags = getDefaultFlags();
		allocNodes();
		initBoundingBox();
	}

	CircuitElm(int xa, int ya, int xb, int yb, int f) {
		x = xa;
		y = ya;
		x2 = xb;
		y2 = yb;
		flags = f;
		allocNodes();
		initBoundingBox();
	}

	protected void adjustBbox(int x1, int y1, int x2, int y2) {
		if (x1 > x2) {
			int q = x1;
			x1 = x2;
			x2 = q;
		}
		if (y1 > y2) {
			int q = y1;
			y1 = y2;
			y2 = q;
		}
		x1 = min(boundingBox.x, x1);
		y1 = min(boundingBox.y, y1);
		x2 = max(boundingBox.x + boundingBox.width - 1, x2);
		y2 = max(boundingBox.y + boundingBox.height - 1, y2);
		boundingBox.setBounds(x1, y1, x2 - x1, y2 - y1);
	}

	protected void adjustBbox(Point p1, Point p2) {
		adjustBbox(p1.x, p1.y, p2.x, p2.y);
	}

	protected void allocNodes() {
		nodes = new int[getPostCount() + getInternalNodeCount()];
		volts = new double[getPostCount() + getInternalNodeCount()];
	}

//	determine if moving this element by (dx,dy) will put it on top of another element
	public boolean allowMove(int dx, int dy) {
		int nx = x + dx;
		int ny = y + dy;
		int nx2 = x2 + dx;
		int ny2 = y2 + dy;
		for (int i = 0; i != sim.elmList.size(); i++) {
			CircuitElm ce = sim.getElm(i);
			if (ce.x == nx && ce.y == ny && ce.x2 == nx2 && ce.y2 == ny2) {
				return false;
			}
			if (ce.x == nx2 && ce.y == ny2 && ce.x2 == nx && ce.y2 == ny) {
				return false;
			}
		}
		return true;
	}

	protected Polygon calcArrow(Point a, Point b, double al, double aw) {
		Polygon poly = new Polygon();
		Point p1 = new Point();
		Point p2 = new Point();
		int adx = b.x - a.x;
		int ady = b.y - a.y;
		double l = Math.sqrt(adx * adx + ady * ady);
		poly.addPoint(b.x, b.y);
		CircuitUtil.interpPoint2(a, b, p1, p2, 1 - al / l, aw);
		poly.addPoint(p1.x, p1.y);
		poly.addPoint(p2.x, p2.y);
		return poly;
	}

	protected Polygon calcArrowReverse(Point a, Point b, double al, double aw) {
		Polygon poly = new Polygon();
//		Point p1 = new Point();
//		Point p2 = new Point();
		double adx = b.x - a.x;
		double ady = b.y - a.y;
		double l = Math.sqrt(adx * adx + ady * ady);
		if (l > 0) {
			adx /= l;
			ady /= l;
			double bdx = -ady; // orthogonal unit vector
			double bdy = adx; //
			poly.addPoint((int) Math.round(b.x + 1 - adx * al), (int) Math.round(b.y + 1 - ady * al));
			poly.addPoint((int) Math.round(b.x + 1 - bdx * al), (int) Math.round(b.y + 1 - bdy * aw));
			poly.addPoint((int) Math.round(b.x + 1 + bdx * al), (int) Math.round(b.y + 1 + bdy * aw));
		}
		return poly;
	}

	protected void calcLeads(int len) {
		if (dn < len || len == 0) {
			lead1 = point1;
			lead2 = point2;
		} else {
			lead1 = CircuitUtil.interpPoint(point1, point2, (dn - len) / (2 * dn));
			lead2 = CircuitUtil.interpPoint(point1, point2, (dn + len) / (2 * dn));
		}
	}

	void calculateCurrent() {
	}

	protected boolean canViewInScope() {
		return getPostCount() <= 2;
	}

	void delete() {
	}

	void doAdjust() {
	}

	void doStep() {
	}

	void drag(int xx, int yy) {
		xx = sim.snapGrid(xx);
		yy = sim.snapGrid(yy);
		if (noDiagonal) {
			if (abs(x - xx) < abs(y - yy)) {
				xx = x;
			} else {
				yy = y;
			}
		}
		x2 = xx;
		y2 = yy;
		setPoints();
	}

	void draw(Graphics g) {
	}

	protected void draw2Leads(Graphics g) {
		// draw first lead
		setVoltageColor(g, volts[0]);
		CircuitUtil.drawThickLine(g, point1, lead1);

		// draw second lead
		setVoltageColor(g, volts[1]);
		CircuitUtil.drawThickLine(g, lead2, point2);
	}

	protected void drawCenteredText(Graphics g, String s, int x, int y, boolean cx) {
		FontMetrics fm = g.getFontMetrics();
		int w = fm.stringWidth(s);
		if (cx) {
			x -= w / 2;
		}
		g.drawString(s, x, y + fm.getAscent() / 2);
		adjustBbox(x, y - fm.getAscent() / 2, x + w, y + fm.getAscent() / 2 + fm.getDescent());
	}

	protected void drawCoil(Graphics g, int hs, Point p1, Point p2, double v1, double v2) {
//		double len = CircuitUtil.distance(p1, p2);
		int segments = 30; // 10*(int) (len/10);
		double segf = 1. / segments;

		ps1.setLocation(p1);
		for (int i = 0; i != segments; i++) {
			double cx = (((i + 1) * 6. * segf) % 2) - 1;
			double hsx = Math.sqrt(1 - cx * cx);
			if (hsx < 0) {
				hsx = -hsx;
			}
			CircuitUtil.interpPoint(p1, p2, ps2, i * segf, hsx * hs);
			double v = v1 + (v2 - v1) * i / segments;
			setVoltageColor(g, v);
			CircuitUtil.drawThickLine(g, ps1, ps2);
			ps1.setLocation(ps2);
		}
	}

	protected void doDots(Graphics g) {
		updateDotCount();
		if (sim.dragElm != this) {
			drawDots(g, point1, point2, curcount);
		}
	}

	protected void drawDots(Graphics g, Point pa, Point pb, double pos) {
		if (sim.stoppedCheck.getState() || pos == 0 || !sim.dotsCheckItem.getState()) return;
		int dx = pb.x - pa.x;
		int dy = pb.y - pa.y;
		double dn = Math.sqrt(dx * dx + dy * dy);
		g.setColor(Color.yellow);
		int ds = 16;
		pos %= ds;
		if (pos < 0) {
			pos += ds;
		}
		double di = 0;
		for (di = pos; di < dn; di += ds) {
			int x0 = (int) (pa.x + di * dx / dn);
			int y0 = (int) (pa.y + di * dy / dn);
			g.fillRect(x0 - 1, y0 - 1, 4, 4);
		}
	}

	protected void drawPost(Graphics g, int x0, int y0) {
		g.setColor(whiteColor);
		g.fillOval(x0 - 3, y0 - 3, 7, 7);
	}

	protected void drawPost(Graphics g, int x0, int y0, int n) {
		if (sim.dragElm == null && !needsHighlight() && sim.getCircuitNode(n).links.size() == 2) {
			return;
		}
		if (sim.mouseMode == CirSim.MODE_DRAG_ROW || sim.mouseMode == CirSim.MODE_DRAG_COLUMN) {
			return;
		}
		drawPost(g, x0, y0);
	}

	protected void drawPosts(Graphics g) {
		for (int i = 0; i != getPostCount(); i++) {
			Point p = getPost(i);
			drawPost(g, p.x, p.y, nodes[i]);
		}
	}

	protected void drawValues(Graphics g, String s, double hs) {
		if (s == null) {
			return;
		}
		g.setFont(unitsFont);
		FontMetrics fm = g.getFontMetrics();
		int w = fm.stringWidth(s);
		g.setColor(whiteColor);
		int ya = fm.getAscent() / 2;
		int xc, yc;
		if (this instanceof RailElm || this instanceof SweepElm) {
			xc = x2;
			yc = y2;
		} else {
			xc = (x2 + x) / 2;
			yc = (y2 + y) / 2;
		}
		int dpx = (int) (dpx1 * hs);
		int dpy = (int) (dpy1 * hs);
		if (dpx == 0) {
			g.drawString(s, xc - w / 2, yc - abs(dpy) - 2);
		} else {
			int xx = xc + abs(dpx) + 2;
			if (this instanceof VoltageElm || (x < x2 && y > y2)) {
				xx = xc - (w + abs(dpx) + 2);
			}
			g.drawString(s, xx, yc + dpy + ya);
		}
	}

	String dump() {
		int t = getDumpType();
		return (t < 127 ? ((char) t) + " " : t + " ") + x + " " + y + " " + x2 + " " + y2 + " " + flags;
	}

	int getBasicInfo(String arr[]) {
		arr[1] = "I = " + CircuitUtil.getCurrentDText(getCurrent());
		arr[2] = "Vd = " + CircuitUtil.getVoltageDText(getVoltageDiff());
		return 3;
	}

	Rectangle getBoundingBox() {
		return boundingBox;
	}

	boolean getConnection(int n1, int n2) {
		return true;
	}

	double getCurrent() {
		return current;
	}

	int getDefaultFlags() {
		return 0;
	}

	protected Class<?> getDumpClass() {
		return getClass();
	}

	int getDumpType() {
		return 0;
	}

	@Override
	public EditInfo getEditInfo(int n) {
		return null;
	}

	void getInfo(String arr[]) {
	}

	int getInternalNodeCount() {
		return 0;
	}

	int getNode(int n) {
		return nodes[n];
	}

	Point getPost(int n) {
		return (n == 0) ? point1 : (n == 1) ? point2 : null;
	}

	/**
	 * Post : 桩。 像是引脚的意思， 比如三极管是3个桩，普通的就2个
	 * @return
	 */
	int getPostCount() {
		return 2;
	}

	double getPostVoltage(int x) {
		return volts[x];
	}

	double getPower() {
		return getVoltageDiff() * current;
	}

	String getScopeUnits(int x) {
		return (x == 1) ? "W" : "V";
	}

	double getScopeValue(int x) {
		return (x == 1) ? getPower() : getVoltageDiff();
	}

	int getShortcut() {
		return 0;
	}

	// 电压差
	double getVoltageDiff() {
		return volts[0] - volts[1];
	}

	int getVoltageSource() {
		return voltSource;
	}

	int getVoltageSourceCount() {
		return 0;
	}

	boolean hasGroundConnection(int n1) {
		return false;
	}

	void initBoundingBox() {
		boundingBox = new Rectangle();
		boundingBox.setBounds(min(x, x2), min(y, y2), abs(x2 - x) + 1, abs(y2 - y) + 1);
	}

	boolean isCenteredText() {
		return false;
	}

	boolean isGraphicElmt() {
		return false;
	}

	boolean isSelected() {
		return selected;
	}

	boolean isWire() {
		return false;
	}

	void move(int dx, int dy) {
		x += dx;
		y += dy;
		x2 += dx;
		y2 += dy;
		boundingBox.move(dx, dy);
		setPoints();
	}

	void movePoint(int n, int dx, int dy) {
		if (n == 0) {
			x += dx;
			y += dy;
		} else {
			x2 += dx;
			y2 += dy;
		}
		setPoints();
	}

	boolean needsHighlight() {
		return sim.mouseElm == this || selected;
	}

	boolean needsShortcut() {
		return getShortcut() > 0;
	}

	Point[] newPointArray(int n) {
		Point a[] = new Point[n];
		while (n > 0) {
			a[--n] = new Point();
		}
		return a;
	}

	/**
	 * 非线性元件，返回true：表示是“非线性元件”， false表示是“线性元件”
	 * @return
	 */
	boolean nonLinear() {
		return false;
	}

	void reset() {
		for (int i = 0; i != getPostCount() + getInternalNodeCount(); i++) {
			volts[i] = 0;
		}
		curcount = 0;
	}

	void selectRect(Rectangle r) {
		selected = r.intersects(boundingBox);
	}

	void setBbox(int x1, int y1, int x2, int y2) {
		if (x1 > x2) {
			int q = x1;
			x1 = x2;
			x2 = q;
		}
		if (y1 > y2) {
			int q = y1;
			y1 = y2;
			y2 = q;
		}
		boundingBox.setBounds(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
	}

	void setBbox(Point p1, Point p2, double w) {
		setBbox(p1.x, p1.y, p2.x, p2.y);
//		int gx = p2.y - p1.y;
//		int gy = p1.x - p2.x;
		int dpx = (int) (dpx1 * w);
		int dpy = (int) (dpy1 * w);
		adjustBbox(p1.x + dpx, p1.y + dpy, p1.x - dpx, p1.y - dpy);
	}

	void setConductanceColor(Graphics g, double w0) {
		w0 *= powerMult;
		double w = (w0 < 0) ? -w0 : w0;
		if (w > 1) {
			w = 1;
		}
		int rg = (int) (w * 255);
		g.setColor(new Color(rg, rg, rg));
	}

	void setCurrent(int x, double c) {
		current = c;
	}

	@Override
	public void setEditValue(int n, EditInfo ei) {
	}

	void setNode(int p, int n) {
		nodes[p] = n;
	}

	void setNodeVoltage(int n, double c) {
		volts[n] = c;
		calculateCurrent();
	}

	void setPoints() {
		dx = x2 - x;
		dy = y2 - y;
		dn = Math.sqrt(dx * dx + dy * dy);
		dpx1 = dy / dn;
		dpy1 = -dx / dn;
		dsign = (dy == 0) ? CircuitUtil.sign(dx) : CircuitUtil.sign(dy);
		point1 = new Point(x, y);
		point2 = new Point(x2, y2);
	}

	void setPowerColor(Graphics g, boolean yellow) {

//		if (conductanceCheckItem.getState()) {
//			setConductanceColor(g, current / getVoltageDiff());
//			return;
//		}

		if (!sim.powerCheckItem.getState()) {
			return;
		}

		setPowerColor(g, getPower());
	}

	void setPowerColor(Graphics g, double w0) {
		w0 *= powerMult;
		double w = (w0 < 0) ? -w0 : w0;
		if (w > 1) {
			w = 1;
		}
		int rg = 128 + (int) (w * 127);
		int b = (int) (128 * (1 - w));

//		if (yellow) g.setColor(new Color(rg, rg, b)); else

		if (w0 > 0) {
			g.setColor(new Color(rg, b, b));
		} else {
			g.setColor(new Color(b, rg, b));
		}
	}

	protected void setSelected(boolean x) {
		selected = x;
	}

	void setupAdjust() {
	}

	protected void setVoltageColor(Graphics g, double volts) {
		if (needsHighlight()) {
			g.setColor(selectColor);
			return;
		}
		if (!sim.voltsCheckItem.getState()) {
			if (!sim.powerCheckItem.getState()) {// && !conductanceCheckItem.getState())
				g.setColor(whiteColor);
			}
			return;
		}
		int c = (int) ((volts + voltageRange) * (COLOR_SCALE_COUNT - 1) / (voltageRange * 2));
		if (c < 0) {
			c = 0;
		}
		if (c >= COLOR_SCALE_COUNT) {
			c = COLOR_SCALE_COUNT - 1;
		}
		g.setColor(colorScale[c]);
	}

	protected void setVoltageSource(int n, int v) {
		voltSource = v;
	}

	void stamp() {
	}

	/**
	 * 迭代、循环
	 */
	public void startIteration() {
	}

	protected void updateDotCount() {
		curcount = updateDotCount(current, curcount);
	}

	protected double updateDotCount(double cur, double cc) {
		if (sim.stoppedCheck.getState()) {
			return cc;
		}
		double cadd = cur * currentMult;
//		if (cur != 0 && cadd <= .05 && cadd >= -.05) cadd = (cadd < 0) ? -.05 : .05;
		cadd %= 8;
//		if (cadd > 8) cadd = 8; if (cadd < -8) cadd = -8;
		return cc + cadd;
	}
}
