package com.cas.circuit;

public interface Editable {
	EditInfo getEditInfo(int n);

	void setEditValue(int n, EditInfo ei);
}
