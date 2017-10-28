package com.cas.circuit.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cas.circuit.ILinkTarget;
import com.cas.circuit.ILinker;
import com.cas.circuit.Plug;
import com.cas.circuit.po.CablePO;
import com.cas.circuit.po.WirePO;
import com.cas.circuit.util.R;
import com.cas.util.StringUtil;
import com.cas.util.Util;
import com.cas.util.parser.ParserFactory;
import com.cas.util.vo.BaseVO;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;

/**
 * 电缆类,其实就是集成了许多导线
 * @author sco_pra
 */
public class Cable extends BaseVO<CablePO> implements ILinker {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7617146462947528605L;

//	private SaveConnect saveData;

	private Map<String, Wire> mark_wire = new LinkedHashMap<String, Wire>();

	private List<Wire> bindWires = new ArrayList<Wire>();
	private Wire prevWire;

	private Plug plug1;
	private Plug plug2;

	// 用List是因为可能存在两段线在不同的节点中
//	private List<Spatial> models = new ArrayList<Spatial>();
	private Map<Spatial, ILinkTarget> models = new HashMap<Spatial, ILinkTarget>();

	// 对于一头多头的线缆，表示正在连接的内线
	private Wire nowConnectingWire;

	private Object toConntectPlugOrTerm;

	private float width;

	Format format1;
	Format format2;

	private boolean brokenBreak = false;

	/**
	 * 是否为元器件上线缆
	 */
	private boolean isElecComp;

	/**
	 * 导线编号
	 */
	private String wireNum;

	public void switchPlugOrTrem() {
		// 如果是CP类型的
		if (plug2 == null) {
			List<Wire> mark_wires = new ArrayList<Wire>(mark_wire.values());
			if (toConntectPlugOrTerm == plug1) {
				toConntectPlugOrTerm = mark_wires.get(0);
			} else {
				int lastIndex = mark_wires.indexOf(toConntectPlugOrTerm);
				if (lastIndex == mark_wires.size() - 1) {// 到最后了
					toConntectPlugOrTerm = plug1;
				} else {
					toConntectPlugOrTerm = mark_wires.get(lastIndex + 1);
				}
			}
		} else {
			if (toConntectPlugOrTerm == plug1) {
				toConntectPlugOrTerm = plug2;
			} else {
				toConntectPlugOrTerm = plug1;
			}
		}
	}

	@Override
	protected String getLocalKey() {
		return po.getId();
	}

	@Override
	protected void toValueObject() {
		super.toValueObject();
		ParserFactory factory = ParserFactory.getFactory("Jack_Cable_Format");
		if (factory == null) {
			factory = ParserFactory.createFactory("Jack_Cable_Format");
		}
//		FIXME
		String format = po.getFormat();
		if (!Util.isEmpty(format)) {
			Map<Object, Format> formatMap = factory.getParser(Format.class, "com/cas/circuit/config/format.xml").getDataMap();
			if (format.contains("|")) {
				List<String> lis = StringUtil.split(format);
				format1 = formatMap.get(lis.get(0));
				format2 = formatMap.get(lis.get(1));
				if (format1 == null) {
					throw new RuntimeException("没有名称为 ['" + lis.get(0) + "'] 的制式");
				}
				if (format2 == null) {
					throw new RuntimeException("没有名称为 ['" + lis.get(1) + "'] 的制式");
				}
			} else {
				format1 = formatMap.get(format);
				format2 = formatMap.get(format);
				if (format1 == null) {
					throw new RuntimeException("没有名称为 ['" + format + "'] 的制式");
				}
			}
		} else {
			throw new RuntimeException("电缆没有制式");
		}
//		if (Util.isNumeric(po.getWidth())) {
//			width = Float.parseFloat(po.getWidth());
//		}
	}

	@Override
	protected void addChild(BaseVO<?> child) {
		super.addChild(child);
		Wire wire = (Wire) child;
//
//		wires.add(wire);
		if (!Util.isEmpty(wire.getPO().getMark())) {
			mark_wire.put(wire.getPO().getMark(), wire);
		}
	}

	@Override
	protected void onAllChildAdded() {
		super.onAllChildAdded();
		if (children.size() == 0) {
			Wire wire = null;
			WirePO wirePO = null;
			for (int i = 1; i <= format1.getTurns(); i++) {
				wirePO = new WirePO();
				wirePO.setStitch1(String.valueOf(i));
				wirePO.setStitch2(String.valueOf(i));
				wire = new Wire();
				wire.setPO(wirePO);
				children.add(wire);
			}
		}
		makePlug();
		// 初始化当前连接的
		toConntectPlugOrTerm = plug1;
	}

	private void makePlug() {
		Wire wire = null;
		for (int i = 0; i < children.size(); i++) {
			wire = (Wire) children.get(i);
			if (wire.getTerm1() != null) {
				if (plug1 == null) {
					plug1 = new Plug(this);
				}
				plug1.addTerminal(wire.getTerm1());
			}
			if (wire.getTerm2() != null) {
				if (plug2 == null) {
					plug2 = new Plug(this);
				}
				plug2.addTerminal(wire.getTerm2());
			}
		}
		if (plug1 == null && plug2 == null) {
			throw new RuntimeException("电缆配置有问题,电缆至少要有一个插头");
		}

		if (plug1 != null) {
			plug1.setFormat(format1);
		}
		if (plug2 != null) {
			plug2.setFormat(format2);
		}

//		System.out.println("this =" + this);
	}

//	public List<Wire> getWires() {
//		return wires;
//	}

	public Map<String, Wire> getMark_wire() {
		return mark_wire;
	}

//	电缆的插入动作,实际上是电缆中每一根导线的捆绑动作
	public void plugIn(Jack jack) {
		if (jack.getPlug() != null) {
			throw new RuntimeException("已经有插头了");
		}
//		System.out.println(this + ".plugIn(" + jack +")");

		Plug choose = null;
		if (plug1 != null && !plug1.isInserted()) {
			choose = plug1;
		} else if (plug2 != null && !plug2.isInserted()) {
			choose = plug2;
		}
		jack.setPlug(choose);
		choose.setJack(jack);

		choose.setInserted(true);

		Map<String, Terminal> terms = choose.getTerminals();
		Iterator<Entry<String, Terminal>> it = terms.entrySet().iterator();
		Entry<String, Terminal> entry = null;
		while (it.hasNext()) {
			entry = it.next();
			if (jack.getStitch().get(entry.getKey()) == null) {
				throw new RuntimeException("没有配置" + jack.getPO().getName() + "的第" + entry.getKey() + "根针脚");
			}
			entry.getValue().setContacted(jack.getStitch().get(entry.getKey()));
			jack.getStitch().get(entry.getKey()).setContacted(entry.getValue());

			Set<String> envs = R.findEnvsOn(entry.getValue(), jack.getStitch().get(entry.getKey()));
			for (String string : envs) {
				R.getR(string).shareVoltage();
			}
		}
	}

//	电缆的拔出动作,实际上是电缆中每一根导线的解绑动作
	public void plugOut(Plug plug) {
		if (plug == null) {
			throw new RuntimeException("没有插头了");
		}
		if (plug.getJack() != null) {
			plug.getJack().setPlug(null);
		}
		plug.setJack(null);
		plug.setInserted(false);

		Map<String, Terminal> terms = plug.getTerminals();
		Iterator<Entry<String, Terminal>> it = terms.entrySet().iterator();
		Entry<String, Terminal> entry = null;
		while (it.hasNext()) {
			entry = it.next();
			entry.getValue().setContacted(null);
			plug.getTerminals().get(entry.getKey()).setContacted(null);

			Set<String> envs = R.findEnvsOn(entry.getValue(), plug.getTerminals().get(entry.getKey()));
			for (String string : envs) {
				R.getR(string).shareVoltage();
			}
		}
	}

	public void plugOut() {
		plugOut(this.plug1);

		if (plug2 != null) {
			plugOut(plug2);
		} else {
			Map<String, Terminal> terms = plug1.getTerminals();
			Iterator<Entry<String, Terminal>> it = terms.entrySet().iterator();
			Terminal termInPlug = null;
			List<ILinker> wires = null;
			Terminal otherInWire = null;
			while (it.hasNext()) {
				termInPlug = it.next().getValue();
				wires = termInPlug.getLinkers();
				for (ILinker wire : wires) {
					otherInWire = (Terminal) wire.getAnother(termInPlug);
					if (otherInWire == null) {
						continue;
					}
					if (otherInWire.getContacted() != null) {
						otherInWire.getContacted().setContacted(null);
					}
					otherInWire.setContacted(null);
					wire.unbind(otherInWire);
				}
			}
		}
	}

	public Plug getAnother(Plug plug) {
		if (plug1 == plug) {
			return plug2;
		} else if (plug2 == plug) {
			return plug1;
		}
		return null;
	}

	public Format getAnotherFormat(Format format) {
		if (format1 == format) {
			return format2;
		} else if (format2 == format) {
			return format1;
		}
		return null;
	}

	public boolean isSinglePlug() {
		return (plug1 == null && plug2 != null) || (plug2 == null && plug1 != null);
	}

	public Plug getPlug1() {
		return plug1;
	}

	public Plug getPlug2() {
		return plug2;
	}

	public String getTurns() {
		return String.valueOf(format1.getTurns());
	}

	public Wire getNowConnectingWire() {
		return nowConnectingWire;
	}

	public void setNowConnectingWire(String wireKey) {
		this.nowConnectingWire = mark_wire.get(wireKey);
	}

	public List<Wire> getBindWires() {
		return bindWires;
	}

	public float getWidth() {
		return width;
	}

	@Override
	public String toString() {
		return "Cable [" + this.po.getId() + " plug1=" + plug1 + "plug2=" + plug2 + "]";
	}

	@Override
	public Cable clone() {
		Cable clone = (Cable) super.clone();
		return clone;
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();
		mark_wire = new HashMap<String, Wire>();
		bindWires = new ArrayList<Wire>();
		plug1 = null;
		plug2 = null;
		format1 = null;
		format2 = null;
		models = new HashMap<Spatial, ILinkTarget>();
		nowConnectingWire = null;
		toConntectPlugOrTerm = null;
	}

	public Object getToConntectPlugOrTerm() {
		return toConntectPlugOrTerm;
	}

	public boolean isBrokenBreak() {
		return brokenBreak;
	}

	public void setBrokenBreak(boolean brokenBreak) {
		this.brokenBreak = brokenBreak;
		for (BaseVO<?> baseVO : children) {
			((Wire) baseVO).setBrokenBreak(brokenBreak);
		}
	}

	/**
	 * 正常绑定线缆插头
	 */
	@Override
	public void bind(ILinkTarget target) {
		if (target instanceof Jack) {
			Jack jack = (Jack) target;
			plugIn(jack);
			switchPlugOrTrem();
			List<ILinker> linkers = jack.getLinkers();
			if (linkers.size() == 0) {
				linkers.add(this);
			} else {
				linkers.set(0, this);
			}
		}
	}

	/**
	 * 特殊绑定线缆端子
	 */
	public void bindTerm(ILinkTarget target, Spatial mdl) {
		if (target instanceof Terminal) {
			Terminal terminal = (Terminal) target;
			if (nowConnectingWire.isBothBinded()) {
				return;
			}
			nowConnectingWire.bind(terminal);
			nowConnectingWire.getModels().put(mdl, terminal);
			switchPlugOrTrem();
		}
	}

	@Override
	public void unbind() {
		plugOut();
		models = new HashMap<Spatial, ILinkTarget>();
		nowConnectingWire = null;
	}

	@Override
	public void unbind(ILinkTarget target) {
		Jack jack = (Jack) target;
		plugOut(jack.getPlug());
		List<ILinker> linkers = jack.getLinkers();
		if (linkers.contains(this)) {
			linkers.remove(this);
		}
	}

	@Override
	public boolean isBothBinded() {
		boolean result = true;
		for (Entry<String, Wire> mw : mark_wire.entrySet()) {
			result = result && mw.getValue().isBothBinded();
		}
		result = result && (plug1 == null || (plug1 != null && plug1.getJack() != null));
		result = result && (plug2 == null || (plug2 != null && plug2.getJack() != null));
		return result;
	}

	@Override
	public ILinkTarget getAnother(ILinkTarget target) {
		Jack jack = (Jack) target;
		Plug plug = getAnother(jack.getPlug());
		return plug.getJack();
	}

	@Override
	public ILinkTarget getLinkTarget1() {
		if (plug1 == null) {
			return null;
		}
		return plug1.getJack();
	}

	@Override
	public ILinkTarget getLinkTarget2() {
		if (plug2 == null) {
			return null;
		}
		return plug2.getJack();
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

	@Override
	public ColorRGBA getColor() {
		return ColorRGBA.Gray;
	}

	@Override
	public Map<Spatial, ILinkTarget> getModels() {
		return models;
	}

	public boolean isElecComp() {
		return isElecComp;
	}

	public void setElecComp(boolean isElecComp) {
		this.isElecComp = isElecComp;
	}

	@Override
	public String getWireNum() {
		return wireNum;
	}

	@Override
	public void setWireNum(String wireNum) {
		this.wireNum = wireNum;
	}

	public Wire getPrevWire() {
		return prevWire;
	}

	public void setPrevWire(Wire prevWire) {
		this.prevWire = prevWire;
	}
}
