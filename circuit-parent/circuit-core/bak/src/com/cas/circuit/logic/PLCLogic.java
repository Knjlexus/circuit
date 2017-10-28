package com.cas.circuit.logic;

import com.cas.circuit.BaseElectricCompLogic;
import com.cas.circuit.vo.Terminal;
import com.jme3.scene.Node;

import javafish.clients.opc.variant.Variant;

/**
 * @author Administrator
 */
public abstract class PLCLogic extends BaseElectricCompLogic {
	protected static String SIGNAL_EVN;

	protected byte mode;
	protected boolean local;
	protected ISignalListener state;

	protected Terminal term_L;
	protected Terminal term_N;
	protected Terminal term_GND;

	public PLCLogic() {
		super();
	}

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);
		term_L = elecComp.getDef().getTerminal("L");
		term_N = elecComp.getDef().getTerminal("N");
		term_GND = elecComp.getDef().getTerminal("GND");
	}

	public void update(String singalAddress, Variant value) {
	}

	public void setStateListener(ISignalListener state) {
		this.state = state;
	}

	public void setMode(byte mode) {
		this.mode = mode;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	public Variant getItemValue(String itemName) {
		return null;
	}

}
