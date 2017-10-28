package com.cas.circuit.logic.sensor;

import com.cas.circuit.Voltage;
import com.cas.circuit.logic.SensorLogic;
import com.cas.circuit.util.MesureResult;
import com.cas.circuit.util.R;
import com.cas.circuit.vo.Jack;
import com.cas.circuit.vo.Terminal;
import com.cas.robot.common.Dispatcher;
import com.jme3.collision.CollisionResult;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

/**
 * 光纤传感器（E3Z-NA11、E3X-ZD11）<br>
 * <br>
 * 传感器发出射线碰撞到物体且物体不为白色则传感器跳变为0，反之传感器跳变为1
 */
public class FiberSensorLogic extends SensorLogic {

	private Terminal terminal1;
	private Terminal terminal2;

	public FiberSensorLogic() {
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
		} else if (enabled && !tmp) {
			enabled = false;
		}
	}

	@Override
	protected void controlUpdate(float tpf) {
//		Vector3f origin = start.getWorldTranslation();
//		Vector3f goal = end.getWorldTranslation();
//		// 标准化方向，保证与射线碰撞点的距离计算正确
//		Vector3f direction = goal.add(origin.negate()).normalize();
		ray.setOrigin(start.getWorldTranslation());
//		ray.setDirection(direction);

		results.clear();
		rootNode.collideWith(ray, results);
		// 无工件输出24V
		if (results.size() == 0) {
			sensorIO.doSwitch(1);
			return;
		}
		boolean hasSignal = false;
		for (CollisionResult result : results) {
			if (result.getDistance() > 2 * limit) {
				continue;
			}
			Geometry geometry = result.getGeometry();
			if (elecCompMdl.hasChild(geometry)) {
				continue;
			}
			if (!"workpiece".equals(geometry.getUserData("type"))) {
				continue;
			}
			String color = geometry.getUserData("color"); // 工件颜色：黑色 和 白色
			if (result.getDistance() <= limit || "white".equals(color)) {
				hasSignal = true;
				break;
			}
		}
		sensorIO.doSwitch(hasSignal ? 1 : 0);
	}

}
