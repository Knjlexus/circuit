/**
 * 
 */
package com.cas.circuit.vo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.cas.circuit.Voltage;

/**
 * @author 张振宇 Aug 18, 2015 7:59:48 PM
 */
public class IP {
	private Voltage voltage;
//	处在同一电势的连接头
	private List<Terminal> terminalList = new CopyOnWriteArrayList<Terminal>();
//	处在同一电势的所有电阻
	private List<ResisRelation> resisRelationList = new CopyOnWriteArrayList<ResisRelation>();

	private List<CR> CRList = new ArrayList<CR>();

	Set<Wire> passedWires = new HashSet<Wire>();
	Set<Terminal> passedContacted = new HashSet<Terminal>();
	Set<Terminal> passedResis = new HashSet<Terminal>();

	/**
	 * 
	 */
	public IP(Voltage voltage) {
//		克隆后的voltage对象,只保留电源环境,电压类型,相位
		this.voltage = voltage.clone();
//		System.out.println("IP.IP()" + this);
	}

	/**
	 * 在该电势处添加一个连接头
	 */
	public void addTerminal(Terminal terminal) {
		if (terminal == null) {
//			System.out.println("IP.addTerminal() NoNo!!!!!!");
			return;
		}
//		synchronized (terminalList) {
		if (terminalList.contains(terminal)) {
			System.out.println("调用这个方法时注意点,最好在外面判断是否已经加过了");
			return;
		}
		if (terminal.getIsopotential().containsValue(this)) {
			System.err.println("this will never been invoked");
		} else {
			terminalList.add(terminal);
			terminal.addIsopotetial(voltage.getEnv(), this);
		}
//		}
	}

	/**
	 * @return the terminals
	 */
	public List<Terminal> getTerminals() {
		return terminalList;
	}

	/**
	 * 
	 */
	public boolean hasTerminal(Terminal term) {
		return terminalList.indexOf(term) != -1;
	}

	/**
	 * @return the compositeResistanceList
	 */
	public List<CR> getCRList() {
		return CRList;
	}

	/**
	 * @return the resisRelationList
	 */
	public List<ResisRelation> getResisRelationList() {
		return resisRelationList;
	}

//
//	/**
//	 * @return the resisTerminalList
//	 */
//	public List<Terminal> getResisTerminalList() {
//		return resisTerminalList;
//	}

	/**
	 * @return the passedWires
	 */
	public Set<Wire> getPassedWires() {
		return passedWires;
	}

	/**
	 * @return the passedContacted
	 */
	public Set<Terminal> getPassedContacted() {
		return passedContacted;
	}

	/**
	 * @return the passedZeroResis
	 */
	public Set<Terminal> getPassedResis() {
		return passedResis;
	}

////	处在同一电势的连接头
//	private List<Terminal> terminalList = new ArrayList<Terminal>();
////	处在同一电势是电阻一端的连接头
//	private List<Terminal> resisTerminalList = new ArrayList<Terminal>();
////	处在同一电势的所有电阻
//	private List<ResisRelation> resisRelationList = new ArrayList<ResisRelation>();
//
//	private List<CR> compositeResistanceList = new ArrayList<CR>();
//
//	Set<Wire> passedWires = new HashSet<Wire>();
//	Set<Terminal> passedContacted = new HashSet<Terminal>();
//	Set<Terminal> passedResis = new HashSet<Terminal>();

	public void detory() {
		for (Terminal terminal : terminalList) {
			terminal.removeIsopotetial(voltage.getEnv());
//			System.out.println(terminal+" -- > "+terminal.getIsopotential());
		}
		terminalList.clear();
		resisRelationList.clear();
		CRList.clear();
		passedWires.clear();
		passedContacted.clear();
		passedResis.clear();
	}

	public void clearVolt() {
		for (Terminal terminal : terminalList) {
			terminal.removeVolt(voltage.getEnv());
		}
//		Iterator<Terminal> it = terminalList.iterator();
//		while (it.hasNext()) {
////			System.out.println("" + terminalList.hashCode() +"-"+terminalList.size());
//			it.next().removeVolt(voltage.getEnv());
////			System.out.println(terminal+" -- > "+terminal.getIsopotential());
//		}
	}

	/**
	 * @param terminal
	 */
	public void removeTerminal(Terminal terminal) {
		terminalList.remove(terminal);
		passedWires.removeAll(terminal.getLinkers());
		passedContacted.remove(terminal);
		passedResis.removeAll(terminal.getResisRelationMap().values());
	}

	/**
	 * @return the voltage
	 */
	public Voltage getVoltage() {
		return voltage;
	}

	/**
	 * @return the voltage
	 */
	public float getVoltageValue() {
		if (voltage == null) {
		}
		return voltage.getValue();
	}

	/**
	 * @param value the voltage to set
	 */
	public void setVoltageValue(float value) {
		voltage.setValue(value);
		for (Terminal term : terminalList) {
			term.addVolt(voltage);
		}
	}

//	/*
//	 * (non-Javadoc)
//	 * @see java.lang.Object#toString()
//	 */
//	@Override
//	public String toString() {
//		return terminalList.toString() + " : " + voltage;
//	}
}
