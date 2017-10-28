package com.cas.circuit.event;

import java.util.ArrayList;
import java.util.List;

import com.cas.circuit.ConnectionHandler;
import com.cas.circuit.ConnectionUtil;
import com.cas.circuit.msg.LinkerMsg;
import com.cas.circuit.vo.Cable;
import com.cas.circuit.vo.ElecComp;
import com.cas.circuit.vo.Jack;
import com.cas.circuit.vo.Wire;
import com.cas.network.util.ClientMgr;
import com.cas.robot.common.Dispatcher;
import com.cas.robot.common.IMouseEventHandler;
import com.cas.robot.common.ui.ITip;
import com.cas.util.parser.ParserFactory;

public class TransducersHandler implements IMouseEventHandler {

	private ConnectionHandler handler;

	private Cable cableTemplate;

	private Cable cable;

	private ElecComp elecComp;

	private Jack jack;

	private boolean modifiable;

	public TransducersHandler(ConnectionHandler handler, ElecComp elecComp, Jack jack, boolean modifiable) {
		this.handler = handler;
		this.elecComp = elecComp;
		this.jack = jack;
		this.modifiable = modifiable;
		jack.setModel(elecComp.getCompLogic().getElecCompMdl());
		// 传感器只有一个Jack，此处代码写死
		ParserFactory factory = ParserFactory.getFactory("Jack_Cable_Format");
		if (factory == null) {
			factory = ParserFactory.createFactory("Jack_Cable_Format");
		}
		try {
			cableTemplate = factory.getParser(Cable.class, "com/cas/circuit/config/cable.xml").getDataMap().get(jack.getPO().getCable()).clone();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void setInfoVisible(boolean visible) {
		if (visible) {
			Dispatcher.getIns().getTip().showTip(ITip.NORMAL, elecComp.toString());
		} else {
			Dispatcher.getIns().getTip().showTip(ITip.NORMAL, "");
		}
	}

	/**
	 * 显示传感器接线面板
	 */
	public void showWirePanel() {
		// 判断是否可以切换至传感器线缆
		if (!handler.checkChange()) {
			Dispatcher.getIns().getTip().showTip(ITip.ERROR, ConnectionUtil.CONNECTING_CANNOT_CHANGE);
//			state.getMaintenanceUI().showInfo(IAssembleUI.ERROR, UIConsts.CONNECTING_CANNOT_CHANGE);
			return;
		}
		// 如果当前传感器未选中过接线，则初始化传感器线缆
		if (cable == null || cable.getLinkTarget1() == null) {
			cable = this.cableTemplate.clone();
			cable.bind(jack);
			cable.setElecComp(true);
			cable.getModels().put(jack.getModel(), jack);
			handler.setCurrLinker(cable);
			LinkerMsg reqMsg = handler.getLinkerData().addLinker(cable, jack, null);

			if (!handler.isAlone()) {
				// 如果为元器件中线缆，先发绑定元器件的
				reqMsg.setType(LinkerMsg.ADD_LINKER);
				reqMsg.setElecComp(true);
				ClientMgr.send(reqMsg);
			}
		}
		// 取消上一次选中的高亮
		handler.setHighLightLinker(handler.getSelected(), false);
		// 删除上一次的连线
		handler.setLineVisible(false);
		// 如果传感器线缆已经绑定完成，选中传感器即选中线缆
		if (cable.isBothBinded()) {
			handler.selectLinker(cable);
			handler.setCurrSelectedModifiable(modifiable);
			return;
		}
		// 如果线缆为单连接头线缆
		if(cable.isSinglePlug()){
			// 获得当前等待接线的线缆上导线
			Wire wire = cable.getNowConnectingWire();
			// 如果线缆上导线为空，则为传感器第一次接线，设置当前接线为传感器线缆
			if (wire == null) {
				handler.setCurrLinker(cable);
				handler.initLinkerLine(cable, jack.getModel());
				handler.setLineVisible(true);
			}
			// 根据导线标签名生成初始化线缆上导线面板
			List<Wire> wires = new ArrayList<Wire>(cable.getMark_wire().values());
			handler.getUI().initXianLanPanel(wires, true);
			// 选中面板上导线
			if (wire != null) {
				int index = wires.indexOf(wire);
				handler.getUI().showBorder(index);
			} else {
				cable.setNowConnectingWire(wires.get(0).getPO().getMark());
			}
		}else{
			// 设置当前选中为线缆
			handler.setCurrLinker(cable);
			handler.initLinkerLine(cable, jack.getModel());
			handler.setLineVisible(true);
		}
	}

	public void setCable(Cable cable) {
		this.cable = cable;
	}

}
