package com.cas.circuit.logic;

import com.cas.circuit.BaseElectricCompLogic;
import com.cas.circuit.Voltage;
import com.cas.circuit.util.MesureResult;
import com.cas.circuit.util.R;
import com.cas.circuit.vo.ElecCompDef;
import com.cas.circuit.vo.Terminal;
import com.jme3.scene.Node;

public class MMT_4QLogic extends BaseElectricCompLogic {

	private Terminal dcin1;
	private Terminal dcin2;

	private Terminal en;
	private Terminal com1;

	private Terminal dir;
	private Terminal com2;

	private Terminal out1;
	private Terminal out2;

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);

		ElecCompDef def = elecComp.getDef();
		dcin1 = def.getTerminal("DCIN+");
		dcin2 = def.getTerminal("DCIN-");

		en = def.getTerminal("EN");
		com1 = def.getTerminal("COM1");

		dir = def.getTerminal("DIR");
		com2 = def.getTerminal("COM2");

		out1 = def.getTerminal("OUT+");
		out2 = def.getTerminal("OUT-");
	}

	@Override
	protected void onReceivedLocal(Terminal terminal) {
		MesureResult dcinResult = R.matchRequiredVolt(Voltage.IS_DC, dcin1, dcin2, 24, 2);

		R r1 = R.getR("MMT_4Q_OUT2-OUT1" + this.hashCode());
		R r2 = R.getR("MMT_4Q_OUT1-OUT2" + this.hashCode());
		if (dcinResult == null) {
			if (r1 != null) {
				r1.shutPowerDown();
			}
			if (r2 != null) {
				r2.shutPowerDown();
			}
			return;
		}

		MesureResult output = R.mesureVoltage(en, com1);
		// en--com1之间为24V则不输出，0V则输出
		if (output == null || Math.abs(output.getVolt() - 24) <= 2) {
			if (r1 != null) {
				r1.shutPowerDown();
			}
			if (r2 != null) {
				r2.shutPowerDown();
			}
			return;
		}

		MesureResult direction = R.mesureVoltage(dir, com2);
		// dir--com2之间为24V则正转，0V则反转
		if (direction == null) {
			if (r1 != null) {
				r1.shutPowerDown();
			}
			if (r2 != null) {
				r2.shutPowerDown();
			}
		} else if (Math.abs(direction.getVolt() - 24) <= 2) {
			if (r1 == null) {
				r1 = R.create("MMT_4Q_OUT2-OUT1" + this.hashCode(), Voltage.IS_DC, out2, out1, 24);
				r1.shareVoltage();
			}
			if (r2 != null) {
				r2.shutPowerDown();
			}
		} else if (Math.abs(direction.getVolt()) <= 2) {
			if (r2 == null) {
				r2 = R.create("MMT_4Q_OUT1-OUT2" + this.hashCode(), Voltage.IS_DC, out1, out2, 24);
				r2.shareVoltage();
			}
			if (r1 != null) {
				r1.shutPowerDown();
			}
		}
	}
}
