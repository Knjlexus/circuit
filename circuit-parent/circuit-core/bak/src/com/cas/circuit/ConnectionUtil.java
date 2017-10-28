package com.cas.circuit;

import java.util.List;

import org.apache.log4j.Logger;

import com.cas.circuit.vo.Format;
import com.cas.circuit.vo.Terminal;
import com.cas.gas.vo.GasPort;
import com.cas.robot.common.Dispatcher;
import com.cas.util.Util;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public final class ConnectionUtil {
	protected static final Logger log = Logger.getLogger(ConnectionUtil.class);
	/**
	 * 插头接入方向-X轴
	 */
	public static final String AXIS_X = "X";
	/**
	 * 插头接入方向-Y轴
	 */
	public static final String AXIS_Y = "Y";
	/**
	 * 插头接入方向-Z轴
	 */
	public static final String AXIS_Z = "Z";
	/**
	 * 端子接线点的轴方向-X轴正方向
	 */
	public static final String POSITIVE_AXIS_X = "X+";
	/**
	 * 端子接线点的轴方向-Y轴正方向
	 */
	public static final String POSITIVE_AXIS_Y = "Y+";
	/**
	 * 端子接线点的轴方向-Z轴正方向
	 */
	public static final String POSITIVE_AXIS_Z = "Z+";
	/**
	 * 端子接线点的轴方向-X轴负方向
	 */
	public static final String NEGATIVE_AXIS_X = "X-";
	/**
	 * 端子接线点的轴方向-Y轴负方向
	 */
	public static final String NEGATIVE_AXIS_Y = "Y-";
	/**
	 * 端子接线点的轴方向-Z轴负方向
	 */
	public static final String NEGATIVE_AXIS_Z = "Z-";
	/**
	 * 一个端子上接线头绕轴旋转的最大度数-最小度数
	 */
	public static final int ANGLE_RANGE = 150;
	/**
	 * 端子在中心偏移的距离
	 */
	public static final float DISTANCE_RANGE = 0.0015f;

	public static final int CONNECT_NOEN = 0;
	public static final int CONNECT_PIPE = 1;
	public static final int CONNECT_WIRE = 2;
	public static final int CONNECT_CABLE = 3;

	public static final String NO_lINKER = "请先选择接线使用的导线、气管或线缆！";
	public static final String JUST_WIRE = "当前端子只允许使用导线接线！";
	public static final String JUST_PIPE = "当前气口只允许使用气管连接！";
	public static final String JUST_CABLE = "当前接口只允许使用线缆连接！";
	public static final String CONNECTING_CANNOT_CHANGE = "正在接线中，不可切换！";
	public static final String JACK_ALREADY_IN_USED = "插座已被占用！";
	public static final String SELECT_ANY_WIRE_IN_CABLE = "请先选择线缆上任意一条导线！";
	public static final String WIRE_IN_CABLE_LINKED_ALREADY = "该导线连接头已经连接";
	public static final String TERMINAL_LIMITED = "该端子可连接数量已满！";
	public static final String SAME_LINKER = "请注意，不要将导线两端连接在同一个端子上！";
	public static final String GASPORT_LIMITED = "该气口已被其他气管占用！";

	public static final String MISMATCHING_OWN = "本地文件夹中存档拥有者与当前登陆人不符，程序已自动覆盖本地存档！";
	
	/**
	 * 添加接线头
	 * @return 新添加的接线头模型
	 */
	public static Spatial getWireTerm() {
		AssetManager assetManager = Dispatcher.getIns().getMainApp().getAssetManager();
		Spatial spatial = assetManager.loadModel("Models/JieXianDuanZi.j3o");

//		创建号码管贴图
		Spatial parent = ((Node) spatial).getChild("Label");
		BitmapFont font = assetManager.loadFont("com/cas/me/fonts/numtext.fnt");
		BitmapText txt1 = getBitmapText(font);
		BitmapText txt2 = getBitmapText(font);
		BitmapText txt3 = getBitmapText(font);
		BitmapText txt4 = getBitmapText(font);
		txt1.setLocalTranslation(new Vector3f(1.3148814f, -1.6f, -1.1656954f));
		txt1.setLocalRotation(new Quaternion(0.8827012f, 0.85739714f, 0.003711f, -0.023661964f));
		txt2.setLocalTranslation(new Vector3f(-1.4674674f, -1.6f, 1.0630512f));
		txt2.setLocalRotation(new Quaternion(-0.0127319945f, -0.0059094937f, -0.8755521f, -0.8649177f));
		txt3.setLocalTranslation(new Vector3f(-1.0670044f, -1.6f, -1.4330314f));
		txt3.setLocalRotation(new Quaternion(-0.60574f, -0.61861926f, 0.6301226f, 0.60679734f));
		txt4.setLocalTranslation(new Vector3f(1.0652375f, -1.6f, 1.4834414f));
		txt4.setLocalRotation(new Quaternion(0.6125322f, 0.62533164f, 0.608011f, 0.6155914f));
		((Node) parent).attachChild(txt1);
		((Node) parent).attachChild(txt2);
		((Node) parent).attachChild(txt3);
		((Node) parent).attachChild(txt4);

//		spatial.setLocalScale(0.001f);
		return spatial;
	}

	private static BitmapText getBitmapText(BitmapFont font) {
		final BitmapText bitmapText = new BitmapText(font);
		bitmapText.setQueueBucket(Bucket.Transparent);
		bitmapText.setLocalScale(0.048106655f);
		bitmapText.setColor(ColorRGBA.Black);
		bitmapText.setName("Text");
		return bitmapText;

	}

	/**
	 * 添加气管头模型
	 * @return
	 */
	public static Spatial getPipeTerm() {
		AssetManager assetManager = Dispatcher.getIns().getMainApp().getAssetManager();
		Spatial spatial = assetManager.loadModel("Models/Qiguan.j3o");
		return spatial;
	}

	/**
	 * 添加插头模型
	 * @return
	 */
	public static Spatial getJackTerm(Format format) {
		AssetManager assetManager = Dispatcher.getIns().getMainApp().getAssetManager();
		Spatial spatial = assetManager.loadModel("Models/Chatou.j3o");
		if (format == null) {
			return spatial;
		}
		String mdlRef = format.getPO().getMdlRef();
		if (Util.notEmpty(mdlRef)) {
			spatial = assetManager.loadModel(mdlRef);
		}
		return spatial;
	}

	/**
	 * 处理导线连接头的位置
	 * @param terms
	 * @param terminal
	 */
	public static void moveTerms(List<Spatial> terms, Terminal terminal) {
		if (terms == null) {
			return;
		}
		int limitNum = terminal.getNum();
		// 获得接线头总数
		int num = terms.size();
		if (num > 2) {
			log.warn("端子上连接头的数量不可能超过2个！");
			return;
		}
		String axis = terminal.getDirection();
		for (int i = 0; i < terms.size(); i++) {
			Spatial term = terms.get(i);
			term.setLocalTranslation(terminal.getModel().getLocalTranslation());
			term.setLocalRotation(new Quaternion());
			String direction = null;
			if (POSITIVE_AXIS_Y.equalsIgnoreCase(axis)) {
				term.rotate(0, 0, 0);
				direction = AXIS_Y;
			} else if (NEGATIVE_AXIS_Y.equalsIgnoreCase(axis)) {
				term.rotate(0, 0, FastMath.PI);
				direction = AXIS_Y;
			} else if (POSITIVE_AXIS_X.equalsIgnoreCase(axis)) {
				term.rotate(0, 0, -FastMath.HALF_PI);
				direction = AXIS_X;
			} else if (NEGATIVE_AXIS_X.equalsIgnoreCase(axis)) {
				term.rotate(0, 0, FastMath.HALF_PI);
				direction = AXIS_X;
			} else if (POSITIVE_AXIS_Z.equalsIgnoreCase(axis)) {
				term.rotate(FastMath.HALF_PI, 0, 0);
				direction = AXIS_Z;
			} else if (NEGATIVE_AXIS_Z.equalsIgnoreCase(axis)) {
				term.rotate(-FastMath.HALF_PI, 0, 0);
				direction = AXIS_Z;
			}
			if (limitNum == 1) {
				return;
			}
			float move = DISTANCE_RANGE;
			if (i % 2 == 1) {
				move = DISTANCE_RANGE * -1;
			}
			if (AXIS_X.equalsIgnoreCase(direction)) {
				term.move(0, 0, move);
			} else if (AXIS_Y.equalsIgnoreCase(direction)) {
				term.move(0, 0, move);
			} else if (AXIS_Z.equalsIgnoreCase(direction)) {
				term.move(move, 0, 0);
			}
		}
	}

	/**
	 * 选装气管方向
	 * @param terms
	 * @param gasPort
	 */
	public static void rotatePipe(List<Spatial> terms, GasPort gasPort) {
		if (terms == null) {
			return;
		}
		Spatial term = terms.get(0);
		String axis = gasPort.getDirection();
		term.setLocalTranslation(gasPort.getModel().getLocalTranslation());
		term.setLocalRotation(new Quaternion());
		if (POSITIVE_AXIS_Y.equalsIgnoreCase(axis)) {
			term.rotate(0, 0, 0);
		} else if (NEGATIVE_AXIS_Y.equalsIgnoreCase(axis)) {
			term.rotate(0, 0, FastMath.PI);
		} else if (POSITIVE_AXIS_X.equalsIgnoreCase(axis)) {
			term.rotate(0, 0, -FastMath.HALF_PI);
		} else if (NEGATIVE_AXIS_X.equalsIgnoreCase(axis)) {
			term.rotate(0, 0, FastMath.HALF_PI);
		} else if (POSITIVE_AXIS_Z.equalsIgnoreCase(axis)) {
			term.rotate(FastMath.HALF_PI, 0, 0);
		} else if (NEGATIVE_AXIS_Z.equalsIgnoreCase(axis)) {
			term.rotate(-FastMath.HALF_PI, 0, 0);
		}
	}

	/**
	 * 旋转插头
	 * @param termMdls
	 * @param rotate
	 * @param jackDirection
	 */
	public static void rotateJack(List<Spatial> termMdls, float[] rotate, String jackDirection) {
		if (termMdls == null || termMdls.size() == 0) {
			return;
		}
		Spatial termMdl = termMdls.get(0);
		if (AXIS_X.equalsIgnoreCase(jackDirection)) {
			termMdl.rotate(0, 0, -FastMath.HALF_PI);
		} else if (AXIS_Z.equalsIgnoreCase(jackDirection)) {
			termMdl.rotate(-FastMath.HALF_PI, FastMath.HALF_PI, 0);
		} else if (!Util.isEmpty(rotate)) {
			termMdl.rotate(rotate[0], rotate[1], rotate[2]);
		}
	}
}
