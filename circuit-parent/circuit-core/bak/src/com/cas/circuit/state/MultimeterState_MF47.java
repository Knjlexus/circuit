/**
 * 
 */
package com.cas.circuit.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cas.circuit.Voltage;
import com.cas.circuit.control.PointerControl;
import com.cas.circuit.util.MesureResult;
import com.cas.circuit.util.R;
import com.cas.circuit.vo.Terminal;
import com.cas.robot.common.BaseAppState;
import com.cas.robot.common.MouseEvent;
import com.cas.robot.common.MouseEventAdapter;
import com.cas.robot.common.MouseEventState;
import com.cas.robot.common.consts.JmeConst;
import com.cas.robot.common.util.JmeUtil;
import com.cas.robot.common.util.MdlMapUtil;
import com.cas.util.Util;
import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.Light;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * @author DING 2015年8月8日 上午9:10:37
 */
public class MultimeterState_MF47 extends BaseAppState {

//	private static final String MULTIMETER_R = "multimeterR";
	private static final ColorRGBA MATCOLOR_RED = new ColorRGBA(0.6368628f, 0.0f, 0.0f, 1.0f);
	private static final ColorRGBA MATCOLOR_BLACK = new ColorRGBA(0.13011314f, 0.13011314f, 0.13011314f, 1.0f);

	// 表笔手中模型
	public static final String V_RED_PEN = "v_red_pen";
	public static final String V_BLACK_PEN = "v_black_pen";
	public static final String V_SHORT_PEN = "v_short_pen";
	private static final String SHORT_PEN = "short_pen";
	// 放到端子上的表笔模型
	public static final String C_RED_PEN = "c_red_pen";
	public static final String C_BLACK_PEN = "c_black_pen";
	public static final float MAX_PIAN_DEG = 95;
	public static final float MIN_PIAN_DEG = -5;
//	public static final String POINTER_MDLNAME = "pointer00";
//	public static final String XUANNIU_MDLNAME = "xuanniu";
//	public static final String WEITIAO_MDLNAME = "ADJ";
	public static final String DANG_OFF = "OFF";
	public static final String DANG_BATT = "BATT";
	public static final String DANG_DCMA_500 = "DCmA 500";
	public static final String DANG_DCMA_50 = "DCmA 50";
	public static final String DANG_DCMA_5 = "DCmA 5";
	public static final String DANG_DCMA_0DOT5 = "DCmA 0.5";
	public static final String DANG_DCV_0DOT25 = "DCV 0.25";
	public static final String DANG_DCV_1 = "DCV 1";
	public static final String DANG_DCV_2DOT5 = "DCV 2.5";
	public static final String DANG_DCV_10 = "DCV 10";
	public static final String DANG_DCV_50 = "DCV 50";
	public static final String DANG_DCV_250 = "DCV 250";
	public static final String DANG_DCV_500 = "DCV 500";
	public static final String DANG_DCV_1000 = "DCV 1000";
	public static final String DANG_ACV_1000 = "ACV 1000";
	public static final String DANG_ACV_500 = "ACV 500";
	public static final String DANG_ACV_250 = "ACV 250";
	public static final String DANG_ACV_50 = "ACV 50";
	public static final String DANG_ACV_10 = "ACV 10";
	public static final String DANG_Ω_X10K = "Ω x10k";
	public static final String DANG_Ω_X1K = "Ω x1k";
	public static final String DANG_Ω_X100 = "Ω x100";
	public static final String DANG_Ω_X10 = "Ω x10";
	public static final String DANG_Ω_X1 = "Ω x1";
	// 不同档位的R档，单位为欧姆
	public static List<String> dangNames = new ArrayList<String>();
	private static int nowDangIndex = 0;
	public static Map<Integer, Float> rDang = new HashMap<Integer, Float>();

	private static Terminal redPenTerm;
	private static Terminal blackPenTerm;
	private static float rWeiR1 = 3000;
//	private Spatial pointerMdl;
//	private Spatial xuanniuMdl;
//	private Spatial weitiaoMdl;
	private String now_pen_mdl_ref;
	// 当前表笔的模型
//	private Spatial penModel;
	private Node c_red_pen_mdl;
	private Node c_black_pen_mdl;
	private float nowPianDeg = 0;
	private R multimeterR;
	float wait;

	private Spatial multimeter;
	private Spatial pointerMdl;
	private Spatial mu_pen;
	private Spatial mu_x;
	private Node muNode;
//	XXX
	List<Light> lights = new ArrayList<Light>();
	// 计算公式:
	// R微 = (Rh-Rs)*R档/(Rh-Rs+R档)
	// I = E/(R0+R微+RX+R档)
	// (90°/10) = degRx / I针 → degRx= (90°/10) * I针
	static {
		dangNames.add(DANG_OFF); // 0
		dangNames.add(DANG_BATT); // 1
		dangNames.add(DANG_DCMA_500); // 2
		dangNames.add(DANG_DCMA_50); // 3
		dangNames.add(DANG_DCMA_5); // 4
		dangNames.add(DANG_DCMA_0DOT5); // 5
		dangNames.add(DANG_DCV_0DOT25); // 6
		dangNames.add(DANG_DCV_1); // 7
		dangNames.add(DANG_DCV_2DOT5); // 8
		dangNames.add(DANG_DCV_10); // 9
		dangNames.add(DANG_DCV_50); // 10
		dangNames.add(DANG_DCV_250); // 11
		dangNames.add(DANG_DCV_500); // 12
		dangNames.add(DANG_DCV_1000); // 13
		dangNames.add(DANG_ACV_1000); // 14
		dangNames.add(DANG_ACV_500); // 15
		dangNames.add(DANG_ACV_250); // 16
		dangNames.add(DANG_ACV_50); // 17
		dangNames.add(DANG_ACV_10); // 18
		dangNames.add(DANG_Ω_X10K); // 19
		dangNames.add(DANG_Ω_X1K); // 20
		dangNames.add(DANG_Ω_X100); // 21
		dangNames.add(DANG_Ω_X10); // 22
		dangNames.add(DANG_Ω_X1); // 23

		rDang.put(23, 15.3f);
		rDang.put(22, 165f);
		rDang.put(21, 1780f);
		rDang.put(20, 55400f);
	}

	public MultimeterState_MF47() {
	}

	@Override
	protected void initialize(Application app) {
		initMdls();
		initEvents();
//		loadHandPen();
		now_pen_mdl_ref = V_RED_PEN;
		registerInput();
		this.setEnabled(false);
		onDisable();
	}

	/**
	 * 
	 */
	private void initMdls() {
		muNode = (Node) MdlMapUtil.loadMdlWithAbbr("multimeter", assetManager);
//		muNode.scale(10);
//		muNode.setQueueBucket(Bucket.Translucent);
		mu_pen = muNode.getChild("mu_pen");
		multimeter = muNode.getChild("multimeter");
		mu_x = muNode.getChild("mu_x");
		pointerMdl = ((Node) multimeter).getChild("pointer00");
//		JmeUtil.transparent(mu_x, 0);
//					pointerMdl = ((Node) multimeter).getChild(POINTER_MDLNAME);
//					xuanniuMdl = ((Node) multimeter).getChild(XUANNIU_MDLNAME);
//					weitiaoMdl = ((Node) multimeter).getChild(WEITIAO_MDLNAME);
//					xuanniuMdl.rotate(0, 0, nowDangIndex * 360 * FastMath.DEG_TO_RAD / 24f);
//					加光  6个方向
//		ColorRGBA lightColor = new ColorRGBA(0.5f, 0.5f, 0.5f, 1);
//		lights.add(new DirectionalLight(new Vector3f(10, 0, 0), lightColor));
//		lights.add(new DirectionalLight(new Vector3f(-10, 0, 0), lightColor));
//		lights.add(new DirectionalLight(new Vector3f(0, 10, 0), lightColor));
//		lights.add(new DirectionalLight(new Vector3f(0, -10, 0), lightColor));
//		lights.add(new DirectionalLight(new Vector3f(0, 0, 10), lightColor));
//		lights.add(new DirectionalLight(new Vector3f(0, 0, -10), lightColor));
		for (Light light : lights) {
			muNode.addLight(light);
		}
		app.enqueue(new Runnable() {
			@Override
			public void run() {
				try {
					rootNode.attachChild(muNode);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		});
//		FirstPersonState state = stateManager.getState(FirstPersonState.class);
//		if (state != null) {
//		}
	}

	private void initEvents() {
		MouseEventState mouseEventState = stateManager.getState(MouseEventState.class);
		final Spatial xuanniuMdl = ((Node) multimeter).getChild("xuanniu");
		if (xuanniuMdl != null) {
			mouseEventState.addCandidate(xuanniuMdl, new MouseEventAdapter() {
				@Override
				public void mouseWheel(MouseEvent e) {
					super.mouseWheel(e);
//					System.out.println(e.getWheel());
					boolean wheelUp = (e.getWheel() == 0);
					xuanniuMdl.rotate(0, 0, (wheelUp ? -1 : 1) * 360 * FastMath.DEG_TO_RAD / 24f);
//					JmeUtil.rotateModel(xuanniuMdl, JmeConst.AXIS_Z, (e.getWheel() == 0 ? -1 : 1) * 360 / 24f);
					if (wheelUp && nowDangIndex == 0) {
						nowDangIndex = dangNames.size() - 1;
					} else if (!wheelUp && nowDangIndex == dangNames.size() - 1) {
						nowDangIndex = 0;
					} else {
						nowDangIndex = nowDangIndex + (wheelUp ? -1 : 1);
					}
					refreshMultimeter();
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					super.mouseEntered(e);
//					TODO
					JmeUtil.setSpatialHighLight(e.getSpatial(), JmeConst.YELLOW);
//					JmeUtil.select(e.getSpatial());
				}

				@Override
				public void mouseExited(MouseEvent e) {
					super.mouseExited(e);
//					TODO
					JmeUtil.setSpatialHighLight(e.getSpatial(), ColorRGBA.BlackNoAlpha);
//					JmeUtil.unselect(e.getSpatial());
				}
			});
		}
		final Spatial weitiaoMdl = ((Node) multimeter).getChild("ADJ");
		if (weitiaoMdl != null) {
			mouseEventState.addCandidate(weitiaoMdl, new MouseEventAdapter() {

				@Override
				public void mouseWheel(MouseEvent e) {
					super.mouseWheel(e);
					boolean wheelUp = (e.getWheel() == 1);
					if (rWeiR1 < 0) {
						rWeiR1 = 0;
					} else if (rWeiR1 > 10000) {
						rWeiR1 = 10000;
					} else if (rWeiR1 > 0 && rWeiR1 < 10000) {
						rWeiR1 = rWeiR1 + (wheelUp ? 100 : -100);
						weitiaoMdl.rotate(0, 0, (wheelUp ? 1 : -1) * 27f * FastMath.DEG_TO_RAD / 24f);
						refreshMultimeter();
					} else if (rWeiR1 == 0 && wheelUp) {
						rWeiR1 = rWeiR1 + 100;
						refreshMultimeter();
					} else if (rWeiR1 == 10000 && !wheelUp) {
						rWeiR1 = rWeiR1 - 100;
						refreshMultimeter();
					}
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					super.mouseEntered(e);
					JmeUtil.setSpatialHighLight(e.getSpatial(), JmeConst.YELLOW);
//					JmeUtil.select(e.getSpatial());
				}

				@Override
				public void mouseExited(MouseEvent e) {
					super.mouseExited(e);
					JmeUtil.setSpatialHighLight(e.getSpatial(), ColorRGBA.BlackNoAlpha);
//					JmeUtil.unselect(e.getSpatial());
				}

			});
		}
	}

	/**
	 * 拿回红表笔
	 */
	private void getBackRedPen() {
		JmeUtil.detachModel(c_red_pen_mdl);
		redPenTerm = null;
		loadHandPen(V_RED_PEN);
		refreshMultimeter();
	}

	/**
	 * 拿回黑表笔
	 */
	private void getBackBlackPen() {
		JmeUtil.detachModel(c_black_pen_mdl);
		blackPenTerm = null;
		loadHandPen(V_BLACK_PEN);
		refreshMultimeter();
	}

	/**
	 * 将表笔放到端子上
	 * @param terminal
	 */
	public void putPenToTerminal(Terminal terminal, Spatial termMdl) {
		if (now_pen_mdl_ref == null || V_SHORT_PEN.equals(now_pen_mdl_ref)) {
			return;
		}
		MouseEventState mouseEventState = stateManager.getState(MouseEventState.class);
		// 如果当前拿着红表笔
		if (V_RED_PEN.equals(now_pen_mdl_ref)) {
			// 加载表笔模型
			c_red_pen_mdl = (Node) MdlMapUtil.loadMdlWithAbbr(C_RED_PEN, assetManager);
			((Node) termMdl).attachChild(c_red_pen_mdl);

			// 给表笔加监听
			mouseEventState.addCandidate(c_red_pen_mdl, new MouseEventAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);
					getBackRedPen();
				}
			});
			redPenTerm = terminal;
			if (blackPenTerm != null) {
				JmeUtil.transparent(mu_pen, 0);
				now_pen_mdl_ref = null;
			} else {
				loadHandPen(V_BLACK_PEN);
			}
			refreshMultimeter();
		} else if (V_BLACK_PEN.equals(now_pen_mdl_ref)) {
			c_black_pen_mdl = (Node) MdlMapUtil.loadMdlWithAbbr(C_BLACK_PEN, assetManager);
			((Node) termMdl).attachChild(c_black_pen_mdl);
			mouseEventState.addCandidate(c_black_pen_mdl, new MouseEventAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);
					getBackBlackPen();
				}

			});
			blackPenTerm = terminal;
			if (redPenTerm != null) {
				JmeUtil.transparent(mu_pen, 0);
				now_pen_mdl_ref = null;
			} else {
				loadHandPen(V_RED_PEN);
			}
			refreshMultimeter();
		}
	}

	/**
	 * 加载表笔并卸载原来的表笔
	 */
	private void loadHandPen(String pen_mdl_ref) {
		if (pen_mdl_ref.equals(now_pen_mdl_ref) || V_SHORT_PEN.equals(now_pen_mdl_ref)) {
			return;
		}
		JmeUtil.untransparent(mu_pen);
		now_pen_mdl_ref = pen_mdl_ref;
		Geometry body = (Geometry) ((Node) mu_pen).getChild("body");
		Material mat = body.getMaterial();
		if (V_RED_PEN.equals(pen_mdl_ref)) {
			mat.setColor("Diffuse", MATCOLOR_RED);
		} else if (V_BLACK_PEN.equals(pen_mdl_ref)) {
			mat.setColor("Diffuse", MATCOLOR_BLACK);
		}
	}

	/**
	 * 刷新万用表
	 */
	public void refreshMultimeter() {
		if (multimeterR != null) {
			multimeterR.shutPowerDown();
		}
		// 短接的逻辑
		if (V_SHORT_PEN.equals(now_pen_mdl_ref)) {
			detectResistance(0);
		} else if (redPenTerm != null && blackPenTerm != null) {
			switch (nowDangIndex) {
			case 6:
				detectDCVoltage(0.25f);
				break;
			case 7:
				detectDCVoltage(1);
				break;
			case 8:
				detectDCVoltage(2.5f);
				break;
			case 9:
				detectDCVoltage(10);
				break;
			case 10:
				detectDCVoltage(50);
				break;
			case 11:
				detectDCVoltage(250);
				break;
			case 12:
				detectDCVoltage(500);
				break;
			case 13:
				detectDCVoltage(1000);
				break;
			case 14:
				detectACVoltage(1000);
				break;
			case 15:
				detectACVoltage(500);
				break;
			case 16:
				detectACVoltage(250);
				break;
			case 17:
				detectACVoltage(50);
				break;
			case 18:
				detectACVoltage(10);
				break;
			case 19:
				multimeterR = R.create("multimeterR10.5", Voltage.IS_DC, blackPenTerm, redPenTerm, 10.5f);
				detectResistance(multimeterR.mesure(redPenTerm, blackPenTerm).getValue());
				break;
			case 20:
				multimeterR = R.create("multimeterR1.5", Voltage.IS_DC, blackPenTerm, redPenTerm, 1.5f);
				detectResistance(multimeterR.mesure(redPenTerm, blackPenTerm).getValue());
				break;
			case 21:
				multimeterR = R.create("multimeterR1.5", Voltage.IS_DC, blackPenTerm, redPenTerm, 1.5f);
				detectResistance(multimeterR.mesure(redPenTerm, blackPenTerm).getValue());
				break;
			case 22:
				multimeterR = R.create("multimeterR1.5", Voltage.IS_DC, blackPenTerm, redPenTerm, 1.5f);
				detectResistance(multimeterR.mesure(redPenTerm, blackPenTerm).getValue());
				break;
			case 23:
				multimeterR = R.create("multimeterR1.5", Voltage.IS_DC, blackPenTerm, redPenTerm, 1.5f);
				detectResistance(multimeterR.mesure(redPenTerm, blackPenTerm).getValue());
				break;
			default:
				pianPointer(0);
				break;
			}
		} else {
			pianPointer(0);
		}
	}

	/**
	 * 偏转表头
	 * @param ratio 想要偏转到的比例
	 */
	private void pianPointer(float ratio) {
		float thisPian = 90 * ratio - nowPianDeg;
		if (90 * ratio > MAX_PIAN_DEG) {
			// 如果是正向超量程
			thisPian = MAX_PIAN_DEG - nowPianDeg;
			nowPianDeg = MAX_PIAN_DEG;
		} else if (90 * ratio < MIN_PIAN_DEG) {
			// 如果是负向超量程
			thisPian = MIN_PIAN_DEG - nowPianDeg;
			nowPianDeg = MIN_PIAN_DEG;
		} else {
			nowPianDeg = 90 * ratio;
		}
		if (thisPian != 0) {
//			FIXME
//			rotatePoint(JmeConsts.AXIS_Z, thisPian, JmeConsts.POSITIVE);
			rotatePointer(JmeConst.AXIS_Z, thisPian);
		}
	}

//	private void rotatePoint(int axis, float deg, int direction) {
//		Spatial pointerMdl = ((Node) multimeter).getChild("pointer00");
//		RotateControl control = pointerMdl.getControl(RotateControl.class);
//		if (control == null) {
//			control = new RotateControl(axis, null);
//			pointerMdl.addControl(control);
//		}
//		control.rotate(direction, deg * FastMath.DEG_TO_RAD);
//	}

	/**
	 * 测电压
	 * @param maxVoltage 量程
	 */
	private void detectACVoltage(float maxVoltage) {
		MesureResult volt = R.mesureVoltage(redPenTerm, blackPenTerm);
//		System.out.println("MultimeterState_MF47.detectACVoltage() 电压值=" + volt[1] );
		if (volt != null && volt.getType() == Voltage.IS_AC) {
			pianPointer(volt.getVolt() / maxVoltage);
		} else {
			pianPointer(0);
		}
	}

	/**
	 * 测电压
	 * @param maxVoltage 量程
	 */
	private void detectDCVoltage(float maxVoltage) {
		MesureResult volt = R.mesureVoltage(redPenTerm, blackPenTerm);
//		System.out.println("MultimeterState_MF47.detectACVoltage() 电压值=" + volt[1] );
		if (volt != null && volt.getType() == Voltage.IS_DC) {
			pianPointer(volt.getVolt() / maxVoltage);
		} else {
			pianPointer(0);
		}
	}

	/**
	 * 测电阻
	 * @param rX 待测电阻值
	 */
	private void detectResistance(float rX) {
//		System.out.println("MultimeterState_MF47 测得电阻为: " + rX);
		if (nowDangIndex == 19) {// 电阻量程10k
			float circuitV = 10.5f;
			float circuitR = (rWeiR1 + 20000) * ((10000 - rWeiR1) + 2500) / (10000 + 2500 + 20000) + 141000 + 17300 + rX + 1;// 1欧姆是电源内阻
			float circuitI = circuitV / circuitR;// 干路电流
			float circuitIBiaoTou = circuitI * (rWeiR1 + 20000) / (10000 + 2500 + 20000);
			float manpianI = 0.0000462f;
			pianPointer(circuitIBiaoTou / manpianI);
		} else if (nowDangIndex == 20 || nowDangIndex == 21 || nowDangIndex == 22 || nowDangIndex == 23) {// 电阻量程1 -10k
			float circuitV = 1.5f;
			float circuitR1 = (rWeiR1 + 20000) * ((10000 - rWeiR1) + 2500) / (10000 + 2500 + 20000) + 17300;
			float circuitRDang = rDang.get(nowDangIndex);
			float circuitR = (circuitR1 * circuitRDang) / (circuitR1 + circuitRDang) + rX + 1;
			float circuitI = circuitV / circuitR;// 干路电流
			float circuitIBiaoTou = (circuitI * circuitRDang / (circuitR1 + circuitRDang)) * (rWeiR1 + 20000) / (10000 + 2500 + 20000);
			float manpianI = 0.0000462f;
			pianPointer(circuitIBiaoTou / manpianI);
		} else {
			pianPointer(0);
		}
	}

	/**
	 * 
	 */
	private void endShortRefresh() {
//		rotatePoint(JmeConsts.AXIS_Z, nowPianDeg, JmeConsts.NEGATIVE);
		rotatePointer(JmeConst.AXIS_Z, -nowPianDeg);
		nowPianDeg = 0;
	}

	/**
	 * 短接表笔
	 */
	private void shortCircuitHandPen(boolean isPressed) {
		if (!isEnabled()) {
			return;
		}
		if (redPenTerm == null && blackPenTerm == null) {
//			FirstPersonState state = stateManager.getState(FirstPersonState.class);
//			if (state != null) {
			if (isPressed) {
				this.now_pen_mdl_ref = V_SHORT_PEN;
//				penModel = MdlMapUtil.loadMdlWithAbbr(V_SHORT_PEN, assetManager);
				JmeUtil.untransparent(mu_x);
				JmeUtil.transparent(mu_pen, 0);
				// 短接的逻辑
				refreshMultimeter();
			} else {
				this.now_pen_mdl_ref = V_RED_PEN;
				JmeUtil.untransparent(mu_pen);
				JmeUtil.transparent(mu_x, 0);
//				penModel = MdlMapUtil.loadMdlWithAbbr(V_RED_PEN, assetManager);
				// 停止短接的逻辑
//					JmeUtil.rotateModel(pointerMdl, JmeConst.AXIS_Z, -90);
				// 短接的逻辑
				endShortRefresh();
			}
//				state.addHandModel(penModel);
		}
	}

	public void registerInput() {
		if (!inputManager.hasMapping(SHORT_PEN)) {
			inputManager.addMapping(SHORT_PEN, new KeyTrigger(KeyInput.KEY_X));
		}
		inputManager.addListener(new ActionListener() {
			@Override
			public void onAction(String name, boolean isPressed, float tpf) {
				if (name.equals(SHORT_PEN)) {
					shortCircuitHandPen(isPressed);
//					loadHandPen(V_BLACK_PEN);
				}
			}
		}, SHORT_PEN);
	}

	@Override
	public void stateAttached(AppStateManager stateManager) {
		super.stateAttached(stateManager);
	}

	@Override
	public void stateDetached(AppStateManager stateManager) {
		if (inputManager.hasMapping(SHORT_PEN)) {
			inputManager.deleteMapping(SHORT_PEN);
		}
		getBackRedPen();
		getBackBlackPen();
//		mouseEvent
		MouseEventState eventState = stateManager.getState(MouseEventState.class);
		Spatial xuanniuMdl = ((Node) multimeter).getChild("xuanniu");
		eventState.removeCandidate(xuanniuMdl);
		Spatial weitiaoMdl = ((Node) multimeter).getChild("ADJ");
		eventState.removeCandidate(weitiaoMdl);
//		control
		pointerMdl.removeControl(PointerControl.class);

		rootNode.detachChild(muNode);
	}

	@Override
	public void update(float tpf) {
		super.update(tpf);
		if (muNode != null) {
			muNode.setLocalTranslation(cam.getLocation());
			muNode.setLocalRotation(cam.getRotation());
		}
		wait += tpf;
		if (wait > 0.3f) {
			wait = 0;
			refreshMultimeter();
		}
	}

	@Override
	protected void cleanup(Application app) {
		// TODO Auto-generated method stub

	}

//	public static void main(String[] args) {
////		rWeiR1 = 0;
////		float circuitV = 10.5f;
////		float circuitR = (rWeiR1 + 20000) * ((10000 - rWeiR1) + 2500) / (10000 + 2500 + 20000) + 141000 + 17300;
////		float circuitI = circuitV / circuitR;
////		float circuitIBiaoTou = circuitI * (rWeiR1 + 20000) / (10000 + 2500 + 20000);
////
////		System.out.println(circuitIBiaoTou);
//		new SimpleApplication() {
//			@Override
//			public void simpleInitApp() {
////				模型的鼠标监听
//				MouseEventState.create(stateManager);
//				assetManager.registerLocator("H:\\eclipsex32\\robot-client\\assets", FileLocator.class);
//				flyCam.setEnabled(false);
//				flyCam.setMoveSpeed(20);
//				stateManager.attach(new MultimeterState_MF47());
//				viewPort.setBackgroundColor(ColorRGBA.Gray);
//			}
//		}.start();
//	}

	private void rotatePointer(final String axis, final float deltaDeg) {
		if (Util.isEmpty(pointerMdl)) {
			System.out.println("JmeUtil.rotateModel() null !");
			return;
		}
		PointerControl control = pointerMdl.getControl(PointerControl.class);
		if (control == null) {
			pointerMdl.addControl(control = new PointerControl());
		}
		control.setEnabled(true);
//		control.setExecutor(null, true);
//		control.setRotateSpeed(240);
		pointerMdl.setUserData(JmeConst.ROTATE_AXIS, axis);
		Object leastDeltaDegObj = pointerMdl.getUserData(JmeConst.ROTATE_ANGLE);
		if (leastDeltaDegObj == null) {
			pointerMdl.setUserData(JmeConst.ROTATE_ANGLE, deltaDeg);
		} else {
			Float leastDeltaDeg = (Float) leastDeltaDegObj;
			pointerMdl.setUserData(JmeConst.ROTATE_ANGLE, deltaDeg + leastDeltaDeg);
		}
	}

	@Override
	protected void onDisable() {
		super.onDisable();
//		记录原来模型的透明度
		getBackBlackPen();
		getBackRedPen();
		JmeUtil.untransparent(mu_x);
		JmeUtil.transparent(muNode, ColorRGBA.BlackNoAlpha, true);
		Spatial xuanniuMdl = ((Node) multimeter).getChild("xuanniu");
		MouseEventState.setToMouseVisible(xuanniuMdl, false);
		Spatial weitiaoMdl = ((Node) multimeter).getChild("ADJ");
		MouseEventState.setToMouseVisible(weitiaoMdl, false);
	}

	@Override
	protected void onEnable() {
		super.onEnable();
		JmeUtil.untransparent(muNode);
		JmeUtil.transparent(mu_x, 0);
		Spatial xuanniuMdl = ((Node) multimeter).getChild("xuanniu");
		MouseEventState.setToMouseVisible(xuanniuMdl, true);
		Spatial weitiaoMdl = ((Node) multimeter).getChild("ADJ");
		MouseEventState.setToMouseVisible(weitiaoMdl, true);
	}
}
