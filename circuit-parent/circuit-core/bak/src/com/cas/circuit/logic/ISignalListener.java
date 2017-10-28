package com.cas.circuit.logic;

import javafish.clients.opc.variant.Variant;

public interface ISignalListener {

	void onSignalChanged(String elecCompKey, String address, Variant value);
}
