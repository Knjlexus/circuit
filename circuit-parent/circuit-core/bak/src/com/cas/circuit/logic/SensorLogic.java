package com.cas.circuit.logic;

import java.io.IOException;

import com.cas.circuit.BaseElectricCompLogic;
import com.cas.circuit.vo.Magnetism;
import com.cas.circuit.vo.SensorIO;
import com.cas.robot.common.Dispatcher;
import com.jme3.collision.CollisionResults;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.util.clone.Cloner;
import com.jme3.util.clone.JmeCloneable;

public abstract class SensorLogic extends BaseElectricCompLogic implements Control, JmeCloneable {
	protected boolean enabled = false; // 默认不启用control的功能
	protected Spatial spatial;
	protected SensorIO sensorIO;
	protected Node rootNode;
	protected Ray ray;
	protected float limit;

	protected CollisionResults results = new CollisionResults();
	protected Spatial start;
	protected Spatial end;

	public SensorLogic() {
		super();
		rootNode = Dispatcher.getIns().getMainApp().getRootNode();
	}

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);

		Magnetism magnetism = elecComp.getDef().getMagnetisms().iterator().next();
		if (magnetism == null) {
			log.error("传感器配置有问题");
		}
		sensorIO = magnetism.getSensorIOs().iterator().next();
		if (sensorIO == null) {
			log.error("传感器配置有问题");
		}

		elecCompMdl.addControl(this);

		try {
			start = elecCompMdl.getChild("start");
			end = elecCompMdl.getChild("end");

			Vector3f origin = start.getWorldTranslation();
			Vector3f goal = end.getWorldTranslation();
			limit = FastMath.abs(goal.distance(origin)); // spatial.getUserData("limit");
			// 标准化方向，保证与射线碰撞点的距离计算正确
			Vector3f direction = goal.add(origin.negate()).normalize();
			ray = new Ray(origin, direction);
			ray.setLimit(limit);

//			// FIXME 创建Line
//			Line line = new Line(origin, goal);
//			line.setLineWidth(1);
//			final Geometry geometry = new Geometry("Line", line);
//			JmeUtil.setTranslucent(geometry, true);
//			Material material = new Material(Dispatcher.getIns().getMainApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
//			material.setColor("Color", ColorRGBA.Green);
//			geometry.setMaterial(material);
//			Dispatcher.getIns().getMainApp().enqueue(new Runnable() {
//				@Override
//				public void run() {
//					rootNode.attachChild(geometry);
//				}
//			});
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void setSpatial(Spatial spatial) {
		if (this.spatial != null && spatial != null && spatial != this.spatial) {
			throw new IllegalStateException("This control has already been added to a Spatial");
		}
		this.spatial = spatial;
	}

	public Spatial getSpatial() {
		return spatial;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * To be implemented in subclass.
	 */
	protected abstract void controlUpdate(float tpf);

	/**
	 * To be implemented in subclass.
	 */
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}

	/**
	 * Default implementation of cloneForSpatial() that simply clones the control and sets the spatial.
	 * 
	 * <pre>
	 * AbstractControl c = clone();
	 * c.spatial = null;
	 * c.setSpatial(spatial);
	 * </pre>
	 *
	 * Controls that wish to be persisted must be Cloneable.
	 */
	@Override
	public Control cloneForSpatial(Spatial spatial) {
		try {
			AbstractControl c = (AbstractControl) clone();
//			c.spatial = null; // to keep setSpatial() from throwing an exception
			c.setSpatial(spatial);
			return c;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Can't clone control for spatial", e);
		}
	}

	@Override
	public Object jmeClone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Can't clone control for spatial", e);
		}
	}

	@Override
	public void cloneFields(Cloner cloner, Object original) {
		this.spatial = cloner.clone(spatial);
	}

	public void update(float tpf) {
		if (!enabled) return;

		controlUpdate(tpf);
	}

	public void render(RenderManager rm, ViewPort vp) {
		if (!enabled) return;

		controlRender(rm, vp);
	}

	public void write(JmeExporter ex) throws IOException {
		OutputCapsule oc = ex.getCapsule(this);
		oc.write(enabled, "enabled", true);
		oc.write(spatial, "spatial", null);
	}

	public void read(JmeImporter im) throws IOException {
		InputCapsule ic = im.getCapsule(this);
		enabled = ic.readBoolean("enabled", true);
		spatial = (Spatial) ic.readSavable("spatial", null);
	}

}
