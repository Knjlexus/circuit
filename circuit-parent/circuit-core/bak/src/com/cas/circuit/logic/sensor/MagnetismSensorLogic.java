package com.cas.circuit.logic.sensor;

import com.cas.circuit.logic.SensorLogic;
import com.cas.util.Util;
import com.jme3.collision.CollisionResult;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

/**
 * 磁性开关（D-C73、D-Z73、D-A93）<br>
 * <br>
 * 当传感器发出射线接触到磁性物体时，传感器跳变为1，反之传感器跳变为0
 */
public class MagnetismSensorLogic extends SensorLogic {

	public MagnetismSensorLogic() {
		enabled = true;
	}

	@Override
	protected void controlUpdate(float tpf) {
		Vector3f origin = start.getWorldTranslation();
		Vector3f goal = end.getWorldTranslation();
//		// 标准化方向，保证与射线碰撞点的距离计算正确
		Vector3f direction = goal.add(origin.negate()).normalize();
		ray.setOrigin(start.getWorldTranslation());
		ray.setDirection(direction);

		results.clear();
		rootNode.collideWith(ray, results);
		boolean magnet = false;
		for (CollisionResult result : results) {
			if (result.getDistance() > limit) {
				continue;
			}
			Geometry geometry = result.getGeometry();
			if (Util.isEmpty(geometry.getUserData("magnet"))) {
				continue;
			}
			magnet = true;
//			System.out.println(spatial.getName() + "射线遇到磁性物体" + geometry.getName() + "!");
			break;
		}
		sensorIO.doSwitch(magnet ? 1 : 0);
	}

}
