package com.cas.circuit;

public interface ImportExportDialog {
	public enum Action {
		IMPORT, EXPORT
	};

	public void execute();

	public void setDump(String dump);
}
