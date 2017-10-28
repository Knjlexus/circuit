package com.cas.circuit;

import java.util.HashMap;
import java.util.Map;

import com.cas.circuit.vo.Cable;
import com.cas.circuit.vo.Format;
import com.cas.circuit.vo.Jack;
import com.cas.circuit.vo.Terminal;

public class Plug {

	private Cable cable;
	private Jack jack;
	private boolean inserted;
	private Format format;

//	Key: index，对应插孔中的针脚号
	private Map<String, Terminal> terminals = new HashMap<String, Terminal>();

	public Plug(Cable cable) {
		super();
		this.cable = cable;
	}

	public Map<String, Terminal> getTerminals() {
		return terminals;
	}

	public void addTerminal(Terminal term1) {
		terminals.put(term1.getPO().getIndex(), term1);
	}

	public boolean isInserted() {
		return inserted;
	}

	public void setInserted(boolean inserted) {
		this.inserted = inserted;
	}

	public Cable getCable() {
		return cable;
	}

	public Format getFormat() {
		return format;
	}

	public void setFormat(Format format) {
		if (format == null) {
			throw new RuntimeException("不能给设置插头设置null值");
		}
		this.format = format;
//		format.getCableMap().put(cable.getPO().getId(), cable);
		format.getCableList().add(cable);
	}

	public Jack getJack() {
		return jack;
	}

	public void setJack(Jack jack) {
		this.jack = jack;
	}
}