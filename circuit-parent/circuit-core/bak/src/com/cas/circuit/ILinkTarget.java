package com.cas.circuit;

import java.util.List;

import com.jme3.scene.Spatial;

public interface ILinkTarget {

	List<ILinker> getLinkers();

	String getDirection();

	Spatial getModel();

	String getElecCompKey();

	String getTargetKey();

//	String getTargetName();

//	Object getPO();
}
