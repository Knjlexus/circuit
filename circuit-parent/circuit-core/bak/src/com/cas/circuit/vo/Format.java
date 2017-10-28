package com.cas.circuit.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cas.circuit.po.FormatPO;
import com.cas.util.Util;
import com.cas.util.vo.BaseVO;

public class Format extends BaseVO<FormatPO> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1749684626122497086L;

	private int turns;

	private Map<String, Cable> cableMap = new HashMap<String, Cable>();
	private List<Cable> cableList = new ArrayList<Cable>();

	@Override
	protected void toValueObject() {
		super.toValueObject();
		if (!Util.isEmpty(po.getTurns())) {
			turns = Integer.parseInt(po.getTurns());
		}
	}

	@Override
	protected void addChild(BaseVO<?> child) {
		super.addChild(child);
		cableMap.put(((Cable) child).getPO().getId(), (Cable) child);
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.cfg.vo.BaseVO#getLocalKey()
	 */
	@Override
	protected String getLocalKey() {
		return po.getId();
	}

	public int getTurns() {
		return turns;
	}

	public Map<String, Cable> getCableMap() {
		return cableMap;
	}

	public List<Cable> getCableList() {
		return cableList;
	}

	public void setTurns(int turns) {
		this.turns = turns;
	}

	@Override
	public String toString() {
		return "Format [turns=" + turns + "]";
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.cfg.vo.BaseVO#cleanUp()
	 */
	@Override
	protected void cleanUp() {
		super.cleanUp();
		cableMap = new HashMap<String, Cable>();
		cableList = new ArrayList<Cable>();
	}

}
