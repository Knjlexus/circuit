package com.cas.circuit.logic;

import java.util.List;

import com.cas.circuit.BaseElectricCompLogic;
import com.cas.circuit.control.VS10N051CControl;
import com.cas.circuit.vo.ControlIO;
import com.cas.circuit.vo.Magnetism;
import com.cas.circuit.vo.Terminal;
import com.jme3.scene.Node;

public class VS10N051CLogic extends BaseElectricCompLogic {

	private VS10N051CControl control;

	private ControlIO button;

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);

		List<Magnetism> magnetisms = elecComp.getDef().getMagnetisms();
		for (Magnetism magnetism : magnetisms) {
			List<ControlIO> controlIOs = magnetism.getControlIOs();
			for (ControlIO controlIO : controlIOs) {
				button = controlIO;
				break;
			}
		}

		// 添加碰撞监听control
		control = new VS10N051CControl(button);
		elecCompMdl.addControl(control);
	}

	@Override
	protected void onReceivedLocal(Terminal terminal) {
		super.onReceivedLocal(terminal);
	}

	public void buttonClicked(int index) {
		button.doSwitch(index);
	}
}
