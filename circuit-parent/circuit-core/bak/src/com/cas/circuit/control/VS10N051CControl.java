package com.cas.circuit.control;

import com.cas.circuit.vo.ControlIO;
import com.cas.robot.common.Dispatcher;
import com.cas.robot.common.MouseEventState;
import com.cas.robot.common.consts.JmeConst;
import com.cas.robot.common.control.BaseAppControl;
import com.cas.robot.common.control.RotateControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class VS10N051CControl extends BaseAppControl {

	private boolean temp;
	private boolean collised;
	private Spatial collision;

	private Spatial start;
	private Spatial end;

	private Ray ray;

	private ControlIO button;

	private float limit;

	private RotateControl rotateControl;

	public VS10N051CControl(ControlIO button) {
		super(Dispatcher.getIns().getMainApp());
		this.button = button;
	}

	@Override
	public void setSpatial(Spatial spatial) {
		collision = ((Node) spatial).getChild("collision");
		collision.addControl(rotateControl = new RotateControl(JmeConst.AXIS_Y));

		start = ((Node) spatial).getChild("start");
		end = ((Node) spatial).getChild("end");

		Vector3f origin = start.getWorldTranslation();
		Vector3f goal = end.getWorldTranslation();

		limit = origin.distance(goal);

		ray = new Ray(origin, goal.add(origin.negate()).normalize());
		ray.setLimit(limit);
		
		super.setSpatial(spatial);
	}

	@Override
	protected void controlUpdate(float tpf) {
		// 监听碰撞
		CollisionResults results = new CollisionResults();
		rootNode.collideWith(ray, results);
		temp = false;
		for (CollisionResult result : results) {
			if (result.getDistance() > limit) {
				continue;
			}
			Geometry geometry = result.getGeometry();
			if (((Node) spatial).hasChild(geometry)) {
				continue;
			}
			temp = true;
		}

		if (collised != temp) {
			collised = temp;
			if (collised) {
				rotateControl.rotate(JmeConst.NEGATIVE, FastMath.DEG_TO_RAD * 10);
//				collision.rotate(0, FastMath.DEG_TO_RAD * -10, 0);
				button.doSwitch(1);
				// 被碰撞按下之后禁止鼠标监听
				MouseEventState.setToMouseVisible(collision, false);
			} else {
				rotateControl.rotate(JmeConst.POSITIVE, FastMath.DEG_TO_RAD * 10);
//				collision.rotate(0, FastMath.DEG_TO_RAD * 10, 0);
				button.doSwitch(0);
				// 离开碰撞松开之后启用鼠标监听
				MouseEventState.setToMouseVisible(collision, true);
			}
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}
}
