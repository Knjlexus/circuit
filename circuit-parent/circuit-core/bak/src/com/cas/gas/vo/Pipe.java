package com.cas.gas.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cas.circuit.ILinkTarget;
import com.cas.circuit.ILinker;
import com.cas.gas.po.PipePO;
import com.cas.gas.util.G;
import com.cas.util.vo.BaseVO;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;

/**
 * 气管信息封装对象
 * @param <T>
 * @功能 Pipe.java
 * @作者 CWJ
 * @创建日期 2016年5月18日
 * @修改人 CWJ
 */
public class Pipe extends BaseVO<PipePO> implements ILinker {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8513048041397321242L;

//	气管连接头
	private GasPort port1;
	private GasPort port2;
//	气管模型
//	private List<Spatial> model = new ArrayList<Spatial>();
	private Map<Spatial, ILinkTarget> models = new HashMap<Spatial, ILinkTarget>();
	// 气管直径（预留）
	private float width;
	private ColorRGBA color = ColorRGBA.Black;

	private String wireNum;

	public Pipe() {
	}

	public Pipe(float width, ColorRGBA color) {
		this.width = width;
		this.color = color;
	}

	@Override
	protected void toValueObject() {
	}

	@Override
	public void bind(ILinkTarget port) {
		if (isBothBinded() || port == null) {
			return;
		}
		if (port1 == null) {
			port1 = (GasPort) port;
		} else if (port2 == null) {
			port2 = (GasPort) port;
		}
		List<ILinker> linkers = port.getLinkers();
		if (linkers.size() == 0) {
			linkers.add(this);
		} else {
			linkers.set(0, this);
		}
//		((GasPort) port).setPipe(this);
		if (port1 != null && port2 != null) {
			G.g().refreshGasPressure();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.circuit.ILinker#unbind(com.cas.circuit.ILinkTarget)
	 */
	@Override
	public void unbind(ILinkTarget target) {
		if (target == null) {
			return;
		}
		List<ILinker> linkers = target.getLinkers();
		if (linkers.contains(this)) {
			linkers.remove(this);
		}
		GasPort port = (GasPort) target;

//		port.setPipe(null);

		G.g().refreshGasPressure();

		if (port1 == port) {
			port1 = null;
		} else if (port2 == port) {
			port2 = null;
		}
	}

//	/**
//	 * 绑定导线连接头模型
//	 * @param lineTMdl 连接头模型
//	 */
//	@Override
//	public void bindLinkMdl(ILinkTarget port, Spatial pipeTMdl) {
//		if (isBothBinded() || pipeTMdl == null) {
//			return;
//		}
//		if (pipeTMdl1 == null) {
//			pipeTMdl1 = pipeTMdl;
//		} else if (pipeTMdl2 == null) {
//			pipeTMdl2 = pipeTMdl;
//		}
//		port.getLinkMdls().add(pipeTMdl);
//	}

//	@Override
//	public void unbindLinkMdl(ILinkTarget term, Spatial lineTMdl) {
//		if (term == null) {
//			return;
//		}
//		term.getLinkMdls().remove(lineTMdl);
//	}

	@Override
	public void unbind() {
//		unbindLinkMdl(port1, pipeTMdl1);
//		unbindLinkMdl(port2, pipeTMdl2);
		unbind(port1);
		unbind(port2);
		models = new HashMap<Spatial, ILinkTarget>();
	}

	@Override
	public GasPort getAnother(ILinkTarget port) {
		if (port1 == port) {
			return port2;
		} else if (port2 == port) {
			return port1;
		} else {
			System.err.println("不可能!!!!!!!!!!!!!!!!!");
		}
		return null;
	}

	/**
	 * @param po
	 */
	public Pipe(PipePO po) {
		super(po);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isBothBinded() {
		return port1 != null && port2 != null;
	}

	@Override
	public ILinkTarget getLinkTarget1() {
		return port1;
	}

	@Override
	public ILinkTarget getLinkTarget2() {
		return port2;
	}

	@Override
	public List<Spatial> getLinkMdlByTarget(ILinkTarget target) {
		List<Spatial> linkMdls = new ArrayList<Spatial>();
		for (Entry<Spatial, ILinkTarget> model : models.entrySet()) {
			if (model.getValue().equals(target)) {
				linkMdls.add(model.getKey());
			}
		}
		return linkMdls;
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.circuit.ILinker#getModel()
	 */
	@Override
	public Map<Spatial, ILinkTarget> getModels() {
		return models;
	}

	public float getWidth() {
		return width;
	}

	@Override
	public ColorRGBA getColor() {
		return color;
	}

	@Override
	public String toString() {
		return "Pipe [port1=" + port1 + ", port2=" + port2 + "]" + hashCode();
	}

	@Override
	protected Pipe clone() {
		Pipe clone = (Pipe) super.clone();
		return clone;
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();
		port1 = null;
		port2 = null;
//		pipeTMdl1 = null;
//		pipeTMdl2 = null;
	}

	@Override
	public String getWireNum() {
		return wireNum;
	}

	@Override
	public void setWireNum(String wireNum) {
		this.wireNum = wireNum;
	}

}
