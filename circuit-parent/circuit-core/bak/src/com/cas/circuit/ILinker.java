package com.cas.circuit;

import java.util.List;
import java.util.Map;

import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;

public interface ILinker {
	/**
	 * 接到一个指定的目标上
	 */
	void bind(ILinkTarget target);

	void unbind();

	/**
	 * 从指定的目标上断开
	 * @param <T>
	 */
	void unbind(ILinkTarget target);

	/**
	 * 判断连接物两头是否都连接
	 */
	boolean isBothBinded();

	/**
	 * 根据连接物上的一个连接目标 获取 另外一个连接目标
	 * @param <T>
	 */
	ILinkTarget getAnother(ILinkTarget target);

	ILinkTarget getLinkTarget1();

	ILinkTarget getLinkTarget2();

	List<Spatial> getLinkMdlByTarget(ILinkTarget target);

	ColorRGBA getColor();

	Map<Spatial, ILinkTarget> getModels();

	String getWireNum();

	void setWireNum(String wireNum);
}
