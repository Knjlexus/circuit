package com.cas.circuit.po;

import com.cas.util.Util;

public class ResisRelationPO {

	private String term1Id;
	private String term2Id;
	private String value;

	public String getTerm1Id() {
		return term1Id;
	}

	public void setTerm1Id(String term1Id) {
		this.term1Id = term1Id;
	}

	public String getTerm2Id() {
		return term2Id;
	}

	public void setTerm2Id(String term2Id) {
		this.term2Id = term2Id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return term1Id + "--" + term2Id + ":" + value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((term1Id == null) ? 0 : term1Id.hashCode() + ((term2Id == null) ? 0 : term2Id.hashCode()));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ResisRelationPO other = (ResisRelationPO) obj;
		return (Util.equals(term1Id, other.term1Id) && Util.equals(term2Id, other.term2Id)) || (Util.equals(term1Id, other.term2Id) && Util.equals(term2Id, other.term1Id));
	}

}
