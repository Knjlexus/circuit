package com.cas.circuit.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.cas.circuit.ILinkTarget;
import com.cas.circuit.ILinker;
import com.cas.circuit.po.TerminalPO;
import com.cas.circuit.po.WirePO;
import com.cas.circuit.util.R;
import com.cas.util.StringUtil;
import com.cas.util.Util;
import com.cas.util.vo.BaseVO;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;

/**
 * 导线
 * @author sco_pra
 */
public class Wire extends BaseVO<WirePO> implements ILinker {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1683821954631303090L;

//	导线的两个连接头
	private Terminal term1;
	private Terminal term2;
//	导线模型，用List是因为可能存在两段线在不同的节点中
//	private List<Spatial> model = new ArrayList<Spatial>();
	private Map<Spatial, ILinkTarget> models = new HashMap<Spatial, ILinkTarget>();

	private float width;
	private ColorRGBA color = ColorRGBA.Black;
//	private Cable belongCable;

//	private List<Vector3f> centerLocations = new ArrayList<Vector3f>();

	private boolean brokenBreak = false;

	private String wireNum;

	public Wire() {
	}

	public Wire(float width, ColorRGBA color) {
		this.width = width;
		this.color = color;
	}

	@Override
	protected void toValueObject() {
		super.toValueObject();

//		需要手动创建,这里的terminal是线缆插头上的针孔.
		parseTerm(po.getStitch1());
		parseTerm(po.getStitch2());

		if (!Util.isEmpty(po.getWidth()) && Util.isNumeric(po.getWidth())) {
			width = Float.parseFloat(po.getWidth());
		} else {
//			System.err.println("线宽配置错误");
		}
		List<String> colorStr = null;
		if (Util.notEmpty(po.getColor())) {
			colorStr = StringUtil.split(po.getColor(), ',');
		}
		List<Float> colorList = new ArrayList<Float>();
		if (colorStr != null) {
			for (String string : colorStr) {
				if (Util.isNumeric(string)) {
					colorList.add(Float.parseFloat(string));
				}
			}
		}
		if (colorList.size() == 4) {
			color = new ColorRGBA(colorList.get(0), colorList.get(1), colorList.get(2), colorList.get(3));
		} else {
//			System.err.println("有线的颜色配置错误");
		}
	}

	private void parseTerm(String stitchNo) {
		if (Util.isEmpty(stitchNo)) {
			return;
		}
		if (Util.isNumeric(stitchNo)) {
			bind(new Terminal(new TerminalPO(stitchNo)));
		} else if (stitchNo.contains("|")) {
			String[] arr = stitchNo.split("\\|");
			Wire wire = null;
			Terminal tmp = null;
			for (int i = 0; i < arr.length; i++) {
				if (i + 1 < arr.length) {
					wire = new Wire();
					if (i == 0) {
						Terminal first = new Terminal(new TerminalPO(arr[i]));

						this.bind(first);
						wire.bind(first);

						tmp = new Terminal(new TerminalPO(arr[i + 1]));
						wire.bind(tmp);
					} else {
						wire.bind(tmp);

						tmp = new Terminal(new TerminalPO(arr[i + 1]));
						wire.bind(tmp);
					}
				}
			}
		}
	}

	/**
	 * 将导线绑到连接头上
	 */
	@Override
	public void bind(ILinkTarget term) {
		if (isBothBinded() || term == null) {
			return;
		}
//		System.out.println(this + "Wire.bind()" +  term);
		if (term1 == null) {
			term1 = (Terminal) term;
		} else if (term2 == null) {
			term2 = (Terminal) term;
		}

		List<ILinker> linkers = term.getLinkers();
		if (!linkers.contains(this)) {
			linkers.add(this);
		}

		if (term1 != null && term2 != null) {
//			电路发生改变
			Set<String> envs = R.findEnvsOn(term1, term2);
			for (String string : envs) {
				R.getR(string).shareVoltage();
			}
//			term1.receivedVoltage(term2);
//			term2.receivedVoltage(term1);
		}
	}

	@Override
	public void unbind(ILinkTarget term) {
		if (term == null) {
			return;
		}

		term.getLinkers().remove(this);

		if (term1 != null && term2 != null) {
//			电路发生改变
			Set<String> envs = R.findEnvsOn(term1, term2);

			for (String string : envs) {
				R.getR(string).shareVoltage();
			}
		}

		if (term1 == term) {
			term1 = null;
		} else if (term2 == term) {
			term2 = null;
		}
	}

	@Override
	public void unbind() {
		if (term1 != null) {
			term1.getLinkers().remove(this);
		}
		if (term2 != null) {
			term2.getLinkers().remove(this);
		}

		unbind(term1);
		unbind(term2);
		models = new HashMap<Spatial, ILinkTarget>();
	}

	/**
	 * @param po
	 */
	public Wire(WirePO po) {
		super(po);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Terminal getAnother(ILinkTarget term) {

		if (term1 == null || term2 == null) {
			System.err.println("不可能!!!!!!!!!!!!!!!!! \r\n" + this + "term12 == " + term1 + " , term2=" + term2);
		}

		if (term1 == term) {
			return term2;
		} else if (term2 == term) {
			return term1;
//		} else {
		}
		return null;
	}

	/**
	 * 导线上有电压变化
	 */
	public void voltageChanged(boolean volt) {
		if (term1 == null || term2 == null) {
			return;
		}

//		// 线产生电效果
//		List<Spatial> lineModel = models;
//		if (belongCable != null) {
//			lineModel = belongCable.getModels();
//		}
//		final SimpleApplication app = Dispatcher.getIns().getMainApp();
////		System.out.println("Wire.voltageChanged()"+ lineModel);
//		for (Spatial spatial : lineModel) {
//			if (spatial != null) {
//				if (volt) {
////					System.out.println("高亮显示导线" + this);
//					JmeUtil.setMaterialEffect(app, HightLightType.WIREFRAME, spatial, ColorRGBA.Red, true);
//				} else {
////					System.out.println("取消高亮显示导线" + this);
//					JmeUtil.setMaterialEffect(app, HightLightType.WIREFRAME, spatial, ColorRGBA.Red, false);
//				}
//			}
//		}
	}

	@Override
	public boolean isBothBinded() {
		return term1 != null && term2 != null;
	}

	public Terminal getTerm1() {
		return term1;
	}

	public Terminal getTerm2() {
		return term2;
	}

	@Override
	public ILinkTarget getLinkTarget1() {
		return term1;
	}

	@Override
	public ILinkTarget getLinkTarget2() {
		return term2;
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

//	@Override
//	public Spatial getLinkMdl1() {
//		return linkMdl1;
//	}
//
//	@Override
//	public Spatial getLinkMdl2() {
//		return linkMdl2;
//	}

	public float getWidth() {
		return width;
	}

	@Override
	public ColorRGBA getColor() {
		return color;
	}

	@Override
	public String getWireNum() {
		return wireNum;
	}

	@Override
	public void setWireNum(String wireNum) {
		this.wireNum = wireNum;
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.circuit.ILinker#getModel()
	 */
	@Override
	public Map<Spatial, ILinkTarget> getModels() {
		return models;
	}

//	public Cable getBelongCable() {
//		return belongCable;
//	}
//
//	public void setBelongCable(Cable belongCable) {
//		this.belongCable = belongCable;
//	}

	@Override
	public String toString() {
		return "Wire [term1=" + term1 + ", term2=" + term2 + "]" + hashCode();
	}

	@Override
	protected Wire clone() {
		Wire clone = (Wire) super.clone();
		return clone;
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();
		term1 = null;
		term2 = null;
//		linkMdl1 = null;
//		linkMdl2 = null;
		models = new HashMap<Spatial, ILinkTarget>();
//		centerLocations = new ArrayList<Vector3f>();
	}

//	public List<Vector3f> getCenterLocations() {
//		return centerLocations;
//	}

	public boolean isBrokenBreak() {
		return brokenBreak;
	}

	public void setBrokenBreak(boolean brokenBreak) {
		this.brokenBreak = brokenBreak;
		shareTerm(term1);
		shareTerm(term2);
	}

	private void shareTerm(Terminal term) {
		Set<String> envs = term.getResidualVolt().keySet();
		R r = null;
		for (String env : envs) {
			r = R.getR(env);
			r.shareVoltage();
		}
	}
}
