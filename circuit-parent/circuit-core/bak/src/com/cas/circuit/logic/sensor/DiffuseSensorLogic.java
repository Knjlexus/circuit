package com.cas.circuit.logic.sensor;

import com.cas.circuit.Voltage;
import com.cas.circuit.logic.SensorLogic;
import com.cas.circuit.util.MesureResult;
import com.cas.circuit.util.R;
import com.cas.circuit.vo.Jack;
import com.cas.circuit.vo.Terminal;
import com.cas.util.Util;
import com.jme3.collision.CollisionResult;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

/**
 * 漫射型光电传感器（CX-441、MHT15-N2317） <br>
 * 光电反射式传感器（E3Z-LS61） <br>
 * 光电式对射传感器（GE6-N1111，实际情况中配合GS6-D1311） <br>
 * 电容式传感器（CLG5-1K） <br>
 * 红外线光电开关（SB03-1K）<br>
 * <br>
 * 只要元器件发出射线碰撞到障碍物就切换通断（碰撞到透明物体则视为无障碍物）
 */
public class DiffuseSensorLogic extends SensorLogic {

	private Terminal terminal1;
	private Terminal terminal2;

	public DiffuseSensorLogic() {
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
		MesureResult result = R.matchRequiredVolt(Voltage.IS_DC, terminal1, terminal2, 24, 2f);

		boolean tmp = result != null;

		if (!enabled && tmp) {
			enabled = true;
			System.err.println("DiffuseSensorLogic 开始工作啦。。。。");
		} else if (enabled && !tmp) {
			enabled = false;
			System.err.println("DiffuseSensorLogic 没法工作。。。。");
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
		boolean collision = false;
		for (CollisionResult collisionResult : results) {
			// 碰撞模型之间距离在射线限制之内
			if (collisionResult.getDistance() > limit) {
				continue;
			}
			Geometry geometry = collisionResult.getGeometry();
			if (elecCompMdl.hasChild(geometry)) {
				continue;
			}
//			FIXME 使用UserData具体的值判断
			if (Util.notEmpty(geometry.getUserData("translucent"))) {
				continue;
			}
			collision = true;
			break;
		}
		sensorIO.doSwitch(collision ? 1 : 0);
	}

}
