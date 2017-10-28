package com.cas.circuit.logic.sensor;

import com.cas.circuit.Voltage;
import com.cas.circuit.logic.SensorLogic;
import com.cas.circuit.util.MesureResult;
import com.cas.circuit.util.R;
import com.cas.circuit.vo.Jack;
import com.cas.circuit.vo.Terminal;
import com.cas.robot.common.Dispatcher;
import com.jme3.collision.CollisionResult;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;

/**
 * 电感式接近开关（ C-MPXSC-2-12S）<br>
 * <br>
 * 当传感器发出射线接触到金属物体时，传感器跳变为1，反之传感器跳变为0
 */
public class InductanceLogic extends SensorLogic {

	private Terminal terminal1;
	private Terminal terminal2;

	public InductanceLogic() {
		rootNode = Dispatcher.getIns().getMainApp().getRootNode();
	}

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);
		Jack jack = elecComp.getDef().getJackMap().get(elecComp.getDef().getPO().getModel());
		terminal1 = jack.getStitch().get("1");
		terminal2 = jack.getStitch().get("2");

		if (terminal1 == null) {
			throw new RuntimeException(elecComp + "中没能找到连接头 1");
		}
		if (terminal2 == null) {
			throw new RuntimeException(elecComp + "中没能找到连接头 2");
		}
	}

	@Override
	protected void onReceivedLocal(Terminal terminal) {
		super.onReceivedLocal(terminal);
		MesureResult result = R.matchRequiredVolt(Voltage.IS_DC, terminal1, terminal2, 24, 2f);

		boolean tmp = result != null;

		if (!enabled && tmp) {
			enabled = true;
			System.err.println("InductanceLogic 开始工作啦。。。。");
		} else if (enabled && !tmp) {
			enabled = false;
			System.err.println("InductanceLogic 没法工作。。。。");
		}
	}

	@Override
	protected void controlUpdate(float tpf) {
		Vector3f origin = start.getWorldTranslation();
		Vector3f goal = end.getWorldTranslation();
		// 标准化方向，保证与射线碰撞点的距离计算正确
		Vector3f direction = goal.add(origin.negate()).normalize();
		ray.setOrigin(origin);
		ray.setDirection(direction);

		results.clear();
		rootNode.collideWith(ray, results);
		int index = 0;
		for (CollisionResult result : results) {
			if (result.getDistance() > limit) {
				continue;
			}
			Geometry geometry = result.getGeometry();
			if (geometry.getCullHint() == CullHint.Always) {
				continue;
			}
			if ("metal".equals(geometry.getUserData("material"))) {// 工件材质：Nylon，Metal
				index = 1;
				break;
			}
		}
		sensorIO.doSwitch(index);
	}

}
