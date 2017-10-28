/**
 * 
 */
package com.cas.circuit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cas.circuit.vo.Terminal;

/**
 * @author 张振宇 2015年9月29日 下午4:48:49
 * @param <E>
 */
public class TermTeam {
//	Unique
	private String teamName;
	private Set<Terminal> teamMamber = new HashSet<Terminal>();
	private Map<Terminal, Integer> signed = new HashMap<Terminal, Integer>();

	/**
	 * @param teamMamber
	 */
	public TermTeam(String teamName, Terminal... teamMamber) {
		for (Terminal terminal : teamMamber) {
			this.teamMamber.add(terminal);
			terminal.setTeam(this);
		}
	}

	/**
	 * @param teamName2
	 * @param value
	 */
	public TermTeam(String teamName, List<Terminal> teamMamber) {
//		System.out.println("TermTeam.TermTeam()");
		for (Terminal terminal : teamMamber) {
			this.teamMamber.add(terminal);
			terminal.setTeam(this);
		}
	}

	public void signIn(Terminal terminal) {
//		是组内的成员
		if (!teamMamber.contains(terminal)) {
			return;
		}
		if (!signed.containsKey(terminal)) {
			signed.put(terminal, 0);
		}
		signed.put(terminal, signed.get(terminal) + 1);
	}

	public boolean resetIfReady() {
		boolean result = false;
		if (teamMamber.size() == signed.size()) {
			result = true;

			for (Entry<Terminal, Integer> entry : signed.entrySet()) {
				if (entry.getValue() == 0) {
					result = false;
					break;
				}
			}

			if (result) {
				for (Entry<Terminal, Integer> entry : signed.entrySet()) {
					entry.setValue(entry.getValue() - 1);
				}
			}
		}
		return result;
	}

	/**
	 * @return the teamName
	 */
	public String getTeamName() {
		return teamName;
	}

	/**
	 * @param teamName the teamName to set
	 */
	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	/**
	 * 
	 */
	public Object getCount() {
		return signed;
	}

}
