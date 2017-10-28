
/**
 * 
 */
package com.cas.circuit;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cas.circuit.consts.IOType;
import com.cas.circuit.vo.ControlIO;
import com.cas.circuit.vo.ElecComp;
import com.cas.circuit.vo.ElecCompDef;
import com.cas.circuit.vo.Jack;
import com.cas.circuit.vo.LightIO;
import com.cas.circuit.vo.Magnetism;
import com.cas.circuit.vo.Terminal;
import com.cas.gas.vo.GasPort;
import com.cas.util.Util;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * @author 张振宇 Jul 29, 2015 2:20:09 PM
 */
public class BaseElectricCompLogic {
	public static Logger log = Logger.getLogger(BaseElectricCompLogic.class);
	public static final String ELECCOMP_STATE = "eleccomp_state";
	protected ElecComp elecComp;
	protected Node elecCompMdl;
	protected boolean workable;

	public BaseElectricCompLogic() {
	}

	/**
	 * @param elecComp the elecComp to set
	 */
	public void setElecComp(ElecComp elecComp) {
		if (elecComp != null) {
			this.elecComp = elecComp;
		}
	}

	public void initialize(Node elecCompMdl) {
//		XXX for test
//		System.out.println("BaseElectricCompLogic.initialize()" + Thread.currentThread().getName());
//		log.log(Level.INFO, "加载元器件{0},用时{1}", new Object[] { elecComp.getPO().getTagName(), System.currentTimeMillis() });
		this.elecCompMdl = elecCompMdl;
//		读取元器件铭牌信息
		readNameplateInfo();
//		elecCompMdl = (Node) MdlMapUtil.loadMdlWithAbbr(getElecComp().getDef().getPO().getMdlRef(), app.getAssetManager());
//		elecCompMdl = (Node) parentMdl.getChild(getElecComp().getDef().getPO().getMdlRef());
//		elecCompMdl = (Node) parentMdl.getChild(getElecComp().getPO().getMdlName());
//		elecCompMdl = parentMdl;
//		registerEvent();
//		elecCompMdl.setLocalTranslation(getElecComp().getLocation());
//		elecCompMdl.rotate(getElecComp().getRotation()[0], getElecComp().getRotation()[1], getElecComp().getRotation()[2]);
//		elecCompMdl.setUserData(BaseElectricCompLogic.ELECCOMP_STATE, BaseElectricCompLogic.this);

		Spatial tempSpatial = null;
//		遍历元气件中所有插座
		Collection<Jack> jacks = getElecComp().getDef().getJackMap().values();
		for (Jack jack : jacks) {
			tempSpatial = getChild(elecCompMdl, jack.getPO().getMdlName(), jack);
			jack.setModel(tempSpatial);
		}
//		遍历元气件中所有连接头
		Collection<Terminal> terminals = getElecComp().getDef().getTerminalMap().values();
		for (Terminal terminal : terminals) {
			tempSpatial = getChild(elecCompMdl, terminal.getPO().getMdlName(), terminal);
			if (tempSpatial == null) {
				log.warn(elecComp.getDef().getPO().getName() + "未找到模型名为" + terminal.getPO().getMdlName() + "的端子模型！");
				continue;
			}
			terminal.setModel(tempSpatial);
			// FIXME 端子连接数量的标签
			String numStr = ((Node) tempSpatial).getChild(0).getUserData("num");
			if (Util.isNumeric(numStr)) {
				terminal.setNum(Integer.parseInt(numStr));
			} else {
				terminal.setNum(1);
			}
		}
//		遍历元器件中所有气口
		Collection<GasPort> gasPorts = getElecComp().getDef().getGasPortMap().values();
		for (GasPort gasPort : gasPorts) {
			tempSpatial = getChild(elecCompMdl, gasPort.getPO().getMdlName(), gasPort);
			gasPort.setModel(tempSpatial);
		}
//		TODO 加入元气件按钮开关...
		ElecCompDef compDef = getElecComp().getDef();
		List<Magnetism> magnetisms = compDef.getMagnetisms();
		for (Magnetism magnetism : magnetisms) {
//			遍历磁环静中所有按钮
			for (ControlIO controlIO : magnetism.getControlIOs()) {
				tempSpatial = getChild(elecCompMdl, controlIO.getPO().getMdlName(), controlIO);
				controlIO.setModel(tempSpatial);
			}
//			遍历磁环静中所有指示灯
			for (LightIO lightIO : magnetism.getLightIOs()) {
				tempSpatial = getChild(elecCompMdl, lightIO.getPO().getMdlName(), lightIO);
				lightIO.setModel(tempSpatial);
			}
		}
//		遍历元气件中所有指示灯
		for (LightIO lightIO : compDef.getLightIOs()) {
			tempSpatial = getChild(elecCompMdl, lightIO.getPO().getMdlName(), lightIO);
			lightIO.setModel(tempSpatial);
		}
	}

	protected void readNameplateInfo() {
	}

	protected String getNameplate(String key, String defValue) {
		Map<String, String> properties = getElecComp().getDef().getProperties();
		if (properties.containsKey(key)) {
			return properties.get(key);
		}
		return defValue;
	}

	Spatial getChild(final Node parent, final String childName, final Object obj) {
		if (parent == null) {
			return null;
		}
		Spatial child = parent.getChild(childName);
//		if (child == null) {
//			log.error(parent + "中找不到名字为" + childName + "的模型" + obj);
//		}
		return child;
	}

	public Node getElecCompMdl() {
		return elecCompMdl;
	}

	/**
	 * @param terminal
	 */
	public final void onReceived(Terminal terminal) {
		if (terminal.getIoType() == IOType.OUTPUT) {
			return;
		}
		TermTeam team = terminal.getTeam();
		team.signIn(terminal);
		if (!team.resetIfReady()) {
			return;
		}
		onReceivedLocal(terminal);
	}

	/**
	 * 气压发生变化
	 * @param gasPort
	 */
	public void onReceivedGP(GasPort gasPort) {
	}

	/**
	 * @param terminal
	 */
	protected void onReceivedLocal(Terminal terminal) {
	}

	/**
	 * @return the elecComp
	 */
	public ElecComp getElecComp() {
		return elecComp;
	}

	public void destroy() {
	}

	/**
	 * @return the batchEnable
	 */
	public boolean isBatchEnable() {
//		默认不可批处理
//		return "1".equals(elecCompMdl.getUserData(UDKConsts.Boolean_BATCHABLE));
		return false;
	}

	public void onButtonAction(ControlIO button, boolean press) {
	}

	public boolean isWorkable() {
		return workable;
	}

}