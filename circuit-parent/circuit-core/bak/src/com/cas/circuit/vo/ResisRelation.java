package com.cas.circuit.vo;

import java.util.Map;

import com.cas.circuit.po.ResisRelationPO;
import com.cas.util.Util;
import com.cas.util.vo.BaseVO;

public class ResisRelation extends BaseVO<ResisRelationPO> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2968611210762645002L;

	private Terminal term1;
	private Terminal term2;

	private boolean activated;
	private Float value;

	private String termIds;

	/**
	 * 
	 */
	public ResisRelation() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param po
	 */
	public ResisRelation(ResisRelationPO po) {
		super(po);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param term1
	 * @param term2
	 * @param value
	 */
	public ResisRelation(Terminal term1, Terminal term2, Float value, boolean activated) {
		super();
		this.term1 = term1;
		this.term2 = term2;
		this.value = value;
		this.activated = activated;
	}

	@Override
	protected void toValueObject() {
		super.toValueObject();
		if (!Util.isEmpty(po.getValue())) {
			value = Float.parseFloat(po.getValue());
		}
		termIds = po.getTerm1Id() + "-" + po.getTerm2Id();
//		System.out.println("ResisRelation.toValueObject()[" + termIds +"]["+value+"Ω]");
	}

	public String getAnotherTermId(String termId) {
		if (po.getTerm1Id().equals(termId)) {
			return po.getTerm2Id();
		}
		return po.getTerm1Id();
	}

	@Override
	public void setParent(BaseVO<?> parent) {

	}

	public Float getValue() {
		return value;
	}

	/**
	 * @return the term1
	 */
	public Terminal getTerm1() {
		return term1;
	}

	/**
	 * @return the term2
	 */
	public Terminal getTerm2() {
		return term2;
	}

	public void setTerm1(Terminal term1) {
		this.term1 = term1;
	}

	public void setTerm2(Terminal term2) {
		this.term2 = term2;
	}

	/**
	 * @param termAndStich
	 */
	void initTerminals(Map<String, Terminal> termAndStich) {
		term1 = termAndStich.get(po.getTerm1Id());
		term2 = termAndStich.get(po.getTerm2Id());
		if (term1 == null || term2 == null) {
			throw new RuntimeException(ResisRelation.class.getCanonicalName() + "  initTerminals");
		}

		term1.getResisRelationMap().put(term2, this);
		term2.getResisRelationMap().put(term1, this);
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	public String getTermIds() {
		return termIds;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return term1.getIndex() + "--" + term2.getIndex();
	}

}
